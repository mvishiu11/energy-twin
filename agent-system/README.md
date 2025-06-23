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

Simulation parameters (tick duration, weather, agent definitions) are read from
`simulation-config.json`. The system is designed to be interactive: clients can
start, pause or adjust the simulation speed at runtime.

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

For more details on configuration files and API usage, see the project root
[README](../README.md).
