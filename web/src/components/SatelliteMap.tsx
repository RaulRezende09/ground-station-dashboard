import { useEffect, useRef } from 'react';
import * as maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';

export function SatelliteMap() {
    const mapContainer = useRef<HTMLDivElement>(null);
    const mapRef = useRef<maplibregl.Map | null>(null);

    useEffect(() => {
        if (!mapContainer.current || mapRef.current) return;

        mapRef.current = new maplibregl.Map({
            container: mapContainer.current,
            style: 'https://tiles.openfreemap.org/styles/liberty',
            center: [0, 0],
            zoom: 1.5,
        });

        return () => {
            mapRef.current?.remove();
            mapRef.current = null;
        };
    }, []);

    return <div ref={mapContainer} className="h-screen w-screen" />;
}