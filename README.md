# Energy Twin

Energy Twin is a proof-of-concept digital twin of a small-scale microgrid. It combines
an agent-based simulation (written in Java with Spring Boot and JADE) with a
modern web dashboard built as an Nx monorepo. The project allows experimentation
with renewable energy sources, battery storage, and external loads while
providing real-time metrics and control over the running simulation.

This repository contains two main packages:

- [`agent-system`](./agent-system) – Spring Boot API and JADE-based agents.
- [`web`](./web) – React based dashboard using Nx.

Additional design documentation is available in the [`docs`](./docs) directory.

## Prerequisites

- **Java 21** and **Maven 3** – required to build and run the agent system.
- **Node.js 22** and **npm** – required for the web dashboard (see
  [`web/.nvmrc`](./web/.nvmrc)).

Ensure these tools are installed and available on your PATH.

## Installation

Clone the repository and install dependencies for both subprojects:

```bash
# Agent system dependencies
cd agent-system/energy-twin
mvn package -DskipTests

# Web dependencies
cd ../../web
npm install
```

The JADE JAR is already included under `agent-system/energy-twin/lib`. If you
prefer using a locally installed JADE library, follow the instructions in
[`agent-system/README.md`](./agent-system/README.md).

## Running the Projects

### Agent System

Start the simulation API with:

```bash
cd agent-system/energy-twin
mvn spring-boot:run
```

The API runs on `http://localhost:8081`. Swagger UI is available at
`http://localhost:8081/swagger-ui/index.html`.

### Web Dashboard

Run the frontend in development mode:

```bash
cd web
npx nx serve web
```

The dashboard will open on `http://localhost:4200` by default and connects to the
agent system through REST/WebSocket endpoints.

## Further Reading

- Details about available API endpoints, simulation configuration and JADE setup
  are described in [`agent-system/README.md`](./agent-system/README.md).
- Nx usage and commands for the frontend can be found in [`web/README.md`](./web/README.md).
- Architecture decision records and project goals live in the [`docs`](./docs)
  folder.
