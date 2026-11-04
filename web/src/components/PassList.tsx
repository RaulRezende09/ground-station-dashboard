import { compassPoint } from '../lib/compass.ts'
import { formatDuration, formatLocal, formatUtc } from '../lib/time.ts'
import type { SatellitePass } from '../types.ts'

interface PassListProps {
    passes: SatellitePass[] | null
    loading: boolean
    error: string | null
    selectedAos: string | null
    onSelect: (aos: string) => void
}

export function PassList({ passes, loading, error, selectedAos, onSelect }: PassListProps) {
    if (loading) return <p className="text-sm text-slate-500">Calculating passes…</p>
    if (error) return <p className="text-sm text-red-600">{error}</p>
    if (!passes || passes.length === 0) {
        return <p className="text-sm text-slate-500">No passes in the next 48 hours.</p>
    }

    return (
        <ul className="flex flex-col gap-1">
            {passes.map((pass) => (
                <li key={pass.aos}>
                    <button
                        type="button"
                        onClick={() => onSelect(pass.aos)}
                        className={
                            pass.aos === selectedAos
                                ? 'w-full rounded bg-sky-600 px-2 py-1 text-left text-sm text-white'
                                : 'w-full rounded px-2 py-1 text-left text-sm text-slate-700 hover:bg-slate-200'
                        }
                    >
                        <div className="font-medium">{formatLocal(pass.aos)}</div>
                        <div className="text-xs opacity-80">
                            {formatUtc(pass.aos)} · {formatDuration(pass.durationSeconds)}
                        </div>
                        <div className="text-xs opacity-80">
                            max {pass.maxElevationDeg.toFixed(0)}° ·{' '}
                            {compassPoint(pass.aosAzimuthDeg)} → {compassPoint(pass.losAzimuthDeg)}
                        </div>
                    </button>
                </li>
            ))}
        </ul>
    )
}