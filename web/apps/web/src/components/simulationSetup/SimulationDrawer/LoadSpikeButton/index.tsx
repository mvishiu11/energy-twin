import { Button, createListCollection, Dialog, Field, Flex, Heading, Input, Portal, Select } from "@chakra-ui/react"
import { ChartNoAxesCombined } from "lucide-react"
import { useMemo, useRef, useState } from "react"
import { Controller, SubmitHandler, useForm } from "react-hook-form"
import { LuZap } from "react-icons/lu"
import { useLoadSpike } from "../../../../infrastructure/fetching"
import { useEntitiesStore } from "../../../../infrastructure/stores/entitiesStore"
import { toaster } from "../../../ui/toaster"

type LoadSpikeButtonProps = {
    disabled?: boolean
}

interface FormValues {
    buildingId: string
    ticksDuration: number
    loadSpikeRate: number
}

export function LoadSpikeButton({ disabled }: LoadSpikeButtonProps) {
    const containerRef = useRef<HTMLDivElement>(null)
    const [isOpen, setIsOpen] = useState(false)

    const {
        mapEntities: { buildings },
    } = useEntitiesStore()

    const buildingsCollection = useMemo(
        () =>
            createListCollection({ items: buildings.map(building => ({ label: building.name, value: building.id })) }),
        [buildings],
    )

    const defaultBuildingId = buildings[0]?.id || ""

    const { mutate: simulateLoadSpike, isPending: isLoadSpikePending } = useLoadSpike({
        onSuccess: () => {
            toaster.create({
                title: "Load spike simulated",
                type: "success",
            })
            setIsOpen(false)
        },
        onError: () => {
            toaster.create({
                title: "Failed to simulate load spike",
                type: "error",
            })
            setIsOpen(false)
        },
    })

    const {
        control,
        handleSubmit,
        register,
        formState: { errors },
    } = useForm<FormValues>({
        defaultValues: {
            buildingId: defaultBuildingId,
            ticksDuration: 5,
            loadSpikeRate: 2,
        },
        mode: "onChange",
    })

    const onSubmit: SubmitHandler<FormValues> = (data: FormValues) => {
        simulateLoadSpike({
            name: data.buildingId,
            rate: data.loadSpikeRate,
            ticks: data.ticksDuration,
        })
    }

    return (
        <Dialog.Root
            motionPreset="slide-in-bottom"
            open={isOpen}
            placement="center"
            onOpenChange={e => setIsOpen(e.open)}>
            <Dialog.Trigger asChild>
                <Button disabled={disabled} variant="surface">
                    Simulate Load Spike <ChartNoAxesCombined />
                </Button>
            </Dialog.Trigger>
            <Portal>
                <Dialog.Backdrop />
                <Dialog.Positioner>
                    <Dialog.Content ref={containerRef} as="form" onSubmit={handleSubmit(onSubmit)}>
                        <Dialog.Header>
                            <Dialog.Title>
                                <Heading size="lg">Simulate Load Spike</Heading>
                            </Dialog.Title>
                        </Dialog.Header>
                        <Dialog.Body>
                            <Flex direction="column" gap="4">
                                <Controller
                                    control={control}
                                    name="buildingId"
                                    render={({ field }) => (
                                        <Field.Root invalid={!!errors.buildingId}>
                                            <Field.Label>Select Building</Field.Label>
                                            <Select.Root
                                                collection={buildingsCollection}
                                                value={[field.value]}
                                                onValueChange={details => field.onChange(details.value[0])}>
                                                <Select.HiddenSelect />
                                                <Select.Control>
                                                    <Select.Trigger>
                                                        <Select.ValueText placeholder="Select Building" />
                                                    </Select.Trigger>
                                                    <Select.IndicatorGroup>
                                                        <Select.Indicator />
                                                    </Select.IndicatorGroup>
                                                </Select.Control>
                                                <Portal container={containerRef}>
                                                    <Select.Positioner>
                                                        <Select.Content>
                                                            {buildingsCollection.items.map(building => (
                                                                <Select.Item key={building.value} item={building}>
                                                                    {building.label}
                                                                    <Select.ItemIndicator />
                                                                </Select.Item>
                                                            ))}
                                                        </Select.Content>
                                                    </Select.Positioner>
                                                </Portal>
                                            </Select.Root>
                                            <Field.ErrorText>{errors.buildingId?.message}</Field.ErrorText>
                                        </Field.Root>
                                    )}
                                    rules={{ required: "Building selection is required" }}
                                />
                                <Field.Root invalid={!!errors.ticksDuration}>
                                    <Field.Label>Ticks duration</Field.Label>
                                    <Input
                                        {...register("ticksDuration", {
                                            min: {
                                                message: "Ticks duration must be at least 1",
                                                value: 1,
                                            },
                                            required: "Ticks duration is required",
                                        })}
                                        min={1}
                                        type="number"
                                    />
                                    <Field.ErrorText>{errors.ticksDuration?.message}</Field.ErrorText>
                                </Field.Root>
                                <Field.Root invalid={!!errors.loadSpikeRate}>
                                    <Field.Label>Load spike rate</Field.Label>
                                    <Input
                                        {...register("loadSpikeRate", {
                                            min: {
                                                message: "Load spike rate must be greater than 0",
                                                value: 0.1,
                                            },
                                            required: "Load spike rate is required",
                                        })}
                                        min="0.1"
                                        step="0.1"
                                        type="number"
                                    />
                                    <Field.ErrorText>{errors.loadSpikeRate?.message}</Field.ErrorText>
                                </Field.Root>
                            </Flex>
                        </Dialog.Body>
                        <Dialog.Footer>
                            <Dialog.ActionTrigger asChild>
                                <Button type="button" variant="outline">
                                    Close
                                </Button>
                            </Dialog.ActionTrigger>
                            <Button loading={isLoadSpikePending} loadingText="Simulating load spike" type="submit">
                                Simulate Load Spike
                                <LuZap />
                            </Button>
                        </Dialog.Footer>
                    </Dialog.Content>
                </Dialog.Positioner>
            </Portal>
        </Dialog.Root>
    )
}
