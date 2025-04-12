import { IconButton } from "@chakra-ui/react"
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
        <IconButton ref={setNodeRef} rounded="xl" {...listeners} {...attributes} size="xl">
            {children}
        </IconButton>
    )
}
