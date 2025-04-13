import { Icon, IconButton } from "@chakra-ui/react"
import { createLazyFileRoute } from "@tanstack/react-router"
import { useCallback, useMemo, useRef, useState } from "react"
import { LuDatabaseZap, LuSettings2, LuSun } from "react-icons/lu"
import Map, { Layer, MapRef, Source } from "react-map-gl/mapbox"
import { DndContext, DragOverlay, UniqueIdentifier } from "@dnd-kit/core"
import { snapCenterToCursor } from "@dnd-kit/modifiers"
import { useMarkers } from "../components/map/_hooks/useMarkers"
import { LogsWindow } from "../components/simulationRuntime"
import { SimulationDrawer } from "../components/simulationSetup/SimulationDrawer"
import { Toolkit } from "../components/simulationSetup/Toolkit"
import { idToIconMap } from "../components/simulationSetup/Toolkit/dndIds"
import { Tooltip } from "../components/ui/tooltip"
import { useDrawerStore } from "../infrastructure/stores/drawerStore"
import { mapConfig } from "../services/mapConfig"

export const Route = createLazyFileRoute("/")({
    component: RouteComponent,
})

function RouteComponent() {
    const { isOpen, setIsOpen } = useDrawerStore()
    const mapRef = useRef<MapRef>(null)
    const [globalCoordinates, setGlobalCoordinates] = useState<{ clientX: number; clientY: number }>()
    const [activeId, setActiveId] = useState<UniqueIdentifier | null>(null)

    const { markers: batteriesMarkers, addMarker: addBatteryMarker } = useMarkers({
        type: "battery",
        component: (
            <Icon color="green.500" size="2xl">
                <LuDatabaseZap strokeWidth={2.5} />
            </Icon>
        ),
    })

    const { markers: solarMarker, addMarker: addSolarMarker } = useMarkers({
        type: "solar",
        component: (
            <Icon color="green.500" size="2xl">
                <LuSun strokeWidth={2.5} />
            </Icon>
        ),
    })

    const combinedMarkers = useMemo(() => [...batteriesMarkers, ...solarMarker], [batteriesMarkers, solarMarker])

    const mouseMoveHandler = useCallback((event: MouseEvent) => {
        const { clientX, clientY } = event
        setGlobalCoordinates({ clientX, clientY })
    }, [])

    return (
        <div style={{ width: "100%", height: "100vh" }}>
            <DndContext
                modifiers={[snapCenterToCursor]}
                onDragEnd={() => {
                    setActiveId(null)
                    document.removeEventListener("mousemove", mouseMoveHandler)
                    if (mapRef.current && globalCoordinates) {
                        const { lng, lat } = mapRef.current
                            .getMap()
                            .unproject([globalCoordinates.clientX, globalCoordinates.clientY])
                        switch (activeId) {
                            case "battery":
                                addBatteryMarker([lng, lat], `Battery ${crypto.randomUUID()}`)
                                break
                            case "solar":
                                addSolarMarker([lng, lat], `Solar Panel ${crypto.randomUUID()}`)
                                break
                        }
                    }
                }}
                onDragStart={({ active }) => {
                    setActiveId(active.id)
                    document.addEventListener("mousemove", mouseMoveHandler)
                }}>
                <Map
                    ref={mapRef}
                    initialViewState={{
                        longitude: mapConfig.longitude,
                        latitude: mapConfig.latitude,
                        zoom: mapConfig.zoom,
                    }}
                    mapboxAccessToken={import.meta.env.VITE_MAPBOX_ACCESS_TOKEN}
                    mapStyle="mapbox://styles/mapbox/light-v11">
                    {combinedMarkers}
                    <Source data={mapConfig.areaOfInterest} id="areaOfInterest" type="geojson" />
                    <Layer
                        id="areaOfInterest"
                        paint={{
                            "line-color": "green",
                            "line-width": 4,
                            "line-dasharray": [3, 3],
                            "line-opacity": 0.5,
                        }}
                        source="areaOfInterest"
                        type="line"
                    />
                </Map>

                <Toolkit />
                <DragOverlay
                    dropAnimation={null}
                    style={{
                        display: "grid",
                        placeItems: "center",
                        cursor: "grabbing",
                    }}>
                    {activeId && (
                        <Icon color="green.300" size="2xl">
                            {idToIconMap[activeId as keyof typeof idToIconMap]}
                        </Icon>
                    )}
                </DragOverlay>
            </DndContext>
            <Tooltip content="Open simulation setup" positioning={{ placement: "top" }}>
                <IconButton
                    bottom="10"
                    hidden={isOpen}
                    position="fixed"
                    right="10"
                    rounded="xl"
                    size="xl"
                    variant="subtle"
                    zIndex="max"
                    onClick={() => setIsOpen(true)}>
                    <LuSettings2 />
                </IconButton>
            </Tooltip>
            <SimulationDrawer />
            <LogsWindow />
        </div>
    )
}
