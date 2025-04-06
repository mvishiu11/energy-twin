import { BaseEntityCard, BaseEntityCardProps } from ".."
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"
import { EditableField } from "../../EditableField"

type BatteryEntityCardProps = Omit<BaseEntityCardProps, "type"> & {
    capacity: number
}

export function BatteryEntityCard({ id, name, capacity }: BatteryEntityCardProps) {
    const { updateBattery } = useSimulationStore()

    const handleValueChange = (value: string) => {
        if (isNaN(Number(value))) return

        updateBattery(id, { capacity: Number(value) })
    }

    return (
        <BaseEntityCard id={id} name={name} type="battery">
            <EditableField label="Capacity" value={capacity.toString()} onChange={handleValueChange} />
        </BaseEntityCard>
    )
}
