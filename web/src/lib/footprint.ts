import type { Geometry } from 'geojson'
import type { FootprintResponse } from '../types.ts'

// The backend closes footprints that enclose a pole by adding vertices at
// latitude +/-90. They only exist to fill the polygon correctly, so the visible
// outline must be drawn without them.
export function footprintOutline(footprint: FootprintResponse['footprint']): Geometry {
    const ring = footprint.coordinates[0]
    const enclosesPole = ring.some(([, lat]) => Math.abs(lat) >= 90)

    const line = enclosesPole
        ? ring.filter(([, lat]) => Math.abs(lat) < 90).slice(0, -1)
        : ring

    return { type: 'LineString', coordinates: line }
}