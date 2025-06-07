import { Card, Flex, Heading } from "@chakra-ui/react"
import React from "react"
import { LuInfo } from "react-icons/lu"
import { Tooltip as TooltipUI } from "../../ui/tooltip"

interface ChartCardProps {
    title: string
    tooltip: string
    children: React.ReactNode
}

export function ChartCard({ title, tooltip, children }: ChartCardProps) {
    return (
        <Flex direction="column" gap="4" overflow="hidden">
            <InfoTitle title={title} tooltip={tooltip} />
            <Card.Root size="lg">
                <Card.Body p="4">{children}</Card.Body>
            </Card.Root>
        </Flex>
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
