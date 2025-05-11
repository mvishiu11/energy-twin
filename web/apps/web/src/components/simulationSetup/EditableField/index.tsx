import { Editable, Field, IconButton } from "@chakra-ui/react"
import { LuPencilLine } from "react-icons/lu"

type EditableFieldProps = {
    label: string
    value: string
    disabled?: boolean
    onChange: (value: string) => void
}

export function EditableField({ label, value, onChange, disabled }: EditableFieldProps) {
    return (
        <Field.Root orientation="horizontal">
            <Field.Label>{label}:</Field.Label>
            <Editable.Root disabled={disabled} value={value} onValueChange={e => onChange(e.value)}>
                <Editable.Preview />
                <Editable.Input />
                <Editable.Control>
                    <Editable.EditTrigger asChild>
                        <IconButton size="xs" variant="ghost">
                            <LuPencilLine />
                        </IconButton>
                    </Editable.EditTrigger>
                </Editable.Control>
            </Editable.Root>
        </Field.Root>
    )
}
