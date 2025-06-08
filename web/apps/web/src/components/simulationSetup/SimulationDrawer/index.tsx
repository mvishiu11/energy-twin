import { Accordion, Button, EmptyState, Flex, Heading, IconButton, Separator } from "@chakra-ui/react"
import { AnimatePresence, motion } from "motion/react"
import { memo, ReactNode, useMemo } from "react"
import {
    LuBuilding,
    LuCirclePause,
    LuCirclePlay,
    LuCircleStop,
    LuDatabaseZap,
    LuSun,
    LuX,
    LuZapOff,
} from "react-icons/lu"
import {
    useBlackout,
    usePauseSimulation,
    useStartSimulation,
    useStopSimulation,
} from "../../../infrastructure/fetching"
import { resumeSimulation } from "../../../infrastructure/fetching/api"
import { useDrawerStore } from "../../../infrastructure/stores/drawerStore"
import { useSimulationRuntimeStore } from "../../../infrastructure/stores/simulationRuntimeStore"
import { useSimulationStore } from "../../../infrastructure/stores/simulationStore"
import { BatteryEntityCard } from "../EntityCard/BatteryEntityCard"
import { BuildingEntityCard } from "../EntityCard/BuildingEntityCard"
import { SolarEntityCard } from "../EntityCard/SolarEntityCard"
import { LoadSpikeButton } from "./LoadSpikeButton"
import { SimulationSettings } from "./SimulationSettings"
import { DrawerRoot } from "./styles"
import { WeatherSettings } from "./WeatherSettings"

const MemoizedBatteryEntityCard = memo(BatteryEntityCard)
const MemoizedBuildingEntityCard = memo(BuildingEntityCard)
const MemoizedSolarEntityCard = memo(SolarEntityCard)
const MemoizedSimulationSettings = memo(SimulationSettings)
const MemoizedWeatherSettings = memo(WeatherSettings)

export function SimulationDrawer() {
    const {
        mapEntities,
        isRunning,
        tickIntervalMilliseconds,
        externalSourceCost,
        externalSourceCap,
        weather,
        isPaused,
    } = useSimulationStore()
    const { isOpen, setIsOpen, drawerWidth } = useDrawerStore()
    const { mutate: startSimulation } = useStartSimulation()
    const { mutate: pauseSimulation } = usePauseSimulation()
    const { mutate: stopSimulation } = useStopSimulation()
    const { mutate: simulateBlackout } = useBlackout()

    // Using the simulation runtime store directly instead of subscribing to websocket
    const { agentStates } = useSimulationRuntimeStore()

    const jsonStringConfig = useMemo(
        () => ({
            simulation: {
                tickIntervalMillis: tickIntervalMilliseconds,
                externalSourceCost: externalSourceCost,
                externalSourceCap: externalSourceCap,
                metricsPerNTicks: 2,
                weather: {
                    sunriseTick: weather.sunriseTick,
                    sunsetTick: weather.sunsetTick,
                    sunPeakTick: weather.sunPeakTick,
                    gPeak: weather.gPeak,
                    tempMeanDay: weather.tempMeanDay,
                    tempMeanNight: weather.tempMeanNight,
                    sigmaG: weather.sigmaG,
                    sigmaT: weather.sigmaT,
                },
                agents: [
                    ...mapEntities.batteries.map(battery => ({
                        type: "energyStorage",
                        name: battery.id,
                        initialSoC: 10,
                        capacity: battery.capacity,
                        etaCharge: battery.etaCharge,
                        etaDischarge: battery.etaDischarge,
                        cRate: battery.cRate,
                        selfDischarge: battery.selfDischarge,
                    })),
                    ...mapEntities.solar.map(solar => ({
                        type: "energySource",
                        name: solar.id,
                        noOfPanels: solar.noOfPanels,
                        area: solar.area,
                        efficiency: solar.efficiency,
                        tempCoeff: solar.tempCoeff,
                        noct: solar.noct,
                    })),
                    ...mapEntities.buildings.map(building => ({
                        type: "load",
                        name: building.id,
                        nominalLoad: building.nominalLoad,
                    })),
                ],
            },
        }),
        [mapEntities, tickIntervalMilliseconds, externalSourceCost, externalSourceCap, weather],
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
                        <Flex
                            backgroundColor="white"
                            borderBottom="1px solid #e2e8f0"
                            direction="row"
                            justify="space-between"
                            paddingBottom="2"
                            paddingTop="6"
                            position="sticky"
                            top="0"
                            zIndex="999">
                            <Heading size="xl">Simulation Setup</Heading>
                            <IconButton rounded="xl" variant="ghost" onClick={() => setIsOpen(false)}>
                                <LuX />
                            </IconButton>
                        </Flex>
                        <Flex direction="column" gap="4">
                            <Heading size="md">Events</Heading>
                            <Flex direction="row" gap="2">
                                <Button disabled={!isRunning} variant="surface" onClick={() => simulateBlackout()}>
                                    Simulate Blackout <LuZapOff />
                                </Button>
                                <LoadSpikeButton disabled={!isRunning} />
                            </Flex>
                        </Flex>
                        <Separator />
                        <MemoizedSettingsAccordion />
                        <Flex direction="column" gap="4">
                            <Heading size="md">Batteries</Heading>
                            {mapEntities.batteries.length ? (
                                mapEntities.batteries.map(battery => (
                                    <MemoizedBatteryEntityCard
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
                                    <MemoizedSolarEntityCard
                                        key={solar.id}
                                        area={solar.area}
                                        currentProduction={agentStates[solar.id]?.production}
                                        efficiency={solar.efficiency}
                                        id={solar.id}
                                        name={solar.name}
                                        noct={solar.noct}
                                        noOfPanels={solar.noOfPanels}
                                        tempCoeff={solar.tempCoeff}
                                    />
                                ))
                            ) : (
                                <EmptyStateMessage
                                    icon={<LuSun />}
                                    message="No solar panels added. Drag and drop a solar panel from the toolkit at the bottom to add one."
                                />
                            )}
                            <Heading size="md">Buildings</Heading>
                            {mapEntities.buildings.length ? (
                                mapEntities.buildings.map(building => (
                                    <MemoizedBuildingEntityCard
                                        key={building.id}
                                        currentLoad={agentStates[building.id]?.demand}
                                        id={building.id}
                                        name={building.name}
                                        nominalLoad={building.nominalLoad}
                                    />
                                ))
                            ) : (
                                <EmptyStateMessage
                                    icon={<LuBuilding />}
                                    message="No buildings added. Drag and drop a building from the toolkit at the bottom to add one."
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
                                            if (isPaused) {
                                                resumeSimulation()
                                            } else {
                                                stopSimulation()
                                            }
                                        } else {
                                            startSimulation(jsonStringConfig as any)
                                        }
                                    }}>
                                    {isRunning ? (isPaused ? "Resume" : "Stop") : "Start"}
                                    {isRunning ? isPaused ? <LuCirclePlay /> : <LuCircleStop /> : <LuCirclePlay />}
                                </Button>
                            </motion.div>
                            <Button
                                disabled={!isRunning || isPaused}
                                variant="outline"
                                onClick={() => pauseSimulation()}>
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

function SettingsAccordion() {
    return (
        <Accordion.Root collapsible multiple defaultValue={["simulation-parameters"]} size="lg" variant="enclosed">
            <MemoizedSimulationSettings />
            <MemoizedWeatherSettings />
        </Accordion.Root>
    )
}

const MemoizedSettingsAccordion = memo(SettingsAccordion)
