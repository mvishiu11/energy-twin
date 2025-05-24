import { HStack, Icon } from "@chakra-ui/react"
import { LuDatabaseZap } from "react-icons/lu"
import { useSimulationRuntimeStore } from "../../../infrastructure/stores/simulationRuntimeStore"
import { useSimulationStore } from "../../../infrastructure/stores/simulationStore"
import { colorMap } from "../../simulationSetup/EntityCard/BatteryEntityCard"
import { ChargeBar, ChargeBarContainer } from "./styles"

type BatteryMarkerProps = {
    id: string
}

export function BatteryMarker({ id }: BatteryMarkerProps) {
    const { agentStates } = useSimulationRuntimeStore()
    const {
        mapEntities: { batteries },
    } = useSimulationStore()

    const chargeLevel = agentStates[id]?.stateOfCharge ?? 0
    const maxCapacity = batteries.find(battery => battery.id === id)?.capacity ?? 0

    return (
        <HStack>
            <Icon color="green.500" size="2xl">
                <LuDatabaseZap strokeWidth={2.5} />
            </Icon>
            <ChargeBarContainer>
                <ChargeBar
                    $backgroundColor={colorMap(chargeLevel / (maxCapacity ?? 1))}
                    animate={{ height: `${(chargeLevel / (maxCapacity ?? 1)) * 100}%` }}
                />
            </ChargeBarContainer>
        </HStack>
    )
}
