import { useCallback } from "react"
import { create } from "zustand"

export type EntityType = "battery" | "building" | "solar"

export type MapEntity = {
    id: string
    name: string
    coordinates: [number, number]
}

export type Battery = MapEntity & {
    capacity: number
    etaCharge: number
    etaDischarge: number
    cRate: number
    selfDischarge: number
    initialSoC: number
}

export type Solar = MapEntity & {
    noOfPanels: number
    area: number
    efficiency: number
    tempCoeff: number
    noct: number
}

export type Building = MapEntity & {
    nominalLoad: number
}

export type EntitiesState = {
    mapEntities: {
        batteries: Battery[]
        solar: Solar[]
        buildings: Building[]
    }
    addBattery: (entity: Battery) => void
    addSolar: (entity: Solar) => void
    addBuilding: (entity: Building) => void
    updateBattery: (id: string, updates: Partial<Battery>) => void
    updateSolar: (id: string, updates: Partial<Solar>) => void
    updateBuilding: (id: string, updates: Partial<Building>) => void
    selectedEntityId?: string
    setSelectedEntityId: (id?: string) => void
    removeEntity: (id: string) => void
}

export const useEntitiesStore = create<EntitiesState>()(set => ({
    mapEntities: {
        batteries: [],
        solar: [],
        buildings: [],
    },
    addBattery: (entity: Battery) =>
        set(state => ({
            mapEntities: {
                ...state.mapEntities,
                batteries: [...state.mapEntities.batteries, entity],
            },
        })),
    addSolar: (entity: Solar) =>
        set(state => ({
            mapEntities: {
                ...state.mapEntities,
                solar: [...state.mapEntities.solar, entity],
            },
        })),
    addBuilding: (entity: Building) =>
        set(state => ({
            mapEntities: {
                ...state.mapEntities,
                buildings: [...state.mapEntities.buildings, entity],
            },
        })),
    updateBattery: (id: string, updates: Partial<Battery>) =>
        set(state => ({
            mapEntities: {
                ...state.mapEntities,
                batteries: state.mapEntities.batteries.map(battery =>
                    battery.id === id ? { ...battery, ...updates } : battery,
                ),
            },
        })),
    updateSolar: (id: string, updates: Partial<Solar>) =>
        set(state => ({
            mapEntities: {
                ...state.mapEntities,
                solar: state.mapEntities.solar.map(solar => (solar.id === id ? { ...solar, ...updates } : solar)),
            },
        })),
    updateBuilding: (id: string, updates: Partial<Building>) =>
        set(state => ({
            mapEntities: {
                ...state.mapEntities,
                buildings: state.mapEntities.buildings.map(building =>
                    building.id === id ? { ...building, ...updates } : building,
                ),
            },
        })),
    selectedEntityId: undefined,
    setSelectedEntityId: (id?: string) => set({ selectedEntityId: id }),
    removeEntity: (id: string) =>
        set(state => ({
            mapEntities: {
                ...state.mapEntities,
                batteries: state.mapEntities.batteries.filter(battery => battery.id !== id),
                solar: state.mapEntities.solar.filter(solar => solar.id !== id),
                buildings: state.mapEntities.buildings.filter(building => building.id !== id),
            },
        })),
}))

export const useFindEntityById = () => {
    const { mapEntities } = useEntitiesStore()
    return useCallback(
        (id: string) => {
            return (
                mapEntities.batteries.find(battery => battery.id === id) ||
                mapEntities.solar.find(solar => solar.id === id) ||
                mapEntities.buildings.find(building => building.id === id)
            )
        },
        [mapEntities],
    )
}
