import { create } from "zustand"

export type BenchmarkState = {
    isRunning: boolean
    setIsRunning: (isRunning: boolean) => void
    progress: number
    setProgress: (progress: number) => void
    targetTicks: number
    setTargetTicks: (ticks: number) => void
}

export const useBenchmarkStore = create<BenchmarkState>()(set => ({
    isRunning: false,
    setIsRunning: (isRunning: boolean) => set({ isRunning }),
    progress: 0,
    setProgress: (progress: number) => set({ progress }),
    targetTicks: 0,
    setTargetTicks: (ticks: number) => set({ targetTicks: ticks }),
}))
