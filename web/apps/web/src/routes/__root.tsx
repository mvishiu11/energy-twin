import { createRootRoute, Outlet } from "@tanstack/react-router"

export const Route = createRootRoute({
    component: RootComponent,
})

function RootComponent() {
    return (
        <>
            <div>Hello "__root"!</div>
            <Outlet />
        </>
    )
}
