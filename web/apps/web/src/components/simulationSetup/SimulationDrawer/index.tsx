import { Button, EmptyState, Flex, Heading, IconButton } from "@chakra-ui/react"
import { AnimatePresence } from "motion/react"
import { ReactNode } from "react"
import { LuCirclePlay, LuCircleStop, LuDatabaseZap, LuSun, LuX } from "react-icons/lu"
import { startSimulation, stopSimulation } from "../../../infrastructure/fetching/api"
import { useDrawerStore } from "../../../infrastructure/stores/drawerStore"
import { useSimulationStore } from "../../../infrastructure/stores/simulationStore"
import { BatteryEntityCard } from "../EntityCard/BatteryEntityCard"
import { SolarEntityCard } from "../EntityCard/SolarEntityCard"
import { DrawerRoot } from "./styles"

export function SimulationDrawer() {
    const { mapEntities } = useSimulationStore()
    const { isOpen, setIsOpen, drawerWidth } = useDrawerStore()

    return (
        <AnimatePresence>
            {isOpen && (
                <DrawerRoot
                    animate={{ translateX: "0%" }}
                    exit={{ translateX: "100%" }}
                    initial={{ translateX: "100%" }}>
                    <Flex direction="column" gap="4" width={drawerWidth}>
                        <Flex direction="row" justify="space-between">
                            <Heading size="xl">Simulation Setup</Heading>
                            <IconButton rounded="xl" variant="ghost" onClick={() => setIsOpen(false)}>
                                <LuX />
                            </IconButton>
                        </Flex>
                        <Flex direction="column" gap="4">
                            <Heading size="xl">Batteries</Heading>
                            {mapEntities.batteries.length ? (
                                mapEntities.batteries.map(battery => (
                                    <BatteryEntityCard
                                        key={battery.id}
                                        capacity={battery.capacity}
                                        id={battery.id}
                                        name={battery.name}
                                    />
                                ))
                            ) : (
                                <EmptyStateMessage
                                    icon={<LuDatabaseZap />}
                                    message="No batteries added. Drag and drop a battery from the toolkit at the bottom to add one."
                                />
                            )}
                            <Heading size="xl">Solar Panels</Heading>
                            {mapEntities.solar.length ? (
                                mapEntities.solar.map(solar => (
                                    <SolarEntityCard
                                        key={solar.id}
                                        id={solar.id}
                                        name={solar.name}
                                        productionRate={solar.productionRate}
                                    />
                                ))
                            ) : (
                                <EmptyStateMessage
                                    icon={<LuSun />}
                                    message="No solar panels added. Drag and drop a solar panel from the toolkit at the bottom to add one."
                                />
                            )}
                        </Flex>
                        <Flex direction="row" gap="2" justifyContent="flex-end">
                            <Button variant="solid" onClick={() => startSimulation()}>
                                Start
                                <LuCirclePlay />
                            </Button>
                            <Button colorPalette="red" variant="solid" onClick={() => stopSimulation()}>
                                Stop
                                <LuCircleStop />
                            </Button>
                        </Flex>
                    </Flex>
                </DrawerRoot>
            )}
        </AnimatePresence>
    )
}

type EmptyStateProps = {
    message: string
    icon: ReactNode
}

function EmptyStateMessage({ message, icon }: EmptyStateProps) {
    return (
        <EmptyState.Root>
            <EmptyState.Content>
                <EmptyState.Indicator>{icon}</EmptyState.Indicator>
                <EmptyState.Description>{message}</EmptyState.Description>
            </EmptyState.Content>
        </EmptyState.Root>
    )
}
