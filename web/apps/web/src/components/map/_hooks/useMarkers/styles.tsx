import { css } from "@emotion/react"
import styled from "@emotion/styled"

export const SelectedMarker = styled.div<{ isSelected: boolean }>`
    ${({ isSelected }) =>
        isSelected &&
        css`
            padding: 4px;
            border: 2px solid #71717a;
            border-radius: 12px;
        `}
`
