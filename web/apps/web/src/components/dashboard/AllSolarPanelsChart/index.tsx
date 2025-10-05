import { Chart, useChart } from "@chakra-ui/charts"
import { CartesianGrid, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from "recharts"
import { useEntitiesStore } from "../../../infrastructure/stores/entitiesStore"
import { useSimulationRuntimeStore } from "../../../infrastructure/stores/simulationRuntimeStore"

// Reusing the same series colors as the AllBatteriesChart component likely uses
const seriesColors = [
    "blue.500",
    "green.500",
    "orange.500",
    "purple.500",
    "red.500",
    "teal.500",
    "yellow.500",
    "cyan.500",
]

export function AllSolarPanelsChart() {
    const { mapEntities } = useEntitiesStore()
    const { solarPanelsChartData } = useSimulationRuntimeStore()

    const chart = useChart({
        data: solarPanelsChartData,
        series: mapEntities.solar.map((solarPanel, index: number) => ({
            name: solarPanel.id,
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
                {chart.series.map(item => (
                    <Line
                        key={item.name}
                        dataKey={chart.key(item.name)}
                        dot={false}
                        isAnimationActive={false}
                        opacity={chart.getSeriesOpacity(String(item.name))}
                        stroke={chart.color(item.color)}
                        strokeWidth={2}
                    />
                ))}
            </LineChart>
        </Chart.Root>
    )
}
