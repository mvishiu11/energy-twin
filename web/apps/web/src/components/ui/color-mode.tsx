"use client"

import type { IconButtonProps, SpanProps } from "@chakra-ui/react"
import { ClientOnly, IconButton, Skeleton, Span } from "@chakra-ui/react"
import * as React from "react"
import { LuMoon, LuSun } from "react-icons/lu"
import { ThemeProvider, useTheme } from "next-themes"
import type { ThemeProviderProps } from "next-themes"

export type ColorModeProviderProps = ThemeProviderProps

export function ColorModeProvider(props: ColorModeProviderProps) {
    return <ThemeProvider disableTransitionOnChange attribute="class" {...props} />
}

export type ColorMode = "dark" | "light"

export interface UseColorModeReturn {
    colorMode: ColorMode
    setColorMode: (colorMode: ColorMode) => void
    toggleColorMode: () => void
}

export function useColorMode(): UseColorModeReturn {
    const { resolvedTheme, setTheme } = useTheme()
    const toggleColorMode = () => {
        setTheme(resolvedTheme === "dark" ? "light" : "dark")
    }
    return {
        colorMode: resolvedTheme as ColorMode,
        setColorMode: setTheme,
        toggleColorMode,
    }
}

export function useColorModeValue<T>(light: T, dark: T) {
    const { colorMode } = useColorMode()
    return colorMode === "dark" ? dark : light
}

export function ColorModeIcon() {
    const { colorMode } = useColorMode()
    return colorMode === "dark" ? <LuMoon /> : <LuSun />
}

type ColorModeButtonProps = Omit<IconButtonProps, "aria-label">

export const ColorModeButton = React.forwardRef<HTMLButtonElement, ColorModeButtonProps>(
    function ColorModeButton(props, ref) {
        const { toggleColorMode } = useColorMode()
        return (
            <ClientOnly fallback={<Skeleton boxSize="8" />}>
                <IconButton
                    ref={ref}
                    aria-label="Toggle color mode"
                    size="sm"
                    variant="ghost"
                    onClick={toggleColorMode}
                    {...props}
                    css={{
                        _icon: {
                            width: "5",
                            height: "5",
                        },
                    }}>
                    <ColorModeIcon />
                </IconButton>
            </ClientOnly>
        )
    },
)

export const LightMode = React.forwardRef<HTMLSpanElement, SpanProps>(function LightMode(props, ref) {
    return (
        // @ts-expect-error - chackra-ui auto generated code. Ignoring it for now. Might fix it later.
        <Span
            ref={ref}
            className="chakra-theme light"
            color="fg"
            colorPalette="gray"
            colorScheme="light"
            display="contents"
            {...props}
        />
    )
})

export const DarkMode = React.forwardRef<HTMLSpanElement, SpanProps>(function DarkMode(props, ref) {
    return (
        // @ts-expect-error - chackra-ui auto generated code. Ignoring it for now. Might fix it later.
        <Span
            ref={ref}
            className="chakra-theme dark"
            color="fg"
            colorPalette="gray"
            colorScheme="dark"
            display="contents"
            {...props}
        />
    )
})
