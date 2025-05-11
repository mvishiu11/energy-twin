import { createContext, ReactNode, use, useEffect } from "react"
import { RxStomp, RxStompConfig } from "@stomp/rx-stomp"
import SockJS from "sockjs-client/dist/sockjs"

const WebSocketContext = createContext<typeof rxStompClient | null>(null)

type WebSocketProviderProps = {
    children: ReactNode
}

const rxStompConfig: RxStompConfig = {
    webSocketFactory: () => new SockJS("http://localhost:8081/ws/data"),
    connectHeaders: {},
    heartbeatIncoming: 0,
    heartbeatOutgoing: 20000,
    reconnectDelay: 5000,
    connectionTimeout: 10000,
}

const rxStompClient = new RxStomp()
rxStompClient.configure(rxStompConfig)

export const WebSocketProvider = ({ children }: WebSocketProviderProps) => {
    useEffect(() => {
        rxStompClient.activate()

        return () => {
            if (rxStompClient.connected()) {
                rxStompClient.deactivate()
            }
        }
    }, [])

    return <WebSocketContext value={rxStompClient}>{children}</WebSocketContext>
}

export const useWebSocket = () => {
    const context = use(WebSocketContext)

    if (!context) {
        throw new Error("useWebSocket must be used within a WebSocketProvider")
    }

    return context
}
