import { useEffect, useRef, useState } from 'react'
import * as maplibregl from 'maplibre-gl'
import 'maplibre-gl/dist/maplibre-gl.css'
import { fetchFootprint, fetchGroundTrack, fetchPosition } from '../api'
import type { PositionResponse } from '../types'

const EMPTY_COLLECTION = { type: 'FeatureCollection' as const, features: [] }

interface SatelliteMapProps {
    noradId: number
}

export function SatelliteMap({ noradId }: SatelliteMapProps) {
    const mapContainer = useRef<HTMLDivElement>(null)
    const mapRef = useRef<maplibregl.Map | null>(null)
    const [mapReady, setMapReady] = useState(false)
    const [position, setPosition] = useState<PositionResponse | null>(null)
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

    // Effect 2: fetch data whenever the map is ready or the satellite changes.
    useEffect(() => {
        const map = mapRef.current
        if (!mapReady || !map) return

        let cancelled = false

        async function load() {
            try {
                setError(null)
                const [pos, track, footprint] = await Promise.all([
                    fetchPosition(noradId),
                    fetchGroundTrack(noradId),
                    fetchFootprint(noradId),
                ])
                if (cancelled || !map) return

                const footprintSource = map.getSource('footprint') as maplibregl.GeoJSONSource
                footprintSource.setData({
                    type: 'Feature',
                    properties: {},
                    geometry: footprint.footprint,
                })

                const trackSource = map.getSource('track') as maplibregl.GeoJSONSource
                trackSource.setData({
                    type: 'Feature',
                    properties: {},
                    geometry: track.track,
                })

                const satelliteSource = map.getSource('satellite') as maplibregl.GeoJSONSource
                satelliteSource.setData({
                    type: 'Feature',
                    properties: {},
                    geometry: { type: 'Point', coordinates: [pos.longitude, pos.latitude] },
                })

                setPosition(pos)
            } catch (e) {
                if (!cancelled) {
                    setError(e instanceof Error ? e.message : 'Unexpected error')
                }
            }
        }

        load()

        return () => {
            cancelled = true
        }
    }, [mapReady, noradId])

    return (
        <div className="relative h-screen w-screen">
            <div ref={mapContainer} className="h-full w-full" />

            {position && (
                <div className="absolute left-4 top-4 rounded bg-white/90 px-3 py-2 text-sm shadow">
                    <div className="font-semibold">{position.name}</div>
                    <div>
                        {position.latitude.toFixed(2)}°, {position.longitude.toFixed(2)}° ·{' '}
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