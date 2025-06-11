import { useEffect } from "react"
import { Metrics, TickData } from "../websocket/types"
import { useSubscription } from "../websocket/useSubscription"
import { useSimulationRuntimeStore } from "./simulationRuntimeStore"
import { useSimulationStore } from "./simulationStore"

export const SimulationRuntimeStoreUpdater: React.FC = () => {
    const { setTickData, setMetrics, setTickDataLoading, setMetricsLoading } = useSimulationRuntimeStore()
    const { mapEntities } = useSimulationStore()

    const { loading: tickDataLoading, data: tickData } = useSubscription<TickData>("/topic/tickData", {
        tickNumber: 0,
        agentStates: {},
    })

    const { loading: metricsLoading, data: metrics } = useSubscription<Metrics>("/topic/metrics", {
        tickNumber: 0,
        totalProduced: 0,
        totalConsumed: 0,
        cnpNegotiations: 0,
        greenEnergyRatioPct: 0,
    })

    useEffect(() => {
        setTickDataLoading(tickDataLoading)
        setMetricsLoading(metricsLoading)
    }, [tickDataLoading, metricsLoading, setTickDataLoading, setMetricsLoading])

    useEffect(() => {
        setTickData(
            tickData,
            mapEntities.solar.map(solar => solar.id),
            mapEntities.batteries.map(battery => battery.id),
        )
        setMetrics(metrics)
    }, [tickData, mapEntities, setTickData, setMetrics, metrics])

    return null
}
