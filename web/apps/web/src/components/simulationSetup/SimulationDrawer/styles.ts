import { motion } from "motion/react"
import styled from "@emotion/styled"

export const DrawerRoot = styled(motion.div)`
    position: fixed;
    z-index: 1000;
    top: 20px;
    right: 20px;
    bottom: 20px;

    overflow-y: auto;

    height: calc(100vh - 40px);
    padding: 24px;
    border-radius: 24px;

    background-color: white;
`
