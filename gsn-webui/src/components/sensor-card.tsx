import React from "react";
import { Icons } from "./icons";
import { Sensor } from "@/lib/sensor-utils";
import { Badge } from "./ui/badge";

export default function SensorCard({ sensor }: { sensor: Sensor }) {
  return (
    <div className="flex justify-between items-center w-full p-2">
      <div className="flex gap-4 items-center">
        <Icons.sensor className="h-5 w-5" />
        <div className="flex flex-col">
          <h3 className="text-md font-semibold">Sensor {sensor.position}</h3>

          {sensor.lastUpdate != undefined && (
            <div className="flex flex-col">
              <p className="text-sm text-slate-300">Last Update</p>
              <p className="text-sm text-slate-500">
                {sensor.lastUpdate?.toLocaleDateString().substring(0, 5)}{" "}
                {sensor.lastUpdate?.toLocaleTimeString()}
              </p>
            </div>
          )}
        </div>
      </div>
      {sensor.lastUpdate != undefined ? (
        <Badge className="text-sm text-lime-500" variant="outline">
          Active
        </Badge>
      ) : (
        <Badge className="text-sm text-red-500" variant="outline">
          Inactive
        </Badge>
      )}
    </div>
  );
}
