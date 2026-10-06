export interface SatelliteOption {
    noradId: number
    name: string
}

export const SATELLITES: SatelliteOption[] = [
    { noradId: 25544, name: 'ISS' },
    { noradId: 25338, name: 'NOAA-15' },
    { noradId: 28654, name: 'NOAA-18' },
    { noradId: 33591, name: 'NOAA-19' },
    { noradId: 40024, name: 'NanoSatC-BR1' },
]