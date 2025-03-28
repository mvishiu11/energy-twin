# ADR: Suitable Database solution for Green Energy Microgrid Simulation

**Date:** _2025-03-20_
**Status:** Proposed

---

## Context

Our project aims to design and develop an agentic system for simulating a green energy microgrid under various conditions. The system must:

- Simulate microgrid behavior (e.g., renewable energy sources, storage units, weather effects) via autonomous agents.
- Stream logs from each agent via a websocket (or similar) to a centralized backend service.
- Provide a flexible, real-time dashboard that allows users to manipulate simulation parameters (e.g., control time speed, pause, inject events).
- Persist simulation snapshots and logs with fine-grained, timestamped data for historical analysis and future comparisons.

We consider two types of databases:

1. **Relational Databses:**

TimescaleDB - a PostgreSQL based time-series database optimized for storing high-frequency snapshots and logs. Supports automatic partitioning, compression, and fast writes. Seamless integration with Spring Boot allowing SQL-based queries (https://docs.timescale.com/quick-start/latest/java/).

InfluxDB - a purpose-built time-series database designed for high-ingestion rates and real-time analytics. Offers built-in retention policies, downsampling, and an efficient query engine optimized for time-series workloads. It integrates with Spring Boot through custom connectors and the InfluxDB Java client (https://www.baeldung.com/java-influxdb).

2. **NoSQL Databases:**

Apache Cassandra - distributed NoSQL database designed for high write throughput and horizontal scalability. Enables rapidly storing and retrieving larg volumes of simulation snapshots without a strict schema. Less flexibles in terms of quering (https://docs.spring.io/spring-integration/reference/cassandra.html).

MongoDB - document based NoSQL database, useful for storing semi-structured snapshots and logs. Supports fast inserts and flexible schema but may require indexing for optimal performance. Integrates well with Spring Boot, but not specifically optimized for time-series data (https://docs.spring.io/spring-integration/reference/mongodb.html).

---

## Recomendation

**Recommeended database solution is TimescaleDB.**

- **Optimized for Time-Series Data:** Since our microgrid simulation generates high-frequency snapshots and logs, TimescaleDB’s built-in time-series optimizations (hypertables, automatic partitioning, compression) make it ideal for efficient storage and retrieval.

- **Fast Write Performance:** While NoSQL options (like Cassandra) excel in write speed, TimescaleDB is highly optimized for fast inserts while still maintaining SQL flexibility. It can handle large volumes of timestamped data efficiently.

- **Spring Boot integration:** TimescaleDB is built on PostgreSQL, meaning it works natively with Spring Boot and JPA without additional adapters or workarounds. This simplifies development.

- **Querying and Analytics:** TimescaleDB fully supports SQL, making historical analysis, trend detection, and reporting much easier.

- **Scalability:** TimescaleDB enables horizontal scalling and compression to handle growing number of data without decreasing performance.

---

## Rationale

TimescaleDB was chosen because it balances fast writes, SQL-based querying, and native time-series optimizations, making it ideal for storing and retrieving high-frequency snapshots in our simulation. It can run locally, and also supports defining shcema in Spring Boot. Moreover, its integration with Spring Boot and JPA reduces development overhead compared to NoSQL alternatives like Cassandra, which require custom query handling. While Cassandra provides extreme write scalability, it lacks efficient time-series querying and structured analytics, making it less suitable for historical data retrieval and trend analysis. MongoDB, while flexible, does not provide built-in time-series optimizations and may require additional indexing strategies for performance.

---

## Consequences

- **Positive:** Using TimescaleDB ensures efficient storage and querying of timestamped data, reducing development complexity due to its PostgreSQL foundation.

- **Negative:** TimescaleDB, while scalable, may not match Cassandra’s write speed at extreme scales, which could be a concern for very large-scale deployments. If performance issues arise in future expansions, a hybrid approach (e.g., using Cassandra for raw ingestion and TimescaleDB for analytics) may be considered.

---

## Alternatives

Alternative for TimescaleDB is Apache Cassandra. As a distributed NoSQL database, Cassandra can handle high-velocity data ingestion across multiple nodes, making it well-suited for scenarios where rapid, large-scale data storage is the top priority. However, its limited query flexibility and lack of built-in time-series features make it less ideal for in-depth historical analysis compared to TimescaleDB.
