const POINTS = [
    'N', 'NNE', 'NE', 'ENE', 'E', 'ESE', 'SE', 'SSE',
    'S', 'SSW', 'SW', 'WSW', 'W', 'WNW', 'NW', 'NNW',
]

// 0 = north, 90 = east, 180 = south, 270 = west; each point covers 22.5 degrees.
export function compassPoint(azimuthDeg: number): string {
    const index = Math.round(azimuthDeg / 22.5) % POINTS.length
    return POINTS[index]
}