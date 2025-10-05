import { create } from "zustand"

export type Forecast = {
    H_hist: number
    H_pred: number
    replanEvery: number
    epsilonBreak: number
    useMC: number
}

export type ForecastState = {
    forecast: Forecast
    setForecast: (forecast: Forecast) => void
}

export const useForecastStore = create<ForecastState>()(set => ({
    forecast: {
        H_hist: 24,
        H_pred: 24,
        replanEvery: 1,
        epsilonBreak: 0.1,
        useMC: 0,
    },
    setForecast: (forecast: Forecast) => set({ forecast }),
}))
