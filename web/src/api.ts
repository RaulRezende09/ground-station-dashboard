import type {
    FootprintResponse,
    GroundTrackResponse,
    PositionResponse,
} from './types'

async function getJson<T>(path: string): Promise<T> {
    const response = await fetch(path)

    if (!response.ok) {
        let message = `Request failed (${response.status})`
        try {
            const body = await response.json()
            if (body.error) message = body.error
        } catch {
            // the response was not JSON; keep the generic message
        }
        throw new Error(message)
    }

    return response.json() as Promise<T>
}

export function fetchPosition(noradId: number) {
    return getJson<PositionResponse>(`/api/satellites/${noradId}/position`)
}

export function fetchGroundTrack(noradId: number) {
    return getJson<GroundTrackResponse>(`/api/satellites/${noradId}/groundtrack`)
}

export function fetchFootprint(noradId: number, minElevationDeg = 10) {
    return getJson<FootprintResponse>(
        `/api/satellites/${noradId}/footprint?minElevationDeg=${minElevationDeg}`,
    )
}