import { Field } from "@chakra-ui/react"
import { BaseEntityCard, BaseEntityCardProps } from ".."
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"
import { EditableField } from "../../EditableField"

type SolarEntityCardProps = Omit<BaseEntityCardProps, "type"> & {
    productionRate: number
    currentProduction: number
}

export function SolarEntityCard({ id, name, productionRate, currentProduction }: SolarEntityCardProps) {
    const { updateSolar, isRunning } = useSimulationStore()

    const handleValueChange = (value: string) => {
        if (isNaN(Number(value))) return

        updateSolar(id, { productionRate: Number(value) })
    }

    return (
        <BaseEntityCard id={id} name={name} type="solar">
            <EditableField
                disabled={isRunning}
                label="Production Rate"
                value={productionRate.toString()}
                onChange={handleValueChange}
            />
            <Field.Root disabled={isRunning} justifyContent="start" orientation="horizontal">
                <Field.Label>Current production</Field.Label>
                {(currentProduction ?? 0 / 1000).toFixed(2)} kW
            </Field.Root>
        </BaseEntityCard>
    )
}
