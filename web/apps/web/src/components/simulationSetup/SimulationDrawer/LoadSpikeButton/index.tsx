import { Button, createListCollection, Dialog, Field, Flex, Heading, Input, Portal, Select } from "@chakra-ui/react"
import { ChartNoAxesCombined } from "lucide-react"
import { useMemo, useRef, useState } from "react"
import { LuZap } from "react-icons/lu"
import { useLoadSpike } from "../../../../infrastructure/fetching"
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"

type LoadSpikeButtonProps = {
    disabled?: boolean
}

export function LoadSpikeButton({ disabled }: LoadSpikeButtonProps) {
    const containerRef = useRef<HTMLDivElement>(null)
    const { mutate: simulateLoadSpike } = useLoadSpike()

    const {
        mapEntities: { buildings },
    } = useSimulationStore()

    const buildingsCollection = useMemo(
        () =>
            createListCollection({ items: buildings.map(building => ({ label: building.name, value: building.id })) }),
        [buildings],
    )

    const [ticksDuration, setTicksDuration] = useState(5)
    const [selectedBuilding, setSelectedBuilding] = useState(buildings.at(0)?.id)
    const [loadSpikeRate, setLoadSpikeRate] = useState(2)

    return (
        <Dialog.Root placement="center">
            <Dialog.Trigger asChild>
                <Button disabled={disabled} variant="surface">
                    Simulate Load Spike <ChartNoAxesCombined />
                </Button>
            </Dialog.Trigger>
            <Portal>
                <Dialog.Backdrop />
                <Dialog.Positioner>
                    <Dialog.Content ref={containerRef}>
                        <Dialog.Header>
                            <Dialog.Title>
                                <Heading size="lg">Simulate Load Spike</Heading>
                            </Dialog.Title>
                        </Dialog.Header>
                        <Dialog.Body>
                            <Flex direction="column" gap="2">
                                <Select.Root
                                    collection={buildingsCollection}
                                    value={[selectedBuilding ?? ""]}
                                    onValueChange={details => setSelectedBuilding(details.value[0])}>
                                    <Select.HiddenSelect />
                                    <Select.Label>Select Building</Select.Label>
                                    <Select.Control>
                                        <Select.Trigger>
                                            <Select.ValueText placeholder="Select Building" />
                                        </Select.Trigger>
                                        <Select.IndicatorGroup>
                                            <Select.Indicator />
                                        </Select.IndicatorGroup>
                                    </Select.Control>
                                    <Portal container={containerRef}>
                                        <Select.Positioner>
                                            <Select.Content>
                                                {buildingsCollection.items.map(building => (
                                                    <Select.Item key={building.value} item={building}>
                                                        {building.label}
                                                        <Select.ItemIndicator />
                                                    </Select.Item>
                                                ))}
                                            </Select.Content>
                                        </Select.Positioner>
                                    </Portal>
                                </Select.Root>
                                <Field.Root>
                                    <Field.Label>Ticks duration</Field.Label>
                                    <Input
                                        type="number"
                                        value={ticksDuration}
                                        onChange={e => setTicksDuration(Number(e.target.value))}
                                    />
                                </Field.Root>
                                <Field.Root>
                                    <Field.Label>Load spike rate</Field.Label>
                                    <Input
                                        type="number"
                                        value={loadSpikeRate}
                                        onChange={e => setLoadSpikeRate(Number(e.target.value))}
                                    />
                                </Field.Root>
                            </Flex>
                        </Dialog.Body>
                        <Dialog.Footer>
                            <Dialog.ActionTrigger asChild>
                                <Button variant="outline">Close</Button>
                            </Dialog.ActionTrigger>
                            <Dialog.ActionTrigger asChild>
                                <Button
                                    disabled={!selectedBuilding}
                                    onClick={() => {
                                        simulateLoadSpike({
                                            name: selectedBuilding ?? "",
                                            rate: loadSpikeRate,
                                            ticks: ticksDuration,
                                        })
                                    }}>
                                    Simulate Load Spike
                                    <LuZap />
                                </Button>
                            </Dialog.ActionTrigger>
                        </Dialog.Footer>
                    </Dialog.Content>
                </Dialog.Positioner>
            </Portal>
        </Dialog.Root>
    )
}
