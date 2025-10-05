import { create } from "zustand"

export type SimulationState = {
    isRunning: boolean
    setIsRunning: (isRunning: boolean) => void
    isPaused: boolean
    setIsPaused: (isPaused: boolean) => void
}

export const useSimulationStore = create<SimulationState>()(set => ({
    isRunning: false,
    isPaused: false,
    setIsRunning: (isRunning: boolean) => set({ isRunning }),
    setIsPaused: (isPaused: boolean) => set({ isPaused }),
}))
