import { useCallback, useEffect, useState } from "react"
import { IMessage, RxStompState } from "@stomp/rx-stomp"
import { Subscription } from "rxjs"
import { useWebSocket } from "./WebSocketProvider"

export function useSubscription<T>(topic: string, initialState: T) {
    const [data, setData] = useState<T>(initialState)
    const [loading, setLoading] = useState(true)
    const rxStompClient = useWebSocket()

    const subscribeToData = useCallback(() => {
        const subscription = rxStompClient.watch(topic).subscribe((message: IMessage) => {
            try {
                const parsedData = JSON.parse(message.body) as T
                setData(parsedData)
            } catch (error) {
                console.error("Error parsing WebSocket message:", error)
            } finally {
                setLoading(false)
            }
        })

        return subscription
    }, [rxStompClient, topic])

    useEffect(() => {
        let subscription: Subscription | null = null

        if (rxStompClient.connected()) {
            setLoading(true)

            subscription = subscribeToData()
        } else {
            const connectionStateSubscription = rxStompClient.connectionState$.subscribe(state => {
                if (state === RxStompState.OPEN) {
                    subscription = subscribeToData()
                }
            })

            return () => {
                connectionStateSubscription.unsubscribe()
            }
        }

        return () => {
            if (subscription) {
                subscription.unsubscribe()
            }
        }
    }, [rxStompClient, subscribeToData])

    const resetData = () => {
        setData(initialState)
    }

    return { data, loading, resetData }
}
