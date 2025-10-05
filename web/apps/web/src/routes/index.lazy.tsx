import { Box, Icon, IconButton, Tabs } from "@chakra-ui/react"
import { createLazyFileRoute } from "@tanstack/react-router"
import { memo, useCallback, useEffect, useMemo, useRef, useState } from "react"
import { LuBuilding, LuLayoutDashboard, LuMap, LuSettings2, LuSun } from "react-icons/lu"
import Map, { Layer, MapRef, Source } from "react-map-gl/mapbox"
import { DndContext, DragOverlay, UniqueIdentifier } from "@dnd-kit/core"
import { snapCenterToCursor } from "@dnd-kit/modifiers"
import { Dashboard } from "../components/dashboard"
import { useMarkers } from "../components/map/_hooks/useMarkers"
import { BatteryMarker } from "../components/map/BatteryMarker"
import { SimulationDrawer } from "../components/simulationSetup/SimulationDrawer"
import { Toolkit } from "../components/simulationSetup/Toolkit"
import { idToIconMap } from "../components/simulationSetup/Toolkit/dndIds"
import { Tooltip } from "../components/ui/tooltip"
import { useStopSimulation } from "../infrastructure/fetching"
import { useDrawerStore } from "../infrastructure/stores/drawerStore"
import { mapConfig } from "../services/mapConfig"

const MemoizedDrawer = memo(SimulationDrawer)
const MemoizedToolkit = memo(Toolkit)

export const Route = createLazyFileRoute("/")({
    component: RouteComponent,
})

function RouteComponent() {
    const { isOpen, setIsOpen } = useDrawerStore()
    const mapRef = useRef<MapRef>(null)
    const [globalCoordinates, setGlobalCoordinates] = useState<{ clientX: number; clientY: number }>()
    const [activeId, setActiveId] = useState<UniqueIdentifier | null>(null)
    const { mutateAsync: stopSimulation } = useStopSimulation()

    const handleBeforeUnload = useCallback(async () => {
        await stopSimulation()
    }, [stopSimulation])

    useEffect(() => {
        window.addEventListener("beforeunload", handleBeforeUnload)

        return () => {
            window.removeEventListener("beforeunload", handleBeforeUnload)
        }
    }, [handleBeforeUnload])

    const { markers: batteriesMarkers, addMarker: addBatteryMarker } = useMarkers({
        type: "battery",
        component: id => <BatteryMarker id={id} />,
    })

    const { markers: solarMarker, addMarker: addSolarMarker } = useMarkers({
        type: "solar",
        component: (
            <Icon color="green.500" size="2xl">
                <LuSun strokeWidth={2.5} />
            </Icon>
        ),
    })

    const { markers: buildingMarkers, addMarker: addBuildingMarker } = useMarkers({
        type: "building",
        component: (
            <Icon color="blue.500" size="2xl">
                <LuBuilding strokeWidth={2.5} />
            </Icon>
        ),
    })

    const combinedMarkers = useMemo(
        () => [...batteriesMarkers, ...solarMarker, ...buildingMarkers],
        [batteriesMarkers, solarMarker, buildingMarkers],
    )

    const mouseMoveHandler = useCallback((event: MouseEvent) => {
        const { clientX, clientY } = event
        setGlobalCoordinates({ clientX, clientY })
    }, [])

    return (
        <div>
            <Tabs.Root fitted colorScheme="green" defaultValue="map" size="lg" variant="plain" width="full">
                <Tabs.List margin={3}>
                    <Tabs.Trigger value="map">
                        <LuMap />
                        Map
                    </Tabs.Trigger>
                    <Tabs.Trigger value="dashboard">
                        <LuLayoutDashboard />
                        Dashboard
                    </Tabs.Trigger>
                </Tabs.List>
                <Tabs.ContentGroup>
                    <Tabs.Content p={0} value="map">
                        <div style={{ width: "100vw", height: "calc(100vh - 69px)" }}>
                            <DndContext
                                modifiers={[snapCenterToCursor]}
                                onDragEnd={() => {
                                    setActiveId(null)
                                    document.removeEventListener("mousemove", mouseMoveHandler)
                                    if (mapRef.current && globalCoordinates) {
                                        const { lng, lat } = mapRef.current
                                            .getMap()
                                            .unproject([globalCoordinates.clientX, globalCoordinates.clientY - 69])
                                        switch (activeId) {
                                            case "battery":
                                                addBatteryMarker([lng, lat], `Battery ${batteriesMarkers.length + 1}`)
                                                break
                                            case "solar":
                                                addSolarMarker([lng, lat], `Solar Panel ${solarMarker.length + 1}`)
                                                break
                                            case "building":
                                                addBuildingMarker([lng, lat], `Building ${buildingMarkers.length + 1}`)
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
                                <MemoizedToolkit />
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
                            <MemoizedDrawer />
                        </div>
                    </Tabs.Content>
                    <Tabs.Content value="dashboard">
                        <Box paddingX={6}>
                            <Dashboard />
                        </Box>
                    </Tabs.Content>
                </Tabs.ContentGroup>
            </Tabs.Root>
        </div>
    )
}
