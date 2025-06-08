import { ReactNode } from "react"
import { Marker } from "react-map-gl/mapbox"
import { useDrawerStore } from "../../../../infrastructure/stores/drawerStore"
import {
    type Battery,
    type Building,
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
    const { mapEntities, addBattery, addSolar, addBuilding, updateBattery, updateSolar, updateBuilding, setSelectedEntityId, selectedEntityId } =
        useSimulationStore()

    const { setIsOpen } = useDrawerStore()

    const addMarker = (position: [number, number], name: string) => {
        setIsOpen(true)
        let id = "";
        if (type === "battery") {
            id = `Battery ${mapEntities.batteries.length + 1}`
            const entity: Battery = {
                id,
                name,
                coordinates: position,
                capacity: 50000,
                etaCharge: 0.97,
                etaDischarge: 0.96,
                cRate: 0.7,
                selfDischarge: 3.9e-4,
                initialSoC: 90.0,
            }
            addBattery(entity)
        } else if (type === "solar") {
            id = `Solar ${mapEntities.solar.length + 1}`
            const entity: Solar = {
                id,
                name,
                coordinates: position,
                noOfPanels: 200,
                area: 2.0,
                efficiency: 0.21,
                tempCoeff: -0.004,
                noct: 44,
            }
            addSolar(entity)
        } else if (type === "building") {
            id = `Building ${mapEntities.buildings.length + 1}`
            const entity: Building = {
                id,
                name,
                coordinates: position,
                nominalLoad: 50.0,
            }
            addBuilding(entity)
        }
        setSelectedEntityId(id)
    }

    const getEntitiesByType = () => {
        switch(type) {
            case "battery": return mapEntities.batteries;
            case "solar": return mapEntities.solar;
            case "building": return mapEntities.buildings;
            default: return [];
        }
    }

    const markers = getEntitiesByType().map(entity => (
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
                } else if (type === "solar") {
                    updateSolar(entity.id, {
                        coordinates: [lng, lat],
                    })
                } else if (type === "building") {
                    updateBuilding(entity.id, {
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
