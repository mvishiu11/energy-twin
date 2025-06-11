import { Accordion, Heading, HStack, Progress } from "@chakra-ui/react"
import { interpolate } from "motion/react"
import { LuDatabaseZap } from "react-icons/lu"
import { BaseEntityCard, BaseEntityCardProps } from ".."
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"
import { EditableField } from "../../EditableField"

type BatteryEntityCardProps = Omit<BaseEntityCardProps, "type"> & {
    capacity: number
    chargeLevel?: number
    etaCharge?: number
    etaDischarge?: number
    cRate?: number
    selfDischarge?: number
    initialSoC?: number
}

export function BatteryEntityCard({
    id,
    name,
    capacity,
    chargeLevel,
    etaCharge = 0.95,
    etaDischarge = 0.95,
    cRate = 0.5,
    selfDischarge = 0.01,
    initialSoC = 0.5,
}: BatteryEntityCardProps) {
    const { updateBattery, isRunning } = useSimulationStore()

    const handleCapacityChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateBattery(id, { capacity: Number(value) })
    }

    const handleEtaChargeChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateBattery(id, { etaCharge: Number(value) })
    }

    const handleEtaDischargeChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateBattery(id, { etaDischarge: Number(value) })
    }

    const handleCRateChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateBattery(id, { cRate: Number(value) })
    }

    const handleSelfDischargeChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateBattery(id, { selfDischarge: Number(value) })
    }

    const handleInitialSoCChange = (value: string) => {
        if (isNaN(Number(value))) return
        updateBattery(id, { initialSoC: Number(value) })
    }

    return (
        <BaseEntityCard id={id} name={name} type="battery">
            <Accordion.Root collapsible variant="enclosed">
                <Accordion.Item value="configuration">
                    <Accordion.ItemTrigger>
                        <LuDatabaseZap />
                        <Heading size="md">Battery Configuration</Heading>
                        <Accordion.ItemIndicator />
                    </Accordion.ItemTrigger>
                    <Accordion.ItemContent display="flex" flexDirection="column" gap="4">
                        <EditableField
                            disabled={isRunning}
                            label="Capacity"
                            value={capacity.toString()}
                            onChange={handleCapacityChange}
                        />
                        <EditableField
                            disabled={isRunning}
                            label="Charge Efficiency"
                            value={etaCharge.toString()}
                            onChange={handleEtaChargeChange}
                        />
                        <EditableField
                            disabled={isRunning}
                            label="Discharge Efficiency"
                            value={etaDischarge.toString()}
                            onChange={handleEtaDischargeChange}
                        />
                        <EditableField
                            disabled={isRunning}
                            label="C-Rate"
                            value={cRate.toString()}
                            onChange={handleCRateChange}
                        />
                        <EditableField
                            disabled={isRunning}
                            label="Self Discharge"
                            value={selfDischarge.toString()}
                            onChange={handleSelfDischargeChange}
                        />
                        <EditableField
                            disabled={isRunning}
                            label="Initial SoC"
                            value={initialSoC.toString()}
                            onChange={handleInitialSoCChange}
                        />
                    </Accordion.ItemContent>
                </Accordion.Item>
            </Accordion.Root>
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
