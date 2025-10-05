import { Accordion, Field, Heading, Input } from "@chakra-ui/react"
import { LuTrendingUp } from "react-icons/lu"
import { useForecastStore } from "../../../../infrastructure/stores/forecastStore"
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"

export function ForecastSettings() {
    const { forecast, setForecast } = useForecastStore()
    const { isRunning, isPaused } = useSimulationStore()

    return (
        <Accordion.Item value="forecast-parameters">
            <Accordion.ItemTrigger>
                <LuTrendingUp />
                <Heading size="md">Forecast Settings</Heading>
                <Accordion.ItemIndicator />
            </Accordion.ItemTrigger>
            <Accordion.ItemContent>
                <Accordion.ItemBody display="flex" flexDirection="column" gap="4">
                    <Field.Root>
                        <Field.Label>H_hist (Historical Hours)</Field.Label>
                        <Input
                            disabled={isRunning || isPaused}
                            type="number"
                            value={forecast.H_hist}
                            onChange={e => setForecast({ ...forecast, H_hist: Number(e.target.value) })}
                        />
                    </Field.Root>
                    <Field.Root>
                        <Field.Label>H_pred (Prediction Hours)</Field.Label>
                        <Input
                            disabled={isRunning || isPaused}
                            type="number"
                            value={forecast.H_pred}
                            onChange={e => setForecast({ ...forecast, H_pred: Number(e.target.value) })}
                        />
                    </Field.Root>
                    <Field.Root>
                        <Field.Label>Replan Every</Field.Label>
                        <Input
                            disabled={isRunning || isPaused}
                            type="number"
                            value={forecast.replanEvery}
                            onChange={e => setForecast({ ...forecast, replanEvery: Number(e.target.value) })}
                        />
                    </Field.Root>
                    <Field.Root>
                        <Field.Label>Epsilon Break</Field.Label>
                        <Input
                            disabled={isRunning || isPaused}
                            step="0.1"
                            type="number"
                            value={forecast.epsilonBreak}
                            onChange={e => setForecast({ ...forecast, epsilonBreak: Number(e.target.value) })}
                        />
                    </Field.Root>
                    <Field.Root>
                        <Field.Label>Use MC</Field.Label>
                        <Input
                            disabled={isRunning || isPaused}
                            max="1"
                            min="0"
                            type="number"
                            value={forecast.useMC}
                            onChange={e => setForecast({ ...forecast, useMC: Number(e.target.value) })}
                        />
                    </Field.Root>
                </Accordion.ItemBody>
            </Accordion.ItemContent>
        </Accordion.Item>
    )
}
