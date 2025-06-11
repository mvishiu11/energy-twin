The next milestone shifts the Aggregator from **react-and-fire CNP rounds** to a **rolling, probabilistic look-ahead scheduler** that minimises expected energy-loss / external-tariff cost while respecting technical limits and coping with surprises.
Below is an in-depth blueprint that covers data flows, forecasting choices, optimisation logic, uncertainty handling, and the code-level hooks you will later implement.

---

## 1  Functional overview

Every **`N_pred` ticks** (hyper-parameter, e.g. 4–12 h) the Aggregator will

1. **Collect fresh history** of net-load, weather, SoC, outages, etc.
2. **Issue probabilistic forecasts** for:

   * total demand `L̂(t+k)` and PV production `P̂(t+k)` with **quantiles / confidence bands** (e.g. 5 %, 50 %, 95 %). ([sciencedirect.com][1], [ia4tes.org][2], [mdpi.com][3], [link.springer.com][4])
3. **Form a scenario tree** (Monte-Carlo sampling or quantile tree, depth =`H_plan` ticks) to capture uncertainty. ([ietresearch.onlinelibrary.wiley.com][5], [arxiv.org][6])
4. **Solve a stochastic cost-minimisation** that chooses, for each tick in the horizon,

   * battery charge/discharge `u_b,k`,
   * external supply `u_ext,k`,
   * with constraints: SoC bounds, C-rate, efficiency, forecast bands, outage flags.
     Objective = expected ∑ (cost × energy) + **CVaR** penalty for tail shortfall. ([sciencedirect.com][7], [researchgate.net][8])
5. **Emit a tick-by-tick schedule** into an internal queue.
6. **Execute** the first action each real tick; re-plan every `N_pred` ticks (Model Predictive Control). ([mdpi.com][9], [ietresearch.onlinelibrary.wiley.com][5])
7. **Fallback logic**: if a forecast-unseen event (load spike, blackout) would violate constraints, abandon the plan and fire today’s CNP heuristics.

---

## 2  Forecast-engine design

### 2.1 Input features

| Series     | Sampling | Source                               | Notes                           |
| ---------- | -------- | ------------------------------------ | ------------------------------- |
| `Load(t)`  | 1 tick   | LoadAgents → Aggregator              | campus profile + spikes         |
| `PVraw(t)` | 1 tick   | WeatherAgent irradiance × panel meta |                                 |
| `Temp(t)`  | 1 tick   | WeatherAgent                         | affects PV efficiency           |
| Calendar   | derived  | Aggregator                           | hour-of-day, weekday/WE, season |

### 2.2 Model options

| Class                                         | Pros                      | Cons                   | Libraries                                                            |
| --------------------------------------------- | ------------------------- | ---------------------- | -------------------------------------------------------------------- |
| **SARIMA / ARIMAX**                           | fast, interpretable       | poor nonlinear capture | `statsmodels` ([mathworks.com][10])                                  |
| **Quantile Gradient Boosting / LightGBM-QRF** | inherently probabilistic  | needs feature eng.     | `lightgbm`, `sklearn`                                                |
| **LSTM / GRU** with **Monte-Carlo dropout**   | good multivariate capture | heavier training       | `keras`, `pytorch` ([onlinelibrary.wiley.com][11], [nature.com][12]) |
| **TCN-ECANet-GRU hybrid** (state-of-art PV)   | SOTA for PV ramps         | heavy                  | ([nature.com][12], [mdpi.com][13])                                   |

**Recommendation:** start with **LightGBM-Quantile** for both demand and PV because it trains online in milliseconds, gives reliable 5/50/95 % quantiles, and runs inside the Aggregator JVM via **`mleap`** or **ONNX-Runtime for Java**.

### 2.3 Hyper-parameters

* `H_hist` — length of history window (e.g. 3 days).
* `N_pred` — re-plan period (e.g. every 4 ticks).
* `H_plan` — look-ahead horizon (e.g. 12 ticks).
* `α` — CVaR confidence (e.g. 5 %).

