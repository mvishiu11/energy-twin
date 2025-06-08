import { LuBuilding, LuDatabaseZap, LuSun } from "react-icons/lu"

export const dndIds = {
    battery: "battery",
    building: "building",
    solar: "solar",
}

export const idToIconMap: Record<keyof typeof dndIds, React.ReactNode> = {
    battery: <LuDatabaseZap />,
    building: <LuBuilding />,
    solar: <LuSun />,
}
