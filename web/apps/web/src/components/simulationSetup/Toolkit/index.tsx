import { Card, Flex, IconButton } from "@chakra-ui/react"
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
            setCardHeight(cardRef.current.clientHeight + 8)
        }
    }, [])

    return (
        <motion.div animate={{ translateY: open ? cardHeight : 0 }} transition={{ duration: 0.3 }}>
            <Flex
                alignItems="center"
                bottom="2"
                direction="column"
                gap="2"
                left="0"
                position="fixed"
                right="0"
                rounded="xl"
                zIndex="1">
                <IconButton rounded="xl" size="lg" variant="subtle" width="60px" onClick={() => setOpen(prev => !prev)}>
                    <ArrowContainer $isOpened={open}>
                        <LuChevronUp />
                    </ArrowContainer>
                </IconButton>
                <Card.Root ref={cardRef} rounded="xl" variant="elevated">
                    <Flex direction="row" gap="4" p="4">
                        <DraggableButton id={dndIds.battery}>{idToIconMap.battery}</DraggableButton>
                        <DraggableButton id={dndIds.solar}>{idToIconMap.solar}</DraggableButton>
                    </Flex>
                </Card.Root>
            </Flex>
        </motion.div>
    )
}
