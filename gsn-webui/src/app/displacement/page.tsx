import DisplacementChart from "@/components/displacement-chart";
import GaugeChart from "@/components/gauge-chart";
import WindChart from "@/components/wind-chart";
import React from "react";

export default function Page() {
  return (
    <div className="p-8 grid grid-cols-[5fr_2fr] gap-4 w-full">
      <div className="">
        <DisplacementChart />
      </div>

      <div className="">
        <GaugeChart value={0.5} text="10 °C" titel="Current Temperature"/>
      </div>
      
      <div className="">
        <WindChart />
      </div>

      <div className="">
        <GaugeChart value={0.3} text="8 km/h" titel="Current Windspeed"/>
      </div>
    </div>
  );
}
