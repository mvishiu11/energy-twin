import { useEffect, useState } from "react"
import { Chart, useChart } from "@chakra-ui/charts"
import { CartesianGrid, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from "recharts"
import { useSimulationStore } from "../../../infrastructure/stores/simulationStore"
import { TickData } from "../../../infrastructure/websocket/types"
import { useSubscription } from "../../../infrastructure/websocket/useSubscription"

export function AllBatteriesChart() {
    const { mapEntities, isRunning } = useSimulationStore()
    const { data } = useSubscription<TickData>("/topic/tickData", {
        tickNumber: 0,
        agentStates: {},
    })

    const [chartData, setChartData] = useState<Record<string, number>[]>([])

    useEffect(() => {
        if (!isRunning) {
            setChartData([])
            return
        }
        if (data) {
            setChartData(prev => {
                if (data.agentStates && data.tickNumber !== 0) {
                    return [
                        ...prev,
                        {
                            tickNumber: data.tickNumber,
                            ...Object.fromEntries(
                                Object.entries(data.agentStates ?? {})
                                    .filter(([agentId]) =>
                                        mapEntities.batteries.some(battery => battery.id === agentId),
                                    )
                                    .map(([agentId, state]) => [agentId, state.stateOfCharge]),
                            ),
                        },
                    ]
                }
                return prev
            })
        }
    }, [data, isRunning, mapEntities.batteries])

    const chart = useChart({
        data: chartData,
        series: mapEntities.batteries.map((battery, index) => ({
            name: battery.id,
            color: seriesColors[index % seriesColors.length],
        })),
    })

    return (
        <Chart.Root chart={chart} height="100%" width="100%">
            <LineChart data={chart.data}>
                <CartesianGrid stroke={chart.color("border")} vertical={false} />
                <XAxis
                    axisLine={false}
                    dataKey={chart.key("tickNumber")}
                    label={{
                        value: "Tick number",
                        position: "bottom",
                    }}
                    stroke={chart.color("border")}
                />
                <YAxis
                    axisLine={false}
                    label={{
                        value: "State of charge",
                        position: "left",
                        angle: -90,
                    }}
                    stroke={chart.color("border")}
                    tickLine={false}
                    tickMargin={10}
                />
                <Tooltip animationDuration={100} content={<Chart.Tooltip />} cursor={false} />
                <Legend content={<Chart.Legend interaction="hover" />} verticalAlign="top" />
                {chart.series.map(item => (
                    <Line
                        key={item.name}
                        dataKey={chart.key(item.name)}
                        dot={false}
                        isAnimationActive={false}
                        opacity={chart.getSeriesOpacity(item.name)}
                        stroke={chart.color(item.color)}
                        strokeWidth={2}
                    />
                ))}
            </LineChart>
        </Chart.Root>
    )
}

const seriesColors = [
    "green.500",
    "yellow.500",
    "teal.500",
    "purple.500",
    "orange.500",
    "red.500",
    "blue.500",
    "pink.500",
    "gray.500",
    "cyan.500",
]
