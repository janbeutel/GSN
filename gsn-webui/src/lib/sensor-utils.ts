export type Sensor = {
  id: string;
  name: string;
  position: string;
  gpsPosition: LatLng | undefined;
  lastUpdate: Date | undefined;
  voltage: string | undefined;
  temperature: number | undefined;
  humidity: number | undefined;
  uptime: number | undefined;
};

import { LatLng } from "leaflet";
import sensors from "../../data/sensors.json";

export function importSensorsFromFile(): Sensor[] {
  return sensors["network"]["sensornodes"]["sensornode"].map((sensor) => ({
    id: sensor["_node_id"],
    name: "",
    position: sensor["_position"],
    gpsPosition:
      sensor["_latitude"] && sensor["_longitude"]
        ? new LatLng(Number(sensor["_latitude"]), Number(sensor["_longitude"]))
        : undefined,
    voltage: sensor["_vsys"],
    lastUpdate: sensor["_timestamp"]
      ? new Date(Number(sensor["_timestamp"]))
      : undefined,

    temperature: Number(sensor["_temperature"]),
    humidity: Number(sensor["_humidity"]),
    uptime: Number(sensor["_uptime"]),
  }));
}