These move to `simulation.weather`-like block in the JSON and are read by Aggregator each start.

---

## 3  Stochastic optimiser

### 3.1 Variables per scenario `s` and step `k`

```
u_ext[k,s]         ∈ [0 , P_ext_max]          (kWh)
u_bat_dis[k,s]     ∈ [0 , P_bat_max]          (kWh out)
u_bat_chg[k,s]     ∈ [0 , P_bat_max]          (kWh in)
SoC[k,s]           update with ηc/ηd
surplus[k,s]  ≥ 0
shortfall[k,s] ≥ 0
```

### 3.2 Constraints

```
SoC_bounds,   charge≠discharge in same step,
Energy balance:  L̂[k,s] - P̂[k,s] - u_ext + u_bat_dis - u_bat_chg = shortfall - surplus
```

### 3.3 Objective

```
E_s Σ_k [ C_ext·u_ext  +  C_loss·(u_bat_chg+u_bat_dis)(1-η) ]
   + λ·CVaRα( shortfall )
```

where **`C_loss`** derives from round-trip efficiency and degradation cost ([par.nsf.gov][14]).

### 3.4 Solver choice

* Linear for fixed efficiencies → **LP** solved by OjAlgo or OR-Tools.
* Chance-constrained / CVaR converts to LP with aux vars ([sciencedirect.com][7], [ietresearch.onlinelibrary.wiley.com][5]).

Run time for 12 × 50 scenarios ≈ milliseconds.

---

## 4  Aggregator refactor

### 4.1 Modules

```
Aggregator
 ├─ HistoryBuffer (ring arrays)
 ├─ ForecastEngine  (LightGBM-Q model, online update)
 ├─ ScenarioGenerator (quantile or MC bootstrap)
 ├─ MPCScheduler (LP builder + ORTools solver)
 ├─ ActionQueue   (tick → action set)
 └─ ExecutorBehaviour (reads queue each tick)
```

### 4.2 Behaviour changes

* **ProductionConsumptionListener** unchanged.
* **TickSubscriberBehaviour** now:

  1. pushes last tick data into `HistoryBuffer`;
  2. if `tick mod N_pred == 0`, triggers `MPCScheduler`.
* **Handle* CNP behaviours*\* become **fallback only**: executed when `ActionQueue` is empty or an event invalidates SoC/limit feasibility.

### 4.3 New ontologies (edge → Aggregator)

| Ontology            | Sender       | Purpose                                   |
| ------------------- | ------------ | ----------------------------------------- |
| `BAT_CAPABILITY`    | Battery      | P\_max, ηc/ηd, degradation cost           |
| `LOAD_PROFILE_META` | LoadAgents   | optional average profile for better prior |
| `PV_META`           | EnergySource | panel area, temp coeff                    |

Send once at setup; Aggregator caches.

---

## 5  Unforeseen-event handling

1. **EventControlService** already flags *blackout* and *loadSpike*.
2. Executor checks before applying scheduled action:

   * if `EventControl.isBlackout()` ⇒ set `u_ext=0`, re-plan.
   * if `abs(real_net - planned_net) > ε` ⇒ mark **forecast break**, clear queue, re-plan immediately (ε e.g. 2 · σ foresc error). ([mdpi.com][9], [medium.com][15])

---

## 6  Tooling & libraries

| Need            | Java-friendly option                                   | Reason       |
| --------------- | ------------------------------------------------------ | ------------ |
| Quantile GBM    | **LightGBM** via `mleap` or `onnxruntime`              | fast, CPU    |
| MC dropout LSTM | **DL4J**                                               | pure JVM     |
| LP solver       | **Google OR-Tools**, **OjAlgo**                        | MIT / Apache |
| Scenario tree   | Apache Commons-Math RNG                                |              |
| Metrics         | InfluxDB / Timescale for logging forecast error & cost |              |

---

## 7  Road-map

