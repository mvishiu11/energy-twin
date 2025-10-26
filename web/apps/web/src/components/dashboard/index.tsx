import { EmptyState } from "@chakra-ui/react"
import { BarChart, Battery, LineChart as LineChartIcon, PieChart, Zap, ZapOff } from "lucide-react"
import { ReactNode } from "react"
import { Chart, useChart } from "@chakra-ui/charts"
import { CartesianGrid, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from "recharts"
import { useSimulationRuntimeStore } from "../../infrastructure/stores/simulationRuntimeStore"
import { AllBatteriesChart } from "./AllBatteriesChart"
import { AllSolarPanelsChart } from "./AllSolarPanelsChart"
import { ChartCard } from "./ChartCard"
import { PredictionsLoadChart, PredictionsPvChart } from "./PredictionsChart"
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

type EmptyChartProps = {
    message: string
    icon: ReactNode
}

function EmptyChart({ message, icon }: EmptyChartProps) {
    return (
        <EmptyState.Root alignItems="center" display="flex" height="300px" justifyContent="center" width="100%">
            <EmptyState.Content>
                <EmptyState.Indicator>{icon}</EmptyState.Indicator>
                <EmptyState.Description>{message}</EmptyState.Description>
            </EmptyState.Content>
        </EmptyState.Root>
    )
}

export function Dashboard() {
    const {
        greenEnergyRatioChartData,
        totalProducedChartData,
        batteriesChartData,
        solarPanelsChartData,
        predictionData,
    } = useSimulationRuntimeStore()

    const chart = useChart<TotalProducedChartData>({
        data: totalProducedChartData,
        series: [
            { name: "totalProduced", color: "green.500" },
            { name: "totalConsumed", color: "red.500" },
        ],
    })

    const greenEnergyChart = useChart<GreenEnergyRatioChartData>({
        data: greenEnergyRatioChartData,
        series: [{ name: "greenEnergyRatio", color: "green.500" }],
    })

    return (
        <DashboardContainer>
            <ChartCard title="Totals" tooltip="Total energy produced and consumed">
                {!totalProducedChartData || totalProducedChartData.length === 0 ? (
                    <EmptyChart icon={<Zap size={24} />} message="No energy production data available yet" />
                ) : (
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
                )}
            </ChartCard>
            <ChartCard
                title="Green energy ratio"
                tooltip="The ratio of green energy to total energy i.e. green energy / total energy">
                {!greenEnergyRatioChartData || greenEnergyRatioChartData.length === 0 ? (
                    <EmptyChart icon={<PieChart size={24} />} message="No green energy ratio data available yet" />
                ) : (
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
                )}
            </ChartCard>
            <ChartCard
                title="All Batteries"
                tooltip="Displays the state of charge for all battery agents in the simulation.">
                {!batteriesChartData || batteriesChartData.length === 0 ? (
                    <EmptyChart icon={<Battery size={24} />} message="No battery data available yet" />
                ) : (
                    <AllBatteriesChart />
                )}
            </ChartCard>
            <ChartCard
                title="Solar Panel Production"
                tooltip="Displays the production for all solar panel agents in the simulation.">
                {!solarPanelsChartData || solarPanelsChartData.length === 0 ? (
                    <EmptyChart icon={<ZapOff size={24} />} message="No solar panel production data available yet" />
                ) : (
                    <AllSolarPanelsChart />
                )}
            </ChartCard>
            <ChartCard
                title="Load Predictions"
                tooltip="Displays the predictions for all load agents in the simulation.">
                {!predictionData || predictionData.length === 0 ? (
                    <EmptyChart icon={<BarChart size={24} />} message="No load prediction data available yet" />
                ) : (
                    <PredictionsLoadChart />
                )}
            </ChartCard>
            <ChartCard title="Pv Predictions" tooltip="Displays the predictions for all pv agents in the simulation.">
                {!predictionData || predictionData.length === 0 ? (
                    <EmptyChart icon={<LineChartIcon size={24} />} message="No PV prediction data available yet" />
                ) : (
                    <PredictionsPvChart />
                )}
            </ChartCard>
        </DashboardContainer>
    )
}
