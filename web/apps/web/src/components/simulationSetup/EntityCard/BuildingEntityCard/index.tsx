import { Accordion, Field, Heading } from "@chakra-ui/react"
import { LuBuilding } from "react-icons/lu"
import { BaseEntityCard, BaseEntityCardProps } from ".."
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"
import { EditableField } from "../../EditableField"

type BuildingEntityCardProps = Omit<BaseEntityCardProps, "type"> & {
    nominalLoad: number
    currentLoad?: number
}

export function BuildingEntityCard({
    id,
    name,
    nominalLoad = 50.0,
    currentLoad = 0,
}: BuildingEntityCardProps) {
    const { updateBuilding, isRunning } = useSimulationStore()

    const handleNominalLoadChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateBuilding(id, { nominalLoad: Number(value) })
    }

    return (
        <BaseEntityCard id={id} name={name} type="building">
            <Accordion.Root collapsible variant="enclosed">
                <Accordion.Item value="configuration">
                    <Accordion.ItemTrigger>
                        <LuBuilding />
                        <Heading size="md">Building Configuration</Heading>
                        <Accordion.ItemIndicator />
                    </Accordion.ItemTrigger>
                    <Accordion.ItemContent>
                        <Accordion.ItemBody display="flex" flexDirection="column" gap="4">
                            <EditableField
                                disabled={isRunning}
                                label="Nominal Load (kW)"
                                value={nominalLoad.toString()}
                                onChange={handleNominalLoadChange}
                            />
                        </Accordion.ItemBody>
                    </Accordion.ItemContent>
                </Accordion.Item>
            </Accordion.Root>
            {currentLoad !== undefined && (
                <Field.Root disabled={isRunning} justifyContent="start" orientation="horizontal">
                    <Field.Label>Current load</Field.Label>
                    {(currentLoad / 1000).toFixed(2)} kW
                </Field.Root>
            )}
        </BaseEntityCard>
    )
}
