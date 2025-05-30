export type Metrics = {
    tickNumber: number
    totalProduced: number
    totalConsumed: number
    cnpNegotiations: number
    greenEnergyRatioPct: number
}

export type TickData = {
    tickNumber: number
    agentStates: Record<string, AgentState>
}

export type AgentState = {
    demand: number
    production: number
    stateOfCharge: number
}
