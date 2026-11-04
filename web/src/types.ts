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

export interface Station {
    name: string
    latitudeDeg: number
    longitudeDeg: number
    altitudeM: number
}

export interface SkyPoint {
    time: string
    azimuthDeg: number
    elevationDeg: number
}

export interface SatellitePass {
    aos: string
    los: string
    durationSeconds: number
    maxElevationDeg: number
    maxElevationTime: string
    aosAzimuthDeg: number
    losAzimuthDeg: number
    track: SkyPoint[]
}

export interface PassesResponse {
    noradId: number
    name: string
    station: Station
    minElevationDeg: number
    from: string
    to: string
    tleEpoch: string
    passes: SatellitePass[]
}