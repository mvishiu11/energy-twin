import { useState } from "react"
import { FeatureCollection } from "geojson"
import { LayerSpecification, PaintSpecification } from "mapbox-gl"

type UseLayerPointProps<T> = {
    initialGeojson: FeatureCollection
    initialLayerPoint: T
}

export function useLayerPoint<T extends LayerSpecification>({
    initialGeojson,
    initialLayerPoint,
}: UseLayerPointProps<T>) {
    const [geojson, setGeojson] = useState<FeatureCollection>(initialGeojson)
    const [layerFeatures, setLayerFeatures] = useState<T>(initialLayerPoint)

    const updatePosition = (coordinates: [number, number]) => {
        setGeojson(prevGeojson => ({
            ...prevGeojson,
            features: prevGeojson.features.map(feature => ({
                ...feature,
                geometry: {
                    type: "Point",
                    coordinates,
                },
            })),
        }))
    }

    const updatePaint = (paint: PaintSpecification) => {
        setLayerFeatures(prevLayerFeatures => ({
            ...prevLayerFeatures,
            paint,
        }))
    }

    const resetPaint = () => {
        setLayerFeatures(initialLayerPoint)
    }

    return {
        geojson,
        layerFeatures,
        updatePosition,
        updatePaint,
        featureId: initialLayerPoint.id,
        resetPaint,
    }
}
