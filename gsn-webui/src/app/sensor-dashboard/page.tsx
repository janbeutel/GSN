"use client";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Sensor, importSensorsFromFile } from "@/lib/sensor-utils";
import { useMemo, useState } from "react";
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

export default function Dashboard() {
  const sensors = useMemo(() => importSensorsFromFile(), []);

  const [selectedSensor, setSelectedSensor] = useState<Sensor>(sensors[0]);

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
            className="min-h-[500px] rounded-md shadow z-0 mt-4"
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
          <UptimeTracker className="my-4" sendorId={selectedSensor.position} />
          <Card>
            <CardHeader className="pb-2">
              <CardTitle>Sensor Details</CardTitle>
            </CardHeader>
            <CardContent>
              <SensorDetails selectedSensor={selectedSensor} />
              <Tabs defaultValue="graph">
                <div className="flex justify-between items-center pt-4">
                  <h2 className="font-semibold leading-none tracking-tight">
                    Sensor Data
                  </h2>
                  <TabsList>
                    <TabsTrigger value="graph">Graph</TabsTrigger>
                    <TabsTrigger value="table">Table</TabsTrigger>
                  </TabsList>
                </div>
                <TabsContent value="graph">
                  <TimeseriesChart />
                </TabsContent>
                <TabsContent value="table">
                  <div className="mt-4">
                    <table className="w-full text-sm text-gray-500">
                      <thead>
                        <tr>
                          <th className="px-4 py-2 border-b-2">Time</th>
                          <th className="px-4 py-2 border-b-2">Voltage</th>
                          <th className="px-4 py-2 border-b-2">Temperature</th>
                          <th className="px-4 py-2 border-b-2">Humidity</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td className="px-4 py-2 border-b">10:30 AM</td>
                          <td className="px-4 py-2 border-b">3.3V</td>
                          <td className="px-4 py-2 border-b">25°C</td>
                          <td className="px-4 py-2 border-b">45%</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>
        </ScrollArea>
      </main>
    </div>
  );
}