| Sprint | Deliverable                                          |
| ------ | ---------------------------------------------------- |
| **S1** | HistoryBuffer, JSON params, meta-message exchange    |
| **S2** | ForecastEngine (LightGBM-Quantile) + unit tests      |
| **S3** | ScenarioGenerator + LP model (deterministic first)   |
| **S4** | Integrate CVaR / scenario sampling                   |
| **S5** | ExecutorBehaviour and hot fallback                   |
| **S6** | Metrics dashboard: cost vs baseline heuristic        |
| **S7** | Optional: swap in LSTM-probabilistic for PV (GRU-MC) |

---

## 8  Key references

* Probabilistic micro-grid load forecasting techniques ([sciencedirect.com][1])
* Adaptive online probabilistic load models ([ia4tes.org][2])
* Comparative STLF model review ([mdpi.com][3])
* ST-PV power hybrid TCN-GRU forecasting ([nature.com][12])
* CVaR battery scheduling under uncertainty ([sciencedirect.com][7])
* Scenario-based optimal storage dispatch ([ietresearch.onlinelibrary.wiley.com][5])
* Degradation-aware battery models ([par.nsf.gov][14])
* Day-ahead vs real-time two-level control ([mdpi.com][9])
* Improved LSTM microgrid load forecasting ([onlinelibrary.wiley.com][11])
* Distributionally robust optimisation for storage ([researchgate.net][8])

---

Implementing this plan will transform the Aggregator into a predictive,
risk-aware energy manager that continuously minimises expected cost while
keeping contingency plans ready for the inevitable surprises in a real
micro-grid.

[1]: https://www.sciencedirect.com/science/article/pii/S2352484722006758?utm_source=chatgpt.com "Short-term microgrid load probability density forecasting method ..."
[2]: https://www.ia4tes.org/wp-content/uploads/2022/07/Paper3-probabilistic-load-forecasting-based-on-adaptive-online-learning.pdf?utm_source=chatgpt.com "[PDF] Probabilistic Load Forecasting Based on Adaptive Online Learning"
[3]: https://www.mdpi.com/2076-3417/14/11/4442?utm_source=chatgpt.com "From Time-Series to Hybrid Models: Advancements in Short-Term ..."
[4]: https://link.springer.com/article/10.1007/s43621-024-00356-6?utm_source=chatgpt.com "An intelligent model for efficient load forecasting and sustainable ..."
[5]: https://ietresearch.onlinelibrary.wiley.com/doi/10.1049/iet-gtd.2017.0037?utm_source=chatgpt.com "Optimal scheduling of energy storage under forecast uncertainties"
[6]: https://arxiv.org/abs/2107.10013?utm_source=chatgpt.com "Optimal Operation of Power Systems with Energy Storage ... - arXiv"
[7]: https://www.sciencedirect.com/science/article/abs/pii/S0378779622003911?utm_source=chatgpt.com "Optimal Operation of Battery Energy Storage Under Uncertainty ..."
[8]: https://www.researchgate.net/publication/361513598_Optimal_Operation_of_Battery_Energy_Storage_Under_Uncertainty_Using_Data-Driven_Distributionally_Robust_Optimization?utm_source=chatgpt.com "Optimal Operation of Battery Energy Storage Under Uncertainty ..."
[9]: https://www.mdpi.com/1996-1073/12/6/1098?utm_source=chatgpt.com "Practical Operation Strategies for Energy Storage System under ..."
[10]: https://www.mathworks.com/matlabcentral/answers/1748115-electrical-load-forecasting-methods?utm_source=chatgpt.com "Electrical load forecasting methods - MATLAB Answers - MathWorks"
[11]: https://onlinelibrary.wiley.com/doi/10.1155/2022/4017708?utm_source=chatgpt.com "Microgrid Load Forecasting Based on Improved Long Short‐Term ..."
[12]: https://www.nature.com/articles/s41598-024-56751-6?utm_source=chatgpt.com "A short-term forecasting method for photovoltaic power generation ..."
[13]: https://www.mdpi.com/1996-1073/16/14/5436?utm_source=chatgpt.com "A Review of State-of-the-Art and Short-Term Forecasting Models for ..."
[14]: https://par.nsf.gov/servlets/purl/10391019?utm_source=chatgpt.com "[PDF] Scheduling Battery Energy Storage Systems Under Battery Capacity ..."
[15]: https://medium.com/%40mhdabuella/net-load-forecasting-for-resilience-of-microgrids-5344514be107?utm_source=chatgpt.com "Net Load Forecasting for Microgrid Resiliency | by Mohamed Abuella"


