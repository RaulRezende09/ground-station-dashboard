import { useState } from 'react'
import { PassList } from './components/PassList'
import { SatelliteMap } from './components/SatelliteMap'
import { SatelliteSelector } from './components/SatelliteSelector'
import { StationForm } from './components/StationForm'
import { DEFAULT_MIN_ELEVATION_DEG, DEFAULT_STATION } from './data/stations.ts'
import { SATELLITES } from './data/satellites.ts'
import { usePasses } from './hooks/usePasses.ts'
import type { Station } from './types.ts'

function App() {
    const [noradId, setNoradId] = useState(SATELLITES[0].noradId)
    const [station, setStation] = useState<Station>(DEFAULT_STATION)
    const [minElevationDeg, setMinElevationDeg] = useState(DEFAULT_MIN_ELEVATION_DEG)
    const [selectedAos, setSelectedAos] = useState<string | null>(null)

    const { data, loading, error } = usePasses(noradId, station, minElevationDeg)

    function handleApply(newStation: Station, newMinElevation: number) {
        setStation(newStation)
        setMinElevationDeg(newMinElevation)
    }

    return (
        <div className="relative h-screen w-screen">
            <SatelliteMap noradId={noradId} />
            <SatelliteSelector selected={noradId} onSelect={setNoradId} />

            <aside className="absolute bottom-4 left-4 flex max-h-[70vh] w-80 max-w-[calc(100vw-2rem)] flex-col gap-3 overflow-y-auto rounded bg-white/95 p-3 shadow">
                <h2 className="text-sm font-semibold text-slate-800">Ground station</h2>
                <StationForm
                    station={station}
                    minElevationDeg={minElevationDeg}
                    onApply={handleApply}
                />
                <h2 className="text-sm font-semibold text-slate-800">Next passes</h2>
                <PassList
                    passes={data?.passes ?? null}
                    loading={loading}
                    error={error}
                    selectedAos={selectedAos}
                    onSelect={setSelectedAos}
                />
            </aside>
        </div>
    )
}

export default App