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
    predictedLoadKw: number
    predictedPvKw: number
    errorLoadKw: number
    errorPvKw: number
    fanLoPv: number[]
    fanHiPv: number[]
    fanLoLoad: number[]
    fanHiLoad: number[]
}

export type AgentState = {
    demand: number
    production: number
    stateOfCharge: number
}
