import { ReactNode, useCallback, useState } from "react"
import { Marker } from "react-map-gl/mapbox"

type UseMultipleLayersProps = {
    initialCoordinates?: [number, number][]
    component: ReactNode
}

export function useMultipleMarkers({ initialCoordinates, component }: UseMultipleLayersProps) {
    const [markersPositions, setMarkersPositions] = useState<[number, number][]>(initialCoordinates ?? [])

    const addMarker = useCallback((position: [number, number]) => {
        setMarkersPositions(prevMarkers => [...prevMarkers, position])
    }, [])

    return {
        markers: markersPositions.map((coordinates, index) => (
            <Marker
                key={index}
                draggable
                latitude={coordinates[1]}
                longitude={coordinates[0]}
                onDragEnd={e => {
                    setMarkersPositions(prevMarkers => {
                        const { lng, lat } = e.target.getLngLat()
                        const newMarkers = [...prevMarkers]
                        newMarkers[index] = [lng, lat]
                        return newMarkers
                    })
                }}>
                {component}
            </Marker>
        )),
        markersPositions,
        addMarker,
    }
}

export const mergeMarkers = (...markers: ReactNode[]) => {
    return markers
}
