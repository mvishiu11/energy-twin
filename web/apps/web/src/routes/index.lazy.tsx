import { Badge, Icon } from "@chakra-ui/react"
import { createLazyFileRoute } from "@tanstack/react-router"
import { useRef } from "react"
import { LuDatabaseZap } from "react-icons/lu"
import Map, { MapRef } from "react-map-gl/mapbox"
import { useMultipleMarkers } from "../components/map/_hooks/useMultipleMarkers"

export const Route = createLazyFileRoute("/")({
    component: RouteComponent,
})

function RouteComponent() {
    const mapRef = useRef<MapRef>(null)

    const { markers } = useMultipleMarkers({
        component: (
            <Icon color="green.500" size="2xl">
                <LuDatabaseZap strokeWidth={3} />
            </Icon>
        ),
        initialCoordinates: [
            [21.01167245859113, 52.22010375163748],
            [21.01167254585911, 52.22010375163748],
            [21.01167254585911, 52.22010375163748],
        ],
    })

    return (
        <>
            <Map
                ref={mapRef}
                initialViewState={{
                    longitude: 21.011672545859113,
                    latitude: 52.22010375163748,
                    zoom: 18,
                }}
                mapboxAccessToken={import.meta.env.VITE_MAPBOX_ACCESS_TOKEN}
                mapStyle="mapbox://styles/mapbox/light-v11"
                style={{ width: "100vw", height: "100vh" }}>
                {markers}
            </Map>
            <Badge colorScheme="blue">Hello</Badge>
        </>
    )
}
