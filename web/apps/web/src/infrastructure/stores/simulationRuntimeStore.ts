import { takeRight } from "es-toolkit"
import { create } from "zustand"
import { AgentState, Metrics, TickData } from "../websocket/types"

type TotalProducedChartData = {
    tickNumber: number
    totalProduced: number
    totalConsumed: number
}

type GreenEnergyRatioChartData = {
    tickNumber: number
    greenEnergyRatio: number
}

type AgentChartData = {
    tickNumber: number
    [agentId: string]: number
}

type SimulationRuntimeState = {
    // Core data
    tickNumber: number
    agentStates: Record<string, AgentState>
    tickDataLoading: boolean
    metrics: Metrics
    metricsLoading: boolean

    // Chart data
    totalProducedChartData: TotalProducedChartData[]
    greenEnergyRatioChartData: GreenEnergyRatioChartData[]
    solarPanelsChartData: AgentChartData[]
    batteriesChartData: AgentChartData[]

    // Actions
    setTickData: (data: TickData, solarIds: string[], batteryIds: string[]) => void
    setMetrics: (metrics: Metrics) => void
    setTickDataLoading: (loading: boolean) => void
    setMetricsLoading: (loading: boolean) => void
    resetChartData: () => void
    resetTickData: () => void
    resetMetrics: () => void
}

const initialTickData = {
    tickNumber: 0,
    agentStates: {},
}

const framesToKeep = 168

const initialMetrics = {
    tickNumber: 0,
    totalProduced: 0,
    totalConsumed: 0,
    cnpNegotiations: 0,
    greenEnergyRatioPct: 0,
}

export const useSimulationRuntimeStore = create<SimulationRuntimeState>()(set => ({
    // Initial core data
    tickNumber: initialTickData.tickNumber,
    agentStates: initialTickData.agentStates,
    tickDataLoading: true,
    metrics: initialMetrics,
    metricsLoading: true,

    // Initial chart data
    totalProducedChartData: [],
    greenEnergyRatioChartData: [],
    solarPanelsChartData: [],
    batteriesChartData: [],

    // Actions
    setTickData: (data: TickData, solarIds: string[], batteryIds: string[]) => {
        if (data.tickNumber === 0) {
            return
        }
        const batteryAgents = Object.fromEntries(
            Object.entries(data.agentStates ?? {})
                .filter(([agentId]) => batteryIds.includes(agentId))
                .map(([agentId, state]) => [agentId, state.stateOfCharge]),
        )

        const solarPanelAgents = Object.fromEntries(
            Object.entries(data.agentStates ?? {})
                .filter(([agentId]) => solarIds.includes(agentId))
                .map(([agentId, state]) => [agentId, state.production]),
        )

        const solarPanelData = {
            tickNumber: data.tickNumber,
            ...solarPanelAgents,
        }

        const batteryData = {
            tickNumber: data.tickNumber,
            ...batteryAgents,
        }

        set(state => ({
            tickNumber: data.tickNumber,
            agentStates: data.agentStates,
            tickDataLoading: false,
            solarPanelsChartData: takeRight(
                [...state.solarPanelsChartData, ...(solarPanelAgents ? [solarPanelData] : [])],
                framesToKeep,
            ),
            batteriesChartData: takeRight(
                [...state.batteriesChartData, ...(batteryAgents ? [batteryData] : [])],
                framesToKeep,
            ),
        }))
    },

    setMetrics: (metrics: Metrics) => {
        if (metrics.tickNumber === 0) {
            return
        }
        const totalProducedData = {
            tickNumber: metrics.tickNumber,
            totalProduced: metrics.totalProduced,
            totalConsumed: metrics.totalConsumed,
        }

        const greenEnergyData = {
            tickNumber: metrics.tickNumber,
            greenEnergyRatio: metrics.greenEnergyRatioPct,
        }

        set(state => ({
            metrics,
            metricsLoading: false,
            totalProducedChartData: takeRight([...state.totalProducedChartData, totalProducedData], framesToKeep),
            greenEnergyRatioChartData: takeRight([...state.greenEnergyRatioChartData, greenEnergyData], framesToKeep),
        }))
    },

    setTickDataLoading: (loading: boolean) => set({ tickDataLoading: loading }),
    setMetricsLoading: (loading: boolean) => set({ metricsLoading: loading }),

    resetChartData: () =>
        set({
            totalProducedChartData: [],
            greenEnergyRatioChartData: [],
            solarPanelsChartData: [],
            batteriesChartData: [],
        }),

    resetTickData: () =>
        set({
            tickNumber: initialTickData.tickNumber,
            agentStates: initialTickData.agentStates,
            tickDataLoading: true,
            solarPanelsChartData: [],
            batteriesChartData: [],
        }),

    resetMetrics: () =>
        set({
            metrics: initialMetrics,
            metricsLoading: true,
            totalProducedChartData: [],
            greenEnergyRatioChartData: [],
        }),
}))
