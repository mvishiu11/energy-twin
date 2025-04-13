import { Box, Code, Flex, Heading, IconButton, Text } from "@chakra-ui/react"
import { Maximize2, Minimize2, X } from "lucide-react"
import { useEffect, useRef, useState } from "react"
import { useLogs } from "../../infrastructure/fetching"
import { useColorModeValue } from "../ui/color-mode"

interface LogsWindowProps {
    initialPosition?: { x: number; y: number }
    onClose?: () => void
}

export const LogsWindow = ({ onClose }: LogsWindowProps) => {
    const { data: logs } = useLogs()
    const [isMinimized, setIsMinimized] = useState(false)
    const [position, setPosition] = useState({ x: 10, y: 10 })
    const logsContainerRef = useRef<HTMLDivElement>(null)

    const bgColor = useColorModeValue("gray.50", "gray.800")
    const headerBgColor = useColorModeValue("gray.200", "gray.700")
    const borderColor = useColorModeValue("gray.300", "gray.600")
    const textColor = useColorModeValue("gray.800", "gray.100")
    const codeBg = useColorModeValue("blackAlpha.50", "whiteAlpha.50")

    // Auto-scroll to bottom when logs change
    useEffect(() => {
        if (logsContainerRef.current && logs && logs.length > 0) {
            const container = logsContainerRef.current
            container.scrollTo({
                top: container.scrollHeight,
                behavior: "smooth",
            })
        }
    }, [logs])

    const handleMouseDown = (e: React.MouseEvent) => {
        const initialX = e.clientX - position.x
        const initialY = e.clientY - position.y

        const handleMouseMove = (e: MouseEvent) => {
            setPosition({
                x: e.clientX - initialX,
                y: e.clientY - initialY,
            })
        }

        const handleMouseUp = () => {
            document.removeEventListener("mousemove", handleMouseMove)
            document.removeEventListener("mouseup", handleMouseUp)
        }

        document.addEventListener("mousemove", handleMouseMove)
        document.addEventListener("mouseup", handleMouseUp)
    }

    return (
        <Box
            bg={bgColor}
            border="1px solid"
            borderColor={borderColor}
            borderRadius="md"
            boxShadow="lg"
            height={isMinimized ? "auto" : "500px"}
            overflow="hidden"
            position="fixed"
            style={{ left: position.x, top: position.y }}
            width={isMinimized ? "200px" : "700px"}>
            <Flex
                alignItems="center"
                bg={headerBgColor}
                cursor="move"
                justifyContent="space-between"
                p={2}
                userSelect="none"
                onMouseDown={handleMouseDown}>
                <Heading fontFamily="monospace" size="sm">
                    Simulation Logs
                </Heading>
                <Flex>
                    <IconButton
                        aria-label={isMinimized ? "Maximize" : "Minimize"}
                        mr={1}
                        size="sm"
                        variant="ghost"
                        onClick={() => setIsMinimized(!isMinimized)}>
                        {isMinimized ? <Maximize2 size={16} /> : <Minimize2 size={16} />}
                    </IconButton>
                    <IconButton aria-label="Close" size="sm" variant="ghost" onClick={onClose}>
                        <X size={16} />
                    </IconButton>
                </Flex>
            </Flex>
            {!isMinimized && (
                <Box
                    ref={logsContainerRef}
                    fontFamily="monospace"
                    fontSize="sm"
                    height="calc(100% - 40px)"
                    overflowY="auto"
                    padding="4">
                    <Code
                        bg={codeBg}
                        borderRadius="md"
                        color={textColor}
                        display="block"
                        padding="4"
                        whiteSpace="pre-wrap"
                        width="100%">
                        {logs && logs.length > 0 ? (
                            logs.map((log, index) => (
                                <Text key={index} mb={1}>
                                    {log}
                                </Text>
                            ))
                        ) : (
                            <Text color="gray.500">No logs available...</Text>
                        )}
                    </Code>
                </Box>
            )}
        </Box>
    )
}
