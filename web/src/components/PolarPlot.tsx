import { elevationColor, elevationGradient } from '../lib/elevationColor.ts'
import { polarToXY } from '../lib/polar.ts'
import { compassPoint } from '../lib/compass.ts'
import { formatDuration, formatLocal, formatUtc } from '../lib/time.ts'
import type { SatellitePass } from '../types.ts'

interface PolarPlotProps {
    pass: SatellitePass | null
}

const SIZE = 240
const CENTER = SIZE / 2
const RADIUS = 96

const RINGS = [0, 30, 60]
const CARDINALS = [
    { label: 'N', azimuth: 0 },
    { label: 'E', azimuth: 90 },
    { label: 'S', azimuth: 180 },
    { label: 'W', azimuth: 270 }
]

function ringRadius(elevationDeg: number): number {
    return ((90 - elevationDeg) / 90) * RADIUS
}

export function PolarPlot({ pass }: PolarPlotProps) {
    const points = pass
        ? pass.track.map((p) => ({
            ...polarToXY(p.azimuthDeg, p.elevationDeg, RADIUS, CENTER, CENTER),
            source: p,
        }))
        : []

    const first = points[0]
    const last = points[points.length - 1]
    let peak = points[0]
    for (const point of points) {
        if (point.source.elevationDeg > peak.source.elevationDeg) peak = point
    }

    return (
        <div className="flex flex-col gap-2">
            <svg
                viewBox={`0 0 ${SIZE} ${SIZE}`}
                className="h-auto w-full"
                role="img"
                aria-label="Sky plot of the selected pass"
            >
                {RINGS.map((elevation) => (
                    <circle
                        key={elevation}
                        cx={CENTER}
                        cy={CENTER}
                        r={ringRadius(elevation)}
                        className="fill-none stroke-slate-300"
                    />
                ))}

                <line x1={CENTER} y1={CENTER - RADIUS} x2={CENTER} y2={CENTER + RADIUS} className="stroke-slate-200" />
                <line x1={CENTER - RADIUS} y1={CENTER} x2={CENTER + RADIUS} y2={CENTER} className="stroke-slate-200" />

                {[30, 60].map((elevation) => (
                    <text
                        key={elevation}
                        x={CENTER + 3}
                        y={CENTER - ringRadius(elevation) - 2}
                        className="fill-slate-400 text-[8px]"
                    >
                        {elevation}°
                    </text>
                ))}

                {CARDINALS.map(({ label, azimuth }) => {
                    const { x, y } = polarToXY(azimuth, 0, RADIUS + 14, CENTER, CENTER)
                    return (
                        <text
                            key={label}
                            x={x}
                            y={y}
                            textAnchor="middle"
                            dominantBaseline="middle"
                            className="fill-slate-600 text-[11px] font-semibold"
                        >
                            {label}
                        </text>
                    )
                })}

                {pass && (
                    <>
                        {points.slice(1).map((p, i) => {
                            const previous = points[i]
                            const elevation = (previous.source.elevationDeg + p.source.elevationDeg) / 2
                            return (
                                <line
                                    key={p.source.time}
                                    x1={previous.x}
                                    y1={previous.y}
                                    x2={p.x}
                                    y2={p.y}
                                    stroke={elevationColor(elevation)}
                                    strokeWidth={3}
                                    strokeLinecap="round"
                                />
                            )
                        })}

                        {points.map((p) => (
                            <circle key={p.source.time} cx={p.x} cy={p.y} r={3} className="fill-transparent">
                                <title>
                                    {formatLocal(p.source.time)} · az {p.source.azimuthDeg.toFixed(0)}° · el{' '}
                                    {p.source.elevationDeg.toFixed(0)}°
                                </title>
                            </circle>
                        ))}

                        <circle cx={first.x} cy={first.y} r={4} className="fill-emerald-500 stroke-white" />
                        <circle cx={last.x} cy={last.y} r={4} className="fill-red-500 stroke-white" />
                        <circle cx={peak.x} cy={peak.y} r={4} className="fill-amber-400 stroke-white" />

                        <text x={first.x + 6} y={first.y - 6} className="fill-emerald-700 text-[9px] font-semibold">
                            AOS
                        </text>
                        <text x={last.x + 6} y={last.y - 6} className="fill-red-700 text-[9px] font-semibold">
                            LOS
                        </text>
                    </>
                )}
            </svg>

            {pass && (
                <div>
                    <div className="h-2 rounded" style={{ background: elevationGradient() }} />
                    <div className="mt-0.5 flex justify-between text-[10px] text-slate-500">
                        <span>0°</span>
                        <span>elevation</span>
                        <span>90°</span>
                    </div>
                </div>
            )}

            {pass ? (
                <div className="text-xs text-slate-600">
                    <div className="font-medium text-slate-800">{formatLocal(pass.aos)}</div>
                    <div>
                        {formatUtc(pass.aos)} → {formatUtc(pass.los)} · {formatDuration(pass.durationSeconds)}
                    </div>
                    <div>
                        max {pass.maxElevationDeg.toFixed(1)}° · {compassPoint(pass.aosAzimuthDeg)} →{' '}
                        {compassPoint(pass.losAzimuthDeg)}
                    </div>
                </div>
            ) : (
                <p className="text-xs text-slate-500">Select a pass to see its path across the sky.</p>
            )}
        </div>
    )
}