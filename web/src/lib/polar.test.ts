import { describe, expect, it } from 'vitest'
import { polarToXY } from './polar.ts'

const R = 100
const C = 120

describe('polarToXY', () => {
    it('puts the zenith at the center', () => {
        const { x, y } = polarToXY(0, 90, R, C, C)
        expect(x).toBeCloseTo(C)
        expect(y).toBeCloseTo(C)
    })

    it('puts the northern horizon at the top', () => {
        const { x, y } = polarToXY(0, 0, R, C, C)
        expect(x).toBeCloseTo(C)
        expect(y).toBeCloseTo(C - R)
    })

    it('puts the eastern horizon on the right', () => {
        const { x, y } = polarToXY(90, 0, R, C, C)
        expect(x).toBeCloseTo(C + R)
        expect(y).toBeCloseTo(C)
    })

    it('puts the southern horizon at the bottom and the western one on the left', () => {
        expect(polarToXY(180, 0, R, C, C).y).toBeCloseTo(C + R)
        expect(polarToXY(270, 0, R, C, C).x).toBeCloseTo(C - R)
    })

    it('places 30 degrees of elevation two thirds of the way out', () => {
        const { y } = polarToXY(0, 30, R, C, C)
        expect(C - y).toBeCloseTo((60 / 90) * R)
    })

    it('clamps elevations outside 0..90', () => {
        expect(polarToXY(0, -10, R, C, C).y).toBeCloseTo(C - R)
        expect(polarToXY(0, 120, R, C, C).y).toBeCloseTo(C)
    })
})