import React from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "./ui/card";
import Tracker from "./tracker";

interface Tracker {
  color: String;
  tooltip: string;
}

const data: Tracker[] = [
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "yellow", tooltip: "Degraded" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "yellow", tooltip: "Degraded" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "gray", tooltip: "Maintenance" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "yellow", tooltip: "Degraded" },
  { color: "#22c55e", tooltip: "Operational" },
];

const data1: Tracker[] = [
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#f43f5e", tooltip: "Downtime" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "gray", tooltip: "Maintenance" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
  { color: "#22c55e", tooltip: "Operational" },
];
interface UptimeTrackerProps extends React.HTMLAttributes<HTMLDivElement> {
  sendorId:string
}

export default function UptimeTracker(props: UptimeTrackerProps) {
  console.log(props.sendorId)
  return (
    <Card {...props}>
      <CardHeader className="pb-2">
        <CardTitle>Status</CardTitle>
        <div className="flex flex-row justify-between text-sm text-muted-foreground">
          <p>December 2023</p>
          <p>Uptime {props.sendorId=="6"?"96%":"92%"}</p>
        </div>
      </CardHeader>
      <CardContent>
        <Tracker data={props.sendorId=="6"?data:data1} />
      </CardContent>
    </Card>
  );
}
