import { describe, expect, it } from 'vitest'
import { elevationColor, elevationGradient } from './elevationColor.ts'

describe('elevationColor', () => {
    it('uses the amber end of the scale at the horizon', () => {
        expect(elevationColor(0)).toBe('hsl(35, 85%, 50%)')
    })

    it('uses the indigo end of the scale at the zenith', () => {
        expect(elevationColor(90)).toBe('hsl(245, 85%, 40%)')
    })

    it('gives different colors to low and high elevations', () => {
        expect(elevationColor(15)).not.toBe(elevationColor(60))
    })

    it('clamps elevations outside 0..90', () => {
        expect(elevationColor(-20)).toBe(elevationColor(0))
        expect(elevationColor(120)).toBe(elevationColor(90))
    })

    it('builds a left-to-right gradient from the same scale', () => {
        const gradient = elevationGradient()
        expect(gradient.startsWith('linear-gradient(to right, ')).toBe(true)
        expect(gradient).toContain(elevationColor(0))
        expect(gradient).toContain(elevationColor(90))
    })
})