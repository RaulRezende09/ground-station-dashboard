export interface PlotPoint {
    x: number
    y: number
}

// Sky plot geometry: zenith (90 deg) at the center, horizon (0 deg) on the outer
// circle, north up, azimuth growing clockwise.
export function polarToXY(
    azimuthDeg: number,
    elevationDeg: number,
    radius: number,
    cx: number,
    cy: number,
): PlotPoint {
    const clamped = Math.min(90, Math.max(0, elevationDeg))
    const r = ((90 - clamped) / 90) * radius
    const azimuth = (azimuthDeg * Math.PI) / 180
    return {
        x: cx + r * Math.sin(azimuth),
        y: cy - r * Math.cos(azimuth),
    }
}