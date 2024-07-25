import React from "react";
import { HoverCard, HoverCardContent, HoverCardTrigger } from "./ui/hover-card";

interface TrackerProps extends React.HTMLAttributes<HTMLDivElement> {
  data: any[];
}

export default function Tracker({ data, ...props }: TrackerProps) {
  return (
    <div className={props.className}>
      <div className="flex gap-1 justify-between">
        {data.map((item, index) => (
          <HoverCard key={index}>
            <HoverCardTrigger
              className="rounded w-3 h-6 cursor-pointer"
              style={{ backgroundColor: item.color }}
            ></HoverCardTrigger>
            <HoverCardContent>
              {new Date().toLocaleDateString().substring(0, 4)}{" "}
              {new Date().toLocaleTimeString()}: {item.tooltip}
            </HoverCardContent>
          </HoverCard>
        ))}
      </div>
    </div>
  );
}
