import { useMutation, useQuery } from "@tanstack/react-query"
import { toaster } from "../../components/ui/toaster"
import { useSimulationStore } from "../stores/simulationStore"
import {
    getAllLogs,
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
                description: "The simulation has been started.",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Simulation failed to start",
                description: "The simulation failed to start.",
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
                description: "The simulation has been paused.",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Simulation failed to pause",
                description: "The simulation failed to pause.",
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
                description: "The simulation has been stopped.",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Simulation failed to stop",
                description: "The simulation failed to stop.",
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
                description: "The weather has been injected.",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Failed to inject weather",
                description: "The weather failed to inject.",
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
                description: "The simulation has been resumed.",
                type: "success",
            })
        },
        onError: () => {
            toaster.create({
                title: "Simulation failed to resume",
                description: "The simulation failed to resume.",
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
