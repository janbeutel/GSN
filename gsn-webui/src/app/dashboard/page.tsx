"use client";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Sensor, importSensorsFromFile } from "@/lib/sensor-utils";
import { useEffect, useMemo, useState } from "react";
import "leaflet/dist/leaflet.css";
import { activeSensorIcon, sensorIcon } from "@/components/sensor-map-icon";
import { MapContainer, TileLayer, Marker } from "react-leaflet";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { MapInternal } from "@/components/map-internal";
import TimeseriesChart from "@/components/timeseries-chart";
import SensorDetails from "@/components/sensor-details";
import { SearchList } from "@/components/search-list";
import UptimeTracker from "@/components/uptime-tracker";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { start } from "repl";
import e from "express";
import { endianness } from "os";
import { EDGE_RUNTIME_WEBPACK } from "next/dist/shared/lib/constants";



async function fetchChartSensors() {
  const response = await fetch(
    `http://localhost:3000/api/rest?`,
  );

  if (!response.ok) {
    throw new Error("Error fetching data");
  }

  const data = await response.json();
  // Access the `features` array from the `data` object
  const features = data.data.features;

  const sensors = Array.isArray(features) && features.length > 0
    ? features.map((feature) => {

        const { properties, geometry } = feature;
        const { latitude, longitude, stats } = properties;
        
        // Extract gps postition if exists
        let gpsPosition;
        if (geometry && geometry.coordinates && geometry.coordinates.length >= 2) {
          gpsPosition = geometry.coordinates.slice(0, 2).reverse();
        } else {
          gpsPosition = [47.264178, 11.343582];   
        }

        // Extract fields and their datatypes
        const fields = properties.fields.map(field => ({
          name: field.name,
          type: field.type,
          unit: field.unit
        }));
        
        const date = new Date(stats['end-datetime']);

        return {
          name: properties.vs_name,
          gpsPosition,
          latitude,
          longitude,
          fields,
          startTime: stats['start-datetime'],
          endTime: stats['end-datetime'],
          lastUpdate: date,
          // ...properties, // Include other properties as needed
        };
      })
    : [];

  return sensors;
}

export default function Dashboard() {

  const [sensors, setSensors] = useState([{
    gpsPosition: {
      lat: 51.505,
      lng: -0.09,
    }
  }]);

  const [selectedSensor, setSelectedSensor] = useState<Sensor>(0);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const fetchedSensors = await fetchChartSensors();
        setSensors(fetchedSensors);
        if (fetchedSensors.length > 0) {
          setSelectedSensor(fetchedSensors[0]);
        }
      } catch (error) {
        console.error('Error fetching sensors:', error);
      }
    };

    fetchData();
  }, []);
  
  return (
    <div className="flex flex-1 max-h-[calc(100vh-60px)]">
       <aside className="flex flex-col w-[350px] border-r bg-white border-t">
        <SearchList
          sensors={sensors}
          selectedSensor={selectedSensor}
          setSelectedSensor={setSelectedSensor}
        />
      </aside>
      <main className="flex flex-col flex-1 pl-5 pb-4">
        <ScrollArea className="px-4">
          <MapContainer
            center={sensors[0].gpsPosition}
            zoom={16}
            scrollWheelZoom={false}
            className="min-h-[300px] rounded-md shadow z-0 mt-4"
          >
            <TileLayer
              attribution="Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community"
              url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
            />
            {sensors
              .filter((s) => s.gpsPosition != undefined)
              .map((sensor, i) => (
                <Marker
                  key={i}
                  position={sensor.gpsPosition!}
                  icon={
                    selectedSensor == sensor ? activeSensorIcon : sensorIcon
                  }
                  eventHandlers={{
                    click: () => setSelectedSensor(sensor),
                  }}
                />
              ))}
            <MapInternal selectedSensor={selectedSensor} />
          </MapContainer>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle>Sensor Details</CardTitle>
            </CardHeader>
            <CardContent>
              <SensorDetails selectedSensor={selectedSensor} />
              <TimeseriesChart selectedSensor={selectedSensor} />
            </CardContent>
          </Card>
        </ScrollArea>
      </main>
    </div>
  );
}
