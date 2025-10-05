import { Accordion, Field, Heading } from "@chakra-ui/react"
import { LuSun } from "react-icons/lu"
import { BaseEntityCard, BaseEntityCardProps } from ".."
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"
import { useEntitiesStore } from "../../../../infrastructure/stores/entitiesStore"
import { EditableField } from "../../EditableField"

type SolarEntityCardProps = Omit<BaseEntityCardProps, "type"> & {
    _productionRate?: number
    currentProduction?: number
    noOfPanels?: number
    area?: number
    efficiency?: number
    tempCoeff?: number
    noct?: number
}

export function SolarEntityCard({
    id,
    name,
    currentProduction = 0,
    noOfPanels = 10,
    area = 1.5,
    efficiency = 0.2,
    tempCoeff = -0.004,
    noct = 45,
}: SolarEntityCardProps) {
    const { updateSolar } = useEntitiesStore()
    const { isRunning } = useSimulationStore()

    const handleNoOfPanelsChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateSolar(id, { noOfPanels: Number(value) })
    }

    const handleAreaChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateSolar(id, { area: Number(value) })
    }

    const handleEfficiencyChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateSolar(id, { efficiency: Number(value) })
    }

    const handleTempCoeffChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateSolar(id, { tempCoeff: Number(value) })
    }

    const handleNoctChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateSolar(id, { noct: Number(value) })
    }

    return (
        <BaseEntityCard id={id} name={name} type="solar">
            <Accordion.Root collapsible variant="enclosed">
                <Accordion.Item value="configuration">
                    <Accordion.ItemTrigger>
                        <LuSun />
                        <Heading size="md">Solar Configuration</Heading>
                        <Accordion.ItemIndicator />
                    </Accordion.ItemTrigger>
                    <Accordion.ItemContent>
                        <Accordion.ItemBody display="flex" flexDirection="column" gap="4">
                            <EditableField
                                disabled={isRunning}
                                label="Number of Panels"
                                value={noOfPanels.toString()}
                                onChange={handleNoOfPanelsChange}
                            />
                            <EditableField
                                disabled={isRunning}
                                label="Panel Area (m²)"
                                value={area.toString()}
                                onChange={handleAreaChange}
                            />
                            <EditableField
                                disabled={isRunning}
                                label="Efficiency"
                                value={efficiency.toString()}
                                onChange={handleEfficiencyChange}
                            />
                            <EditableField
                                disabled={isRunning}
                                label="Temperature Coefficient"
                                value={tempCoeff.toString()}
                                onChange={handleTempCoeffChange}
                            />
                            <EditableField
                                disabled={isRunning}
                                label="NOCT (°C)"
                                value={noct.toString()}
                                onChange={handleNoctChange}
                            />
                        </Accordion.ItemBody>
                    </Accordion.ItemContent>
                </Accordion.Item>
            </Accordion.Root>
            <Field.Root disabled={isRunning} justifyContent="start" orientation="horizontal">
                <Field.Label>Current production</Field.Label>
                {(currentProduction ?? 0 / 1000).toFixed(2)} kW
            </Field.Root>
        </BaseEntityCard>
    )
}
