import { ReactNode } from "react"
import { Marker } from "react-map-gl/mapbox"
import { useDrawerStore } from "../../../../infrastructure/stores/drawerStore"
import {
    type Battery,
    EntityType,
    type Solar,
    useSimulationStore,
} from "../../../../infrastructure/stores/simulationStore"
import { SelectedMarker } from "./styles"

type UseMarkersProps = {
    type: EntityType
    component: ((id: string) => ReactNode) | ReactNode
}

export function useMarkers({ type, component }: UseMarkersProps) {
    const { mapEntities, addBattery, addSolar, updateBattery, updateSolar, setSelectedEntityId, selectedEntityId } =
        useSimulationStore()

    const { setIsOpen } = useDrawerStore()

    const addMarker = (position: [number, number], name: string) => {
        setIsOpen(true)
        let id: string
        if (type === "battery") {
            id = `Battery ${mapEntities.batteries.length + 1}`
            const entity: Battery = {
                id,
                name,
                coordinates: position,
                capacity: 50000,
            }
            addBattery(entity)
        } else {
            id = `Solar ${mapEntities.solar.length + 1}`
            const entity: Solar = {
                id,
                name,
                coordinates: position,
                productionRate: 75,
            }
            addSolar(entity)
        }
        setSelectedEntityId(id)
    }

    const markers = (type === "battery" ? mapEntities.batteries : mapEntities.solar).map(entity => (
        <Marker
            key={entity.id}
            draggable
            latitude={entity.coordinates[1]}
            longitude={entity.coordinates[0]}
            onClick={() => {
                setSelectedEntityId(entity.id)
                setIsOpen(true)
            }}
            onDragEnd={e => {
                const { lng, lat } = e.target.getLngLat()
                if (type === "battery") {
                    updateBattery(entity.id, {
                        coordinates: [lng, lat],
                    })
                } else {
                    updateSolar(entity.id, {
                        coordinates: [lng, lat],
                    })
                }
            }}>
            <SelectedMarker isSelected={selectedEntityId === entity.id}>
                {typeof component === "function" ? component(entity.id) : component}
            </SelectedMarker>
        </Marker>
    ))

    return {
        markers,
        addMarker,
    }
}
