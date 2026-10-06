import { useState } from 'react'
import { SatelliteMap } from './components/SatelliteMap'
import { SatelliteSelector } from './components/SatelliteSelector'
import { SATELLITES } from './satellites'

function App() {
  const [noradId, setNoradId] = useState(SATELLITES[0].noradId)

  return (
      <div className="relative h-screen w-screen">
        <SatelliteMap noradId={noradId} />
        <SatelliteSelector selected={noradId} onSelect={setNoradId} />
      </div>
  )
}

export default App