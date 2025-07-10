import { Button, createListCollection, Dialog, Field, Flex, Heading, Input, Portal, Select } from "@chakra-ui/react"
import { useMemo, useRef, useState } from "react"
import { Controller, SubmitHandler, useForm } from "react-hook-form"
import { LuWrench } from "react-icons/lu"
import { useBreakPanel } from "../../../../infrastructure/fetching"
import { useSimulationStore } from "../../../../infrastructure/stores/simulationStore"
import { toaster } from "../../../ui/toaster"

type BreakPanelButtonProps = {
    disabled: boolean
}

interface FormValues {
    solarId: string
    ticksDuration: number
}

export function BreakPanelButton({ disabled }: BreakPanelButtonProps) {
    const containerRef = useRef<HTMLDivElement>(null)
    const [isOpen, setIsOpen] = useState(false)

    const {
        mapEntities: { solar },
    } = useSimulationStore()

    const solarCollection = useMemo(
        () =>
            createListCollection({
                items: solar.map(solar => ({ label: solar.name, value: solar.id })),
            }),
        [solar],
    )

    const defaultSolarId = solar.at(0)?.id || ""

    const { mutate: breakPanel, isPending: isBreakPanelPending } = useBreakPanel({
        onSuccess: () => {
            toaster.create({
                title: "Break panel simulated",
                type: "success",
            })
            setIsOpen(false)
        },
        onError: () => {
            toaster.create({
                title: "Failed to simulate break panel",
                type: "error",
            })
            setIsOpen(false)
        },
    })

    const {
        control,
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormValues>({
        defaultValues: {
            solarId: defaultSolarId,
            ticksDuration: 5,
        },
        mode: "onChange",
    })

    const onSubmit: SubmitHandler<FormValues> = (data: FormValues) => {
        breakPanel({
            name: data.solarId,
            ticks: data.ticksDuration,
        })
    }

    return (
        <Dialog.Root open={isOpen} onOpenChange={e => setIsOpen(e.open)}>
            <Dialog.Trigger asChild>
                <Button disabled={disabled} variant="surface">
                    Break Panel
                    <LuWrench />
                </Button>
            </Dialog.Trigger>
            <Portal>
                <Dialog.Backdrop />
                <Dialog.Positioner>
                    <Dialog.Content ref={containerRef} as="form" onSubmit={handleSubmit(onSubmit)}>
                        <Dialog.Header>
                            <Dialog.Title>
                                <Heading size="lg">Break Panel</Heading>
                            </Dialog.Title>
                        </Dialog.Header>
                        <Dialog.Body>
                            <Flex direction="column" gap="4">
                                <Controller
                                    control={control}
                                    name="solarId"
                                    render={({ field }) => (
                                        <Field.Root invalid={!!errors.solarId}>
                                            <Field.Label>Select Solar Panel</Field.Label>
                                            <Select.Root
                                                collection={solarCollection}
                                                value={[field.value]}
                                                onValueChange={details => field.onChange(details.value[0])}>
                                                <Select.HiddenSelect />
                                                <Select.Control>
                                                    <Select.Trigger>
                                                        <Select.ValueText placeholder="Select Solar Panel" />
                                                    </Select.Trigger>
                                                    <Select.IndicatorGroup>
                                                        <Select.Indicator />
                                                    </Select.IndicatorGroup>
                                                </Select.Control>
                                                <Portal container={containerRef}>
                                                    <Select.Positioner>
                                                        <Select.Content>
                                                            {solarCollection.items.map(solar => (
                                                                <Select.Item key={solar.value} item={solar}>
                                                                    {solar.label}
                                                                    <Select.ItemIndicator />
                                                                </Select.Item>
                                                            ))}
                                                        </Select.Content>
                                                    </Select.Positioner>
                                                </Portal>
                                            </Select.Root>
                                            <Field.ErrorText>{errors.solarId?.message}</Field.ErrorText>
                                        </Field.Root>
                                    )}
                                    rules={{ required: "Solar panel selection is required" }}
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
                            </Flex>
                        </Dialog.Body>
                        <Dialog.Footer>
                            <Dialog.ActionTrigger asChild>
                                <Button type="button" variant="outline">
                                    Close
                                </Button>
                            </Dialog.ActionTrigger>
                            <Button loading={isBreakPanelPending} loadingText="Breaking panel" type="submit">
                                Break Panel
                                <LuWrench />
                            </Button>
                        </Dialog.Footer>
                    </Dialog.Content>
                </Dialog.Positioner>
            </Portal>
        </Dialog.Root>
    )
}
