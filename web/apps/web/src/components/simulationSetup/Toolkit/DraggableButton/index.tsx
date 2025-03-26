import { Button } from "@chakra-ui/react"
import { PropsWithChildren } from "react"
import { useDraggable } from "@dnd-kit/core"

type DraggableButtonsProps = {
    id: string
}

export function DraggableButton({ children, id }: PropsWithChildren<DraggableButtonsProps>) {
    const { attributes, listeners, setNodeRef } = useDraggable({
        id,
    })

    return (
        <Button ref={setNodeRef} {...listeners} {...attributes} size="lg">
            {children}
        </Button>
    )
}
