import L from "leaflet";

const sensorIcon = new L.Icon({
  iconUrl: "sensor.png",
  iconSize: new L.Point(30, 30),
  className: "rounded-full bg-sky-100 border-2 border-sky-400",
});


const activeSensorIcon = new L.Icon({
  iconUrl: "sensor.png",
  iconSize: new L.Point(30, 30),
  className: "rounded-full bg-lime-100 border-2 border-lime-400",
});

export { sensorIcon, activeSensorIcon };
