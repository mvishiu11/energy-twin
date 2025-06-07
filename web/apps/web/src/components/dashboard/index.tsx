import { useEffect, useState } from "react"
import { Chart, useChart } from "@chakra-ui/charts"
import { CartesianGrid, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from "recharts"
import { useSimulationStore } from "../../infrastructure/stores/simulationStore"
import { Metrics } from "../../infrastructure/websocket/types"
import { useSubscription } from "../../infrastructure/websocket/useSubscription"
import { AllBatteriesChart } from "./AllBatteriesChart"
import { AllSolarPanelsChart } from "./AllSolarPanelsChart"
import { ChartCard } from "./ChartCard"
import { DashboardContainer } from "./styles"

type TotalProducedChartData = {
    tickNumber: number
    totalProduced: number
    totalConsumed: number
}

type GreenEnergyRatioChartData = {
    tickNumber: number
    greenEnergyRatio: number
}

export function Dashboard() {
    const { isRunning } = useSimulationStore()
    const { data } = useSubscription<Metrics>("/topic/metrics", {
        tickNumber: 0,
        totalProduced: 0,
        totalConsumed: 0,
        cnpNegotiations: 0,
        greenEnergyRatioPct: 0,
    })

    const [chartData, setChartData] = useState<TotalProducedChartData[]>([])
    const [greenEnergyRatio, setGreenEnergyRatio] = useState<GreenEnergyRatioChartData[]>([])

    useEffect(() => {
        if (!isRunning) {
            setChartData([])
            setGreenEnergyRatio([])
            return
        }
        setChartData(prev => [
            ...prev,
            { tickNumber: data.tickNumber, totalProduced: data.totalProduced, totalConsumed: data.totalConsumed },
        ])
        setGreenEnergyRatio(prev => [
            ...prev,
            { tickNumber: data.tickNumber, greenEnergyRatio: data.greenEnergyRatioPct },
        ])
    }, [data, isRunning])

    const chart = useChart<TotalProducedChartData>({
        data: chartData,
        series: [
            { name: "totalProduced", color: "green.500" },
            { name: "totalConsumed", color: "red.500" },
        ],
    })

    const greenEnergyChart = useChart<GreenEnergyRatioChartData>({
        data: greenEnergyRatio,
        series: [{ name: "greenEnergyRatio", color: "green.500" }],
    })

    return (
        <DashboardContainer>
            <ChartCard title="Totals" tooltip="Total energy produced and consumed">
                <Chart.Root chart={chart} height="100%" overflow="hidden" width="100%">
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
                            dataKey={chart.key("totalProduced")}
                            label={{
                                value: "Total produced",
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
            </ChartCard>
            <ChartCard
                title="Green energy ratio"
                tooltip="The ratio of green energy to total energy i.e. green energy / total energy">
                <Chart.Root chart={greenEnergyChart} height="100%" width="100%">
                    <LineChart data={greenEnergyChart.data}>
                        <CartesianGrid stroke={greenEnergyChart.color("border")} vertical={false} />
                        <XAxis
                            axisLine={false}
                            dataKey={greenEnergyChart.key("tickNumber")}
                            label={{
                                value: "Tick number",
                                position: "bottom",
                            }}
                            stroke={greenEnergyChart.color("border")}
                        />
                        <YAxis
                            axisLine={false}
                            dataKey={greenEnergyChart.key("greenEnergyRatio")}
                            label={{
                                value: "Green energy ratio",
                                position: "left",
                                angle: -90,
                            }}
                            stroke={greenEnergyChart.color("border")}
                            tickLine={false}
                            tickMargin={10}
                        />
                        <Tooltip animationDuration={100} content={<Chart.Tooltip />} cursor={false} />
                        <Legend content={<Chart.Legend interaction="hover" />} verticalAlign="top" />
                        {greenEnergyChart.series.map(item => (
                            <Line
                                key={item.name}
                                dataKey={greenEnergyChart.key(item.name)}
                                dot={false}
                                isAnimationActive={false}
                                opacity={greenEnergyChart.getSeriesOpacity(item.name)}
                                stroke={greenEnergyChart.color(item.color)}
                                strokeWidth={2}
                            />
                        ))}
                    </LineChart>
                </Chart.Root>
            </ChartCard>
            <ChartCard
                title="All Batteries"
                tooltip="Displays the state of charge for all battery agents in the simulation.">
                <AllBatteriesChart />
            </ChartCard>
            <ChartCard
                title="Solar Panel Production"
                tooltip="Displays the production for all solar panel agents in the simulation.">
                <AllSolarPanelsChart />
            </ChartCard>
        </DashboardContainer>
    )
}
