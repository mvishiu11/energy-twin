import { Field as ChakraField } from "@chakra-ui/react"
import { forwardRef } from "react"

export interface FieldProps extends ChakraField.RootProps {
    label?: React.ReactNode
    helperText?: React.ReactNode
    errorText?: React.ReactNode
    optionalText?: React.ReactNode
}

export const Field = forwardRef<HTMLDivElement, FieldProps>(function Field(props, ref) {
    const { label, children, helperText, errorText, optionalText, ...rootProps } = props
    return (
        <ChakraField.Root ref={ref} {...rootProps}>
            {label && (
                <ChakraField.Label>
                    {label}
                    <ChakraField.RequiredIndicator fallback={optionalText} />
                </ChakraField.Label>
            )}
            {children}
            {helperText && <ChakraField.HelperText>{helperText}</ChakraField.HelperText>}
            {errorText && <ChakraField.ErrorText>{errorText}</ChakraField.ErrorText>}
        </ChakraField.Root>
    )
})

