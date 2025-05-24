package com.energytwin.microgrid.registry;

import com.energytwin.microgrid.ws.dto.TickDataMessage;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AgentStateRegistry {

    private final ConcurrentMap<String, TickDataMessage.AgentState> states = new ConcurrentHashMap<>();

    public void update(String agentName, TickDataMessage.AgentState state){
        states.put(agentName, state);
    }

    public Map<String, TickDataMessage.AgentState> all(){
        return states;
    }

    public double getTotalEnergyDemand() {
        return states.values().stream()
                .mapToDouble(TickDataMessage.AgentState::getDemand)
                .sum();
    }

    public double getTotalGreenEnergyGeneration() {
        return states.values().stream()
                .mapToDouble(TickDataMessage.AgentState::getProduction)
                .sum();
    }
}
