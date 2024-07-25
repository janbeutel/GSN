import { getOptimalResolution, getResolution } from "@/lib/chart-utils";
import { Pool } from 'pg';

export const dynamic = "force-dynamic";

const pool = new Pool({
  user: 'enter username here',
  host: 'lochmatter.uibk.ac.at',
  database: 'enter database name here',
  password: 'enter password here',
  port: 5432,
});

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    
    var startTime = Number(searchParams.get("startTime"));
    var endTime = Number(searchParams.get("endTime"));
    var resolution = 0;

    if(!searchParams.get("resolution")){
      resolution = 1000 * 60 * 60 * 24 * 28;
    } else if (searchParams.get("resolution") == 'dynamic'){
      resolution = getOptimalResolution(startTime, endTime);
    } else {
      resolution = getResolution(searchParams.get("resolution") || "");
    }

    if(startTime >= 0 && endTime >= 0){

      const timeQuery =
      startTime && endTime
        ? `AND timed > ${Math.floor(startTime)} AND timed < ${Math.floor(endTime)}`
        : "";

      const query = `SELECT time_bucket(${resolution}, timed) AS monthly, avg(displacement_dx1::numeric) as avg
                      FROM matterhorn_displacement WHERE position=1 ${timeQuery}
                      GROUP BY monthly
                      ORDER BY monthly ASC;`;

      const { rows } = await pool.query(query);

      // Create an object containing the rows data and any other relevant data
      const responseData = {
        rows
      };
    
      // Create a new response object with JSON data and status 200
      const response = new Response(JSON.stringify(responseData), {
        status: 200,
        headers: { 'Content-Type': 'application/json' } // Set appropriate content type
      });

      return response;

    } else {
      const query = `SELECT time_bucket(${resolution}, timed) AS monthly, avg(displacement_dx1::numeric) as avg
                      FROM matterhorn_displacement WHERE position=1
                      GROUP BY monthly
                      ORDER BY monthly ASC;`;

      const { rows } = await pool.query(query);

      // Create an object containing the rows data and any other relevant data
      const responseData = {
        rows
      };
    
      // Create a new response object with JSON data and status 200
      const response = new Response(JSON.stringify(responseData), {
        status: 200,
        headers: { 'Content-Type': 'application/json' } // Set appropriate content type
      });

      return response;

    }

  } catch (error) {
    // Handle errors appropriately
    console.error("Error occurred:", error);
    
    // Create a new response object for error with status 500
    const errorResponse = new Response(JSON.stringify({ error: "Internal server error" }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' } // Set appropriate content type
    });

    return errorResponse;
  }
}