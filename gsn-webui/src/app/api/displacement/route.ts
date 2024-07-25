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

  const query = `SELECT generation_time ts, avg(displacement_dx1) from matterhorn_displacement WHERE position=6 ${timeQuery} SAMPLE BY ${resolution} order by ts asc`;

  const HOST = "http://lochmatter.uibk.ac.at:9000";

  const url = `${HOST}/exec?query=${encodeURIComponent(query)}`;
  return await fetch(url);
}
