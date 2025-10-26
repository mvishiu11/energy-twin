import { Box, Button, Fieldset, Heading, Input, Progress, Stack, Text } from "@chakra-ui/react"
import { useCallback, useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import Papa from "papaparse"
import {
    useBlackout,
    useBreakPanel,
    useLoadSpike,
    useStartSimulation,
    useStopSimulation,
} from "../../infrastructure/fetching"
import { useBenchmarkStore } from "../../infrastructure/stores/benchmarkStore"
import { useEntitiesStore } from "../../infrastructure/stores/entitiesStore"
import { useForecastStore } from "../../infrastructure/stores/forecastStore"
import { useSimulationRuntimeStore } from "../../infrastructure/stores/simulationRuntimeStore"
import { useSimulationSettingsStore } from "../../infrastructure/stores/simulationSettingsStore"
import { useWeatherStore } from "../../infrastructure/stores/weatherStore"
import { Field } from "../ui/field"
import { toaster } from "../ui/toaster"

type BenchmarkFormValues = {
    // Battery config
    batteryCapacity: number
    batteryEtaCharge: number
    batteryEtaDischarge: number
    batteryCRate: number
    batterySelfDischarge: number
    batteryInitialSoC: number

    // Solar config
    solarNoOfPanels: number
    solarArea: number
    solarEfficiency: number
    solarTempCoeff: number
    solarNoct: number

    // Building config
    buildingNominalLoad: number

    // Simulation duration
    durationTicks: number
}

export function Benchmarks() {
    const { isRunning, setIsRunning, progress, setProgress, targetTicks, setTargetTicks } = useBenchmarkStore()
    const {
        tickNumber,
        totalProducedChartData,
        greenEnergyRatioChartData,
        batteriesChartData,
        solarPanelsChartData,
        predictionData,
        resetChartData,
    } = useSimulationRuntimeStore()
    const { addBattery, addSolar, addBuilding } = useEntitiesStore()
    const { tickIntervalMilliseconds, externalSourceCost, externalSourceCap } = useSimulationSettingsStore()
    const { weather } = useWeatherStore()
    const { forecast } = useForecastStore()
    const { mutateAsync: startSimulation, isPending: isStartPending } = useStartSimulation()
    const { mutateAsync: stopSimulation } = useStopSimulation()
    const [isEventTriggered, setIsEventTriggered] = useState(false)
    const { mutate: breakPanel } = useBreakPanel()
    const { mutate: triggerBlackout } = useBlackout()
    const { mutate: triggerLoadSpike } = useLoadSpike()

    const triggerEvent = useCallback(
        (type: "batteryBreak" | "blackout" | "loadSpike" | "solarBreak", ticks?: number) => {
            switch (type) {
                case "blackout":
                    triggerBlackout()
                    break
                case "loadSpike":
                    triggerLoadSpike({ name: "BenchmarkBuilding", rate: 2, ticks: ticks ?? 5 })
                    break
                case "solarBreak":
                    breakPanel({ name: "BenchmarkSolar", ticks: ticks ?? 5 })
                    break
                case "batteryBreak":
                    breakPanel({ name: "BenchmarkBattery", ticks: ticks ?? 5 })
                    break
            }
        },
        [triggerBlackout, triggerLoadSpike, breakPanel],
    )

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<BenchmarkFormValues>({
        defaultValues: {
            batteryCapacity: 100,
            batteryEtaCharge: 0.95,
            batteryEtaDischarge: 0.95,
            batteryCRate: 1.0,
            batterySelfDischarge: 0.001,
            batteryInitialSoC: 50,

            // Default solar values
            solarNoOfPanels: 200,
            solarArea: 2.0,
            solarEfficiency: 0.2,
            solarTempCoeff: -0.004,
            solarNoct: 45,

            buildingNominalLoad: 50,

            durationTicks: 336,
        },
    })

    const downloadCSV = useCallback(() => {
        const length = totalProducedChartData.length
        console.log(predictionData)

        const data = []
        for (let i = 0; i < length; i++) {
            const totalData = totalProducedChartData[i]
            const currentTick = totalData?.tickNumber ?? ""
            const greenData = greenEnergyRatioChartData.find(data => data.tickNumber === currentTick)
            const batteryData = batteriesChartData.find(data => data.tickNumber === currentTick)
            const solarData = solarPanelsChartData.find(data => data.tickNumber === currentTick)
            const predData = predictionData.find(data => data.tickNumber === currentTick)

            // Get battery SoC (first battery in the object)
            const batteryKeys = batteryData ? Object.keys(batteryData).filter(k => k !== "tickNumber") : []
            const batterySoC = batteryKeys.length > 0 ? (batteryData?.[batteryKeys[0]] ?? "") : ""

            // Get solar production (first solar in the object)
            const solarKeys = solarData ? Object.keys(solarData).filter(k => k !== "tickNumber") : []
            const solarProd = solarKeys.length > 0 ? (solarData?.[solarKeys[0]] ?? "") : ""

            if (!totalData) continue

            data.push({
                tickNumber: totalData?.tickNumber ?? "",
                totalProduced: totalData?.totalProduced ?? "",
                totalConsumed: totalData?.totalConsumed ?? "",
                greenEnergyRatio: greenData?.greenEnergyRatio ?? "",
                batteryStateOfCharge: batterySoC,
                solarProduction: solarProd,
                predictedLoadKw: predData?.predictedLoadKw ?? "",
                predictedPvKw: predData?.predictedPvKw ?? "",
                errorLoadKw: predData?.errorLoadKw ?? "",
                errorPvKw: predData?.errorPvKw ?? "",
            })
        }

        // Use Papa Parse to generate CSV
        const csv = Papa.unparse(data)

        // Create blob and download
        const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" })
        const link = document.createElement("a")
        const url = URL.createObjectURL(blob)
        link.setAttribute("href", url)
        link.setAttribute("download", `benchmark_${new Date().toISOString()}.csv`)
        link.style.visibility = "hidden"
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
    }, [totalProducedChartData, greenEnergyRatioChartData, batteriesChartData, solarPanelsChartData, predictionData])

    const handleBenchmarkComplete = useCallback(async () => {
        await stopSimulation()
        downloadCSV()
        toaster.create({
            title: "Benchmark completed",
            description: "CSV file has been downloaded",
            type: "success",
        })
    }, [downloadCSV, stopSimulation])

    // Monitor simulation progress
    useEffect(() => {
        if (isRunning && targetTicks > 0) {
            const progressPercent = Math.min((tickNumber / targetTicks) * 100, 100)
            setProgress(progressPercent)

            if (tickNumber > 200 && !isEventTriggered) {
                triggerEvent("batteryBreak", 10)
                console.log("battery break triggered")
                setIsEventTriggered(true)
            }

            // Stop when target is reached
            if (tickNumber >= targetTicks) {
                handleBenchmarkComplete()
            }
        }
    }, [
        tickNumber,
        targetTicks,
        isRunning,
        setProgress,
        handleBenchmarkComplete,
        isEventTriggered,
        breakPanel,
        triggerEvent,
    ])

    const onSubmit = async (data: BenchmarkFormValues) => {
        // Reset chart data before starting
        resetChartData()
        setProgress(0)
        setTargetTicks(data.durationTicks)

        const batteryConfig = {
            name: "BenchmarkBattery",
            initialSoC: data.batteryInitialSoC,
            capacity: data.batteryCapacity,
            etaCharge: data.batteryEtaCharge,
            etaDischarge: data.batteryEtaDischarge,
            cRate: data.batteryCRate,
            selfDischarge: data.batterySelfDischarge,
        }

        const solarConfig = {
            name: "BenchmarkSolar",
            noOfPanels: data.solarNoOfPanels,
            area: data.solarArea,
            efficiency: data.solarEfficiency,
            tempCoeff: data.solarTempCoeff,
            noct: data.solarNoct,
        }

        const buildingConfig = {
            name: "BenchmarkBuilding",
            nominalLoad: data.buildingNominalLoad,
        }

        addBattery({
            id: "BenchmarkBattery",
            coordinates: [0, 0],
            ...batteryConfig,
        })

        addSolar({
            id: "BenchmarkSolar",
            coordinates: [0, 0],
            ...solarConfig,
        })
        addBuilding({
            id: "BenchmarkBuilding",
            coordinates: [0, 0],
            ...buildingConfig,
        })

        // Build simulation config
        const config = {
            simulation: {
                tickIntervalMillis: tickIntervalMilliseconds,
                externalSourceCost: externalSourceCost.toFixed(1),
                externalSourceCap: externalSourceCap.toFixed(1),
                metricsPerNTicks: 2,
                weather: {
                    sunriseTick: weather.sunriseTick,
                    sunsetTick: weather.sunsetTick,
                    sunPeakTick: weather.sunPeakTick,
                    gPeak: weather.gPeak,
                    tempMeanDay: weather.tempMeanDay,
                    tempMeanNight: weather.tempMeanNight,
                    sigmaG: weather.sigmaG,
                    sigmaT: weather.sigmaT,
                },
                forecast: {
                    enablePredictive: 1,
                    H_hist: 168,
                    H_pred: 4,
                    replanEvery: 2,
                    epsilonBreak: 1000000.1,
                    useMC: forecast.useMC,
                },
                agents: [
                    {
                        type: "energyStorage",
                        ...batteryConfig,
                    },
                    {
                        type: "energySource",
                        ...solarConfig,
                    },
                    {
                        type: "load",
                        ...buildingConfig,
                    },
                ],
            },
        }

        await startSimulation(config as any)
        setIsRunning(true)
    }

    const handleStop = async () => {
        await stopSimulation()
        setIsRunning(false)
    }

    return (
        <Box maxW="1200px" mx="auto" p={6}>
            <Heading mb={6}>Benchmark Mode</Heading>
            <Text color="gray.600" mb={8}>
                Configure one battery, one solar panel, and one load to run a benchmark simulation. The results will be
                automatically downloaded as CSV when complete.
            </Text>

            {isRunning && (
                <Box bg="green.50" borderRadius="lg" mb={6} p={4}>
                    <Text fontWeight="medium" mb={2}>
                        Benchmark Running...
                    </Text>
                    <Progress.Root value={progress}>
                        <Progress.Track>
                            <Progress.Range />
                        </Progress.Track>
                    </Progress.Root>
                    <Text fontSize="sm" mt={2}>
                        Tick: {tickNumber} / {targetTicks} ({progress.toFixed(1)}%)
                    </Text>
                    <Button colorScheme="red" mt={4} size="sm" onClick={handleStop}>
                        Stop Benchmark
                    </Button>
                </Box>
            )}

            <form onSubmit={handleSubmit(onSubmit)}>
                <Stack gap={6}>
                    {/* Battery Configuration */}
                    <Fieldset.Root>
                        <Fieldset.Legend fontSize="lg" fontWeight="semibold">
                            Battery Configuration
                        </Fieldset.Legend>
                        <Fieldset.Content>
                            <Stack gap={4}>
                                <Field invalid={!!errors.batteryCapacity} label="Capacity (kWh)">
                                    <Input
                                        {...register("batteryCapacity", {
                                            required: "Capacity is required",
                                            min: { value: 0, message: "Must be positive" },
                                        })}
                                        disabled={isRunning}
                                        step="0.1"
                                        type="number"
                                    />
                                    {errors.batteryCapacity && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.batteryCapacity.message}
                                        </Text>
                                    )}
                                </Field>

                                <Field invalid={!!errors.batteryEtaCharge} label="Charge Efficiency">
                                    <Input
                                        {...register("batteryEtaCharge", {
                                            required: "Charge efficiency is required",
                                            min: { value: 0, message: "Must be between 0 and 1" },
                                            max: { value: 1, message: "Must be between 0 and 1" },
                                        })}
                                        disabled={isRunning}
                                        step="0.01"
                                        type="number"
                                    />
                                    {errors.batteryEtaCharge && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.batteryEtaCharge.message}
                                        </Text>
                                    )}
                                </Field>

                                <Field invalid={!!errors.batteryEtaDischarge} label="Discharge Efficiency">
                                    <Input
                                        {...register("batteryEtaDischarge", {
                                            required: "Discharge efficiency is required",
                                            min: { value: 0, message: "Must be between 0 and 1" },
                                            max: { value: 1, message: "Must be between 0 and 1" },
                                        })}
                                        disabled={isRunning}
                                        step="0.01"
                                        type="number"
                                    />
                                    {errors.batteryEtaDischarge && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.batteryEtaDischarge.message}
                                        </Text>
                                    )}
                                </Field>

                                <Field invalid={!!errors.batteryCRate} label="C-Rate">
                                    <Input
                                        {...register("batteryCRate", {
                                            required: "C-Rate is required",
                                            min: { value: 0, message: "Must be positive" },
                                        })}
                                        disabled={isRunning}
                                        step="0.1"
                                        type="number"
                                    />
                                    {errors.batteryCRate && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.batteryCRate.message}
                                        </Text>
                                    )}
                                </Field>

                                <Field invalid={!!errors.batterySelfDischarge} label="Self Discharge Rate">
                                    <Input
                                        {...register("batterySelfDischarge", {
                                            required: "Self discharge rate is required",
                                            min: { value: 0, message: "Must be positive" },
                                        })}
                                        disabled={isRunning}
                                        step="0.001"
                                        type="number"
                                    />
                                    {errors.batterySelfDischarge && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.batterySelfDischarge.message}
                                        </Text>
                                    )}
                                </Field>

                                <Field invalid={!!errors.batteryInitialSoC} label="Initial State of Charge (%)">
                                    <Input
                                        {...register("batteryInitialSoC", {
                                            required: "Initial SoC is required",
                                            min: { value: 0, message: "Must be between 0 and 100" },
                                            max: { value: 100, message: "Must be between 0 and 100" },
                                        })}
                                        disabled={isRunning}
                                        step="1"
                                        type="number"
                                    />
                                    {errors.batteryInitialSoC && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.batteryInitialSoC.message}
                                        </Text>
                                    )}
                                </Field>
                            </Stack>
                        </Fieldset.Content>
                    </Fieldset.Root>

                    {/* Solar Configuration */}
                    <Fieldset.Root>
                        <Fieldset.Legend fontSize="lg" fontWeight="semibold">
                            Solar Panel Configuration
                        </Fieldset.Legend>
                        <Fieldset.Content>
                            <Stack gap={4}>
                                <Field invalid={!!errors.solarNoOfPanels} label="Number of Panels">
                                    <Input
                                        {...register("solarNoOfPanels", {
                                            required: "Number of panels is required",
                                            min: { value: 1, message: "Must be at least 1" },
                                        })}
                                        disabled={isRunning}
                                        step="1"
                                        type="number"
                                    />
                                    {errors.solarNoOfPanels && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.solarNoOfPanels.message}
                                        </Text>
                                    )}
                                </Field>

                                <Field invalid={!!errors.solarArea} label="Area per Panel (m²)">
                                    <Input
                                        {...register("solarArea", {
                                            required: "Area is required",
                                            min: { value: 0, message: "Must be positive" },
                                        })}
                                        disabled={isRunning}
                                        step="0.1"
                                        type="number"
                                    />
                                    {errors.solarArea && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.solarArea.message}
                                        </Text>
                                    )}
                                </Field>

                                <Field invalid={!!errors.solarEfficiency} label="Efficiency">
                                    <Input
                                        {...register("solarEfficiency", {
                                            required: "Efficiency is required",
                                            min: { value: 0, message: "Must be between 0 and 1" },
                                            max: { value: 1, message: "Must be between 0 and 1" },
                                        })}
                                        disabled={isRunning}
                                        step="0.01"
                                        type="number"
                                    />
                                    {errors.solarEfficiency && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.solarEfficiency.message}
                                        </Text>
                                    )}
                                </Field>

                                <Field invalid={!!errors.solarTempCoeff} label="Temperature Coefficient">
                                    <Input
                                        {...register("solarTempCoeff", {
                                            required: "Temperature coefficient is required",
                                        })}
                                        disabled={isRunning}
                                        step="0.001"
                                        type="number"
                                    />
                                    {errors.solarTempCoeff && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.solarTempCoeff.message}
                                        </Text>
                                    )}
                                </Field>

                                <Field invalid={!!errors.solarNoct} label="NOCT (°C)">
                                    <Input
                                        {...register("solarNoct", {
                                            required: "NOCT is required",
                                            min: { value: 0, message: "Must be positive" },
                                        })}
                                        disabled={isRunning}
                                        step="1"
                                        type="number"
                                    />
                                    {errors.solarNoct && (
                                        <Text color="red.500" fontSize="sm">
                                            {errors.solarNoct.message}
                                        </Text>
                                    )}
                                </Field>
                            </Stack>
                        </Fieldset.Content>
                    </Fieldset.Root>

                    {/* Building/Load Configuration */}
                    <Fieldset.Root>
                        <Fieldset.Legend fontSize="lg" fontWeight="semibold">
                            Load Configuration
                        </Fieldset.Legend>
                        <Fieldset.Content>
                            <Field invalid={!!errors.buildingNominalLoad} label="Nominal Load (kW)">
                                <Input
                                    {...register("buildingNominalLoad", {
                                        required: "Nominal load is required",
                                        min: { value: 0, message: "Must be positive" },
                                    })}
                                    disabled={isRunning}
                                    step="0.1"
                                    type="number"
                                />
                                {errors.buildingNominalLoad && (
                                    <Text color="red.500" fontSize="sm">
                                        {errors.buildingNominalLoad.message}
                                    </Text>
                                )}
                            </Field>
                        </Fieldset.Content>
                    </Fieldset.Root>

                    {/* Duration Configuration */}
                    <Fieldset.Root>
                        <Fieldset.Legend fontSize="lg" fontWeight="semibold">
                            Simulation Duration
                        </Fieldset.Legend>
                        <Fieldset.Content>
                            <Field
                                helperText="1 tick = 1 minute by default. 1440 ticks = 24 hours"
                                invalid={!!errors.durationTicks}
                                label="Duration (ticks)">
                                <Input
                                    {...register("durationTicks", {
                                        required: "Duration is required",
                                        min: { value: 1, message: "Must be at least 1 tick" },
                                    })}
                                    disabled={isRunning}
                                    step="1"
                                    type="number"
                                />
                                {errors.durationTicks && (
                                    <Text color="red.500" fontSize="sm">
                                        {errors.durationTicks.message}
                                    </Text>
                                )}
                            </Field>
                        </Fieldset.Content>
                    </Fieldset.Root>

                    <Button
                        colorScheme="green"
                        disabled={isRunning || isStartPending}
                        loading={isStartPending}
                        size="lg"
                        type="submit">
                        Start Benchmark
                    </Button>
                </Stack>
            </form>
        </Box>
    )
}
