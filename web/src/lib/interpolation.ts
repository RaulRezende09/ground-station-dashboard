export type Coordinate = [number, number] // [longitude, latitude]

// Difference between two longitudes taking the short way around the globe.
// Example: from 179.9 to -179.8 is +0.3, not -359.7.
function lonDelta(from: number, to: number): number {
    let delta = to - from
    if (delta > 180) delta -= 360
    if (delta < -180) delta += 360
    return delta
}

function wrapLongitude(lon: number): number {
    if (lon > 180) return lon - 360
    if (lon < -180) return lon + 360
    return lon
}

// Joins the antimeridian segments back into a single ordered list of points,
// dropping the synthetic edge points (lon = ±180) that the backend adds only
// to close the visual gap: they are not 30-second samples.
export function flattenTrack(segments: number[][][]): Coordinate[] {
    return segments
        .flat()
        .filter(([lon]) => Math.abs(lon) !== 180)
        .map(([lon, lat]): Coordinate => [lon, lat])
}

// Returns a fractional index on the track: 60.4 means "40% of the way from
// point 60 to point 61".
export function locateOnTrack(points: Coordinate[], lon: number, lat: number): number {
    // 1) nearest track point
    let nearest = 0
    let nearestDistance = Infinity
    points.forEach(([pointLon, pointLat], i) => {
        const dLon = lonDelta(lon, pointLon)
        const dLat = pointLat - lat
        const distance = dLon * dLon + dLat * dLat
        if (distance < nearestDistance) {
            nearestDistance = distance
            nearest = i
        }
    })

    // 2) refine: project the position onto the two segments touching that point
    let bestIndex = nearest
    let bestDistance = Infinity
    for (const start of [nearest - 1, nearest]) {
        if (start < 0 || start + 1 >= points.length) continue

        const [aLon, aLat] = points[start]
        const [bLon, bLat] = points[start + 1]
        const abLon = lonDelta(aLon, bLon)
        const abLat = bLat - aLat
        const apLon = lonDelta(aLon, lon)
        const apLat = lat - aLat

        const lengthSquared = abLon * abLon + abLat * abLat
        const t =
            lengthSquared === 0
                ? 0
                : Math.min(1, Math.max(0, (apLon * abLon + apLat * abLat) / lengthSquared))

        const dLon = apLon - abLon * t
        const dLat = apLat - abLat * t
        const distance = dLon * dLon + dLat * dLat
        if (distance < bestDistance) {
            bestDistance = distance
            bestIndex = start + t
        }
    }

    return bestIndex
}

// Linear interpolation between the two track points around a fractional index.
// Returns null when the index is outside the track.
export function positionAt(points: Coordinate[], index: number): Coordinate | null {
    if (points.length < 2 || index < 0 || index > points.length - 1) return null

    const i = Math.min(Math.floor(index), points.length - 2)
    const t = index - i
    const [aLon, aLat] = points[i]
    const [bLon, bLat] = points[i + 1]

    return [wrapLongitude(aLon + lonDelta(aLon, bLon) * t), aLat + (bLat - aLat) * t]
}