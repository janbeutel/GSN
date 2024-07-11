import DisplacementChart from "@/components/displacement-chart";
import TemperatureRockChart from "@/components/temperature-rock-chart";

export default function Home() {
  return (
    <ul>
      <DisplacementChart />;
      <TemperatureRockChart />;
    </ul>
  )
}
