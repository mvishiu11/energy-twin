import { Button, Checkbox } from "@chakra-ui/react"
import { createLazyFileRoute } from "@tanstack/react-router"
import styled from "@emotion/styled"

export const Route = createLazyFileRoute("/")({
    component: RouteComponent,
})

function RouteComponent() {
    return (
        <StyledDiv>
            <h1>Home</h1>
            <Button>Button</Button>
            <Checkbox.Root>
                <Checkbox.HiddenInput />
                <Checkbox.Label>Checkbox</Checkbox.Label>
                <Checkbox.Control />
            </Checkbox.Root>
        </StyledDiv>
    )
}

const StyledDiv = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
    margin: 1rem;
`
