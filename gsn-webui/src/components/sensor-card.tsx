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
          <h3 className="text-md font-semibold">Sensor {sensor.name}</h3>
        </div>
      </div>
    </div>
  );
}
