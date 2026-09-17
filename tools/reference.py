from sgp4.api import Satrec, jday

# Mesmo TLE usado no teste Java — se um mudar, o outro tem que mudar junto
LINE_1 = "1 25544U 98067A   26255.51782528 -.00002182  00000-0 -11606-4 0  9990"
LINE_2 = "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"

satellite = Satrec.twoline2rv(LINE_1, LINE_2)

# Mesma data usada no teste Java — ano, mês, dia, hora, minuto, segundo (UTC)
julian_day, fraction = jday(2026, 9, 19, 12, 0, 0)
error_code, position_km, velocity_km_s = satellite.sgp4(julian_day, fraction)

if error_code != 0:
    raise RuntimeError(f"sgp4 returned error code {error_code}")

print("TEME position (km):", position_km)