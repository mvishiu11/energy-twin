# ADR: Central Architectural Approach for Green Energy Microgrid Simulation

**Date:** 2025-03-17  
**Status:** Approved

---

## Context

Our project aims to design and develop an agentic system for simulating a green energy microgrid under various conditions. The system must:
- Simulate microgrid behavior (e.g., renewable energy sources, storage units, weather effects) via autonomous agents.
- Stream logs from each agent via a websocket (or similar) to a centralized backend service.
- Provide a flexible, real-time dashboard that allows users to manipulate simulation parameters (e.g., control time speed, pause, inject events).
- Persist simulation snapshots and logs with fine-grained, timestamped data for historical analysis and future comparisons.

We consider two architectural approaches:

1. **Batch Simulation within a Larger Spring-based Backend:**  
   The simulation would run as a complete workflow triggered by frontend requests. Logs would be aggregated into periodic snapshots stored in a database. This approach simplifies integration with Spring but offers limited real-time interactivity.

2. **Interactive Agentic Simulation as the Central Element:**  
   Agents operate continuously in real time, synchronized on a common timestep. The simulation allows dynamic time control (speed up, slow down, pause) and event injection during runtime, providing a true digital twin experience.

---

## Reccomendation

**Recommended approach is a hybrid approach: an interactive agentic simulation as the central element, paired with time-series database (e. g. TimescaleDB) for storing snapshots and logs.**

- **Interactive Simulation:** Enables real-time control and dynamic event injection, which aligns with our long-term goals for an interactive digital twin of the microgrid.
- **Historical data is still available:** Historical data is stored by a time-series database, allowing for revisiting historical data without interacting with the agent (simply by appropriate measures being provided within classical backend).
- **Example of time-series DB - TimescaleDB:** Being built on PostgreSQL, it integrates seamlessly with Spring Boot, offers full SQL support, automatic time-based partitioning, and compression, which are ideal for efficiently managing our timestamped simulation data. This DB is only a suggestion for now, to be researched further (TBR).

---

## Rationale

- **Real-Time Interactivity:**  
  Hybrid approach allows for on-the-fly manipulation of simulation parameters. This real-time control is critical for design validation, rapid prototyping, and fine-tuning the microgrid under various conditions.

- **Scalability & Flexibility in Data Storage:**  
  TimescaleDB’s robust time-series features—such as automatic partitioning (hypertables), efficient compression, and native SQL queries—provide a strong foundation for storing and querying the high-resolution snapshots and logs generated during simulation. Its relational backbone also facilitates integration with other business data if needed.

- **Integration with Existing Tooling:**  
  Leveraging TimescaleDB ensures straightforward integration with our Spring Boot-based backend using Spring Data JPA, reducing the overhead associated with using a specialized NoSQL time-series store (e.g., InfluxDB) that might require custom adapters. Again, this can be revisited on further research.

---

## Considered Alternatives

- **Batch Simulation Approach:**  
  While simpler to implement within the Spring ecosystem, the batch approach does not support the interactive, real-time control that our project roadmap requires for dynamic simulation and user engagement. If this requirement is not central to the system, we may reconsider this approach.

- **Alternative Databases (e.g., InfluxDB):**  
  InfluxDB is a strong candidate for time-series data; however, its native query languages (InfluxQL/Flux) and less straightforward integration with Spring Boot make it a less attractive option compared to TimescaleDB, which benefits from the familiarity and maturity of PostgreSQL.

---

## Consequences

- **Increased Complexity:**  
  The interactive agentic simulation requires additional mechanisms for synchronizing agents and controlling simulation time. However, this complexity is justified by the enhanced flexibility and user control over simulation dynamics. In case it proves to be too difficult in practice, batch approach may be revisited.

- **Robustness & Scalability:**  
  Using time-series database leverages a proven relational system with strong time-series optimizations, ensuring our historical data storage is both performant and scalable. This decision also lays a foundation for further extensions, such as integrating additional business data.

- **Future Enhancements:**  
  With a flexible, interactive system at its core, future iterations may add advanced visualization and automated anomaly detection features without major changes to the underlying architecture.

---

**Conclusion:**  
The chosen architecture (Approach 2 with TimescaleDB) best meets our long-term requirements for a flexible, interactive simulation system that not only supports real-time control and dynamic event injection but also efficiently stores and retrieves historical simulation data for analysis and troubleshooting. It is however, quite complex to implement in practice and the batch approach may need to be revisited.