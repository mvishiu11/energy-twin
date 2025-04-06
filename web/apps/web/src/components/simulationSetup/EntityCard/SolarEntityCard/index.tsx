import { BaseEntityCard, BaseEntityCardProps } from ".."
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"
import { EditableField } from "../../EditableField"

type SolarEntityCardProps = BaseEntityCardProps & {
    productionRate: number
}

export function SolarEntityCard({ id, name, productionRate }: SolarEntityCardProps) {
    const { updateSolar } = useSimulationStore()

    const handleValueChange = (value: string) => {
        if (isNaN(Number(value))) return

        updateSolar(id, { productionRate: Number(value) })
    }

    return (
        <BaseEntityCard id={id} name={name}>
            <EditableField label="Production Rate" value={productionRate.toString()} onChange={handleValueChange} />
        </BaseEntityCard>
    )
}
