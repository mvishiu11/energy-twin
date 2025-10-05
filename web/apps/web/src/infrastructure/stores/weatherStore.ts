import { create } from "zustand"

export type Weather = {
    sunriseTick: number
    sunsetTick: number
    sunPeakTick: number
    gPeak: number
    tempMeanDay: number
    tempMeanNight: number
    sigmaG: number
    sigmaT: number
}

export type WeatherState = {
    weather: Weather
    setWeather: (weather: Weather) => void
}

export const useWeatherStore = create<WeatherState>()(set => ({
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
}))
