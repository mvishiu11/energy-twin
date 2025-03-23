import { createLazyFileRoute } from "@tanstack/react-router"
import { useRef } from "react"
import Map, { MapRef } from "react-map-gl/mapbox"
import { useMultipleLayers } from "../components/map/_hooks/useMultipleLayers"

export const Route = createLazyFileRoute("/")({
    component: RouteComponent,
})

function RouteComponent() {
    const mapRef = useRef<MapRef>(null)

    const { sources, mapProps } = useMultipleLayers({
        geojsonTemplate: {
            type: "FeatureCollection",
            features: [
                {
                    type: "Feature",
                    properties: {},
                    geometry: {
                        type: "Point",
                    },
                },
            ],
        },
        initialCoordinates: [
            [21.01167245859113, 52.22010375163748],
            [21.01167254585911, 52.22010375163748],
            [21.01167254585911, 52.22010375163748],
        ],
        paintTemplate: {
            "circle-radius": 10,
            "circle-color": "#007cbf",
        },
    })

    return (
        <Map
            ref={mapRef}
            initialViewState={{
                longitude: 21.011672545859113,
                latitude: 52.22010375163748,
                zoom: 18,
            }}
            mapboxAccessToken={import.meta.env.VITE_MAPBOX_ACCESS_TOKEN}
            mapStyle="mapbox://styles/mapbox/streets-v12"
            style={{ width: "100vw", height: "100vh" }}
            {...mapProps}>
            {sources}
        </Map>
    )
}
