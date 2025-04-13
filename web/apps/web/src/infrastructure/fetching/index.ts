import { useQuery } from "@tanstack/react-query"
import { getLogs } from "./api"

export function useLogs() {
    return useQuery({
        queryKey: ["logs"],
        queryFn: () => getLogs(),
        refetchInterval: 1000,
    })
}
