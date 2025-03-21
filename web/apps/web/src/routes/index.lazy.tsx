import { createLazyFileRoute } from "@tanstack/react-router"
import { useRef, useState } from "react"
import Map, { Layer, MapRef, Source } from "react-map-gl/mapbox"
import { useLayerPoint } from "../components/map/_hooks/useLayerPoint"

export const Route = createLazyFileRoute("/")({
    component: RouteComponent,
})

function RouteComponent() {
    const mapRef = useRef<MapRef>(null)
    const [isDragging, setIsDragging] = useState<boolean>(false)
    const { updatePosition, layerFeatures, updatePaint, featureId, geojson, resetPaint } = useLayerPoint({
        initialGeojson: {
            type: "FeatureCollection",
            features: [
                {
                    type: "Feature",
                    geometry: {
                        type: "Point",
                        coordinates: [21.011672545859113, 52.22010375163748],
                    },
                    properties: { title: "Plac Politechniki" },
                },
            ],
        },
        initialLayerPoint: {
            id: "point",
            type: "circle",
            source: "points",
            paint: { "circle-radius": 10, "circle-color": "#007cbf" },
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
            interactiveLayerIds={["point"]}
            mapboxAccessToken={import.meta.env.VITE_MAPBOX_ACCESS_TOKEN}
            mapStyle="mapbox://styles/mapbox/streets-v12"
            style={{ width: "100vw", height: "100vh" }}
            onMouseDown={e => {
                e.preventDefault()
                const feature = e.features?.find(feature => feature.layer?.id === featureId)
                if (feature) {
                    setIsDragging(true)
                }
            }}
            onMouseEnter={e => {
                if (e.features?.find(feature => feature.layer?.id === featureId)) {
                    updatePaint({ "circle-color": "#6abcff", "circle-radius": 11 })
                }
            }}
            onMouseLeave={() => {
                resetPaint()
            }}
            onMouseMove={e => {
                if (!isDragging) return
                e.preventDefault()
                const coords = e.lngLat
                updatePosition([coords.lng, coords.lat])
            }}
            onMouseUp={() => {
                setIsDragging(false)
                resetPaint()
            }}>
            <Source data={geojson} id="points" type="geojson">
                <Layer {...layerFeatures} />
            </Source>
        </Map>
    )
}
