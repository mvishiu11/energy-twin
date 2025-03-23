import { Button, Card, Flex } from "@chakra-ui/react"
import { motion } from "motion/react"
import { useLayoutEffect, useRef, useState } from "react"
import { LuChevronUp } from "react-icons/lu"
import { dndIds, idToIconMap } from "./dndIds"
import { DraggableButton } from "./DraggableButton"
import { ArrowContainer } from "./styles"

export function Toolkit() {
    const [open, setOpen] = useState(false)
    const cardRef = useRef<HTMLDivElement>(null)
    const [cardHeight, setCardHeight] = useState(0)

    useLayoutEffect(() => {
        if (cardRef.current) {
            setCardHeight(cardRef.current.clientHeight)
        }
    }, [])

    return (
        <motion.div animate={{ translateY: open ? cardHeight : 0 }} transition={{ duration: 0.3 }}>
            <Flex alignItems="center" bottom="0" direction="column" left="0" position="fixed" right="0" zIndex="1">
                <Button
                    borderBottom="none"
                    color="green.500"
                    roundedBottom="none"
                    size="sm"
                    variant="subtle"
                    width="60px"
                    onClick={() => setOpen(prev => !prev)}>
                    <ArrowContainer $isOpened={open}>
                        <LuChevronUp />
                    </ArrowContainer>
                </Button>
                <Card.Root ref={cardRef} roundedBottom="none">
                    <Flex direction="row" gap="4" p="4">
                        <DraggableButton id={dndIds.battery}>{idToIconMap.battery}</DraggableButton>
                        <DraggableButton id={dndIds.solar}>{idToIconMap.solar}</DraggableButton>
                    </Flex>
                </Card.Root>
            </Flex>
        </motion.div>
    )
}
