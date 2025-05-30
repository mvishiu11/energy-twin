import { Box, Code, Flex, Heading, IconButton, Text } from "@chakra-ui/react"
import { Maximize2, Minimize2, X } from "lucide-react"
import { useRef, useState } from "react"
import { useLogs } from "../../infrastructure/fetching"
import { useColorModeValue } from "../ui/color-mode"

interface LogsWindowProps {
    initialPosition?: { x: number; y: number }
    onClose?: () => void
}

export const LogsWindow = ({ onClose }: LogsWindowProps) => {
    const { data: logs } = useLogs()
    const [isMinimized, setIsMinimized] = useState(true)
    const [position, setPosition] = useState({ x: 10, y: 10 })
    const logsContainerRef = useRef<HTMLDivElement>(null)

    const bgColor = useColorModeValue("gray.50", "gray.800")
    const headerBgColor = useColorModeValue("gray.200", "gray.700")
    const borderColor = useColorModeValue("gray.300", "gray.600")
    const textColor = useColorModeValue("gray.800", "gray.100")
    const codeBg = useColorModeValue("blackAlpha.50", "whiteAlpha.50")

    // useEffect(() => {
    //     if (logsContainerRef.current && logs) {
    //         const container = logsContainerRef.current
    //         container.scrollTo({
    //             top: container.scrollHeight,
    //             behavior: "smooth",
    //         })
    //     }
    // }, [logs])

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
            backgroundColor={bgColor}
            border="1px solid"
            borderColor={borderColor}
            borderRadius="md"
            boxShadow="lg"
            height={isMinimized ? "auto" : "500px"}
            overflow="hidden"
            position="fixed"
            style={{ left: position.x, top: position.y }}
            width={isMinimized ? "200px" : "1000px"}>
            <Flex
                alignItems="center"
                backgroundColor={headerBgColor}
                cursor="move"
                justifyContent="space-between"
                padding={2}
                userSelect="none"
                onMouseDown={handleMouseDown}>
                <Heading fontFamily="monospace" size="sm">
                    Simulation Logs
                </Heading>
                <Flex>
                    <IconButton marginRight={1} size="sm" variant="ghost" onClick={() => setIsMinimized(!isMinimized)}>
                        {isMinimized ? <Maximize2 size={16} /> : <Minimize2 size={16} />}
                    </IconButton>
                    <IconButton size="sm" variant="ghost" onClick={onClose}>
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
                        backgroundColor={codeBg}
                        borderRadius="md"
                        color={textColor}
                        display="block"
                        padding="4"
                        whiteSpace="pre-wrap"
                        width="100%">
                        {logs ? (
                            <Text color="gray.500">{Object.keys(logs).map(key => logs[key])}</Text>
                        ) : (
                            <Text color="gray.500">No logs available...</Text>
                        )}
                    </Code>
                </Box>
            )}
        </Box>
    )
}
