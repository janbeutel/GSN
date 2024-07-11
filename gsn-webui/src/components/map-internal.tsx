import { Sensor } from "@/lib/sensor-utils";
import { useMap } from "react-leaflet/hooks";

export function MapInternal({ selectedSensor }: { selectedSensor: Sensor }) {
  const map = useMap();
  selectedSensor.gpsPosition && map.setView(selectedSensor.gpsPosition);
  return null;
}
