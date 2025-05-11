import { useMutation, useQuery } from "@tanstack/react-query"
import { useSimulationStore } from "../stores/simulationStore"
import { getAllLogs, pauseSimulation, startSimulation, stopSimulation } from "./api"

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
        mutationFn: async (config: string) => {
            const response = await startSimulation({ body: config })
            setIsRunning(true)
            return response.data
        },
        mutationKey: ["start-simulation"],
    })
}

export function usePauseSimulation() {
    const { setIsRunning } = useSimulationStore()

    return useMutation({
        mutationFn: async () => {
            const response = await pauseSimulation()
            setIsRunning(false)
            return response.data
        },
        mutationKey: ["pause-simulation"],
    })
}

export function useStopSimulation() {
    const { setIsRunning } = useSimulationStore()

    return useMutation({
        mutationFn: async () => {
            const response = await stopSimulation()
            setIsRunning(false)
            return response.data
        },
        mutationKey: ["stop-simulation"],
    })
}
