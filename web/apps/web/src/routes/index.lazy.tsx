import { createLazyFileRoute } from "@tanstack/react-router"
import { useRef, useState } from "react"
import Map, { Layer, LayerProps, MapRef, Source } from "react-map-gl/mapbox"
import { FeatureCollection, Position } from "geojson"

export const Route = createLazyFileRoute("/")({
    component: RouteComponent,
})

function RouteComponent() {
    const [point, setPoint] = useState<Position>([21.011672545859113, 52.22010375163748])
    const [color, setColor] = useState<string>("#007cbf")
    const mapRef = useRef<MapRef>(null)

    const geojson: FeatureCollection = {
        type: "FeatureCollection",
        features: [
            {
                type: "Feature",
                geometry: {
                    type: "Point",
                    coordinates: point,
                },
                properties: { title: "Plac Politechniki" },
            },
        ],
    }

    const layerPoint: LayerProps = {
        id: "point",
        type: "circle",
        source: "points",
        paint: { "circle-radius": 10, "circle-color": color },
    }

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
            onMouseEnter={e => {
                console.log(e)
                setColor("#fff")
            }}
            onMouseLeave={e => {
                console.log(e)
                setColor("#007cbf")
            }}>
            <Source data={geojson} id="points" type="geojson">
                <Layer {...layerPoint} />
            </Source>
        </Map>
    )
}
