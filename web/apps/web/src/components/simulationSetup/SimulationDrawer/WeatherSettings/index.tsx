import {
    Accordion,
    Button,
    Dialog,
    DialogBackdrop,
    DialogPositioner,
    Field,
    Flex,
    Heading,
    Input,
    Portal,
    Text,
} from "@chakra-ui/react"
import { LuCloud, LuExternalLink } from "react-icons/lu"
import { useUpdateWeather } from "../../../../infrastructure/fetching"
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"

export function WeatherSettings() {
    const { weather, isRunning, isPaused } = useSimulationStore()
    const { mutate: updateWeather } = useUpdateWeather()

    return (
        <Accordion.Item value="weather-settings">
            <Accordion.ItemTrigger>
                <LuCloud />
                <Heading size="md">Weather Settings</Heading>
                <Accordion.ItemIndicator />
            </Accordion.ItemTrigger>
            <Accordion.ItemContent>
                <Accordion.ItemBody display="flex" flexDirection="column" gap="4">
                    {isRunning || isPaused ? (
                        <Flex alignItems="center" gap="5">
                            <Text>The simulation is running but you can still inject weather</Text>
                            <Dialog.Root placement="center">
                                <Dialog.Trigger asChild>
                                    <Button size="xs" variant="outline">
                                        <LuExternalLink />
                                    </Button>
                                </Dialog.Trigger>
                                <Portal>
                                    <DialogBackdrop />
                                    <DialogPositioner>
                                        <Dialog.Content>
                                            <Dialog.Header>
                                                <Dialog.Title>Inject Weather</Dialog.Title>
                                            </Dialog.Header>
                                            <Dialog.Body>
                                                <WeatherForm />
                                            </Dialog.Body>
                                            <Dialog.Footer>
                                                <Dialog.ActionTrigger asChild>
                                                    <Button variant="outline">Close</Button>
                                                </Dialog.ActionTrigger>
                                                <Dialog.ActionTrigger asChild>
                                                    <Button onClick={() => updateWeather({ body: weather } as any)}>
                                                        Inject
                                                        <LuCloud />
                                                    </Button>
                                                </Dialog.ActionTrigger>
                                            </Dialog.Footer>
                                        </Dialog.Content>
                                    </DialogPositioner>
                                </Portal>
                            </Dialog.Root>
                        </Flex>
                    ) : (
                        <WeatherForm />
                    )}
                </Accordion.ItemBody>
            </Accordion.ItemContent>
        </Accordion.Item>
    )
}

function WeatherForm() {
    const { weather, setWeather } = useSimulationStore()

    return (
        <>
            <Field.Root>
                <Field.Label>Day Mean Temperature</Field.Label>
                <Input
                    max={100}
                    min={-100}
                    type="number"
                    value={weather.tempMeanDay}
                    onChange={e => setWeather({ ...weather, tempMeanDay: Number(e.target.value) })}
                />
            </Field.Root>
            <Field.Root>
                <Field.Label>Night Mean Temperature</Field.Label>
                <Input
                    max={100}
                    min={-100}
                    type="number"
                    value={weather.tempMeanNight}
                    onChange={e => setWeather({ ...weather, tempMeanNight: Number(e.target.value) })}
                />
            </Field.Root>
            <Field.Root>
                <Field.Label>Sunrise Tick</Field.Label>
                <Input
                    min={1}
                    type="number"
                    value={weather.sunriseTick}
                    onChange={e => setWeather({ ...weather, sunriseTick: Number(e.target.value) })}
                />
            </Field.Root>
            <Field.Root>
                <Field.Label>Sunset Tick</Field.Label>
                <Input
                    min={1}
                    type="number"
                    value={weather.sunsetTick}
                    onChange={e => setWeather({ ...weather, sunsetTick: Number(e.target.value) })}
                />
            </Field.Root>
            <Field.Root>
                <Field.Label>Sun Peak Tick</Field.Label>
                <Input
                    min={1}
                    type="number"
                    value={weather.sunPeakTick}
                    onChange={e => setWeather({ ...weather, sunPeakTick: Number(e.target.value) })}
                />
            </Field.Root>
            <Field.Root>
                <Field.Label>G Peak</Field.Label>
                <Input
                    min={0.01}
                    type="number"
                    value={weather.gPeak}
                    onChange={e => setWeather({ ...weather, gPeak: Number(e.target.value) })}
                />
            </Field.Root>
            <Field.Root>
                <Field.Label>Sigma T</Field.Label>
                <Input
                    min={0.01}
                    type="number"
                    value={weather.sigmaT}
                    onChange={e => setWeather({ ...weather, sigmaT: Number(e.target.value) })}
                />
            </Field.Root>
            <Field.Root>
                <Field.Label>Sigma G</Field.Label>
                <Input
                    min={0.01}
                    type="number"
                    value={weather.sigmaG}
                    onChange={e => setWeather({ ...weather, sigmaG: Number(e.target.value) })}
                />
            </Field.Root>
        </>
    )
}
