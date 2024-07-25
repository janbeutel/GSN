import {
  CalendarIcon,
  EnvelopeClosedIcon,
  FaceIcon,
  GearIcon,
  PersonIcon,
  RocketIcon,
} from "@radix-ui/react-icons";

import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
  CommandShortcut,
} from "@/components/ui/command";
import { Sensor } from "@/lib/sensor-utils";
import SensorCard from "./sensor-card";

export function SearchList({
  sensors,
  selectedSensor,
  setSelectedSensor,
}: {
  sensors: Sensor[];
  selectedSensor: Sensor;
  setSelectedSensor: (s: Sensor) => void;
}) {
  return (
    <Command>
      <CommandInput className="py-2" placeholder="Search..." />
      <CommandList className="max-h-[calc(100vh-105px)]">
        <CommandEmpty>No results found.</CommandEmpty>
        <CommandGroup>
          {sensors.map((sensor, i) => (
            <CommandItem
              key={i}
              value={sensor.position}
              onSelect={() => setSelectedSensor(sensor)}
              className={
                selectedSensor.position == sensor.position ? "bg-slate-100" : ""
              }
            >
              <SensorCard sensor={sensor} />
            </CommandItem>
          ))}
        </CommandGroup>
        <CommandSeparator />
      </CommandList>
    </Command>
  );
}
