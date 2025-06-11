import { motion } from "motion/react"
import styled from "@emotion/styled"

export const DrawerRoot = styled(motion.div)`
    --drawer-padding: 12px;

    position: fixed;
    z-index: 1000;
    top: calc(var(--drawer-padding) + 60px);
    right: var(--drawer-padding);
    bottom: calc(var(--drawer-padding));

    overflow-y: auto;

    height: calc(100vh - 2 * var(--drawer-padding) - 60px);
    padding: 24px;
    padding-top: 0;
    border: 1px solid #e2e8f0;
    border-radius: 8px;

    background-color: white;
    box-shadow: 0 2px 4px 0 rgb(0 0 0 / 10%);
`
