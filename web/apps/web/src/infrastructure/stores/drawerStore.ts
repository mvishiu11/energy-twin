import { create } from "zustand"

export type DrawerState = {
    isOpen: boolean
    setIsOpen: (open: boolean) => void
}

export const useDrawerStore = create<DrawerState>()(set => ({
    isOpen: false,
    setIsOpen: (open: boolean) => set({ isOpen: open }),
}))
