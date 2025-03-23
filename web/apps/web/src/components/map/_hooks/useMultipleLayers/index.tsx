import { useState } from "react"
import { Layer, Source } from "react-map-gl/mapbox"
import { FeatureCollection } from "geojson"
import { CircleLayerSpecification, MapMouseEvent, PaintSpecification } from "mapbox-gl"
import { v4 as uuid } from "uuid"
import { DeepOmit } from "../../../../utils/types/DeepOmit"

type GeoJsonTemplate = DeepOmit<FeatureCollection, "coordinates">

type UseMultipleLayersProps = {
    geojsonTemplate: GeoJsonTemplate
    paintTemplate: PaintSpecification
    initialCoordinates: [number, number][]
}

export function useMultipleLayers({ geojsonTemplate, initialCoordinates, paintTemplate }: UseMultipleLayersProps) {
    const [interactiveLayerIds] = useState<string[]>(initialCoordinates.map(() => uuid()))
    const [draggedLayerId, setDraggedLayerId] = useState<string | null>(null)
    const [sources, setSources] = useState<{ geojson: FeatureCollection; layer: CircleLayerSpecification }[]>(
        initialCoordinates.map((coords, index) => ({
            geojson: {
                ...geojsonTemplate,
                features: geojsonTemplate.features.map(feature => ({
                    ...feature,
                    geometry: {
                        type: "Point",
                        coordinates: coords,
                    },
                })),
            },
            layer: {
                id: interactiveLayerIds[index],
                type: "circle",
                source: "points",
                paint: paintTemplate,
            },
        })),
    )

    const updatePosition = (index: number, coordinates: [number, number]) => {
        setSources(prevSources => {
            const newSources = [...prevSources]
            newSources[index] = {
                ...newSources[index],
                geojson: {
                    ...newSources[index].geojson,
                    features: newSources[index].geojson.features.map(feature => ({
                        ...feature,
                        geometry: {
                            type: "Point",
                            coordinates,
                        },
                    })),
                },
            }
            return newSources
        })
    }

    const updatePaint = (index: number, paint: CircleLayerSpecification["paint"]) => {
        setSources(prevSources => {
            const newSources = [...prevSources]
            newSources[index] = {
                ...newSources[index],
                layer: {
                    ...newSources[index].layer,
                    paint,
                },
            }
            return newSources
        })
    }

    const resetPaint = (index: number) => {
        setSources(prevSources => {
            const newSources = [...prevSources]
            newSources[index] = {
                ...newSources[index],
                layer: {
                    ...newSources[index].layer,
                    paint: paintTemplate,
                },
            }
            return newSources
        })
    }

    const onMouseEnter = (e: MapMouseEvent) => {
        const index = interactiveLayerIds.findIndex(id => e.features?.find(feature => feature.layer?.id === id))
        if (index !== -1) {
            updatePaint(index, { "circle-color": "#6abcff", "circle-radius": 11 })
        }
    }

    const onMouseLeave = () => {
        interactiveLayerIds.forEach((_, index) => {
            resetPaint(index)
        })
    }

    const onMouseDown = (e: MapMouseEvent) => {
        e.preventDefault()
        const index = interactiveLayerIds.findIndex(id => e.features?.find(feature => feature.layer?.id === id))
        if (index !== -1) {
            setDraggedLayerId(interactiveLayerIds[index])
        }
    }

    const onMouseMove = (e: MapMouseEvent) => {
        e.preventDefault()
        if (draggedLayerId) {
            const index = interactiveLayerIds.findIndex(id => id === draggedLayerId)
            if (index !== -1) {
                updatePosition(index, [e.lngLat.lng, e.lngLat.lat])
            }
        }
    }

    const onMouseUp = () => {
        setDraggedLayerId(null)
    }

    return {
        sources: sources.map(({ geojson, layer }, index) => (
            <Source key={layer.id} data={geojson} id={interactiveLayerIds[index]} type="geojson">
                <Layer {...layer} />
            </Source>
        )),
        mapProps: {
            onMouseEnter,
            onMouseLeave,
            interactiveLayerIds,
            onMouseDown,
            onMouseUp,
            onMouseMove,
        },
    }
}
