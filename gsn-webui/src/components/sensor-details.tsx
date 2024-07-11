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
        <div className="flex items-center">
          <Icons.position className="h-6 w-6 text-slate-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Sensor Position</div>
            <div className="text-lg">{selectedSensor.position}</div>
          </div>
        </div>

        <div className="flex items-center">
          <Icons.sensorId className="h-6 w-6 text-slate-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Current Sensor Id</div>
            <div className="text-lg">{selectedSensor.id}</div>
          </div>
        </div>
      </div>
      <div className="flex gap-8 pt-6 justify-between">
        <div className="flex items-center">
          <Icons.temperature className="h-6 w-6 text-red-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Temperature</div>
            <div className="text-lg">{selectedSensor.temperature} °C</div>
          </div>
        </div>

        <div className="flex items-center">
          <Icons.humidity className="h-6 w-6 text-sky-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Humidity</div>
            <div className="text-lg">{selectedSensor.humidity} %</div>
          </div>
        </div>

        <div className="flex items-center">
          <Icons.battery className="h-6 w-6 text-lime-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Battery Voltage</div>
            <div className="text-lg">{selectedSensor.voltage} V</div>
          </div>
        </div>

        <div className="flex items-center">
          <Icons.lastUpdate className="h-6 w-6 text-slate-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Last Update</div>
            <div className="text-lg">
              {selectedSensor.lastUpdate?.toLocaleDateString().substring(0, 5)}{" "}
              {selectedSensor.lastUpdate?.toLocaleTimeString()}
            </div>
          </div>
        </div>

        <div className="flex items-center">
          <Icons.uptime className="h-6 w-6 text-slate-500 mr-2" />
          <div>
            <div className="text-sm text-gray-500">Uptime</div>
            <div className="text-lg">{selectedSensor.uptime} s</div>
          </div>
        </div>
      </div>
    </>
  );
}