### Predictive Energy-Allocation: Implementation Road-map

*(from “reactive CNP” toward probabilistic Model-Predictive Control)*

---

## Stage 0 – Kick-off & prereqs

| Deliverable                         | Validation | What **you** must supply | Questions to answer                                                                                           |
| ----------------------------------- | ---------- | ------------------------ | ------------------------------------------------------------------------------------------------------------- |
| ✓ Design review (this doc) accepted | n/a        | —                        | • Confirm tick length & time-unit (kWh vs kW) <br>• Decide default horizons: `N_pred`, `H_plan`, confidence α |

---

## Stage 1 – Data plumbing & history buffer

| Item                            | Implementation notes                                                                                                    |
| ------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| **HistoryBuffer** (ring arrays) | `double[] loadHist, pvHist, tempHist, socHist` – size =`H_hist` + margin                                                |
| **Meta-exchange messages**      | `BAT_CAPABILITY`, `LOAD_PROFILE_META`, `PV_META` sent once during `onAgentSetup()` and stored in `AggregatorMetaStore`. |
| **JSON schema update**          | add `forecast`: `{ "N_pred":4, "H_plan":12, "H_hist":72, "alphaCVaR":0.05 }`                                            |

**Validation**
*Unit tests* that after 100 ticks buffer length == `H_hist` and values match last ticks.

**You provide**

* No new files; current Aggregator file path and constants suffice.

---

## Stage 2 – Forecast engine (probabilistic)

| Item                        | Implementation notes                                                                                          |
| --------------------------- | ------------------------------------------------------------------------------------------------------------- |
| **LightGBM–Quantile model** | Train online each `N_pred` ticks on last `H_hist` samples. Output Q05, Q50, Q95 for `Load` and `PVraw` (kWh). |
| **Java binding**            | use `onnxruntime` or `mleap` for fast in-JVM inference (≈ 0.3 ms per horizon).                                |
| **Reliability metrics**     | compute rolling MAE, PICP (prediction-interval coverage probability).                                         |

**Validation**
*Back-test* on stored simulation log: plot Q50 vs real, hit-rate inside Q05–Q95, MAE target < 10 %.

**You provide**

* A log CSV for ≥ 3 virtual days (columns tick, load, pv, temp).
* Decide acceptable MAE/PICP thresholds.

**Questions**

* Which chrono unit is one tick (1 h)?
* May we store incremental model files on disk?

---

## Stage 3 – Scenario generator

| Item                                                                                         | Implementation notes |
| -------------------------------------------------------------------------------------------- | -------------------- |
| **Quantile-tree** (3-branch) or 50 Monte-Carlo draws with Gaussian copula between Load & PV. |                      |
| Convert forecasts to `Scenario[]`, each has probability `p_s` and vectors `L_s[k], P_s[k]`.  |                      |

**Validation**
*Plot* fan chart of 50 scenarios vs true curve for the next `H_plan` ticks during a run.

**You provide**

* Acceptable scenario count (trade-off speed vs resolution).

---

## Stage 4 – Deterministic MPC skeleton

| Item                                                       | Implementation notes |
| ---------------------------------------------------------- | -------------------- |
| **LP variables** for one scenario only (median forecasts). |                      |
| Constraints: SoC, Pmax, extCap, balance.                   |                      |
| Solver: OR-Tools `LinearSolver`.                           |                      |
| After solve create `ActionQueue` (k entries).              |                      |

**Validation**
*Unit tests*: infeasible case returns null; feasible case respects bounds exactly.

**You provide**

* Battery parameters exposed through `BAT_CAPABILITY` message.

---

## Stage 5 – Stochastic extension + CVaR

