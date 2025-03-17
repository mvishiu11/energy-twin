# Selection of Simulation Framework for Microgrid Agent-Based Modeling

**Date**: *2025-17-03*  
**Status**: Approved

## Context
We require an agent-based simulation environment to model and test a microgrid system at a university campus scale. The simulation will involve multiple autonomous agents (representing solar panels, wind turbines, storage units, and building loads) negotiating power flows and control signals in near real-time. Key considerations include:

- **Scalability**: The ability to handle many agents (potentially dozens or hundreds) without severe performance degradation.  
- **Ease of Adoption**: A framework that balances robust features with a learning curve suitable for the existing development team.  
- **Interoperability**: Potential for integration with other libraries or external simulators if needed (e.g., power flow tools).  
- **Community and Documentation**: Availability of community support and documentation to shorten development time.  

### Problem Statement
We need to pick a single multi-agent framework (or environment) that provides a robust platform for agent-based simulation of a microgrid, given our constraints and the time frame.

## Decision
We will adopt **JADE (Java Agent DEvelopment)** as the simulation framework for our microgrid agent-based modeling.

## Status
**Suggested** – pending team consensus but strongly favored based on the evaluation below.

## Options Considered

1. **JADE (Java Agent DEvelopment)**
   - **Pros**:
     - Established FIPA-compliant multi-agent framework with extensive documentation.
     - Mature message-passing capabilities (ACL messages) and built-in agent lifecycle management.
     - Good scalability for dozens/hundreds of agents on modern hardware.
     - Java ecosystem ensures good performance and straightforward concurrency handling.
     - Integrates relatively easily with power simulators (e.g., via co-simulation or Java-based wrappers).
   - **Cons**:
     - Requires Java knowledge, though our team is comfortable enough.
     - Not a built-in power-flow simulator, so we must integrate with external tools if needed (but that is also typical for agent frameworks).

2. **Repast (C++ version)**
   - **Pros**:
     - High-performance C++ library for agent-based simulations with strong computational efficiency.
     - Known for large-scale simulations in certain academic and HPC contexts.
     - Flexibility in modeling complex agent interactions.
   - **Cons**:
     - Steep learning curve for the team; we have limited domain experience in Repast and C++.
     - Less intuitive environment for rapid prototyping or iterative development compared to Java or Python.
     - Smaller user community for the C++ version (compared to, say, Repast for Java or Python-based frameworks).
     - Repast for Java is pretty much a harder-to-learn version of Jade.

3. **MESA (Python-based)**
    - **Pros**:
      - Simple to learn for quick agent-based prototypes, user-friendly with a built-in web interface.
      - Python ecosystem has many libraries for data science, visualization, and ML integration.
   - **Cons**:
     - Performance may become a bottleneck with a large number of agents (Python overhead and single-thread constraints).
     - Scalability concerns for bigger or more complex microgrid scenarios.
     - Our team is concerned about Python concurrency (GIL) and real-time or large-scale simulation overhead.
     - For the current project scope, we do not plan on using any ML, eliminating potential upsides of Python.

## Decision Outcome and Justification

- **Chosen Option**: **JADE**  
- **Primary Reasons**:
  1. **Scalability and Performance**: JADE’s Java foundation typically scales better than MESA’s Python-based approach for large agent populations.  
  2. **Moderate Learning Curve**: Compared to Repast (C++), JADE is relatively straightforward to adopt, especially with existing documentation and examples in the microgrid domain.  
  3. **Proven MAS Features**: JADE is FIPA-compliant, stable, and widely used in academic and industrial multi-agent projects, ensuring a robust messaging system, agent lifecycle management, and integrated development tooling.  
  4. **Ecosystem and Integration**: Java tools and libraries (or bridging to MATLAB/Simulink, OpenDSS, etc.) are well-established. This synergy can expedite co-simulation if we add power-flow or real-time hardware integration in the future.
  5. **Ease of integration with API**: It can be very easily integrated with Spring-based API, giving us an option to write full backend in one languages and good API support, as opposed to Python, where we would have to get creative. As for Repast, none of us has experience in building APIs in C++.

## Consequences

- **Positive**:
  - We leverage a well-documented, standardized agent framework with large references in academic microgrid applications.
  - Java’s performance and concurrency model supports more complex or larger-scale simulations than MESA might handle comfortably.
  - Good synergy with external simulators or libraries, since JADE is frequently used in co-simulation setups.

- **Negative / Trade-offs**:
  - Must implement (or integrate) our own power-flow modeling; JADE does not provide a built-in electrical domain simulator.
  - Requires team members to have or acquire proficiency in Java-based MAS development.
  - Less “lightweight” for quick interactive dashboards or data science tasks than a Python-based approach, though this is mitigated by dedicated frontend.

## References
1. [JADE Documentation](https://jade.tilab.com/)
2. [Repast Documentation](https://repast.github.io/) 
3. [MESA Documentation](https://mesa.readthedocs.io/en/master/)