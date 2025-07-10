import { Chart, useChart } from "@chakra-ui/charts"
import { Area, CartesianGrid, ComposedChart, Legend, Line, Tooltip, XAxis, YAxis } from "recharts"
import { useSimulationRuntimeStore } from "../../../infrastructure/stores/simulationRuntimeStore"

export function PredictionsLoadChart() {
    const { predictionData } = useSimulationRuntimeStore()

    const chartData = predictionData.map(item => ({
        tickNumber: item.tickNumber,
        predictionPv: item.predictedPvKw,
        predictionLoad: item.predictedLoadKw,
        actualLoad: item.predictedLoadKw + item.errorLoadKw,
    }))

    const chart = useChart({
        data: chartData,
        series: [
            { name: "predictionLoad", color: "blue.500" },
            { name: "actualLoad", color: "red.500" },
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
                <Line
                    dataKey={chart.key("predictionLoad")}
                    dot={false}
                    isAnimationActive={false}
                    opacity={chart.getSeriesOpacity("predictionLoad")}
                    stroke={chart.color("blue.500")}
                    strokeDasharray="5 5"
                    strokeWidth={2}
                />
                <Line
                    dataKey={chart.key("actualLoad")}
                    dot={false}
                    isAnimationActive={false}
                    opacity={chart.getSeriesOpacity("actualLoad") ?? 1 * 0.3}
                    stroke={chart.color("red.500")}
                    strokeWidth={2}
                />
            </ComposedChart>
        </Chart.Root>
    )
}

export function PredictionsPvChart() {
    const { predictionData } = useSimulationRuntimeStore()

    const chartData = predictionData.map(item => ({
        tickNumber: item.tickNumber,
        confidenceInterval: [item.fanLoLoad[0], item.fanHiLoad[0]],
        predictionPv: item.predictedPvKw,
        actualPv: item.errorPvKw + item.predictedPvKw,
    }))

    const chart = useChart({
        data: chartData,
        series: [
            { name: "predictionPv", color: "blue.500" },
            { name: "actualPv", color: "red.500" },
        ],
    })

    return (
        <Chart.Root chart={chart} height="100%" width="100%">
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
                <Line
                    dataKey={chart.key("predictionPv")}
                    dot={false}
                    isAnimationActive={false}
                    opacity={chart.getSeriesOpacity("predictionPv")}
                    stroke={chart.color("blue.500")}
                    strokeDasharray="5 5"
                    strokeWidth={2}
                />
                <Line
                    dataKey={chart.key("actualPv")}
                    dot={false}
                    isAnimationActive={false}
                    opacity={chart.getSeriesOpacity("actualPv") ?? 1 * 0.3}
                    stroke={chart.color("red.500")}
                    strokeWidth={2}
                />
                <Area
                    connectNulls
                    dataKey="confidenceInterval"
                    dot={false}
                    fill="#b0ade9"
                    isAnimationActive={false}
                    stroke="none"
                    type="basis"
                />
            </ComposedChart>
        </Chart.Root>
    )
}
