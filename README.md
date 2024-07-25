# README

## Old Documentation

You can find the old GSN documentation of the project at [wiki](https://github.com/LSIR/gsn/wiki).

### Quick demo with Vagrant

On any computer that can run [VirtualBox](https://www.virtualbox.org/) (or any other supported virtual machine provider), install [Vagrant](https://www.vagrantup.com/). To use the `Vagrantfile` provided, go into the folder with the `Vagrantfile` and type `vagrant up` in your terminal. To connect to the virtual box instance, use `vagrant ssh`. When connected the GIT Repository can be cloned onto the VM. If you want to use Visual Studio Code to connect to the running VM use `vagrant ssh-config` and copy the information to ssh config by pressing `F1` or `STRG + Shift + P` in VSCode and choose `Remote-SSH: Open SSH- Configuration file` (Remote-SSH Plugin must be installed in VSCode). 

### Compiling and Running

Prerequisites to compile GSN:

* gsn-core and gsn-extra
  * sbt 0.13.18+
  * Java JDK 11
* gsn-tools and gsn-services
  * sbt 0.13.18+
  * Java JDK 11
  * Scala 2.12
* gsn-webui
  * Node.js

If all prequisites are installed either on your local machine or on the VM using vagrant, change to the repository folder and type `sbt`. This should install all required dependencies. In order to compile gsn-core and gsn-services, switch to the corresponding workspace using the command `project core` or `project services` while using sbt. Then `compile` can be used. Compilation for gsn-core and gsn-services can also done by just using `sbt` in the main directory of the repository and the command `compile`. However to run the individual projects the workspace has to be switched using `project core` or `project services`. To run gsn-core use `restart` in gsn-core workspace. To run gsn-services use `run` in gsn-services workspace. Since some functionality of gsn-services depend on a running gsn-core, gsn-core should be started first and then gsn-services can be started in a separate terminal. Gsn-services should then be available at `http://localhost:9000/ws` and the following credentials can be used:
 * username: root@localhost
 * password: changeme

Important commands:
* clean: remove generated files
* compile: compiles the modules
* package: build jar packages
* project \[core|tools|services]: select a specific projet

### Deployment

For an easy deployment, debian packages can be created for gsn-core and gsn-services. Therefore the code needs to be compiled following the steps of "Compiling and Running". When compiled, `debian:packageBin` can be used in the corresponding workspace gsn-core or gsn-services. This created an installable .deb file in the target folder, which can be moved to a server and installed by `sudo dpkg -i filename.deb`. To start or stop an installed instance following commands can be used:
 * `sudo systemctl start gsn-core.service/gsn-services.service`
 * `sudo systemctl stop gsn-core.service/gsn-services.service`
 * `sudo systemctl restart gsn-core.service/gsn-services.service`

#### Loading your first virtual sensor

To load a virtual sensor into GSN, you need to move its description file (.xml) into the `virtual-sensors` directory.
This directory contains a set of samples that can be used.

You can start by loading the MultiFormatTemperatureHandler virtual sensor (`virtual-sensors/samples/multiFormatSample.xml`).
This virtual sensor generates random values without the need of an actual physical sensor.


#### Starting the GSN Frontend Application

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


