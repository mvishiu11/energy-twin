import { HStack, Progress } from "@chakra-ui/react"
import { interpolate } from "motion/react"
import { BaseEntityCard, BaseEntityCardProps } from ".."
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"
import { EditableField } from "../../EditableField"

type BatteryEntityCardProps = Omit<BaseEntityCardProps, "type"> & {
    capacity: number
    chargeLevel?: number
}

export function BatteryEntityCard({ id, name, capacity, chargeLevel }: BatteryEntityCardProps) {
    const { updateBattery, isRunning } = useSimulationStore()

    const handleValueChange = (value: string) => {
        if (isNaN(Number(value))) return

        updateBattery(id, { capacity: Number(value) })
    }

    return (
        <BaseEntityCard id={id} name={name} type="battery">
            <EditableField
                disabled={isRunning}
                label="Capacity"
                value={capacity.toString()}
                onChange={handleValueChange}
            />
            {chargeLevel !== undefined && (
                <>
                    <Progress.Root max={capacity} min={0} value={chargeLevel}>
                        <HStack gap="2">
                            <Progress.Label>Charge Level</Progress.Label>
                            <Progress.Track flex="1">
                                <Progress.Range background={colorMap(chargeLevel / capacity)} />
                            </Progress.Track>
                            <Progress.ValueText>{(chargeLevel / 1000).toFixed(2)} kW</Progress.ValueText>
                        </HStack>
                    </Progress.Root>
                </>
            )}
        </BaseEntityCard>
    )
}

export const colorMap = interpolate([0, 1], ["#dc2626", "#22c55e"])
