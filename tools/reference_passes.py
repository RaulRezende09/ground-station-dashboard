from datetime import datetime, timezone

from skyfield.api import EarthSatellite, load, wgs84

# Same TLE as the Java tests (keep them in sync)
LINE_1 = "1 25544U 98067A   26255.51782528 -.00002182  00000-0 -11606-4 0  9990"
LINE_2 = "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"

# Public point (Sao Paulo city center), never a home address
LATITUDE_DEG = -23.5505
LONGITUDE_DEG = -46.6333
ALTITUDE_M = 760.0
MIN_ELEVATION_DEG = 10.0

START = datetime(2026, 9, 19, 0, 0, 0, tzinfo=timezone.utc)
END = datetime(2026, 9, 21, 0, 0, 0, tzinfo=timezone.utc)  # 48 h

ts = load.timescale(builtin=True)
satellite = EarthSatellite(LINE_1, LINE_2, "ISS (ZARYA)", ts)
station = wgs84.latlon(LATITUDE_DEG, LONGITUDE_DEG, elevation_m=ALTITUDE_M)

times, events = satellite.find_events(
    station, ts.from_datetime(START), ts.from_datetime(END),
    altitude_degrees=MIN_ELEVATION_DEG)

aos = None
peak = None
for t, event in zip(times, events):
    if event == 0:      # rise
        aos = t
    elif event == 1 and aos is not None:   # culminate
        altitude, _, _ = (satellite - station).at(t).altaz()
        peak = altitude.degrees
    elif event == 2 and aos is not None:   # set
        duration = (t.utc_datetime() - aos.utc_datetime()).total_seconds()
        print(f"AOS {aos.utc_datetime():%Y-%m-%dT%H:%M:%S.%f}Z  "
              f"LOS {t.utc_datetime():%Y-%m-%dT%H:%M:%S.%f}Z  "
              f"dur={duration:.1f}s  max_el={peak:.3f}")
        aos = None