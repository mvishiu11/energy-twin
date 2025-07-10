import { Button, createListCollection, Dialog, Field, Flex, Heading, Input, Portal, Select } from "@chakra-ui/react"
import { useRef, useState } from "react"
import { LuWrench } from "react-icons/lu"
import { useBreakPanel } from "../../../../infrastructure/fetching"
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"

type BreakPanelButtonProps = {
    disabled: boolean
}

export function BreakPanelButton({ disabled }: BreakPanelButtonProps) {
    const containerRef = useRef<HTMLDivElement>(null)
    const { mutate: breakPanel } = useBreakPanel()

    const {
        mapEntities: { solar },
    } = useSimulationStore()

    const solarCollection = createListCollection({
        items: solar.map(solar => ({ label: solar.name, value: solar.id })),
    })

    const [selectedSolar, setSelectedSolar] = useState(solar.at(0)?.id)
    const [ticksDuration, setTicksDuration] = useState(5)

    return (
        <Dialog.Root>
            <Dialog.Trigger asChild>
                <Button disabled={disabled} variant="surface">
                    Break Panel
                    <LuWrench />
                </Button>
            </Dialog.Trigger>
            <Portal>
                <Dialog.Backdrop />
                <Dialog.Positioner>
                    <Dialog.Content ref={containerRef}>
                        <Dialog.Header>
                            <Dialog.Title>
                                <Heading size="lg">Break Panel</Heading>
                            </Dialog.Title>
                        </Dialog.Header>
                        <Dialog.Body>
                            <Flex direction="column" gap="2">
                                <Select.Root
                                    collection={solarCollection}
                                    value={[selectedSolar ?? ""]}
                                    onValueChange={details => setSelectedSolar(details.value[0])}>
                                    <Select.HiddenSelect />
                                    <Select.Label>Select Solar Panel</Select.Label>
                                    <Select.Control>
                                        <Select.Trigger>
                                            <Select.ValueText placeholder="Select Solar Panel" />
                                        </Select.Trigger>
                                        <Select.IndicatorGroup>
                                            <Select.Indicator />
                                        </Select.IndicatorGroup>
                                    </Select.Control>
                                    <Portal container={containerRef}>
                                        <Select.Positioner>
                                            <Select.Content>
                                                {solarCollection.items.map(solar => (
                                                    <Select.Item key={solar.value} item={solar}>
                                                        {solar.label}
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
                            </Flex>
                        </Dialog.Body>
                        <Dialog.Footer>
                            <Dialog.ActionTrigger asChild>
                                <Button variant="outline">Close</Button>
                            </Dialog.ActionTrigger>
                            <Dialog.ActionTrigger asChild>
                                <Button
                                    disabled={!selectedSolar}
                                    onClick={() => {
                                        breakPanel({
                                            name: selectedSolar ?? "",
                                            ticks: ticksDuration,
                                        })
                                    }}>
                                    Break Panel
                                    <LuWrench />
                                </Button>
                            </Dialog.ActionTrigger>
                        </Dialog.Footer>
                    </Dialog.Content>
                </Dialog.Positioner>
            </Portal>
        </Dialog.Root>
    )
}
