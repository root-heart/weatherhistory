// holy crap - thanks to stackoverflow for making my life with this hazy language called typescript a bit easier...
export type DeepPartial<T> = T extends Function
    ? T
    : T extends Array<infer U>
        ? _DeepPartialArray<U>
        : T extends object
            ? _DeepPartialObject<T>
            : T | undefined;
type _DeepPartialArray<T> = Array<DeepPartial<T>>
type _DeepPartialObject<T> = { [P in keyof T]?: DeepPartial<T[P]> };
