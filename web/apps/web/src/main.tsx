import { createRouter, RouterProvider } from "@tanstack/react-router"
import React from "react"
import ReactDOM from "react-dom/client"
import { Provider } from "./components/ui/provider"
import { Toaster } from "./components/ui/toaster"
import { QueryProvider } from "./infrastructure/fetching/QueryClientProvider"
import { WebSocketProvider } from "./infrastructure/websocket/WebSocketProvider"
import { routeTree } from "./routeTree.gen"
import "mapbox-gl/dist/mapbox-gl.css"

const router = createRouter({ routeTree })

declare module "@tanstack/react-router" {
    interface Register {
        router: typeof router
    }
}

const rootElement = document.getElementById("root")
if (rootElement && !rootElement.innerHTML) {
    const root = ReactDOM.createRoot(rootElement)
    root.render(
        <React.StrictMode>
            <Provider defaultTheme="light">
                <QueryProvider>
                    <WebSocketProvider>
                        <RouterProvider router={router} />
                        <Toaster />
                    </WebSocketProvider>
                </QueryProvider>
            </Provider>
        </React.StrictMode>,
    )
}
