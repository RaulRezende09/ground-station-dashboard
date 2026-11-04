import { useEffect, useState } from 'react'
import { fetchPasses } from '../services/api.ts'
import type { PassesResponse, Station } from '../types.ts'

interface Result {
    key: string
    data: PassesResponse | null
    error: string | null
}

export function usePasses(noradId: number, station: Station, minElevationDeg: number) {
    const { name, latitudeDeg, longitudeDeg, altitudeM } = station
    const key = `${noradId}|${name}|${latitudeDeg}|${longitudeDeg}|${altitudeM}|${minElevationDeg}`

    const [result, setResult] = useState<Result | null>(null)

    useEffect(() => {
        let cancelled = false

        fetchPasses(noradId, { name, latitudeDeg, longitudeDeg, altitudeM }, minElevationDeg)
            .then((data) => {
                if (!cancelled) setResult({ key, data, error: null })
            })
            .catch((e: unknown) => {
                if (!cancelled) {
                    const error = e instanceof Error ? e.message : 'Unexpected error'
                    setResult({ key, data: null, error })
                }
            })

        return () => {
            cancelled = true
        }
    }, [key, noradId, name, latitudeDeg, longitudeDeg, altitudeM, minElevationDeg])

    // A result that belongs to older parameters counts as "still loading".
    const current = result?.key === key ? result : null

    return {
        data: current?.data ?? null,
        error: current?.error ?? null,
        loading: current === null,
    }
}