### Prerequisites

- Node.js (recommended version: [specify version])
- Git (for cloning the repository)

### Starting the GSN Frontend Application

1. Install the necessary Node.js dependencies:

```sh
npm install
npm install -g pnpm
npm install pg
```

2. Enter database credentials in `/src/app/api/postgres/route.ts` and `/src/app/api/timeseries/route.ts`
```js
const pool = new Pool({
   user: 'enter username here',
   host: 'lochmatter.uibk.ac.at',
   database: 'enter database name here',
   password: 'enter password here',
   port: 5432,
});
```

4. Enter gsn-services credentials in `/src/app/api/postgres/route.ts`

```js
const CLIENT_ID = 'enter client id here';
const CLIENT_SECRET = 'enter client secret here';
const TOKEN_URL = 'http://ifi-walker.uibk.ac.at:9000/ws/oauth2/token';
const SENSORS_URL = 'http://ifi-walker.uibk.ac.at:9000/ws/api/sensors';
```

3. Start the Next.js application:

```sh
npm run dev
```

This will start the GSN Frontend application in development mode.

3. Open a web browser and navigate to `http://localhost:3000` to view the application (adjust the port if your configuration is different).
