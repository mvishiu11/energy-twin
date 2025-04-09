import { Card } from "@chakra-ui/react"
import { PropsWithChildren } from "react"
import { EntityType, useSimulationStore } from "../../../infrastructure/stores/simulationStore"
import { EditableField } from "../EditableField"

export type BaseEntityCardProps = {
    id: string
    name: string
    type: EntityType
}

export function BaseEntityCard({ id, name, children, type }: PropsWithChildren<BaseEntityCardProps>) {
    const { updateBattery, updateSolar, selectedEntityId, setSelectedEntityId } = useSimulationStore()

    return (
        <Card.Root
            cursor="pointer"
            outlineColor={selectedEntityId === id ? "green.600" : undefined}
            outlineStyle={selectedEntityId === id ? "solid" : undefined}
            outlineWidth={selectedEntityId === id ? 2 : 0}
            variant={selectedEntityId === id ? "elevated" : "subtle"}
            onClick={() => setSelectedEntityId(id)}>
            <Card.Body gap="4">
                <Card.Title>
                    <EditableField
                        label="Name"
                        value={name}
                        onChange={name =>
                            type === "battery" ? updateBattery(id, { name }) : updateSolar(id, { name })
                        }
                    />
                </Card.Title>
                {children}
            </Card.Body>
        </Card.Root>
    )
}
