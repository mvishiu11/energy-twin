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

type Weather = {
    sunriseTick: number
    sunsetTick: number
    sunPeakTick: number
    gPeak: number
    tempMeanDay: number
    tempMeanNight: number
    sigmaG: number
    sigmaT: number
}

export type SimulationState = {
    isRunning: boolean
    setIsRunning: (isRunning: boolean) => void
    isPaused: boolean
    setIsPaused: (isPaused: boolean) => void
    tickIntervalMilliseconds: number
    setTickIntervalMilliseconds: (milliseconds: number) => void
    externalSourceCost: number
    setExternalSourceCost: (cost: number) => void
    externalSourceCap: number
    setExternalSourceCap: (cap: number) => void
    weather: {
        sunriseTick: number
        sunsetTick: number
        sunPeakTick: number
        gPeak: number
        tempMeanDay: number
        tempMeanNight: number
        sigmaG: number
        sigmaT: number
    }
    setWeather: (weather: Weather) => void
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

export const useSimulationStore = create<SimulationState>()(set => ({
    isRunning: false,
    isPaused: false,
    setIsRunning: (isRunning: boolean) => set({ isRunning }),
    setIsPaused: (isPaused: boolean) => set({ isPaused }),
    tickIntervalMilliseconds: 1000,
    setTickIntervalMilliseconds: (milliseconds: number) => set({ tickIntervalMilliseconds: milliseconds }),
    externalSourceCost: 5.0,
    setExternalSourceCost: (cost: number) => set({ externalSourceCost: cost }),
    externalSourceCap: 100,
    setExternalSourceCap: (cap: number) => set({ externalSourceCap: cap }),
    weather: {
        sunriseTick: 6,
        sunsetTick: 18,
        sunPeakTick: 12,
        gPeak: 1000,
        tempMeanDay: 26,
        tempMeanNight: 17,
        sigmaG: 0.15,
        sigmaT: 0.8,
    },
    setWeather: (weather: Weather) => set({ weather }),
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
                    (building.id === id ? { ...building, ...updates } : building)),
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
    const { mapEntities } = useSimulationStore()
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
