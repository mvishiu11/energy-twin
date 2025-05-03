import { useMutation, useQuery } from "@tanstack/react-query"
import { getAllLogs, startSimulation } from "./api"

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
    return useMutation({
        mutationFn: async (config: string) => {
            const response = await startSimulation({ body: config })
            return response.data
        },
        mutationKey: ["start-simulation"],
    })
}
