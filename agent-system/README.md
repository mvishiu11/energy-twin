# Agent System

This module contains the backend simulation engine implemented with
[Spring Boot](https://spring.io/) and
[JADE](https://jade.tilab.com/) agents. The entry point is
`MicrogridSimulationApplication` under `src/main/java`.

The agent system exposes a REST API for controlling the simulation and a
WebSocket endpoint for streaming metrics. OpenAPI documentation is available when
the server is running at `http://localhost:8081/v3/api-docs` and Swagger UI at
`http://localhost:8081/swagger-ui/index.html`.

## Modules

- **agentfusion** – utilities for integrating JADE agents with the Spring context.
- **controller** – REST controllers for starting/stopping the simulation and
  sending events.
- **core** – main simulation logic:
  - `agents` – implementations of aggregator, energy source, storage, load,
    weather and orchestrator agents.
  - `behaviours` – JADE behaviours used by agents (contract-net protocols, tick
    broadcast/subscription, etc.).
  - `forecast`, `planner`, `scenario`, `history` – forecasting and planning tools
    used by the aggregator.
- **registry** – a runtime store of agent states, used to publish metrics.
- **service** – Spring services that manage JADE containers, simulation config
  and log aggregation.
- **ws** – WebSocket configuration and services for pushing metrics to clients.

## Design Assumptions

The system models a microgrid where autonomous agents represent photovoltaic
sources, batteries, loads and an external energy supplier. An
`OrchestratorAgent` broadcasts simulation ticks so that all agents remain in
sync. An `AggregatorAgent` collects production and consumption data and uses
forecasting and planning algorithms to schedule battery operations or negotiate
energy transfers using the contract-net protocol.

Simulation parameters (tick duration, weather, agent definitions) are read from a JSON configuration sent in the `/simulation/start` POST request as request body. And example of the full capabilities of such a configuration can be seen in
[`simulation-config.json`](./energy-twin/src/main/resources/simulation-config.json). The system is designed to be interactive: clients can
start, pause or adjust the simulation speed at runtime.

## Control Modes: Myopic vs Predictive

The agent system can operate in two coordination modes, which differ in how the
`AggregatorAgent` plans the use of local resources (PV, batteries) and the
external grid:

- **Myopic mode (baseline)** – purely reactive control
- **Predictive mode** – forecast-driven, rolling-horizon planning

### Myopic Mode

In **myopic** mode, the aggregator has no look-ahead. At every simulation tick it:

1. Reads the *current* net load (demand minus PV production),
2. Issues commands to batteries and the external source based only on this
   instantaneous value,
3. Follows a fixed priority order, for example:
   - use surplus PV to charge the battery, then export the rest,
   - during deficit, discharge the battery first, then import from the grid.

There is no forecasting, no multi-step optimization, and no explicit notion of
“saving” energy for future scarcity. This mode is useful as a simple baseline and
for debugging, because actions are easy to interpret.

### Predictive Mode

In **predictive** mode, the aggregator switches to a model-predictive style of
control:

1. At regular planning intervals, it requests **short-horizon forecasts** of load
   and PV generation from the forecasting components.
2. It formulates and solves a **linear programming** problem over a finite time
   window (rolling horizon), using these forecasts and device constraints
   (battery SoC dynamics, power limits, etc.).
3. The solver returns an optimal sequence of actions (charge / discharge / import
   / export) for the full horizon; the aggregator:
   - applies **only the first action**,
   - discards the rest,
   - repeats the forecast–plan–act cycle at the next planning step.

This receding-horizon behaviour lets the system proactively **stage energy** in
the battery ahead of expected scarcity (e.g., night or low irradiance) and
better exploit surplus PV. In experiments, the predictive mode increases local
energy self-sufficiency, keeps the battery at higher reserve levels, and reduces
time spent in critically low-resilience operating states, compared to the myopic
baseline.

### Enabling Predictive Mode

To enable predictive mode, set the `forecasting.enablePredictive` flag to `1` in the simulation configuration JSON (see example in
[`simulation-config.json`](./energy-twin/src/main/resources/simulation-config.json)). Conversely, set to `0` to enable myopic mode. By default, the system starts in myopic mode.

## Running and Development

Format the code with:

```bash
mvn spotless:apply
```

Start the API locally:

```bash
mvn spring-boot:run
```

If you want to install the JADE dependency to your local Maven repository, run:

```bash
mvn install:install-file -Dfile=lib/jade.jar \
  -DgroupId=com.tilab.jade -DartifactId=jade -Dversion=4.6.0 -Dpackaging=jar
```

There is also a [Bruno](https://www.usebruno.com) configuration provided for easy testing of the backend part of the application.

For more details on configuration files and API usage, see the project root
[README](../README.md). 
