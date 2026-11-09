import type { SkyPoint } from '../types.ts'

export interface SkyPosition {
    azimuthDeg: number
    elevationDeg: number
}

// Where the satellite is at `nowMs`, or null when the pass is not happening right now.
// The track has one point every few seconds; between two points we move in a straight line.
export function interpolateTrack(track: SkyPoint[], nowMs: number): SkyPosition | null {
    if (track.length < 2) return null

    const times = track.map((point) => Date.parse(point.time))
    if (nowMs < times[0] || nowMs > times[times.length - 1]) return null

    let i = 0
    while (i < times.length - 2 && nowMs > times[i + 1]) {
        i++
    }

    const a = track[i]
    const b = track[i + 1]
    const span = times[i + 1] - times[i]
    const fraction = span === 0 ? 0 : (nowMs - times[i]) / span

    // Take the short way around the circle: 350 -> 10 goes through 0, not through 180.
    const delta = ((b.azimuthDeg - a.azimuthDeg + 540) % 360) - 180

    return {
        azimuthDeg: (a.azimuthDeg + delta * fraction + 360) % 360,
        elevationDeg: a.elevationDeg + (b.elevationDeg - a.elevationDeg) * fraction,
    }
}