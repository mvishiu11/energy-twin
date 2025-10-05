import { create } from "zustand"

export type SimulationSettingsState = {
    tickIntervalMilliseconds: number
    setTickIntervalMilliseconds: (milliseconds: number) => void
    externalSourceCost: number
    setExternalSourceCost: (cost: number) => void
    externalSourceCap: number
    setExternalSourceCap: (cap: number) => void
}

export const useSimulationSettingsStore = create<SimulationSettingsState>()(set => ({
    tickIntervalMilliseconds: 1000,
    setTickIntervalMilliseconds: (milliseconds: number) => set({ tickIntervalMilliseconds: milliseconds }),
    externalSourceCost: 5.0,
    setExternalSourceCost: (cost: number) => set({ externalSourceCost: cost }),
    externalSourceCap: 100,
    setExternalSourceCap: (cap: number) => set({ externalSourceCap: cap }),
}))
