// export type DeepOmit<T, K extends PropertyKey> = {
//     [P in keyof T as P extends K ? never : P]: T[P] extends object ? DeepOmit<T[P], K> : T[P]
// }

export type DeepOmit<T, K extends PropertyKey> =
    T extends Array<infer U>
        ? DeepOmit<U, K>[]
        : T extends object
          ? { [P in keyof T as P extends K ? never : P]: T[P] extends object ? DeepOmit<T[P], K> : T[P] }
          : T
