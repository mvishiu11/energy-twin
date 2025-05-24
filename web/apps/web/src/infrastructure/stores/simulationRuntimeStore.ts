import { create } from "zustand"
import { AgentState } from "../websocket/types"

type SimulationRuntimeState = {
    tickNumber: number
    setTickNumber: (tickNumber: number) => void
    agentStates: Record<string, AgentState>
    setAgentStates: (agentStates: Record<string, AgentState>) => void
}

export const useSimulationRuntimeStore = create<SimulationRuntimeState>()(set => ({
    tickNumber: 0,
    setTickNumber: (tickNumber: number) => set({ tickNumber }),
    agentStates: {},
    setAgentStates: (agentStates: Record<string, AgentState>) => set({ agentStates }),
}))
