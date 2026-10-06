import { SATELLITES } from '../satellites'

interface SatelliteSelectorProps {
    selected: number
    onSelect: (noradId: number) => void
}

export function SatelliteSelector({ selected, onSelect }: SatelliteSelectorProps) {
    return (
        <div className="absolute right-4 top-4 flex flex-col gap-1 rounded bg-white/90 p-2 shadow">
            {SATELLITES.map((satellite) => (
                <button
                    key={satellite.noradId}
                    type="button"
                    onClick={() => onSelect(satellite.noradId)}
                    className={
                        satellite.noradId === selected
                            ? 'rounded bg-sky-600 px-3 py-1 text-left text-sm font-medium text-white'
                            : 'rounded px-3 py-1 text-left text-sm text-slate-700 hover:bg-slate-200'
                    }
                >
                    {satellite.name}
                </button>
            ))}
        </div>
    )
}