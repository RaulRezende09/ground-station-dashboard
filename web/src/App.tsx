import { SatelliteMap } from './components/SatelliteMap'

const ISS_NORAD_ID = 25544

function App() {
  return <SatelliteMap noradId={ISS_NORAD_ID} />
}

export default App