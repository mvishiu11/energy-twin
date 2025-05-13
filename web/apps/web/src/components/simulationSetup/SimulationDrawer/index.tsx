import { Button, EmptyState, Field, Flex, Heading, IconButton, Input } from "@chakra-ui/react"
import { AnimatePresence, motion } from "motion/react"
import { ReactNode, useEffect, useMemo } from "react"
import { LuCirclePause, LuCirclePlay, LuCircleStop, LuDatabaseZap, LuSun, LuX } from "react-icons/lu"
import { usePauseSimulation, useStartSimulation, useStopSimulation } from "../../../infrastructure/fetching"
import { useDrawerStore } from "../../../infrastructure/stores/drawerStore"
import { useSimulationRuntimeStore } from "../../../infrastructure/stores/simulationRuntimeStore"
import { useSimulationStore } from "../../../infrastructure/stores/simulationStore"
import { TickData } from "../../../infrastructure/websocket/types"
import { useSubscription } from "../../../infrastructure/websocket/useSubscription"
import { simulationConfig } from "../../../services/simulationConfig"
import { BatteryEntityCard } from "../EntityCard/BatteryEntityCard"
import { SolarEntityCard } from "../EntityCard/SolarEntityCard"
import { DrawerRoot } from "./styles"

export function SimulationDrawer() {
    const {
        mapEntities,
        isRunning,
        tickIntervalMilliseconds,
        externalSourceCost,
        externalSourceCap,
        setTickIntervalMilliseconds,
        setExternalSourceCost,
        setExternalSourceCap,
    } = useSimulationStore()
    const { isOpen, setIsOpen, drawerWidth } = useDrawerStore()
    const { mutate: startSimulation } = useStartSimulation()
    const { mutate: pauseSimulation } = usePauseSimulation()
    const { mutate: stopSimulation } = useStopSimulation()

    const { data } = useSubscription<TickData>("/topic/tickData", {
        tickNumber: 0,
        agentStates: {},
    })

    const { setTickNumber, setAgentStates, agentStates } = useSimulationRuntimeStore()

    useEffect(() => {
        if (data) {
            setTickNumber(data.tickNumber)
            setAgentStates(data.agentStates)
        }
    }, [data, setAgentStates, setTickNumber])

    const jsonStringConfig = useMemo(
        () => ({
            simulation: {
                tickIntervalMillis: tickIntervalMilliseconds,
                externalSourceCost: externalSourceCost,
                externalSourceCap: externalSourceCap,
                agents: [
                    ...mapEntities.batteries.map(battery => ({
                        type: "energyStorage",
                        name: battery.id,
                        cost: 2,
                        initialSoC: 10,
                        capacity: battery.capacity,
                    })),
                    ...mapEntities.solar.map(solar => ({
                        type: "energySource",
                        name: solar.id,
                        productionRate: solar.productionRate,
                    })),
                    ...simulationConfig.loads,
                ],
            },
        }),
        [mapEntities, tickIntervalMilliseconds, externalSourceCost, externalSourceCap],
    )

    return (
        <AnimatePresence>
            {isOpen && (
                <DrawerRoot
                    animate={{ translateX: "0%" }}
                    exit={{ translateX: "100%" }}
                    initial={{ translateX: "100%" }}
                    transition={{ duration: 0.2, ease: "circInOut" }}>
                    <Flex direction="column" gap="4" width={drawerWidth}>
                        <Flex direction="row" justify="space-between">
                            <Heading size="xl">Simulation Setup</Heading>
                            <IconButton rounded="xl" variant="ghost" onClick={() => setIsOpen(false)}>
                                <LuX />
                            </IconButton>
                        </Flex>
                        <Field.Root>
                            <Field.Label>Tick Interval</Field.Label>
                            <Input
                                type="number"
                                value={tickIntervalMilliseconds}
                                onChange={e => setTickIntervalMilliseconds(Number(e.target.value))}
                            />
                        </Field.Root>
                        <Field.Root>
                            <Field.Label>External Source Cost</Field.Label>
                            <Input
                                type="number"
                                value={externalSourceCost}
                                onChange={e => setExternalSourceCost(Number(e.target.value))}
                            />
                        </Field.Root>
                        <Field.Root>
                            <Field.Label>External Source Cap</Field.Label>
                            <Input
                                type="number"
                                value={externalSourceCap}
                                onChange={e => setExternalSourceCap(Number(e.target.value))}
                            />
                        </Field.Root>
                        <Flex direction="column" gap="4">
                            <Heading size="md">Batteries</Heading>
                            {mapEntities.batteries.length ? (
                                mapEntities.batteries.map(battery => (
                                    <BatteryEntityCard
                                        key={battery.id}
                                        capacity={battery.capacity}
                                        chargeLevel={agentStates[battery.id]?.stateOfCharge}
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
                            <Heading size="md">Solar Panels</Heading>
                            {mapEntities.solar.length ? (
                                mapEntities.solar.map(solar => (
                                    <SolarEntityCard
                                        key={solar.id}
                                        currentProduction={agentStates[solar.id]?.production}
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
                            <motion.div>
                                <Button
                                    colorPalette={isRunning ? "red" : "green"}
                                    variant="solid"
                                    onClick={() => {
                                        if (isRunning) {
                                            stopSimulation()
                                        } else {
                                            startSimulation(jsonStringConfig as any)
                                        }
                                    }}>
                                    {isRunning ? "Stop" : "Start"}
                                    {isRunning ? <LuCircleStop /> : <LuCirclePlay />}
                                </Button>
                            </motion.div>
                            <Button disabled={!isRunning} variant="outline" onClick={() => pauseSimulation()}>
                                Pause
                                <LuCirclePause />
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
