import { Card, Flex, Heading } from "@chakra-ui/react"
import { useEffect, useState } from "react"
import { LuInfo } from "react-icons/lu"
import { Chart, useChart } from "@chakra-ui/charts"
import { CartesianGrid, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from "recharts"
import { Metrics } from "../../infrastructure/websocket/types"
import { useSubscription } from "../../infrastructure/websocket/useSubscription"
import { Tooltip as TooltipUI } from "../ui/tooltip"
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
        setChartData(prev => [
            ...prev,
            { tickNumber: data.tickNumber, totalProduced: data.totalProduced, totalConsumed: data.totalConsumed },
        ])
        setGreenEnergyRatio(prev => [
            ...prev,
            { tickNumber: data.tickNumber, greenEnergyRatio: data.greenEnergyRatioPct },
        ])
    }, [data])

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
            <Flex direction="column" gap={4}>
                <InfoTitle title="Totals" tooltip="Total energy produced and consumed" />
                <Card.Root size="lg">
                    <Card.Body p="4">
                        <Chart.Root chart={chart}>
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
                    </Card.Body>
                </Card.Root>
            </Flex>
            <Flex direction="column" gap={4}>
                <InfoTitle
                    title="Green energy ratio"
                    tooltip="The ratio of green energy to total energy i.e. green energy / total energy"
                />
                <Card.Root size="lg">
                    <Card.Body p="4">
                        <Chart.Root chart={greenEnergyChart}>
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
                    </Card.Body>
                </Card.Root>
            </Flex>
        </DashboardContainer>
    )
}

type InfoTitleProps = {
    title: string
    tooltip: string
}

function InfoTitle({ title, tooltip }: InfoTitleProps) {
    return (
        <TooltipUI content={tooltip} positioning={{ placement: "top" }}>
            <Flex align="center" cursor="pointer" direction="row" gap={2} width="fit-content">
                <Heading>{title}</Heading>
                <LuInfo />
            </Flex>
        </TooltipUI>
    )
}
