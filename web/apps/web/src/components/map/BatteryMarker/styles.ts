import { motion } from "motion/react"
import styled from "@emotion/styled"

export const ChargeBar = styled(motion.div)<{ $backgroundColor: string }>`
    transform: rotate(180deg);
    width: 100%;
    background: ${({ $backgroundColor }) => $backgroundColor};
`
export const ChargeBarContainer = styled.div`
    overflow: hidden;
    display: flex;
    align-items: end;

    width: 15px;
    height: 38px;
    border: 2px solid #71717a;
    border-radius: 4px;
`
