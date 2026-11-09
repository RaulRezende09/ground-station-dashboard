const LOW_HUE = 35 // amber, near the horizon
const HIGH_HUE = 245 // indigo, near the zenith

// Maps an elevation (0..90 degrees) to a color: the higher the satellite, the cooler the color.
export function elevationColor(elevationDeg: number): string {
    const t = Math.min(1, Math.max(0, elevationDeg / 90))
    const hue = LOW_HUE + (HIGH_HUE - LOW_HUE) * t
    const lightness = 50 - 10 * t
    return `hsl(${hue.toFixed(0)}, 85%, ${lightness.toFixed(0)}%)`
}

// The same scale as a CSS gradient, for the legend bar.
export function elevationGradient(): string {
    const stops = [0, 15, 30, 45, 60, 75, 90].map(elevationColor)
    return `linear-gradient(to right, ${stops.join(', ')})`
}