import { getOptimalResolution } from "@/lib/chart-utils";
export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const startTime = Number(searchParams.get("startTime"));
  const endTime = Number(searchParams.get("endTime"));
  const resolution =
    searchParams.get("resolution") ?? getOptimalResolution(startTime, endTime);
  const timeQuery =
    startTime && endTime
      ? `AND generation_time BETWEEN '${new Date(
          startTime,
        ).toJSON()}' AND '${new Date(endTime).toJSON()}'`
      : "";
      const dataRow = searchParams.get("dataRow");

  const query = `SELECT generation_time ts, avg(temperature_nearsurface_t1) from matterhorn_temperature_rock WHERE position=6 ${timeQuery} SAMPLE BY ${resolution} order by ts asc`;
  // const query = `SELECT generation_time ts, ${dataRow} FROM matterhorn_temperature_rock WHERE position=6 ${timeQuery} SAMPLE BY ${resolution} order by ts asc`;
  // const query = `SELECT generation_time ts, ${dataRow} FROM matterhorn_temperature_rock WHERE position=6 AND generation_time> dateadd('d', -100, now()) order by ts asc`;

  const HOST = "http://lochmatter.uibk.ac.at:9000";

  const url = `${HOST}/exec?query=${encodeURIComponent(query)}`;
  return await fetch(url);
}
