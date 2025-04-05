import { create } from "zustand"

export type MapEntity = {
    id: string
    coordinates: [number, number]
}

export type Battery = MapEntity & { capacity: number }

export type Solar = MapEntity & { productionRate: number }

export type SimulationState = {
    isRunning: boolean
    start: () => void
    stop: () => void
    mapEntities: {
        batteries: Battery[]
        solar: Solar[]
    }
    addBattery: (entity: Battery) => void
    addSolar: (entity: Solar) => void
    updateBattery: (id: string, updates: Partial<Battery>) => void
    updateSolar: (id: string, updates: Partial<Solar>) => void
}

export const useSimulationStore = create<SimulationState>()(set => ({
    isRunning: false,
    start: () => set({ isRunning: true }),
    stop: () => set({ isRunning: false }),
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
                    battery.id === id ? { ...battery, ...updates } : battery
                ),
            },
        })),
    updateSolar: (id: string, updates: Partial<Solar>) =>
        set(state => ({
            mapEntities: {
                ...state.mapEntities,
                solar: state.mapEntities.solar.map(solar =>
                    solar.id === id ? { ...solar, ...updates } : solar
                ),
            },
        })),
}))
