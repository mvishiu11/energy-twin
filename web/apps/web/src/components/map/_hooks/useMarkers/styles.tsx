import { css } from "@emotion/react"
import styled from "@emotion/styled"

export const SelectedMarker = styled.div<{ isSelected: boolean }>`
    ${({ isSelected }) =>
        isSelected &&
        css`
            transform: scale(1.2);
            border: 2px solid #0070f3;
        `}
`
