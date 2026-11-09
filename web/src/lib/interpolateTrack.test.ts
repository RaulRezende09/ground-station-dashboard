import { describe, expect, it } from 'vitest'
import { interpolateTrack } from './interpolateTrack.ts'
import type { SkyPoint } from '../types.ts'

const T0 = Date.parse('2026-11-09T12:00:00Z')

function point(secondsAfterStart: number, azimuthDeg: number, elevationDeg: number): SkyPoint {
    return { time: new Date(T0 + secondsAfterStart * 1000).toISOString(), azimuthDeg, elevationDeg }
}

const TRACK = [point(0, 100, 10), point(10, 120, 20), point(20, 140, 10)]

describe('interpolateTrack', () => {
    it('returns null before the pass starts and after it ends', () => {
        expect(interpolateTrack(TRACK, T0 - 1)).toBeNull()
        expect(interpolateTrack(TRACK, T0 + 20_001)).toBeNull()
    })

    it('returns the sample itself when the time matches a sample', () => {
        const result = interpolateTrack(TRACK, T0 + 10_000)
        expect(result?.azimuthDeg).toBeCloseTo(120)
        expect(result?.elevationDeg).toBeCloseTo(20)
    })

    it('moves in a straight line between two samples', () => {
        const result = interpolateTrack(TRACK, T0 + 5_000)
        expect(result?.azimuthDeg).toBeCloseTo(110)
        expect(result?.elevationDeg).toBeCloseTo(15)
    })

    it('uses the last segment at the very end of the pass', () => {
        const result = interpolateTrack(TRACK, T0 + 20_000)
        expect(result?.azimuthDeg).toBeCloseTo(140)
        expect(result?.elevationDeg).toBeCloseTo(10)
    })

    it('goes through north when the azimuth wraps from 350 to 10', () => {
        const wrapping = [point(0, 350, 10), point(10, 10, 20)]
        expect(interpolateTrack(wrapping, T0 + 5_000)?.azimuthDeg).toBeCloseTo(0)
    })

    it('also wraps the other way, from 10 to 350', () => {
        const wrapping = [point(0, 10, 10), point(10, 350, 20)]
        expect(interpolateTrack(wrapping, T0 + 5_000)?.azimuthDeg).toBeCloseTo(0)
    })

    it('returns null for a track too short to interpolate', () => {
        expect(interpolateTrack([point(0, 100, 10)], T0)).toBeNull()
        expect(interpolateTrack([], T0)).toBeNull()
    })
})