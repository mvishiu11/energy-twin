import styled from "@emotion/styled"

export const ArrowContainer = styled.div<{ $isOpened: boolean }>`
    rotate: ${({ $isOpened }) => ($isOpened ? "0deg" : "180deg")};
    transition: rotate 0.3s;
`
