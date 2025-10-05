import { Card, Flex, IconButton } from "@chakra-ui/react"
import { PropsWithChildren } from "react"
import { LuTrash } from "react-icons/lu"
import { EntityType } from "../../../infrastructure/stores/simulationStore"
import { useEntitiesStore } from "../../../infrastructure/stores/entitiesStore"
import { EditableField } from "../EditableField"

export type BaseEntityCardProps = {
    id: string
    name: string
    type: EntityType
}

export function BaseEntityCard({ id, name, children, type }: PropsWithChildren<BaseEntityCardProps>) {
    const { updateBattery, updateSolar, selectedEntityId, setSelectedEntityId, removeEntity } = useEntitiesStore()

    return (
        <Card.Root
            cursor="pointer"
            marginX="1"
            outlineColor={selectedEntityId === id ? "green.600" : undefined}
            outlineStyle={selectedEntityId === id ? "solid" : undefined}
            outlineWidth={selectedEntityId === id ? 2 : 0}
            variant={selectedEntityId === id ? "elevated" : "subtle"}
            onClick={() => setSelectedEntityId(id)}>
            <Card.Body gap="4">
                <Card.Title>
                    <Flex align="center" direction="row" justify="space-between">
                        <EditableField
                            label="Name"
                            value={name}
                            onChange={name =>
                                type === "battery" ? updateBattery(id, { name }) : updateSolar(id, { name })
                            }
                        />
                        <IconButton size="xs" variant="ghost" onClick={() => removeEntity(id)}>
                            <LuTrash />
                        </IconButton>
                    </Flex>
                </Card.Title>
                {children}
            </Card.Body>
        </Card.Root>
    )
}
