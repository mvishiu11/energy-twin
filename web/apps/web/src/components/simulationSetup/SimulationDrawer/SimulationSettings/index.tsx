import { Accordion, Field, Heading, Input } from "@chakra-ui/react"
import { LuSettings } from "react-icons/lu"
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"
import { useSimulationSettingsStore } from "../../../../infrastructure/stores/simulationSettingsStore"

export function SimulationSettings() {
    const {
        tickIntervalMilliseconds,
        setTickIntervalMilliseconds,
        externalSourceCost,
        setExternalSourceCost,
        externalSourceCap,
        setExternalSourceCap,
    } = useSimulationSettingsStore()
    const { isRunning, isPaused } = useSimulationStore()

    return (
        <Accordion.Item value="simulation-parameters">
            <Accordion.ItemTrigger>
                <LuSettings />
                <Heading size="md">Simulation Parameters</Heading>
                <Accordion.ItemIndicator />
            </Accordion.ItemTrigger>
            <Accordion.ItemContent>
                <Accordion.ItemBody display="flex" flexDirection="column" gap="4">
                    <Field.Root>
                        <Field.Label>Tick Interval</Field.Label>
                        <Input
                            disabled={isRunning || isPaused}
                            type="number"
                            value={tickIntervalMilliseconds}
                            onChange={e => setTickIntervalMilliseconds(Number(e.target.value))}
                        />
                    </Field.Root>
                    <Field.Root>
                        <Field.Label>External Source Cost</Field.Label>
                        <Input
                            disabled={isRunning || isPaused}
                            type="number"
                            value={externalSourceCost}
                            onChange={e => setExternalSourceCost(Number(e.target.value))}
                        />
                    </Field.Root>
                    <Field.Root>
                        <Field.Label>External Source Cap</Field.Label>
                        <Input
                            disabled={isRunning || isPaused}
                            type="number"
                            value={externalSourceCap}
                            onChange={e => setExternalSourceCap(Number(e.target.value))}
                        />
                    </Field.Root>
                </Accordion.ItemBody>
            </Accordion.ItemContent>
        </Accordion.Item>
    )
}
