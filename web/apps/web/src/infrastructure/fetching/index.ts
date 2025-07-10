import { useMutation, UseMutationOptions, useQuery } from "@tanstack/react-query"
import { toaster } from "../../components/ui/toaster"
import { useSimulationStore } from "../stores/simulationStore"
import {
    blackout,
    breakSource,
    getAllLogs,
    loadSpike,
    LoadSpikeData,
    pauseSimulation,
    resumeSimulation,
    startSimulation,
    stopSimulation,
    updateWeather,
    UpdateWeatherData,
} from "./api"

export function useLogs() {
    return useQuery({
        queryKey: ["logs"],
        queryFn: async () => {
            const logs = await getAllLogs()
            return logs.data
        },
        refetchInterval: 1000,
    })
}

export function useStartSimulation() {
    const { setIsRunning } = useSimulationStore()

    return useMutation({
        onSuccess: () => {
            toaster.create({
                title: "Simulation started",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Simulation failed to start",
                type: "error",
            })
        },
        mutationFn: async (config: string) => {
            const response = await startSimulation({ body: config })
            setIsRunning(true)
            return response.data
        },
        mutationKey: ["start-simulation"],
    })
}

export function usePauseSimulation() {
    const { setIsPaused } = useSimulationStore()

    return useMutation({
        onSuccess: () => {
            toaster.create({
                title: "Simulation paused",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Simulation failed to pause",
                type: "error",
            })
        },
        mutationFn: async () => {
            const response = await pauseSimulation()
            setIsPaused(true)
            return response.data
        },
        mutationKey: ["pause-simulation"],
    })
}

export function useStopSimulation() {
    const { setIsRunning } = useSimulationStore()

    return useMutation({
        onSuccess: () => {
            toaster.create({
                title: "Simulation stopped",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Simulation failed to stop",
                type: "error",
            })
        },
        mutationFn: async () => {
            const response = await stopSimulation()
            setIsRunning(false)
            return response.data
        },
        mutationKey: ["stop-simulation"],
    })
}

export function useUpdateWeather() {
    return useMutation({
        mutationKey: ["update-weather"],
        mutationFn: async (config: UpdateWeatherData) => {
            const response = await updateWeather(config)
            return response.data
        },
        onSuccess: () => {
            toaster.create({
                title: "Weather injected",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Failed to inject weather",
                type: "error",
            })
        },
    })
}

export function useResumeSimulation() {
    const { setIsPaused } = useSimulationStore()

    return useMutation({
        onSuccess: () => {
            toaster.create({
                title: "Simulation resumed",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Simulation failed to resume",
                type: "error",
            })
        },
        mutationFn: async () => {
            const response = await resumeSimulation()
            setIsPaused(false)
            return response.data
        },
        mutationKey: ["resume-simulation"],
    })
}

export function useBlackout() {
    return useMutation({
        mutationFn: async () => {
            const response = await blackout()
            return response.data
        },
        mutationKey: ["blackout"],
        onSuccess: () => {
            toaster.create({
                title: "Blackout simulated",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Failed to simulate blackout",
                type: "error",
            })
        },
    })
}

export function useLoadSpike(props?: UseMutationOptions<string, unknown, LoadSpikeData["query"]>) {
    return useMutation({
        mutationFn: async ({ name, rate, ticks }) => {
            const response = await loadSpike({
                query: {
                    name,
                    rate,
                    ticks,
                },
            })
            return response.data ?? ""
        },
        mutationKey: ["load-spike"],
        onSuccess: () => {
            toaster.create({
                title: "Load spike simulated",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Failed to simulate load spike",
                type: "error",
            })
        },
        ...props,
    })
}

export function useBreakPanel() {
    return useMutation({
        mutationFn: async ({ name, ticks }: { name: string; ticks?: number }) => {
            const response = await breakSource({
                query: {
                    name,
                    ticks,
                },
            })
            return response.data
        },
        mutationKey: ["break-panel"],
        onSuccess: () => {
            toaster.create({
                title: "Break panel simulated",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Failed to simulate break panel",
                type: "error",
            })
        },
    })
}
