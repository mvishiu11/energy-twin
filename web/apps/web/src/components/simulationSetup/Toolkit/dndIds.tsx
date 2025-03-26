import { LuDatabaseZap, LuSun } from "react-icons/lu"

export const dndIds = {
    battery: "battery",
    solar: "solar",
}

export const idToIconMap: Record<keyof typeof dndIds, React.ReactNode> = {
    battery: <LuDatabaseZap />,
    solar: <LuSun />,
}
