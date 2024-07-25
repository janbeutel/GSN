import http from 'node:http';
import querystring from 'node:querystring';

export const dynamic = "force-dynamic";

const CLIENT_ID = 'enter client id here';
const CLIENT_SECRET = 'enter client secret here';
const TOKEN_URL = 'http://ifi-walker.uibk.ac.at:9000/ws/oauth2/token';
const SENSORS_URL = 'http://ifi-walker.uibk.ac.at:9000/ws/api/sensors';

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const sensorName = searchParams.get('sensorName') || '';

    const accessToken = await getAccessToken();

    if (accessToken) {

      const options = {
        hostname: 'ifi-walker.uibk.ac.at',
        port: 9000,
        path: `/ws/api/sensors`,
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`
        }
      };

      const responsePromise = await new Promise<Response>((resolve, reject) => {
        const req = http.request(options, (res) => {
          let data = '';

          res.on('data', (chunk) => {
            data += chunk;
          });

          res.on('end', () => {
            if (res.statusCode && res.statusCode >= 200 && res.statusCode < 300) {

              const responseData = { data: JSON.parse(data) };
              const responseObj = new Response(JSON.stringify(responseData), {
                status: res.statusCode,
                headers: { 'Content-Type': 'application/json' },
              });
              resolve(responseObj);
            } else {
              const errorResponse = new Response(
                JSON.stringify({ error: `Failed to fetch data: ${res.statusCode}` }),
                {
                  status: res.statusCode,
                  headers: { 'Content-Type': 'application/json' },
                }
              );
              reject(errorResponse);
            }
          });
        });

        req.on('error', (error) => {
          const errorResponse = new Response(
            JSON.stringify({ error: 'Internal server error' }),
            {
              status: 500,
              headers: { 'Content-Type': 'application/json' },
            }
          );
          reject(errorResponse);
        });

        req.end();
      });

      return responsePromise;

    } else {
      const errorResponse = new Response(
        JSON.stringify({ error: 'Failed to obtain access token' }),
        {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        }
      );
      return errorResponse;
    }


  } catch (error) {
    console.error('Error occurred:', error);
    const errorResponse = new Response(
      JSON.stringify({ error: 'Internal server error' }),
      {
        status: 500,
        headers: { 'Content-Type': 'application/json' },
      }
    );
    return errorResponse;
  }
}

async function getAccessToken(): Promise<string | null> {
  try {
    const payload = querystring.stringify({
      grant_type: 'client_credentials',
      client_id: CLIENT_ID,
      client_secret: CLIENT_SECRET,
    });

    const options = {
      hostname: 'ifi-walker.uibk.ac.at',
      port: 9000,
      path: '/ws/oauth2/token',
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': payload.length,
      },
    };

    const tokenPromise = await new Promise<string>((resolve, reject) => {
      const req = http.request(options, (res) => {
        let data = '';

        res.on('data', (chunk) => {
          data += chunk;
        });

        res.on('end', () => {
          if (res.statusCode && res.statusCode === 200) {
            const responseData = JSON.parse(data);
            const accessToken = responseData.access_token;
            resolve(accessToken);
          } else {
            reject(`Failed to obtain access token: ${res.statusCode}`);
          }
        });
      });

      req.on('error', (error) => {
        reject(`Error obtaining access token: ${error.message}`);
      });

      req.write(payload);
      req.end();
    });

    return tokenPromise;
  } catch (error) {
    console.error('Error obtaining access token:', error);
    return null;
  }
}