import type { Station } from '../types.ts'

// A public reference point (Sao Paulo city center), never a home address.
export const DEFAULT_STATION: Station = {
    name: 'Sao Paulo (city center)',
    latitudeDeg: -23.5505,
    longitudeDeg: -46.6333,
    altitudeM: 760,
}

export const DEFAULT_MIN_ELEVATION_DEG = 10