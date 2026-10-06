export interface PositionResponse {
    noradId: number
    name: string
    timestamp: string
    latitude: number
    longitude: number
    altitudeKm: number
    velocityKmS: number
    tleEpoch: string
}

export interface GroundTrackResponse {
    noradId: number
    name: string
    track: {
        type: 'MultiLineString'
        coordinates: number[][][]
    }
}

export interface FootprintResponse {
    noradId: number
    name: string
    radiusKm: number
    minElevationDeg: number
    footprint: {
        type: 'Polygon'
        coordinates: number[][][]
    }
}