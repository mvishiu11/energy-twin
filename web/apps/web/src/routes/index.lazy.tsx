import { createLazyFileRoute } from "@tanstack/react-router"
import styled from "@emotion/styled"

export const Route = createLazyFileRoute("/")({
    component: RouteComponent,
})

function RouteComponent() {
    return (
        <StyledDiv>
            <h1>Home</h1>
        </StyledDiv>
    )
}

const StyledDiv = styled.div`
    color: red;
`
