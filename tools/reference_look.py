from datetime import datetime, timedelta, timezone

from skyfield.api import EarthSatellite, load, wgs84

# Mesmo TLE do PropagationValidationTest (mantenha os dois em sincronia)
LINE_1 = "1 25544U 98067A   26255.51782528 -.00002182  00000-0 -11606-4 0  9990"
LINE_2 = "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"

# Estação de teste: centro de São Paulo (ponto público, nunca a sua casa)
LATITUDE_DEG = -23.5505
LONGITUDE_DEG = -46.6333
ALTITUDE_M = 760.0

INSTANTS = [
    datetime(2026, 9, 19, 16, 16, 0, tzinfo=timezone.utc),
    datetime(2026, 9, 19, 16, 18, 0, tzinfo=timezone.utc),
    datetime(2026, 9, 19, 16, 20, 0, tzinfo=timezone.utc),
]

ts = load.timescale(builtin=True)
satellite = EarthSatellite(LINE_1, LINE_2, "ISS (ZARYA)", ts)
station = wgs84.latlon(LATITUDE_DEG, LONGITUDE_DEG, elevation_m=ALTITUDE_M)


def look(moment):
    altitude, azimuth, distance = (satellite - station).at(ts.from_datetime(moment)).altaz()
    return azimuth.degrees, altitude.degrees, distance.km


for moment in INSTANTS:
    az, el, rng = look(moment)
    # range rate por diferença finita: (distância depois - distância antes) / intervalo
    after = look(moment + timedelta(seconds=0.5))[2]
    before = look(moment - timedelta(seconds=0.5))[2]
    rate = after - before  # km em 1 s = km/s
    print(f"{moment.isoformat()} az={az:.4f} el={el:.4f} range_km={rng:.4f} rate_km_s={rate:.5f}")