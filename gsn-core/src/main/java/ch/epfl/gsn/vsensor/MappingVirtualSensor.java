package ch.epfl.gsn.vsensor;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.beans.DeviceMappings;
import ch.epfl.gsn.beans.SensorMappings;
import ch.epfl.gsn.beans.GeoMapping;
import ch.epfl.gsn.beans.PositionMap;
import ch.epfl.gsn.beans.PositionMappings;
import ch.epfl.gsn.beans.SensorMap;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.storage.DataEnumerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MappingVirtualSensor extends BridgeVirtualSensorPermasense to provide mapping
 * functionality between physical and virtual sensors.
 */
public class MappingVirtualSensor extends BridgeVirtualSensorPermasense {

	private static final transient Logger logger = LoggerFactory.getLogger(MappingVirtualSensor.class);

	private Map<Integer, PositionMappings> positionMappings;
	private Map<Integer, SensorMappings> sensorMappings;
	private ArrayList<GeoMapping> geoMappings;

	/**
	 * initializes the positionMappings, sensorMappings, and geoMappings fields.
	 * 
	 * It loads the latest mapping data from the database for this virtual sensor,
	 * by querying for the max timed row.
	 * 
	 * It then unmarshalls the byte array from the latest row into a DeviceMappings
	 * object, and extracts the positionMappings, sensorMappings, and geoMappings
	 * into the fields.
	 * 
	 * If no data is found, it just initializes empty maps and list.
	 */
	@Override
	public boolean initialize() {
		positionMappings = new Hashtable<Integer, PositionMappings>();
		sensorMappings = new Hashtable<Integer, SensorMappings>();
		geoMappings = new ArrayList<GeoMapping>();

		// load last available mapping information
		String virtual_sensor_name = getVirtualSensorConfiguration().getName();
		StringBuilder query = new StringBuilder("select * from ").append(virtual_sensor_name)
				.append(" where timed = (select max(timed) from ").append(virtual_sensor_name)
				.append(") order by PK desc limit 1");
		ArrayList<StreamElement> latestvalues = new ArrayList<StreamElement>();
		try {
			DataEnumerator result = Main.getStorage(getVirtualSensorConfiguration()).executeQuery(query, false);
			while (result.hasMoreElements()) {
				latestvalues.add(result.nextElement());
			}
		} catch (Exception e) {
			logger.error("ERROR IN EXECUTING, query: " + query);
			logger.error(e.getMessage(), e);
		}
		if (latestvalues.size() > 0) {
			IBindingFactory bfact;
			try {
				Serializable s = latestvalues.get(latestvalues.size() - 1).getData()[0];
				if (s instanceof byte[]) {
					bfact = BindingDirectory.getFactory(DeviceMappings.class);
					IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
					DeviceMappings lastMapping = (DeviceMappings) uctx
							.unmarshalDocument(new ByteArrayInputStream((byte[]) s), "UTF-8");
					for (PositionMappings posMap : lastMapping.positionMappings) {
						positionMappings.put(posMap.position, posMap);
					}
					for (SensorMappings sensorMap : lastMapping.sensorMappings) {
						sensorMappings.put(sensorMap.position, sensorMap);
					}
					for (GeoMapping geoMap : lastMapping.geoMappings) {
						geoMappings.add(geoMap);
					}

					logger.info("successfully imported last mappings.");
				} else {
					logger.warn("data type was " + s.getClass().getCanonicalName());
				}
			} catch (JiBXException e) {
				logger.error("unmarshall did fail: " + e);
			}
		} else {
			logger.info("no old mappings found.");
		}

		return true;
	}

	/**
	 * Handles incoming data from input streams to map device data.
	 * 
	 * This maps incoming data from the "position_mapping", "sensor_mapping",
	 * and "geo_mapping" input streams to the positionMappings, sensorMappings,
	 * and geoMappings objects to track device and sensor metadata.
	 *
	 * @param inputStreamName The name of the input stream for the incoming data
	 * @param data            The StreamElement data object with the incoming data
	 */
	@Override
	public void dataAvailable(String inputStreamName, StreamElement data) {
		int position = (Integer) data.getData("position");

		if (inputStreamName.equalsIgnoreCase("position_mapping")) {
			if (!positionMappings.containsKey(position)) {
				positionMappings.put(position, new PositionMappings(position, new ArrayList<PositionMap>()));
			}
			positionMappings.get(position)
					.add(new PositionMap((Integer) data.getData("device_id"), (Short) data.getData("device_type"),
							(Long) data.getData("begin"), (Long) data.getData("end"),
							(String) data.getData("comment")));
		}

		if (inputStreamName.equalsIgnoreCase("sensor_mapping")) {
			if (!sensorMappings.containsKey(position)) {
				sensorMappings.put(position, new SensorMappings(position, new ArrayList<SensorMap>()));
			}
			sensorMappings.get(position)
					.add(new SensorMap((Long) data.getData("begin"), (Long) data.getData("end"),
							(String) data.getData("sensortype"), (Long) data.getData("sensortype_args"),
							(String) data.getData("comment")));
		}

		if (inputStreamName.equalsIgnoreCase("geo_mapping")) {
			GeoMapping geoMap = new GeoMapping((Integer) data.getData("position"), (Double) data.getData("longitude"),
					(Double) data.getData("latitude"), (Double) data.getData("altitude"),
					(String) data.getData("comment"));
			Iterator<GeoMapping> iter = geoMappings.iterator();
			while (iter.hasNext()) {
				GeoMapping map = iter.next();
				if (map.position.compareTo(geoMap.position) == 0) {
					iter.remove();
				}
			}

			geoMappings.add(geoMap);
		}

		try {
			generateData(data.getTimeStamp());
		} catch (Exception e) {
			logger.error(data.toString(), e);
		}
	}

	/**
	 * Generates data from the position, sensor, and geo mappings.
	 *
	 * This is done synchronously when new mapping data is received to keep
	 * the output stream up to date.
	 *
	 * @param timestamp The timestamp to use for the generated data
	 */
	synchronized void generateData(Long timestamp) {
		DeviceMappings map = new DeviceMappings(new ArrayList<PositionMappings>(positionMappings.values()),
				new ArrayList<SensorMappings>(sensorMappings.values()), geoMappings);

		try {
			IBindingFactory bfact = BindingDirectory.getFactory(DeviceMappings.class);
			IMarshallingContext mctx = bfact.createMarshallingContext();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			mctx.marshalDocument(map, "UTF-8", null, baos);
			StreamElement se = new StreamElement(getVirtualSensorConfiguration().getOutputStructure(),
					new Serializable[] { baos.toString().getBytes() }, timestamp);
			dataProduced(se);
		} catch (JiBXException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}
}