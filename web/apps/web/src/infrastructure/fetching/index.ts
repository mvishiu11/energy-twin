import { useQuery } from "@tanstack/react-query"
import { getLogs } from "./api"

export function useLogs() {
    return useQuery({
        queryKey: ["logs"],
        queryFn: async () => {
            const logs = await getLogs()
            return logs.data
        },
        refetchInterval: 1000,
    })
}
