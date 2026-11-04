import { useState, type FormEvent } from 'react'
import { DEFAULT_STATION } from '../data/stations.ts'
import type { Station } from '../types.ts'

interface StationFormProps {
    station: Station
    minElevationDeg: number
    onApply: (station: Station, minElevationDeg: number) => void
}

const INPUT_CLASS = 'w-full rounded border border-slate-300 px-2 py-1 text-sm'
const LABEL_CLASS = 'flex flex-col gap-1 text-xs font-medium text-slate-600'

function parseField(text: string, label: string, min: number, max: number): number | string {
    if (text.trim() === '') return `${label} is required`
    const value = Number(text)
    if (!(value >= min && value <= max)) return `${label} must be between ${min} and ${max}`
    return value
}

export function StationForm({ station, minElevationDeg, onApply }: StationFormProps) {
    const [name, setName] = useState(station.name)
    const [latitude, setLatitude] = useState(String(station.latitudeDeg))
    const [longitude, setLongitude] = useState(String(station.longitudeDeg))
    const [altitude, setAltitude] = useState(String(station.altitudeM))
    const [minElevation, setMinElevation] = useState(String(minElevationDeg))
    const [error, setError] = useState<string | null>(null)

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        const lat = parseField(latitude, 'Latitude', -90, 90)
        const lon = parseField(longitude, 'Longitude', -180, 180)
        const alt = parseField(altitude, 'Altitude', -500, 9000)
        const minEl = parseField(minElevation, 'Minimum elevation', 0, 89)

        for (const field of [lat, lon, alt, minEl]) {
            if (typeof field === 'string') {
                setError(field)
                return
            }
        }
        if (name.trim() === '') {
            setError('Name is required')
            return
        }

        setError(null)
        onApply(
            {
                name: name.trim(),
                latitudeDeg: lat as number,
                longitudeDeg: lon as number,
                altitudeM: alt as number,
            },
            minEl as number,
        )
    }

    function fillDefault() {
        setName(DEFAULT_STATION.name)
        setLatitude(String(DEFAULT_STATION.latitudeDeg))
        setLongitude(String(DEFAULT_STATION.longitudeDeg))
        setAltitude(String(DEFAULT_STATION.altitudeM))
        setError(null)
    }

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-2">
            <label className={LABEL_CLASS}>
                Name
                <input className={INPUT_CLASS} value={name} onChange={(e) => setName(e.target.value)} />
            </label>

            <div className="grid grid-cols-2 gap-2">
                <label className={LABEL_CLASS}>
                    Latitude (°)
                    <input
                        className={INPUT_CLASS}
                        type="number"
                        step="any"
                        value={latitude}
                        onChange={(e) => setLatitude(e.target.value)}
                    />
                </label>
                <label className={LABEL_CLASS}>
                    Longitude (°)
                    <input
                        className={INPUT_CLASS}
                        type="number"
                        step="any"
                        value={longitude}
                        onChange={(e) => setLongitude(e.target.value)}
                    />
                </label>
                <label className={LABEL_CLASS}>
                    Altitude (m)
                    <input
                        className={INPUT_CLASS}
                        type="number"
                        step="any"
                        value={altitude}
                        onChange={(e) => setAltitude(e.target.value)}
                    />
                </label>
                <label className={LABEL_CLASS}>
                    Min. elevation (°)
                    <input
                        className={INPUT_CLASS}
                        type="number"
                        step="any"
                        value={minElevation}
                        onChange={(e) => setMinElevation(e.target.value)}
                    />
                </label>
            </div>

            {error && <p className="text-xs text-red-600">{error}</p>}

            <div className="flex gap-2">
                <button
                    type="submit"
                    className="rounded bg-sky-600 px-3 py-1 text-sm font-medium text-white hover:bg-sky-700"
                >
                    Calculate passes
                </button>
                <button
                    type="button"
                    onClick={fillDefault}
                    className="rounded px-3 py-1 text-sm text-slate-700 hover:bg-slate-200"
                >
                    Use Sao Paulo
                </button>
            </div>
        </form>
    )
}