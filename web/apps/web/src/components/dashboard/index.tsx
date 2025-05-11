import { Metrics } from "../../infrastructure/websocket/types"
import { useSubscription } from "../../infrastructure/websocket/useSubscription"

export function Dashboard() {
    const { data } = useSubscription<Metrics>("/topic/metrics", {
        tickNumber: 0,
        totalProduced: 0,
        totalConsumed: 0,
        cnpNegotiations: 0,
        greenEnergyRatioPct: 0,
    })

    return <div>{JSON.stringify(data)}</div>
}
