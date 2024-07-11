import { Sensor } from "@/lib/sensor-utils";
import React from "react";
import { Icons } from "./icons";

export default function SensorDetails({
  selectedSensor,
}: {
  selectedSensor: Sensor;
}) {
  return (
    <>
      <div className="flex gap-10 pt-4">
        <div className="flex items-center flex-1">
          <Icons.sensorId className="h-6 w-6 text-slate-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Current Sensor Id</div>
            <div className="text-sm">{selectedSensor.name}</div>
          </div>
        </div>
        <div className="flex items-center flex-1">
          <Icons.position className="h-6 w-6 text-slate-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Sensor Latitude</div>
            <div className="text-sm">{selectedSensor.latitude}</div>
          </div>
        </div>
        <div className="flex items-center flex-1">
          <Icons.position className="h-6 w-6 text-slate-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Sensor Longitude</div>
            <div className="text-sm">{selectedSensor.longitude}</div>
          </div>
        </div>
        <div className="flex items-center flex-1">
          <Icons.lastUpdate className="h-6 w-6 text-slate-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Last Update</div>
            <div className="text-sm">
              {selectedSensor.lastUpdate?.toLocaleDateString().substring(0, 5)}{" "}
              {selectedSensor.lastUpdate?.toLocaleTimeString()}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
