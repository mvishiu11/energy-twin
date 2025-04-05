import { ReactNode } from "react"
import { Marker } from "react-map-gl/mapbox"
import { type Battery, type Solar, useSimulationStore } from "../../../../infrastructure/stores/simulationStore"

type MarkerType = "battery" | "solar"

type UseMarkersProps = {
    type: MarkerType
    component: ReactNode
}

export function useMarkers({ type, component }: UseMarkersProps) {
    const { mapEntities, addBattery, addSolar, updateBattery, updateSolar } = useSimulationStore()

    const addMarker = (position: [number, number]) => {
        if (type === "battery") {
            const entity: Battery = {
                id: crypto.randomUUID(),
                coordinates: position,
                capacity: 100,
            }
            addBattery(entity)
        } else {
            const entity: Solar = {
                id: crypto.randomUUID(),
                coordinates: position,
                productionRate: 100,
            }
            addSolar(entity)
        }
    }

    const markers = (type === "battery" ? mapEntities.batteries : mapEntities.solar).map(entity => (
        <Marker
            key={entity.id}
            draggable
            latitude={entity.coordinates[1]}
            longitude={entity.coordinates[0]}
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
            {component}
        </Marker>
    ))

    return {
        markers,
        addMarker,
    }
}