| Item                                                                                      | Implementation notes |
| ----------------------------------------------------------------------------------------- | -------------------- |
| Extend LP with scenario index ` s`. <br> Introduce CVaR auxiliary vars and α from config. |                      |
| Objective = expected energy cost + λ·CVaR.                                                |                      |

**Validation**
*Monte-Carlo simulation* (300 replay draws) → total cost ≤ deterministic baseline; shortfall CVaR reduced.

**You provide**

* λ weighting factor for CVaR (config).

---

## Stage 6 – Executor & fallback integration

| Item                                                                                                                                           | Implementation notes |                                                                                          |
| ---------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- | ---------------------------------------------------------------------------------------- |
| **ExecutorBehaviour** (high priority) pops first action; sends `ACCEPT_PROPOSAL` messages directly to batteries/external (bypassing live CNP). |                      |                                                                                          |
| Monitor real net-load; if \`                                                                                                                   | real – planned       | > ε`or blackout flag → clear queue and call former`HandleShortfall/Surplus\` behaviours. |

**Validation**
*Scenario test* where a blackout occurs mid-horizon. Verify executor deletes plan and falls back, batteries act accordingly.

**You provide**

* Threshold ε (kWh) for “forecast break”.

---

## Stage 7 – Metrics & plotting

| Item                                                                                                                           | Implementation notes |
| ------------------------------------------------------------------------------------------------------------------------------ | -------------------- |
| Collect per-tick: forecast Q50, real, action chosen, SoC, external kWh.                                                        |                      |
| Write to CSV or time-series DB.                                                                                                |                      |
| **`/metrics/plot` endpoint** – returns PNG of predicted vs real curves for last `M` ticks (use JFreeChart or Python via REST). |                      |

**Validation**
Visual inspection + numerical report: MAE, external-cost, blackout-hours, compare vs old heuristic.

**You provide**

* Decision: Java chart lib or server-side Python for plotting.
* Front-end spec (if plot consumed by GUI).

---

## Stage 8 – Documentation & tuning

* hyper-parameter sweeps (N\_pred, H\_plan, λ).
* Update README, Swagger docs for new endpoints `/forecast/config`, `/metrics/plot`.
* Provide example JSON config and sample plots.

---

### File/Module summary

| Stage | New / modified files                                           |
| ----- | -------------------------------------------------------------- |
| 1     | `HistoryBuffer.java`, `AggregatorMetaStore.java`, config JSON  |
| 2     | `ForecastEngine.java`, `LightGBMModel.onnx`, util for features |
| 3     | `ScenarioGenerator.java`                                       |
| 4     | `MPCSolver.java`, add to Aggregator tick flow                  |
| 5     | Extend `MPCSolver`                                             |
| 6     | `ExecutorBehaviour.java`, modify Aggregator main loop          |
| 7     | `MetricsService.java`, `PlotController.java`                   |
| 8     | Docs, unit-test suites                                         |

---

## Validation matrix

| Stage | Unit tests                | Integration tests          | Manual plots        |
| ----- | ------------------------- | -------------------------- | ------------------- |
| 1     | buffer size, meta store   | –                          | –                   |
| 2     | MAE, PICP                 | forecast vs log            | fan chart           |
| 3     | scenario prob sum=1       | scenario vs quantile       | fan chart           |
| 4     | feasibility               | deterministic dispatch sim | energy balance plot |
| 5     | cost improvement          | 300 MC runs                | CVaR curve          |
| 6     | fail-over trigger         | blackout + spike run       | SoC & ext kWh       |
| 7     | plotting REST returns 200 | –                          | prediction vs real  |

---

### Open Questions

1. Acceptable run-time per `N_pred` planning round? (< 100 ms feasible).
2. Energy cost units – currency or kWh loss proxy?
3. Preferred battery degradation cost model (per-cycle or per-kWh throughput)?
4. Will the front-end call plotting endpoint or will CSV be sufficient?

Answer these before implementation begins, and share any domain datasets you have (historical campus load, irradiance) to bootstrap the forecast engine.
