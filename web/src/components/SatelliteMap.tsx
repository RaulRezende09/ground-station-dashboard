import { useEffect, useRef, useState } from 'react'
import * as maplibregl from 'maplibre-gl'
import 'maplibre-gl/dist/maplibre-gl.css'
import type { Geometry } from 'geojson'
import {
    TRACK_STEP_SECONDS,
    fetchFootprint,
    fetchGroundTrack,
    fetchPosition,
} from '../api'
import {
    type Coordinate,
    flattenTrack,
    locateOnTrack,
    positionAt,
} from '../interpolation'
import type { PositionResponse } from '../types'

const EMPTY_COLLECTION = { type: 'FeatureCollection' as const, features: [] }

const TICK_MS = 1_000
const FOOTPRINT_REFRESH_MS = 10_000
const TRACK_REFRESH_MS = 5 * 60_000

interface SatelliteMapProps {
    noradId: number
}

interface TimeAnchor {
    index: number // fractional track index at the moment the response arrived
    receivedAt: number // Date.now() at that moment
}

function updateSource(map: maplibregl.Map, id: string, geometry: Geometry) {
    const source = map.getSource(id) as maplibregl.GeoJSONSource | undefined
    source?.setData({ type: 'Feature', properties: {}, geometry })
}

export function SatelliteMap({ noradId }: SatelliteMapProps) {
    const mapContainer = useRef<HTMLDivElement>(null)
    const mapRef = useRef<maplibregl.Map | null>(null)
    const trackRef = useRef<Coordinate[]>([])
    const anchorRef = useRef<TimeAnchor | null>(null)
    const [mapReady, setMapReady] = useState(false)
    const [position, setPosition] = useState<PositionResponse | null>(null)
    const [livePosition, setLivePosition] = useState<Coordinate | null>(null)
    const [error, setError] = useState<string | null>(null)

    // Effect 1: create the map once and register the three empty layers.
    useEffect(() => {
        if (!mapContainer.current || mapRef.current) return

        const map = new maplibregl.Map({
            container: mapContainer.current,
            style: 'https://tiles.openfreemap.org/styles/liberty',
            center: [0, 20],
            zoom: 1.5,
        })
        mapRef.current = map

        map.on('load', () => {
            map.addSource('footprint', { type: 'geojson', data: EMPTY_COLLECTION })
            map.addLayer({
                id: 'footprint-fill',
                type: 'fill',
                source: 'footprint',
                paint: { 'fill-color': '#38bdf8', 'fill-opacity': 0.2 },
            })
            map.addLayer({
                id: 'footprint-outline',
                type: 'line',
                source: 'footprint',
                paint: { 'line-color': '#38bdf8', 'line-width': 1.5 },
            })

            map.addSource('track', { type: 'geojson', data: EMPTY_COLLECTION })
            map.addLayer({
                id: 'track-line',
                type: 'line',
                source: 'track',
                paint: { 'line-color': '#f59e0b', 'line-width': 2 },
            })

            map.addSource('satellite', { type: 'geojson', data: EMPTY_COLLECTION })
            map.addLayer({
                id: 'satellite-point',
                type: 'circle',
                source: 'satellite',
                paint: {
                    'circle-radius': 7,
                    'circle-color': '#ef4444',
                    'circle-stroke-color': '#ffffff',
                    'circle-stroke-width': 2,
                },
            })

            setMapReady(true)
        })

        return () => {
            map.remove()
            mapRef.current = null
            setMapReady(false)
        }
    }, [])

    // Effect 2: data loading and the per-second animation.
    useEffect(() => {
        const map = mapRef.current
        if (!mapReady || !map) return

        let cancelled = false

        // Forget everything from the previous satellite.
        trackRef.current = []
        anchorRef.current = null
        setPosition(null)
        setLivePosition(null)

        async function loadTrackAndPosition() {
            try {
                const [pos, track] = await Promise.all([
                    fetchPosition(noradId),
                    fetchGroundTrack(noradId),
                ])
                if (cancelled || !map) return

                const points = flattenTrack(track.track.coordinates)
                trackRef.current = points
                anchorRef.current = {
                    index: locateOnTrack(points, pos.longitude, pos.latitude),
                    receivedAt: Date.now(),
                }

                updateSource(map, 'track', track.track)
                updateSource(map, 'satellite', {
                    type: 'Point',
                    coordinates: [pos.longitude, pos.latitude],
                })

                setPosition(pos)
                setLivePosition([pos.longitude, pos.latitude])
                setError(null)
            } catch (e) {
                if (!cancelled) setError(e instanceof Error ? e.message : 'Unexpected error')
            }
        }

        async function loadFootprint() {
            try {
                const footprint = await fetchFootprint(noradId)
                if (cancelled || !map) return
                updateSource(map, 'footprint', footprint.footprint)
            } catch (e) {
                if (!cancelled) setError(e instanceof Error ? e.message : 'Unexpected error')
            }
        }

        // Move the marker along the track using only the clock (no network).
        function tick() {
            const anchor = anchorRef.current
            if (!anchor || !map) return

            const elapsedSeconds = (Date.now() - anchor.receivedAt) / 1000
            const index = anchor.index + elapsedSeconds / TRACK_STEP_SECONDS
            const coordinate = positionAt(trackRef.current, index)
            if (!coordinate) return

            updateSource(map, 'satellite', { type: 'Point', coordinates: coordinate })
            setLivePosition(coordinate)
        }

        loadTrackAndPosition()
        loadFootprint()

        const trackTimer = setInterval(loadTrackAndPosition, TRACK_REFRESH_MS)
        const footprintTimer = setInterval(loadFootprint, FOOTPRINT_REFRESH_MS)
        const tickTimer = setInterval(tick, TICK_MS)

        return () => {
            cancelled = true
            clearInterval(trackTimer)
            clearInterval(footprintTimer)
            clearInterval(tickTimer)
        }
    }, [mapReady, noradId])

    return (
        <div className="relative h-screen w-screen">
            <div ref={mapContainer} className="h-full w-full" />

            {position && livePosition && (
                <div className="absolute left-4 top-4 rounded bg-white/90 px-3 py-2 text-sm shadow">
                    <div className="font-semibold">{position.name}</div>
                    <div>
                        {livePosition[1].toFixed(2)}°, {livePosition[0].toFixed(2)}° ·{' '}
                        {position.altitudeKm.toFixed(0)} km
                    </div>
                </div>
            )}

            {error && (
                <div className="absolute left-4 top-4 rounded bg-red-600 px-3 py-2 text-sm text-white shadow">
                    {error}
                </div>
            )}
        </div>
    )
}