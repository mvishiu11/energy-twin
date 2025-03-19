import { createLazyFileRoute } from "@tanstack/react-router"

export const Route = createLazyFileRoute("/")({
    component: RouteComponent,
})

function RouteComponent() {
    return (
        <div>
            <h1>Home</h1>
        </div>
    )
}
