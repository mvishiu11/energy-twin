// This file is auto-generated by @hey-api/openapi-ts

import { client as _heyApiClient } from "./client.gen"
import type {
    BlackoutData,
    BlackoutResponse,
    BreakSourceData,
    BreakSourceResponse,
    GetAllLogsData,
    GetAllLogsResponse,
    GetLogsForAgentData,
    GetLogsForAgentResponse,
    LoadSpikeData,
    LoadSpikeResponse,
    PauseSimulationData,
    PauseSimulationResponse,
    ResumeSimulationData,
    ResumeSimulationResponse,
    SetSpeedData,
    SetSpeedResponse,
    StartSimulationData,
    StartSimulationResponse,
    StopSimulationData,
    StopSimulationResponse,
    UpdateWeatherData,
    UpdateWeatherResponse,
} from "./types.gen"
import type { Client, Options as ClientOptions, TDataShape } from "@hey-api/client-fetch"

export type Options<TData extends TDataShape = TDataShape, ThrowOnError extends boolean = boolean> = ClientOptions<
    TData,
    ThrowOnError
> & {
    /**
     * You can provide a client instance returned by `createClient()` instead of
     * individual options. This might be also useful if you want to implement a
     * custom client.
     */
    client?: Client
    /**
     * You can pass arbitrary values through the `meta` object. This can be
     * used to access values that aren't defined as part of the SDK function.
     */
    meta?: Record<string, unknown>
}

export const updateWeather = <ThrowOnError extends boolean = false>(
    options: Options<UpdateWeatherData, ThrowOnError>,
) => {
    return (options.client ?? _heyApiClient).post<UpdateWeatherResponse, unknown, ThrowOnError>({
        url: "/simulation/weather/update",
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...options?.headers,
        },
    })
}

export const stopSimulation = <ThrowOnError extends boolean = false>(
    options?: Options<StopSimulationData, ThrowOnError>,
) => {
    return (options?.client ?? _heyApiClient).post<StopSimulationResponse, unknown, ThrowOnError>({
        url: "/simulation/stop",
        ...options,
    })
}

export const startSimulation = <ThrowOnError extends boolean = false>(
    options: Options<StartSimulationData, ThrowOnError>,
) => {
    return (options.client ?? _heyApiClient).post<StartSimulationResponse, unknown, ThrowOnError>({
        url: "/simulation/start",
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...options?.headers,
        },
    })
}

export const setSpeed = <ThrowOnError extends boolean = false>(options: Options<SetSpeedData, ThrowOnError>) => {
    return (options.client ?? _heyApiClient).post<SetSpeedResponse, unknown, ThrowOnError>({
        url: "/simulation/control/speed",
        ...options,
    })
}

export const resumeSimulation = <ThrowOnError extends boolean = false>(
    options?: Options<ResumeSimulationData, ThrowOnError>,
) => {
    return (options?.client ?? _heyApiClient).post<ResumeSimulationResponse, unknown, ThrowOnError>({
        url: "/simulation/control/resume",
        ...options,
    })
}

export const pauseSimulation = <ThrowOnError extends boolean = false>(
    options?: Options<PauseSimulationData, ThrowOnError>,
) => {
    return (options?.client ?? _heyApiClient).post<PauseSimulationResponse, unknown, ThrowOnError>({
        url: "/simulation/control/pause",
        ...options,
    })
}

export const loadSpike = <ThrowOnError extends boolean = false>(options: Options<LoadSpikeData, ThrowOnError>) => {
    return (options.client ?? _heyApiClient).post<LoadSpikeResponse, unknown, ThrowOnError>({
        url: "/events/loadSpike",
        ...options,
    })
}

export const breakSource = <ThrowOnError extends boolean = false>(options: Options<BreakSourceData, ThrowOnError>) => {
    return (options.client ?? _heyApiClient).post<BreakSourceResponse, unknown, ThrowOnError>({
        url: "/events/breakComponent",
        ...options,
    })
}

export const blackout = <ThrowOnError extends boolean = false>(options?: Options<BlackoutData, ThrowOnError>) => {
    return (options?.client ?? _heyApiClient).post<BlackoutResponse, unknown, ThrowOnError>({
        url: "/events/blackout",
        ...options,
    })
}

export const getAllLogs = <ThrowOnError extends boolean = false>(options?: Options<GetAllLogsData, ThrowOnError>) => {
    return (options?.client ?? _heyApiClient).get<GetAllLogsResponse, unknown, ThrowOnError>({
        url: "/simulation/logs",
        ...options,
    })
}

export const getLogsForAgent = <ThrowOnError extends boolean = false>(
    options: Options<GetLogsForAgentData, ThrowOnError>,
) => {
    return (options.client ?? _heyApiClient).get<GetLogsForAgentResponse, unknown, ThrowOnError>({
        url: "/simulation/logs/{agentName}",
        ...options,
    })
}
