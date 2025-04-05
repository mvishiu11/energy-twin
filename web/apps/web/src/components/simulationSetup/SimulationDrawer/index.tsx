import { CloseButton, Drawer, DrawerTitle, Flex, Heading } from "@chakra-ui/react"
import { useDrawerStore } from "../../../infrastructure/stores/drawerStore"
import { useSimulationStore } from "../../../infrastructure/stores/simulationStore"
import { BatteryEntityCard } from "../EntityCard/BatteryEntityCard"

export function SimulationDrawer() {
    const { mapEntities } = useSimulationStore()
    const { isOpen: drawerIsOpen, setIsOpen } = useDrawerStore()

    return (
        <Drawer.Root open={drawerIsOpen} size="lg" onOpenChange={e => setIsOpen(e.open)}>
            <Drawer.Positioner>
                <Drawer.Content>
                    <Drawer.CloseTrigger asChild>
                        <CloseButton />
                    </Drawer.CloseTrigger>
                    <Drawer.Header>
                        <DrawerTitle>Simulation Setup</DrawerTitle>
                    </Drawer.Header>
                    <Drawer.Body>
                        <Flex direction="column" gap="2">
                            <Heading>Batteries</Heading>
                            {mapEntities.batteries.map(battery => (
                                <BatteryEntityCard
                                    key={battery.id}
                                    capacity={battery.capacity}
                                    id={battery.id}
                                    name={battery.name}
                                />
                            ))}
                        </Flex>
                    </Drawer.Body>
                </Drawer.Content>
            </Drawer.Positioner>
        </Drawer.Root>
    )
}
