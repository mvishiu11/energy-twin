import { useCallback } from "react"
import { create } from "zustand"

export type EntityType = "battery" | "solar"

export type MapEntity = {
    id: string
    name: string
    coordinates: [number, number]
}

export type Battery = MapEntity & { capacity: number }

export type Solar = MapEntity & { productionRate: number }

export type SimulationState = {
    isRunning: boolean
    setIsRunning: (isRunning: boolean) => void
    tickIntervalMilliseconds: number
    setTickIntervalMilliseconds: (milliseconds: number) => void
    externalSourceCost: number
    setExternalSourceCost: (cost: number) => void
    externalSourceCap: number
    setExternalSourceCap: (cap: number) => void
    mapEntities: {
        batteries: Battery[]
        solar: Solar[]
    }
    addBattery: (entity: Battery) => void
    addSolar: (entity: Solar) => void
    updateBattery: (id: string, updates: Partial<Battery>) => void
    updateSolar: (id: string, updates: Partial<Solar>) => void
    selectedEntityId?: string
    setSelectedEntityId: (id?: string) => void
    removeEntity: (id: string) => void
}

export const useSimulationStore = create<SimulationState>()(set => ({
    isRunning: false,
    setIsRunning: (isRunning: boolean) => set({ isRunning }),
    tickIntervalMilliseconds: 1000,
    setTickIntervalMilliseconds: (milliseconds: number) => set({ tickIntervalMilliseconds: milliseconds }),
    externalSourceCost: 5.0,
    setExternalSourceCost: (cost: number) => set({ externalSourceCost: cost }),
    externalSourceCap: 100,
    setExternalSourceCap: (cap: number) => set({ externalSourceCap: cap }),
    mapEntities: {
        batteries: [],
        solar: [],
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
    selectedEntityId: undefined,
    setSelectedEntityId: (id?: string) => set({ selectedEntityId: id }),
    removeEntity: (id: string) =>
        set(state => ({
            mapEntities: {
                ...state.mapEntities,
                batteries: state.mapEntities.batteries.filter(battery => battery.id !== id),
                solar: state.mapEntities.solar.filter(solar => solar.id !== id),
            },
        })),
}))

export const useFindEntityById = () => {
    const { mapEntities } = useSimulationStore()
    return useCallback(
        (id: string) => {
            return (
                mapEntities.batteries.find(battery => battery.id === id) ||
                mapEntities.solar.find(solar => solar.id === id)
            )
        },
        [mapEntities],
    )
}
