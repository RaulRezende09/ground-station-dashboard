# Ground Station Dashboard

> 🚧 **Work in progress.** Building this in 4 incremental releases (Sep–Dec 2026), each one deployed before the next one starts. See [Roadmap](#roadmap) below for current status.

**Live demo:** https://ground-station-dashboard-9tc8.onrender.com/api/satellites/25544/position
![CI](https://github.com/RaulRezende09/ground-station-dashboard/actions/workflows/ci.yml/badge.svg)

A satellite tracking system built from scratch: given a satellite's orbital data (TLE), it computes where the satellite is right now, predicts upcoming passes over a ground station, and — by the final release — visualizes the Doppler shift of its radio signal.

## Why this project

This project is part of my path toward the aerospace industry (GNC / orbital mechanics). Rather than using a quick prototyping library, it's built on **Orekit** — the orbital mechanics library used in ESA-supplier and CNES contexts — to work with the same tools and reference frames used in the industry.

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Orekit 13.1.8, Caffeine (cache) |
| Frontend | React 18, TypeScript, Vite, Tailwind, MapLibre GL, uPlot |
| Data | [Celestrak](https://celestrak.org/) (TLE/GP data), [SatNOGS DB & Network](https://db.satnogs.org/) |
| Infra | Docker, GitHub Actions, Render |
| Validation | JUnit 5, cross-checked against `python-sgp4` |

## Roadmap

Each version is independently deployable and validated before the next one starts.

- [x] **v0.1 — The satellite exists** *(Sep 2026)*
  REST API that returns a satellite's current geodetic position (lat/lon/altitude) from its NORAD ID, propagated with SGP4 via Orekit.
- [ ] **v0.2 — Where it passes** *(Oct 2026)*
  Ground track and visibility footprint rendered on an interactive map (React + MapLibre).
- [ ] **v0.3 — When I can see it** *(Nov 2026)*
  Pass predictions (AOS/LOS, max elevation) for a user-defined ground station, with a polar plot.
- [ ] **v0.4 — Radio** *(Dec 2026)*
  Known transmitters (SatNOGS DB), Doppler curve per pass, and real observations from the SatNOGS Network.

## What's a TLE?

A Two-Line Element set (TLE) is a compact, decades-old format for describing a satellite's orbit — two lines of numbers encoding its epoch, inclination, eccentricity, mean anomaly, and drag term. It's enough for the SGP4 algorithm to propagate the orbit and compute the satellite's position at any nearby point in time. TLEs age: the position error grows roughly 1–3 km per day from the epoch, which is why this project caches them for only 2 hours instead of treating them as permanently valid.

## Reference frames

Getting from "SGP4 output" to "a dot on a map" isn't trivial — it involves three coordinate systems:

- **TEME** — inertial frame, what SGP4 returns directly (does *not* rotate with Earth)
- **ITRF** (ECEF) — Earth-fixed frame, where map coordinates live
- **Geodetic** — latitude/longitude/altitude over the WGS84 ellipsoid
  Orekit handles the full transformation chain between them.

## Validation

Propagation is cross-validated against [`python-sgp4`](https://pypi.org/project/sgp4/), an independent SGP4 implementation.

For ISS (ZARYA), NORAD 25544, propagated to `2026-09-19T12:00:00Z`: **0.7245617773954982 m** deviation in the TEME frame.

Reference script: `tools/reference.py`. Comparison test: `PropagationValidationTest`.

## Data sources & attribution

- Orbital elements: [Celestrak](https://celestrak.org/) — GP/TLE data, mirrored via a scheduled GitHub Action (see Architecture note below), not queried per-request
- Transmitter and observation data: [SatNOGS DB](https://db.satnogs.org/) / [SatNOGS Network](https://network.satnogs.org/)
## Running locally

### With Docker (matches production)

```bash
git clone https://github.com/RaulRezende09/ground-station-dashboard.git
cd ground-station-dashboard
docker build -t ground-station-dashboard .
docker run -p 8080:8080 ground-station-dashboard
```

The API will be available at `http://localhost:8080`.

### Without Docker

Requires JDK 21 and the [Orekit reference data](https://gitlab.orekit.org/orekit/orekit-data) extracted into an `orekit-data/` folder at the project root.

```bash
git clone https://github.com/RaulRezende09/ground-station-dashboard.git
cd ground-station-dashboard
./mvnw spring-boot:run
```

### Try it

```bash
curl http://localhost:8080/api/satellites/25544/position
```

## Architecture note

Satellite data is fetched from a small TLE mirror (`data/tle-mirror.txt`), refreshed every 2 hours by a scheduled GitHub Action, rather than calling Celestrak directly from the deployed service. This was a deliberate fix: Celestrak blocks outbound traffic from some free-tier cloud IP ranges (Render's included), and GitHub Actions runners aren't affected. The mirror currently covers ISS, NOAA-15/18/19, and NanoSatC-BR1 — the same set planned for the v0.2 satellite selector.

## License

MIT
 