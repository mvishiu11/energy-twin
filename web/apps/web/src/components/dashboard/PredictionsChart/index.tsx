import { Chart, useChart } from "@chakra-ui/charts"
import { Area, CartesianGrid, ComposedChart, Legend, Line, Tooltip, XAxis, YAxis } from "recharts"
import { useSimulationRuntimeStore } from "../../../infrastructure/stores/simulationRuntimeStore"

export function PredictionsChart() {
    const { predictionData } = useSimulationRuntimeStore()

    console.log(predictionData)

    const chartData = predictionData.map(item => ({
        tickNumber: item.tickNumber,
        confidenceInterval: [item.fanLo[0], item.fanHi[0]],
        predictionPv: item.predictedPvKw,
    }))

    // console.log(chartData)

    const chart = useChart({
        data: chartData,
        series: [
            { name: "confidenceInterval", color: "gray.500" },
            { name: "predictionPv", color: "gray.500" },
        ],
    })

    return (
        <Chart.Root chart={chart}>
            <ComposedChart data={chart.data}>
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
                        value: "Production",
                        position: "left",
                        angle: -90,
                    }}
                    stroke={chart.color("border")}
                    tickLine={false}
                    tickMargin={10}
                />
                <Tooltip animationDuration={100} content={<Chart.Tooltip />} cursor={false} />
                <Legend content={<Chart.Legend interaction="hover" />} verticalAlign="top" />
                <Area
                    dataKey={chart.key("confidenceInterval")}
                    fill={chart.color("gray.500")}
                    fillOpacity={0.2}
                    isAnimationActive={false}
                    stackId="a"
                    stroke={chart.color("gray.500")}
                />
                <Line
                    dataKey={chart.key("predictionPv")}
                    dot={false}
                    isAnimationActive={false}
                    opacity={chart.getSeriesOpacity("predictionPv")}
                    stroke={chart.color("gray.500")}
                    strokeWidth={2}
                />
            </ComposedChart>
        </Chart.Root>
    )
}
