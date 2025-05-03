import { create } from "zustand"

export type DrawerState = {
    isOpen: boolean
    drawerWidth: string
    setIsOpen: (open: boolean) => void
}

export const useDrawerStore = create<DrawerState>()(set => ({
    isOpen: true,
    drawerWidth: "500px",
    setIsOpen: (open: boolean) => set({ isOpen: open }),
}))
