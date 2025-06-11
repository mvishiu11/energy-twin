import { useEffect, useState } from "react"
import { Metrics, TickData } from "../websocket/types"
import { useSubscription } from "../websocket/useSubscription"
import { useSimulationRuntimeStore } from "./simulationRuntimeStore"
import { useSimulationStore } from "./simulationStore"

export const SimulationRuntimeStoreUpdater: React.FC = () => {
    const { setTickData, setMetrics, setTickDataLoading, setMetricsLoading } = useSimulationRuntimeStore()
    const { mapEntities } = useSimulationStore()
    const [lastTickNumber, setLastTickNumber] = useState(0)
    const [lastMetricsTickNumber, setLastMetricsTickNumber] = useState(0)

    const { loading: tickDataLoading, data: tickData } = useSubscription<TickData>("/topic/tickData", {
        tickNumber: 0,
        agentStates: {},
        predictedLoadKw: 0,
        predictedPvKw: 0,
        errorLoadKw: 0,
        errorPvKw: 0,
        fanLo: [],
        fanHi: [],
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
        setLastTickNumber(tickData.tickNumber)
        setLastMetricsTickNumber(metrics.tickNumber)

        if (tickData.tickNumber !== lastTickNumber) {
            setTickData(
                tickData,
                mapEntities.solar.map(solar => solar.id),
                mapEntities.batteries.map(battery => battery.id),
            )
        }

        if (metrics.tickNumber !== lastMetricsTickNumber) {
            setMetrics(metrics)
        }
    }, [tickData, mapEntities, setTickData, setMetrics, metrics, lastTickNumber, lastMetricsTickNumber])

    return null
}
