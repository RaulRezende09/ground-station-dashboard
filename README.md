# Ground Station Dashboard

> 🚧 **Work in progress.** Building this in 4 incremental releases (Sep–Dec 2026), each one deployed before the next one starts. See [Roadmap](#roadmap) below for current status.

A satellite tracking system built from scratch: given a satellite's orbital data (TLE), it computes where the satellite is right now, predicts upcoming passes over a ground station, and — by the final release — visualizes the Doppler shift of its radio signal.

## Why this project

This project is part of my path toward the aerospace industry (GNC / orbital mechanics). Rather than using a quick prototyping library, it's built on **Orekit** — the orbital mechanics library used in ESA-supplier and CNES contexts — to work with the same tools and reference frames used in the industry.

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Orekit 13.1.8, Caffeine (cache) |
| Frontend | React 18, TypeScript, Vite, Tailwind, MapLibre GL, uPlot |
| Data | [Celestrak](https://celestrak.org/) (TLE/GP data), [SatNOGS DB & Network](https://db.satnogs.org/) |
| Infra | Docker, GitHub Actions, Render/Fly.io |
| Validation | JUnit 5, cross-checked against `python-sgp4` |

## Roadmap

Each version is independently deployable and validated before the next one starts.

- [ ] **v0.1 — The satellite exists** *(Sep 2026)*
  REST API that returns a satellite's current geodetic position (lat/lon/altitude) from its NORAD ID, propagated with SGP4 via Orekit.
- [ ] **v0.2 — Where it passes** *(Oct 2026)*
  Ground track and visibility footprint rendered on an interactive map (React + MapLibre).
- [ ] **v0.3 — When I can see it** *(Nov 2026)*
  Pass predictions (AOS/LOS, max elevation) for a user-defined ground station, with a polar plot.
- [ ] **v0.4 — Radio** *(Dec 2026)*
  Known transmitters (SatNOGS DB), Doppler curve per pass, and real observations from the SatNOGS Network.

## Reference frames

Getting from "SGP4 output" to "a dot on a map" isn't trivial — it involves three coordinate systems:

- **TEME** — inertial frame, what SGP4 returns directly (does *not* rotate with Earth)
- **ITRF** (ECEF) — Earth-fixed frame, where map coordinates live
- **Geodetic** — latitude/longitude/altitude over the WGS84 ellipsoid

Orekit handles the full transformation chain between them.

## Validation

Every propagation is cross-checked against an independent implementation (`python-sgp4`) rather than trusted on faith. Measured deviation will be documented here once v0.1 is deployed.

## Data sources & attribution

- Orbital elements: [Celestrak](https://celestrak.org/) — GP/TLE data, cached (not queried per-request)
- Transmitter and observation data: [SatNOGS DB](https://db.satnogs.org/) / [SatNOGS Network](https://network.satnogs.org/)

## Running locally

*Coming soon — instructions will be added once v0.1 has a working Docker setup.*

## License

MIT
