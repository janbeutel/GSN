package ch.epfl.gsn.wrappers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

//import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.h2.tools.Server;

import com.vividsolutions.jts.geom.Coordinate;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.InputInfo;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.storage.StorageManager;
import ch.epfl.gsn.vsensor.permasense.Converter;

public class DataMappingWrapper extends AbstractWrapper {

	private final static transient Logger logger = LoggerFactory.getLogger(DataMappingWrapper.class);

	private static final short POSITION_MAPPING = 1;
	private static final short GEO_MAPPING = 2;
	private static final short SENSOR_MAPPING = 3;

	private static HashMap<String, Mappings> deployments = new HashMap<String, Mappings>();
	private static Server web;
	private static Connection h2DBconn;
	private static Map<String, Converter> converterList = new TreeMap<String, Converter>();
	private static boolean sensortype_args_available = false;

	private short mappingType;
	private String mappingName;
	private DataField[] outputStructure;
	private String deployment;

	private final static DataField[] positionStructure = new DataField[] {
			new DataField("DEVICE_ID", "INTEGER"),
			new DataField("DEVICE_TYPE", "SMALLINT"),
			new DataField("BEGIN", "BIGINT"),
			new DataField("END", "BIGINT"),
			new DataField("POSITION", "INTEGER"),
			new DataField("COMMENT", "VARCHAR(255)")
	};

	private final static DataField[] geoStructure = new DataField[] {
			new DataField("POSITION", "INTEGER"),
			new DataField("LONGITUDE", "DOUBLE"),
			new DataField("LATITUDE", "DOUBLE"),
			new DataField("ALTITUDE", "DOUBLE"),
			new DataField("COMMENT", "VARCHAR(255)")
	};

	private final static DataField[] sensorStructure = new DataField[] {
			new DataField("POSITION", "INTEGER"),
			new DataField("BEGIN", "BIGINT"),
			new DataField("END", "BIGINT"),
			new DataField("SENSORTYPE", "VARCHAR(32)"),
			new DataField("SENSORTYPE_ARGS", "BIGINT"),
			new DataField("COMMENT", "VARCHAR(255)")
	};

	/**
	 * Initializes the DataMappingWrapper.
	 * This method is responsible for initializing the mapping based on the mapping
	 * type.
	 * It creates the necessary database tables and indexes, and populates them with
	 * data from the virtual sensor.
	 * 
	 * @return true if the initialization is successful, false otherwise.
	 */
	@Override
	public boolean initialize() {
		deployment = getActiveAddressBean().getVirtualSensorName().split("_")[0].toLowerCase();

		mappingName = getActiveAddressBean().getPredicateValueWithException("mapping-type");

		String createTableStatement;
		String createIndexStatement = null;
		if (mappingName.compareToIgnoreCase("position") == 0) {
			mappingType = POSITION_MAPPING;
			mappingName = "position";
			outputStructure = positionStructure;
			createTableStatement = "CREATE TABLE " + deployment
					+ "_position (pk BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
					+ outputStructure[0].getName() + " " + outputStructure[0].getType() + " NOT NULL, "
					+ outputStructure[1].getName() + " " + outputStructure[1].getType() + " NOT NULL, "
					+ outputStructure[2].getName() + " " + outputStructure[2].getType() + " NOT NULL, "
					+ outputStructure[3].getName() + " " + outputStructure[3].getType() + " NULL, "
					+ outputStructure[4].getName() + " " + outputStructure[4].getType() + " NOT NULL, "
					+ outputStructure[5].getName() + " " + outputStructure[5].getType() + ")";
			createIndexStatement = "CREATE INDEX ON " + deployment + "_position (device_id, begin, end)";
		} else if (mappingName.compareToIgnoreCase("geo") == 0) {
			mappingType = GEO_MAPPING;
			mappingName = "geo";
			outputStructure = geoStructure;
			createTableStatement = "CREATE TABLE " + deployment + "_geo ("
					+ outputStructure[0].getName() + " " + outputStructure[0].getType() + " NOT NULL, "
					+ outputStructure[1].getName() + " " + outputStructure[1].getType() + " NOT NULL, "
					+ outputStructure[2].getName() + " " + outputStructure[2].getType() + " NOT NULL, "
					+ outputStructure[3].getName() + " " + outputStructure[3].getType() + " NOT NULL, "
					+ outputStructure[4].getName() + " " + outputStructure[4].getType() + ", PRIMARY KEY(position))";
		} else if (mappingName.compareToIgnoreCase("sensor") == 0) {
			mappingType = SENSOR_MAPPING;
			mappingName = "sensor";
			outputStructure = sensorStructure;
			createTableStatement = "CREATE TABLE " + deployment
					+ "_sensor (pk BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
					+ outputStructure[0].getName() + " " + outputStructure[0].getType() + " NOT NULL, "
					+ outputStructure[1].getName() + " " + outputStructure[1].getType() + " NOT NULL, "
					+ outputStructure[2].getName() + " " + outputStructure[2].getType() + " NULL, "
					+ outputStructure[3].getName() + " " + outputStructure[3].getType() + " NOT NULL, "
					+ outputStructure[4].getName() + " " + outputStructure[4].getType() + " NULL, "
					+ outputStructure[5].getName() + " " + outputStructure[5].getType() + ")";
			createIndexStatement = "CREATE INDEX ON " + deployment + "_sensor (position, begin, end, sensortype)";
		} else {
			logger.error("mapping type not recognized (position/geo/sensor)");
			return false;
		}

		synchronized (deployments) {
			try {
				Statement h2Stat;
				if (deployments.isEmpty()) {
					try {
						Class.forName("org.h2.Driver");
					} catch (ClassNotFoundException e) {
						logger.error(e.getMessage(), e);
						return false;
					}
					if (logger.isDebugEnabled()) {
						try {
							String[] args = { "-webPort", "8082"};
							web = Server.createWebServer(args);
							web.start();
						} catch (SQLException e) {
							logger.warn(e.getMessage(), e);
						}
					}

					// sensortypes are deployment independent
					h2DBconn = DriverManager.getConnection("jdbc:h2:mem:mapping;DB_CLOSE_DELAY=-1", "sa", "");
					h2DBconn.setAutoCommit(true);
					logger.info("connected to jdbc:h2:mem:mapping...");

					h2Stat = h2DBconn.createStatement();

					String insert;
					h2Stat.execute("DROP TABLE IF EXISTS sensortype");
					h2Stat.execute(
							"CREATE TABLE sensortype(sensortype VARCHAR(30) NOT NULL, signal_name VARCHAR(30) NOT NULL, physical_signal VARCHAR(30) NOT NULL, conversion VARCHAR(30), input VARCHAR(30), PRIMARY KEY(sensortype, signal_name, physical_signal))");
					logger.info("create sensortype table for all deployments");
					String[] files = new File("conf/permasense/sensortype_templates").list(new MappingFilenameFilter());
					ListIterator<String> filenames = Arrays.asList(files).listIterator();
					while (filenames.hasNext()) {
						insert = filenames.next();
						h2Stat.execute(
								"INSERT INTO sensortype SELECT '" + insert.substring(0, insert.lastIndexOf('.')) +
										"' AS sensortype, * FROM CSVREAD('conf/permasense/sensortype_templates/"
										+ insert + "')");
					}

					h2Stat.execute("DROP TABLE IF EXISTS sensortype_args");
					h2Stat.execute(
							"CREATE TABLE sensortype_args(sensortype_args BIGINT NOT NULL, physical_signal VARCHAR(30) NOT NULL, value VARCHAR(255) NOT NULL, comment CLOB, PRIMARY KEY(sensortype_args, physical_signal))");
					logger.info("create sensortype_args table for all deployments");
					insert = "conf/permasense/sensortype_args.csv";
					if (new File(insert).exists()) {
						h2Stat.execute("INSERT INTO sensortype_args SELECT * FROM CSVREAD('" + insert + "')");
						sensortype_args_available = true;
					} else {
						logger.info("sensortype_args not available");
					}
				}

				Mappings mapping;
				if (deployments.containsKey(deployment)) {
					mapping = deployments.get(deployment);
				} else {
					mapping = new Mappings();
				}

				switch (mappingType) {
					case POSITION_MAPPING:
						if (mapping.isPositionAvailable()) {
							logger.error("only one Position Mapping Virtual Sensor is allowed per deployment");
							return false;
						}
						break;
					case GEO_MAPPING:
						if (mapping.isGeoAvailable()) {
							logger.error("only one Geo Mapping Virtual Sensor is allowed per deployment");
							return false;
						}
						break;
					case SENSOR_MAPPING:
						if (mapping.isSensorAvailable()) {
							logger.error("only one Sensor Mapping Virtual Sensor is allowed per deployment");
							return false;
						}
						break;
					default:
						break;
				}

				h2Stat = h2DBconn.createStatement();
				h2Stat.execute("DROP TABLE IF EXISTS " + deployment + "_" + mappingName);
				h2Stat.execute(createTableStatement);
				if (createIndexStatement != null) {
					h2Stat.execute(createIndexStatement);
				}
				logger.info("create " + mappingName + " mapping table for " + deployment + " deployment");

				ResultSet rs = null;
				try {
					StringBuilder query = new StringBuilder();
					query.append("select * from ").append(getActiveAddressBean().getVirtualSensorName());
					rs = Main.getStorage(getActiveAddressBean().getVirtualSensorConfig()).executeQueryWithResultSet(
							query, Main.getStorage(getActiveAddressBean().getVirtualSensorConfig()).getConnection());
				} catch (SQLException e) {
					if(logger.isDebugEnabled()){
						logger.debug(e.getMessage());
					}
				}

				try {
					boolean isEmpty = true;
					switch (mappingType) {
						case POSITION_MAPPING:
							mapping.setPositionQueries();
							if (rs != null) {
								while (rs.next()) {
									isEmpty = false;
									Long end = rs.getLong(outputStructure[3].getName());
									if (rs.wasNull()) {
										end = null;
									}
									mapping.executePositionInsert(
											getActiveAddressBean().getVirtualSensorConfig(),
											rs.getInt(outputStructure[0].getName()),
											rs.getShort(outputStructure[1].getName()),
											rs.getLong(outputStructure[2].getName()),
											end,
											rs.getInt(outputStructure[4].getName()),
											rs.getString(outputStructure[5].getName()),
											false);
								}
							}
							break;
						case GEO_MAPPING:
							mapping.setGeoQueries();
							if (rs != null) {
								while (rs.next()) {
									isEmpty = false;
									mapping.executeGeoInsert(
											getActiveAddressBean().getVirtualSensorConfig(),
											rs.getInt(outputStructure[0].getName()),
											rs.getDouble(outputStructure[1].getName()),
											rs.getDouble(outputStructure[2].getName()),
											rs.getDouble(outputStructure[3].getName()),
											rs.getString(outputStructure[4].getName()),
											false);
								}
							}
							break;
						case SENSOR_MAPPING:
							mapping.setSensorQueries();
							if (rs != null) {
								while (rs.next()) {
									isEmpty = false;
									Long end = rs.getLong(outputStructure[2].getName());
									if (rs.wasNull()) {
										end = null;
									}
									Long sensortyp_args = rs.getLong(outputStructure[4].getName());
									if (rs.wasNull()) {
										sensortyp_args = null;
									}
									mapping.executeSensorInsert(
											getActiveAddressBean().getVirtualSensorConfig(),
											rs.getInt(outputStructure[0].getName()),
											rs.getLong(outputStructure[1].getName()),
											end,
											rs.getString(outputStructure[3].getName()),
											sensortyp_args,
											rs.getString(outputStructure[5].getName()),
											false);
								}
							}

							if (sensortype_args_available) {
								mapping.setConversionQuery();
							}
							break;
						default:
							break;
					}
					if (isEmpty) {
						logger.warn(mappingName + " mapping for " + deployment + " deployment is empty");
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

				deployments.put(deployment, mapping);
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				return false;
			}
		}
		return true;
	}

	/**
	 * This method retrieves the position of a device at a specific generation time
	 * for a given deployment, virtual sensor, and input stream.
	 * 
	 * @param device_id       The ID of the device.
	 * @param generationTime  The generation time of the data.
	 * @param deployment      The name of the deployment.
	 * @param vsName          The name of the virtual sensor.
	 * @param inputStreamName The name of the input stream.
	 * @param warn            A flag indicating whether to log a warning if no
	 *                        position mapping is available.
	 * @return The position of the device, or null if no position mapping is
	 *         available.
	 */
	public static Integer getPosition(int device_id, long generationTime, String deployment, String vsName,
			String inputStreamName, boolean warn) {
		Integer pos = null;
		long start = -1;
		if (logger.isDebugEnabled()) {
			start = System.nanoTime();
		}
		Mappings m = deployments.get(deployment);

		if (m == null || !m.isPositionAvailable()) {
			logger.error(vsName + "[source=" + inputStreamName
					+ "]: Position mapping data not available for deployment " + deployment);
			return null;
		}

		try {
			pos = m.executePositionSelect(device_id, generationTime);
		} catch (SQLException e) {
			logger.warn(e.getMessage(), e);
		}

		if (pos == null && (warn || logger.isDebugEnabled())) {
			logger.warn(vsName + "[source=" + inputStreamName + "]: No position mapping available for deployment "
					+ deployment + " device-id " + device_id + " and generation_time=" + generationTime);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(vsName + "[source=" + inputStreamName + "]: getPosition: "
					+ Long.toString((System.nanoTime() - start) / 1000) + " us");
		}

		return pos;
	}

	public static Integer getPosition(int device_id, long generationTime, String deployment, String vsName,
			String inputStreamName) {
		return getPosition(device_id, generationTime, deployment, vsName, inputStreamName, true);
	}

	/**
	 * Retrieves all mapped positions for a given deployment, virtual stream, and
	 * input stream.
	 * 
	 * @param deployment      the deployment identifier
	 * @param vsName          the virtual stream name
	 * @param inputStreamName the input stream name
	 * @return a HashMap containing the mapped positions, where the key is the
	 *         position identifier and the value is the MappedEntry object
	 */
	public static HashMap<Integer, MappedEntry> getAllPositions(String deployment, String vsName,
			String inputStreamName) {
		HashMap<Integer, MappedEntry> allMappedPositions = null;
		long start = -1;
		if (logger.isDebugEnabled()) {
			start = System.nanoTime();
		}

		Mappings m = deployments.get(deployment);

		if (m == null || !m.isPositionAvailable()) {
			logger.error(vsName + "[source=" + inputStreamName
					+ "]: Position mapping data not available for deployment " + deployment);
			return null;
		}

		try {
			allMappedPositions = m.executeAllPositionsSelect();
		} catch (SQLException e) {
			logger.warn(e.getMessage(), e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(vsName + "[source=" + inputStreamName + "]: getAllPositions: "
					+ Long.toString((System.nanoTime() - start) / 1000) + " us");
		}
		return allMappedPositions;
	}

	/**
	 * Retrieves the device type for a given device ID, generation time, deployment,
	 * virtual stream name, and input stream name.
	 * 
	 * @param device_id       The ID of the device.
	 * @param generationTime  The generation time of the device.
	 * @param deployment      The deployment name.
	 * @param vsName          The virtual stream name.
	 * @param inputStreamName The input stream name.
	 * @return The device type as a Short value, or null if the device type mapping
	 *         data is not available.
	 */
	public static Short getDeviceType(int device_id, long generationTime, String deployment, String vsName,
			String inputStreamName) {
		Short deviceType = null;
		long start = -1;
		if (logger.isDebugEnabled()) {
			start = System.nanoTime();
		}
		Mappings m = deployments.get(deployment);

		if (m == null || !m.isPositionAvailable()) {
			logger.error(vsName + "[source=" + inputStreamName
					+ "]: Device type mapping data not available for deployment " + deployment);
			return null;
		}

		try {
			deviceType = m.executeDeviceTypeSelect(device_id, generationTime);
		} catch (SQLException e) {
			logger.warn(e.getMessage(), e);
		}

		if (deviceType == null) {
			logger.warn(vsName + "[source=" + inputStreamName + "]: No device type mapping available for deployment "
					+ deployment + " device-id " + device_id + " and generation_time=" + generationTime);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(vsName + "[source=" + inputStreamName + "]: getDeviceType: "
					+ Long.toString((System.nanoTime() - start) / 1000) + " us");
		}

		return deviceType;
	}

	/**
	 * Retrieves the coordinate associated with the specified position in
	 * the given deployment.
	 *
	 * @param position        The position for which to retrieve the geographic
	 *                        coordinate.
	 * @param deployment      The name of the deployment containing the coordinate
	 *                        mapping data.
	 * @param vsName          The name of the virtual source associated with the
	 *                        coordinate retrieval.
	 * @param inputStreamName The name of the input stream associated with the
	 *                        coordinate retrieval.
	 * @return A Coordinate object representing the geographic coordinate for the
	 *         specified position,
	 *         or null if the coordinate mapping data is not available.
	 * @see Coordinate
	 */
	public static Coordinate getCoordinate(int position, String deployment, String vsName, String inputStreamName) {
		Coordinate coordinate = null;
		long start = -1;
		if (logger.isDebugEnabled()) {
			start = System.nanoTime();
		}

		Mappings m = deployments.get(deployment);

		if (m == null || !m.isGeoAvailable()) {
			logger.error(vsName + "[source=" + inputStreamName
					+ "]: Geographic coordinate mapping data not available for deployment " + deployment);
			return null;
		}

		try {
			coordinate = m.executeGeoSelect(position);
		} catch (SQLException e) {
			logger.warn(e.getMessage(), e);
		}

		if (coordinate == null) {
			logger.warn(vsName + "[source=" + inputStreamName + "]: No coordinate mapping available for deployment "
					+ deployment + " position " + position);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(vsName + "[source=" + inputStreamName + "]: getCoordinate: "
					+ Long.toString((System.nanoTime() - start) / 1000) + " us");
		}

		return coordinate;
	}

	/**
	 * Retrieves the sensor type and related information for a given position,
	 * generation time, deployment, virtual sensor name, and input stream name.
	 * 
	 * @param position        The position of the sensor.
	 * @param generationTime  The generation time of the sensor data.
	 * @param deployment      The deployment name.
	 * @param vsName          The virtual sensor name.
	 * @param inputStreamName The input stream name.
	 * @return An array of Serializable objects containing the sensor type and
	 *         related information. The array has a length of 3 and may contain null
	 *         values.
	 *         If the sensor mapping data is not available for the specified
	 *         deployment, null is returned.
	 */
	public static Serializable[] getSensorType(int position, long generationTime, String deployment, String vsName,
			String inputStreamName) {
		Serializable[] res = new Serializable[] { null, null, null };
		long start = -1;
		if (logger.isDebugEnabled()) {
			start = System.nanoTime();
		}

		Mappings m = deployments.get(deployment);

		if (m == null || !m.isSensorAvailable()) {
			logger.error(vsName + "[source=" + inputStreamName + "]: Sensor mapping data not available for deployment "
					+ deployment);
			return null;
		}

		try {
			res = new Serializable[] { m.executeSensorSelect(position, generationTime),
					m.executeSerialIdSelect(position, generationTime) };
		} catch (SQLException e) {
			logger.warn(e.getMessage(), e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(vsName + "[source=" + inputStreamName + "]: getSensortype: "
					+ Long.toString((System.nanoTime() - start) / 1000) + " us");
		}
		return res;
	}

	/**
	 * Converts sensor data values using the specified deployment's sensor
	 * conversion mapping data.
	 * This method applies conversions to the fields of the provided StreamElement
	 * and returns the updated data.
	 *
	 * @param data            The StreamElement containing sensor data to be
	 *                        converted.
	 * @param deployment      The name of the deployment containing the sensor
	 *                        conversion mapping data.
	 * @param vsName          The name of the virtual source associated with the
	 *                        sensor data conversion.
	 * @param inputStreamName The name of the input stream associated with the
	 *                        sensor data conversion.
	 * @return A new StreamElement with converted sensor data fields, or null if
	 *         conversion mapping data is not available.
	 * @see StreamElement
	 */
	public static StreamElement getConvertedValues(StreamElement data, String deployment, String vsName,
			String inputStreamName) {
		
		StreamElement se = data;
		String convName;
		String[] convResult;
		Converter converter;
		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		long start = -1;
		if (logger.isDebugEnabled()) {
			start = System.nanoTime();
		}

		Mappings m = deployments.get(deployment);

		if (m == null) {
			logger.error(vsName + "[source=" + inputStreamName + "]: Sensor conversion mapping data not available");
			return null;
		}

		ListIterator<String> list = Arrays.asList(se.getFieldNames()).listIterator();
		try {
			while (list.hasNext()) {
				convName = list.next().toLowerCase();
				if (se.getData(convName) == null) {
					if(logger.isDebugEnabled()){
						logger.debug(vsName + "[source=" + inputStreamName + "]: ignoring >" + convName + "<");
					}
				} else {
					convResult = m.executeConversionSelect(((Integer) se.getData("position")).intValue(),
							((Long) se.getData("generation_time")).longValue(),
							convName);

					if (convResult == null) {
						if(logger.isDebugEnabled()){
							logger.debug(vsName + "[source=" + inputStreamName + "]: no conversion found for >" + convName
								+ "<");
						}
					} else {
						// physical_signal, conversion, input, value
						if(logger.isDebugEnabled()){
							logger.debug(vsName + "[source=" + inputStreamName + "]: physical_signal:" + convResult[0]
								+ " conversion:" + convResult[1] +
								" input:" + convResult[2] + " value:" + convResult[3]);
						}

						try {
							synchronized (converterList) {
								if (!converterList.containsKey(convResult[1])) {
									String className = "ch.epfl.gsn.vsensor.permasense."
											+ convResult[1].substring(0, 1).toUpperCase() + convResult[1].substring(1);
									logger.info("Instantiating converter '" + className);
									Class<?> classTemplate = Class.forName(className);
									converterList.put(convResult[1],
											(Converter) classTemplate.getConstructor().newInstance());
								}
								converter = converterList.get(convResult[1]);
							}
							if (convResult[2].isEmpty()) {
								map.put(convResult[0], converter.convert(se.getData(convName), convResult[3], null));
							} else {
								map.put(convResult[0], converter.convert(se.getData(convName), convResult[3],
										se.getData(convResult[2])));
							}
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
			}
			if (!map.isEmpty()) {
				Byte[] types = new Byte[map.size()];
				Arrays.fill(types, DataTypes.VARCHAR);
				se = new StreamElement(se, map.keySet().toArray(new String[] {}),
						types, map.values().toArray(new Serializable[] {}));
			}
		} catch (SQLException e) {
			logger.warn(e.getMessage(), e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(vsName + "[source=" + inputStreamName + "]: conversion: "
					+ Long.toString((System.nanoTime() - start) / 1000) + " us");
		}
		return se;
	}

	/**
	 * Sends data to the wrapper for the specified action using the provided
	 * parameters.
	 * This method handles different types of mappings (POSITION_MAPPING,
	 * GEO_MAPPING, SENSOR_MAPPING),
	 * performs necessary validations, executes mapping-related operations, and
	 * posts the resulting StreamElements.
	 *
	 * @param action      The action to be performed by the wrapper.
	 * @param paramNames  An array of parameter names associated with the action.
	 * @param paramValues An array of parameter values corresponding to the
	 *                    parameter names.
	 * @return An InputInfo object indicating the success or failure of the mapping
	 *         upload.
	 * @see InputInfo
	 */
	public InputInfo sendToWrapper(String action, String[] paramNames, Serializable[] paramValues) {
		if (action.compareToIgnoreCase(mappingName) == 0) {
			SimpleDateFormat sdfWeb = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			sdfWeb.setLenient(true);
			sdfWeb.setTimeZone(Main.getContainerConfig().getTimeZone());
			ArrayList<Serializable[]> streamElements = null;
			int index = 0;
			switch (mappingType) {
				case POSITION_MAPPING:
					Integer deviceId = null, position = null;
					Short deviceType = null;
					Long begin = null, 
						 end = null;
					String comment = null;

					for (String param : paramNames) {
						if (param.equals("device_id")) {
							deviceId = Integer.parseInt((String) paramValues[index]);
						} else if (param.equals("device_type")) {
							deviceType = Short.parseShort((String) paramValues[index]);
							if (deviceType.compareTo((short) 0) == 0) {
								return new InputInfo(getActiveAddressBean().toString(),
										"device type has to be selected", false);
							}
						} else if (param.equals("begin")) {
							if (!((String) paramValues[index]).trim().isEmpty()) {
								try {
									begin = sdfWeb.parse((String) paramValues[index]).getTime();
								} catch (ParseException e) {
									return new InputInfo(getActiveAddressBean().toString(),
											"begin is not in the wright date format (dd/MM/yyyy HH:mm:ss)", false);
								}
							}

						} else if (param.equals("end")) {
							if (!((String) paramValues[index]).trim().isEmpty()) {
								try {
									end = sdfWeb.parse((String) paramValues[index]).getTime();
								} catch (ParseException e) {
									return new InputInfo(getActiveAddressBean().toString(),
											"end is not in the wright date format (dd/MM/yyyy HH:mm:ss)", false);
								}
							}
						} else if (param.equals("position")) {
							position = Integer.parseInt((String) paramValues[index]);
						} else if (param.equals("comment")) {
							comment = (String) paramValues[index];
						}

						index++;
					}

					if (position == null) {
						return new InputInfo(getActiveAddressBean().toString(), "position has to be set", false);
					}
					if (deviceId == null) {
						return new InputInfo(getActiveAddressBean().toString(), "device_id has to be set", false);
					}
					if (comment == null) {
						return new InputInfo(getActiveAddressBean().toString(), "comment has to be set", false);
					}
					try {
						streamElements = deployments.get(deployment).executePositionInsert(
								getActiveAddressBean().getVirtualSensorConfig(), deviceId, deviceType, begin, end,
								position, comment, true);
					} catch (Exception e) {
						return new InputInfo(getActiveAddressBean().toString(), e.getMessage(), false);
					}
					break;
				case GEO_MAPPING:
					position = null;
					Double longitude = null, 
						   latitude = null, 
						   altitude = null;
					comment = null;

					for (String param : paramNames) {
						if (param.equals("position")) {
							position = Integer.parseInt((String) paramValues[index]);
						} else if (param.equals("longitude")) {
							try {
								longitude = Double.parseDouble((String) paramValues[index]);
							} catch (NumberFormatException e) {
								return new InputInfo(getActiveAddressBean().toString(), "longitude must be a double",
										false);
							}
						} else if (param.equals("latitude")) {
							try {
								latitude = Double.parseDouble((String) paramValues[index]);
							} catch (NumberFormatException e) {
								return new InputInfo(getActiveAddressBean().toString(), "latitude must be a double",
										false);
							}
						} else if (param.equals("altitude")) {
							try {
								altitude = Double.parseDouble((String) paramValues[index]);
							} catch (NumberFormatException e) {
								return new InputInfo(getActiveAddressBean().toString(), "altitude must be a double",
										false);
							}
						} else if (param.equals("comment")) {
							comment = (String) paramValues[index];
						}
						index++;
					}

					if (position == null) {
						return new InputInfo(getActiveAddressBean().toString(), "position has to be set", false);
					}
					if (longitude == null) {
						return new InputInfo(getActiveAddressBean().toString(), "longitude has to be set", false);
					}
					if (latitude == null) {
						return new InputInfo(getActiveAddressBean().toString(), "latitude has to be set", false);
					}
					if (altitude == null) {
						return new InputInfo(getActiveAddressBean().toString(), "altitude has to be set", false);
					}
					if (comment == null) {
						return new InputInfo(getActiveAddressBean().toString(), "comment has to be set", false);
					}
					try {
						streamElements = deployments.get(deployment).executeGeoInsert(
								getActiveAddressBean().getVirtualSensorConfig(), position, longitude, latitude,
								altitude, comment, true);
					} catch (Exception e) {
						return new InputInfo(getActiveAddressBean().toString(), e.getMessage(), false);
					}
					break;
				case SENSOR_MAPPING:
					position = null;
					begin = end = null;
					String sensortype = null;
					Long sensortypeArgs = null;
					comment = null;

					for (String param : paramNames) {
						if (param.equals("position")) {
							position = Integer.parseInt((String) paramValues[index]);
						} else if (param.equals("begin")) {
							if (!((String) paramValues[index]).trim().isEmpty()) {
								try {
									begin = sdfWeb.parse((String) paramValues[index]).getTime();
								} catch (ParseException e) {
									return new InputInfo(getActiveAddressBean().toString(),
											"begin is not in the wright date format (dd/MM/yyyy HH:mm:ss)", false);
								}
							}

						} else if (param.equals("end")) {
							if (!((String) paramValues[index]).trim().isEmpty()) {
								try {
									end = sdfWeb.parse((String) paramValues[index]).getTime();
								} catch (ParseException e) {
									return new InputInfo(getActiveAddressBean().toString(),
											"end is not in the wright date format (dd/MM/yyyy HH:mm:ss)", false);
								}
							}
						} else if (param.equals("sensortype")) {
							sensortype = (String) paramValues[index];
						} else if (param.equals("sensortype_args")) {
							if (!((String) paramValues[index]).trim().isEmpty()) {
								sensortypeArgs = Long.parseLong((String) paramValues[index]);
							}
						} else if (param.equals("comment")) {
							comment = (String) paramValues[index];
						}

						index++;
					}

					if (position == null) {
						return new InputInfo(getActiveAddressBean().toString(), "position has to be set", false);
					}
					if (sensortype == null) {
						return new InputInfo(getActiveAddressBean().toString(), "sensortype has to be set", false);
					}
					if (sensortypeArgs == null) {
						return new InputInfo(getActiveAddressBean().toString(), "sensortype_args has to be set", false);
					}
					if (comment == null) {
						return new InputInfo(getActiveAddressBean().toString(), "comment has to be set", false);
					}

					try {
						streamElements = deployments.get(deployment).executeSensorInsert(
								getActiveAddressBean().getVirtualSensorConfig(), position, begin, end, sensortype,
								sensortypeArgs, comment, true);
					} catch (Exception e) {
						return new InputInfo(getActiveAddressBean().toString(), e.getMessage(), false);
					}
					break;
				default:
					break;
			}

			if (streamElements != null) {
				Iterator<Serializable[]> iter = streamElements.iterator();
				while (iter.hasNext()) {
					postStreamElement(iter.next());
				}

			}

			return new InputInfo(getActiveAddressBean().toString(), "mapping upload successfull", true);
		} else {
			logger.warn("action >" + action + "< not supported");
			return new InputInfo(getActiveAddressBean().toString(), "action >" + action + "< not supported", false);
		}
	}

	@Override
	public void dispose() {
		synchronized (deployments) {
			logger.info("remove " + mappingName + " mapping for " + deployment + " deployment");

			Mappings m = deployments.get(deployment);
			switch (mappingType) {
				case POSITION_MAPPING:
					m.resetPositionQueries();
					break;
				case GEO_MAPPING:
					deployments.get(deployment).resetGeoQueries();
					break;
				case SENSOR_MAPPING:
					deployments.get(deployment).resetSensorQueries();
					deployments.get(deployment).resetConversionQuery();
					break;
				default:
					break;
			}

			if (!m.isPositionAvailable() && !m.isGeoAvailable() && !m.isSensorAvailable()) {
				if(logger.isDebugEnabled()){
					logger.debug("remove " + deployment + " deployment");
				}
				deployments.remove(deployment);
			}

			if (deployments.isEmpty()) {
				logger.info("close connection to jdbc:h2:mem:mapping");
				try {
					if (h2DBconn != null) {
						h2DBconn.close();
					}
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}

				if (web != null) {
					if(logger.isDebugEnabled()){
						logger.debug("shut down h2 webserver");
					}
					web.shutdown();
					web.stop();
					web = null;
				}
			}
		}
	}

	@Override
	public DataField[] getOutputFormat() {
		return outputStructure;
	}

	@Override
	public String getWrapperName() {
		return "DataMappingWrapper";
	}

	class Mappings {
		private PreparedStatement position_select = null;
		private PreparedStatement position_insert = null;
		private PreparedStatement all_positions_select = null;
		private PreparedStatement geo_select = null;
		private PreparedStatement geo_insert = null;
		private PreparedStatement sensor_select = null;
		private PreparedStatement sensor_insert = null;
		private PreparedStatement serialid_select = null;
		private PreparedStatement conversion_select = null;

		/**
		 * Sets up the prepared statements for querying and inserting position data.
		 * 
		 * @throws SQLException if there is an error in preparing the statements
		 */
		public synchronized void setPositionQueries() throws SQLException {
			position_select = h2DBconn.prepareStatement("SELECT position, device_type FROM " + deployment
					+ "_position WHERE device_id = ? AND ((end is null AND begin <= ?) OR (? BETWEEN begin AND end)) LIMIT 1");
			position_insert = h2DBconn.prepareStatement("INSERT INTO " + deployment
					+ "_position (device_id, device_type, begin, end, position, comment) VALUES (?,?,?,?,?,?)");
			all_positions_select = h2DBconn.prepareStatement("SELECT position, device_id, device_type FROM "
					+ deployment + "_position where begin = (select max(begin) from " + deployment
					+ "_position i where i.position = " + deployment + "_position.position);");
		}

		public synchronized void resetPositionQueries() {
			position_select = position_insert = all_positions_select = null;
		}

		public boolean isPositionAvailable() {
			return position_select != null;
		}

		/**
		 * Executes the insertion of position mapping data into the database for a
		 * specified virtual sensor configuration.
		 *
		 * @param vSensorConfig The virtual sensor configuration associated with the
		 *                      deployment.
		 * @param deviceId      The unique identifier of the device.
		 * @param deviceType    The type of the device.
		 * @param begin         The start timestamp of the position mapping.
		 * @param end           The end timestamp of the position mapping (null for
		 *                      ongoing mappings).
		 * @param pos           The position identifier.
		 * @param comment       Additional comments for the position mapping.
		 * @param check         Flag indicating whether to perform additional validation
		 *                      checks.
		 * @return An ArrayList of Serializable arrays containing information about the
		 *         inserted position mapping(s).
		 * @throws Exception If an error occurs during the position mapping insertion
		 *                   process.
		 */
		public ArrayList<Serializable[]> executePositionInsert(VSensorConfig vSensorConfig, int deviceId,
				short deviceType, Long begin, Long end, int pos, String comment, boolean check) throws Exception {
			if (position_insert == null) {
				throw new Exception("no position mapping available");
			} else {
				synchronized (position_insert) {
					ArrayList<Serializable[]> ret = new ArrayList<Serializable[]>(2);
					if (check) {
						Statement h2Stat = h2DBconn.createStatement();
						StorageManager gsnsm = Main.getStorage(vSensorConfig);
						ResultSet rs;
						if (deviceId < 0) {
							throw new Exception("device_id must be a positive integer");
						}
						if (pos < 0) {
							throw new Exception("position must be a positive integer");
						}
						if (begin == null && end == null) {
							throw new Exception("at least either begin or end has to specified");
						} else if (begin != null && end != null) {
							if (begin >= end) {
								throw new Exception("begin must be smaller than end");
							}

							// check for overlapping position in this time period
							rs = h2Stat.executeQuery("SELECT pk FROM " + deployment + "_position WHERE position = " +
									pos + " AND ((end IS NOT null AND ((" +
									begin + " >= begin AND " + begin + " < end) OR (" +
									end + " > begin AND " + end + " <= end))) OR (end IS null AND (begin > " +
									begin + " AND begin < " + end + "))) LIMIT 1");

							if (rs.next()) {
								throw new Exception("new position mapping overlaps with existing entry");
							}
							// check for overlapping device_id in this time period
							rs = h2Stat.executeQuery("SELECT pk FROM " + deployment + "_position WHERE device_id = " +
									deviceId + " AND ((end IS NOT null AND ((" +
									begin + " >= begin AND " + begin + " < end) OR (" +
									end + " > begin AND " + end + " <= end))) OR (end IS null AND (begin <= " +
									begin + " OR begin < " + end + "))) LIMIT 1");

							if (rs.next()) {
								throw new Exception("the device_id does already exist in the specified time period");
							}
						} else if (begin == null) {
							rs = h2Stat.executeQuery("SELECT * FROM " + deployment + "_position WHERE position = " +
									pos + " AND device_id = " +
									deviceId + " AND end IS null AND begin < " + end);

							if (rs.next()) {
								if (!rs.isLast()) {
									logger.error("there are too many result sets");
								}

								long h2pk = rs.getLong(1);
								long h2deviceType = rs.getLong(3);
								long h2begin = rs.getLong(4);
								String h2comment = rs.getString(7);

								if (h2deviceType != deviceType) {
									throw new Exception(
											"new mapping has not the same device type as the existing one to be ended");
								}

								gsnsm.executeUpdate(new StringBuilder("DELETE FROM " + vSensorConfig.getName() +
										" WHERE position = " + pos + " AND end IS null AND begin < " + end),
										gsnsm.getConnection());

								if (comment.isEmpty()) {
									h2Stat.executeUpdate("UPDATE " + deployment + "_position SET end = " +
											end + " WHERE pk = " + h2pk);
									ret.add(new Serializable[] { deviceId, deviceType, h2begin, end, pos, h2comment });
								} else {
									h2Stat.executeUpdate("UPDATE " + deployment + "_position SET end = " +
											end + ", comment = '" + comment + "' WHERE pk = " + h2pk);
									ret.add(new Serializable[] { deviceId, deviceType, h2begin, end, pos, comment });
								}

								return ret;
							} else {
								throw new Exception(
										"new mapping does not end an existing entry with the same position and device_id");
							}
						} else if (end == null) {
							// check for overlapping position in this open time period
							rs = h2Stat.executeQuery("SELECT pk FROM " + deployment + "_position WHERE position = " +
									pos + " AND ((end IS null AND " +
									begin + " < begin) OR (end IS NOT null AND " +
									begin + " < end)) LIMIT 1");

							if (rs.next()) {
								throw new Exception("new postion mapping overlaps with existing entry");
							}

							// check for overlapping device_id in this open time period
							rs = h2Stat.executeQuery("SELECT pk FROM " + deployment + "_position WHERE device_id = " +
									deviceId + " AND (end IS null OR (end IS NOT null AND " +
									begin + " < end)) LIMIT 1");

							if (rs.next()) {
								throw new Exception(
										"the device_id does already exist in the specified open time period");
							}
						}

						rs = h2Stat.executeQuery("SELECT pk FROM " + deployment + "_position WHERE position = " +
								pos + " AND device_id != " +
								deviceId + " AND end IS null AND begin = " + begin);
						if (rs.next()) {
							throw new Exception(
									"the same begin for this position is already existing with an other device_id");
						}

						rs = h2Stat.executeQuery("SELECT * FROM " + deployment + "_position WHERE position = " +
								pos + " AND end IS null AND begin = " + begin);
						if (rs.next()) {
							if (!rs.isLast()) {
								logger.error("there are too many result sets");
							}

							if (end != null) {
								long h2pk = rs.getLong(1);
								String h2comment = rs.getString(6);

								gsnsm.executeUpdate(new StringBuilder("DELETE FROM " + vSensorConfig.getName() +
										" WHERE position = " + pos + " AND end IS null AND begin = " + begin),
										gsnsm.getConnection());

								if (comment.isEmpty()) {
									h2Stat.executeUpdate("UPDATE " + deployment + "_position SET end = " +
											end + " WHERE pk = " + h2pk);
									ret.add(new Serializable[] { deviceId, deviceType, begin, end, pos, h2comment });
								} else {
									h2Stat.executeUpdate("UPDATE " + deployment + "_position SET end = " +
											end + ", comment = '" + comment + "' WHERE pk = " + h2pk);
									ret.add(new Serializable[] { deviceId, deviceType, begin, end, pos, comment });
								}
							} else {
								throw new Exception("the same begin for this position is already existing");
							}

							return ret;
						}

						rs = h2Stat.executeQuery("SELECT * FROM " + deployment + "_position WHERE position = " +
								pos + " AND end IS null AND begin < " + begin);

						if (rs.next()) {
							if (!rs.isLast()) {
								logger.error("there are too many result sets");
							}

							long h2pk = rs.getLong(1);
							int h2deviceid = rs.getInt(2);
							Short h2devicetype = rs.getShort(3);
							long h2begin = rs.getLong(4);
							String h2comment = rs.getString(7);
							h2Stat.executeUpdate("UPDATE " + deployment + "_position SET end = " +
									(begin - 1) + " WHERE pk = " + h2pk);

							gsnsm.executeUpdate(new StringBuilder("DELETE FROM " + vSensorConfig.getName() +
									" WHERE position = " + pos + " AND end IS null AND begin < " + begin),
									gsnsm.getConnection());

							ret.add(new Serializable[] { h2deviceid, h2devicetype, h2begin, begin - 1, pos,
									h2comment });
						}
					}

					position_insert.setInt(1, deviceId);
					position_insert.setShort(2, deviceType);
					position_insert.setLong(3, begin);
					if (end == null) {
						position_insert.setNull(4, java.sql.Types.BIGINT);
					} else {
						position_insert.setLong(4, end);
					}
					position_insert.setInt(5, pos);
					position_insert.setString(6, comment);
					position_insert.executeUpdate();
					ret.add(new Serializable[] { deviceId, deviceType, begin, end, pos, comment });

					return ret;
				}
			}

		}

		/**
		 * Executes a position select query to retrieve the position of a device at a
		 * given generation time.
		 * 
		 * @param deviceId       the ID of the device
		 * @param generationTime the generation time
		 * @return the position of the device, or null if not found
		 * @throws SQLException if an error occurs while executing the query
		 */
		public Integer executePositionSelect(int deviceId, long generationTime) throws SQLException {
			Integer pos = null;
			if (position_select != null) {
				synchronized (position_select) {
					position_select.setInt(1, deviceId);
					position_select.setLong(2, generationTime);
					position_select.setLong(3, generationTime);
					ResultSet rs = position_select.executeQuery();
					if (rs.next()) {
						pos = rs.getInt(1);
						if (rs.wasNull()) {
							pos = null;
						}
					}
				}
			}
			return pos;
		}

		/**
		 * Executes a select query to retrieve all positions from the database.
		 * 
		 * @return a HashMap containing the positions as keys and their corresponding
		 *         MappedEntry objects as values.
		 * @throws SQLException if an error occurs while executing the query.
		 */
		public HashMap<Integer, MappedEntry> executeAllPositionsSelect() throws SQLException {
			HashMap<Integer, MappedEntry> allPositions = new HashMap<Integer, MappedEntry>();
			if (all_positions_select != null) {
				synchronized (all_positions_select) {
					ResultSet rs = all_positions_select.executeQuery();
					while (rs.next()) {
						allPositions.put(rs.getInt(1), new MappedEntry(rs.getInt(2), rs.getShort(3)));
					}
				}
			}
			return allPositions;
		}

		/**
		 * Executes a select query to retrieve the device type associated with a given
		 * device ID and generation time.
		 * 
		 * @param deviceId       The ID of the device.
		 * @param generationTime The generation time of the device.
		 * @return The device type associated with the given device ID and generation
		 *         time, or null if not found.
		 * @throws SQLException If an error occurs while executing the select query.
		 */
		public Short executeDeviceTypeSelect(int deviceId, long generationTime) throws SQLException {
			Short deviceType = null;
			if (position_select != null) {
				synchronized (position_select) {
					position_select.setInt(1, deviceId);
					position_select.setLong(2, generationTime);
					position_select.setLong(3, generationTime);
					ResultSet rs = position_select.executeQuery();
					if (rs.next()) {
						deviceType = rs.getShort(2);
						if (rs.wasNull()) {
							deviceType = null;
						}
					}
				}
			}
			return deviceType;
		}

		/**
		 * Sets up the prepared statements for performing geo queries.
		 *
		 * @throws SQLException if there is an error in preparing the statements.
		 */
		public synchronized void setGeoQueries() throws SQLException {
			geo_select = h2DBconn.prepareStatement(
					"SELECT longitude, latitude, altitude FROM " + deployment + "_geo WHERE position = ? LIMIT 1");
			geo_insert = h2DBconn.prepareStatement("INSERT INTO " + deployment + "_geo VALUES (?,?,?,?,?)");
		}

		public synchronized void resetGeoQueries() {
			geo_select = geo_insert = null;
		}

		public boolean isGeoAvailable() {
			return geo_select!= null;
		}

		/**
		 * Executes a geo insert operation, which inserts a new mapping entry into the
		 * database.
		 * 
		 * @param vSensorConfig The configuration of the virtual sensor.
		 * @param pos           The position of the mapping entry.
		 * @param longitude     The longitude value of the mapping entry.
		 * @param latitude      The latitude value of the mapping entry.
		 * @param altitude      The altitude value of the mapping entry.
		 * @param comment       The comment associated with the mapping entry.
		 * @param check         Specifies whether to perform additional checks before
		 *                      inserting the entry.
		 * @return An ArrayList containing the inserted mapping entry as a Serializable
		 *         array.
		 * @throws Exception If there is an error executing the geo insert operation or
		 *                   if the required parameters are not specified.
		 */
		public ArrayList<Serializable[]> executeGeoInsert(VSensorConfig vSensorConfig, Integer pos, Double longitude,
				Double latitude, Double altitude, String comment, boolean check) throws Exception {
			if (geo_insert == null) {
				throw new Exception("no geo mapping available");
			} else {
				synchronized (geo_insert) {
					ArrayList<Serializable[]> ret = new ArrayList<Serializable[]>(1);
					if (check) {
						Statement h2Stat = h2DBconn.createStatement();
						StorageManager gsnsm = Main.getStorage(vSensorConfig);
						if (pos == null || longitude == null || latitude == null || altitude == null) {
							throw new Exception("position, longitude, latitude and altitude have to be specified");
						} else if (pos < 0) {
							throw new Exception("position must be a positive integer");
						} else {
							int h2del = h2Stat
									.executeUpdate("DELETE FROM " + deployment + "_geo WHERE position = " + pos);

							int gsndel = gsnsm
									.executeUpdate(new StringBuilder("DELETE FROM " + vSensorConfig.getName() +
											" WHERE position = " + pos), gsnsm.getConnection());

							if (h2del != gsndel) {
								logger.error("not the same amount of rows deleted (h2del=" + h2del + ", gsndel="
										+ gsndel + ")");
							}
							if (gsndel > 0) {
								logger.warn("An existing geo mapping entry has been overwritten");
							}

						}
					}

					geo_insert.setInt(1, pos);
					geo_insert.setDouble(2, longitude);
					geo_insert.setDouble(3, latitude);
					geo_insert.setDouble(4, altitude);
					geo_insert.setString(5, comment);
					geo_insert.executeUpdate();
					ret.add(new Serializable[] { pos, longitude, latitude, altitude, comment });

					return ret;
				}
			}

		}

		/**
		 * Executes a select query to retrieve the coordinates associated with a given
		 * position.
		 * 
		 * @param pos position.
		 * @return The coordinates associated with the given position or null if not
		 *         found.
		 * @throws SQLException If an error occurs while executing the select query.
		 */
		public Coordinate executeGeoSelect(int pos) throws SQLException {
			Coordinate coord = null;
			if (geo_select != null) {
				synchronized (geo_select) {
					geo_select.setInt(1, pos);
					ResultSet rs = geo_select.executeQuery();
					if (rs.next()) {
						coord = new Coordinate(rs.getDouble(1), rs.getDouble(2), rs.getDouble(3));
						;
						if (rs.wasNull()) {
							coord = null;
						}

					}
				}
			}
			return coord;
		}

		/**
		 * Sets the prepared statements for querying sensor data.
		 * 
		 * @throws SQLException if there is an error executing the SQL statements.
		 */
		public synchronized void setSensorQueries() throws SQLException {
			sensor_select = h2DBconn.prepareStatement("SELECT sensortype, sensortype_args FROM " + deployment
					+ "_sensor WHERE position = ? AND ((end is null AND begin <= ?) OR (? BETWEEN begin AND end)) AND sensortype != 'serialid'");
			serialid_select = h2DBconn.prepareStatement("SELECT sensortype_args AS sensortype_serialid FROM "
					+ deployment
					+ "_sensor WHERE position = ? AND ((end is null AND begin <= ?) OR (? BETWEEN begin AND end)) AND sensortype = 'serialid' LIMIT 1");
			sensor_insert = h2DBconn.prepareStatement("INSERT INTO " + deployment
					+ "_sensor (position, begin, end, sensortype, sensortype_args, comment) VALUES (?,?,?,?,?,?)");
		}

		public synchronized void resetSensorQueries() {
			sensor_select = serialid_select = sensor_insert = null;
		}

		public boolean isSensorAvailable() {
			return sensor_select!= null;
		}

		/**
		 * Executes the insertion of sensor mapping data into the database for a
		 * specified virtual sensor configuration.
		 *
		 * @param vSensorConfig The virtual sensor configuration associated with the
		 *                      deployment.
		 * @param pos           The position identifier.
		 * @param begin         The start timestamp of the sensor mapping.
		 * @param end           The end timestamp of the sensor mapping (null for
		 *                      ongoing mappings).
		 * @param type          The type of the sensor.
		 * @param typeArgs      Additional arguments related to the sensor type.
		 * @param comment       Additional comments for the sensor mapping.
		 * @param check         Flag indicating whether to perform additional validation
		 *                      checks.
		 * @return An ArrayList of Serializable arrays containing information about the
		 *         inserted sensor mapping(s).
		 * @throws Exception If an error occurs during the sensor mapping insertion
		 *                   process.
		 */
		public ArrayList<Serializable[]> executeSensorInsert(VSensorConfig vSensorConfig, Integer pos, Long begin,
				Long end, String type, Long typeArgs, String comment, boolean check) throws Exception {
			if (sensor_insert == null) {
				throw new Exception("no sensor mapping available");
			} else {
				synchronized (sensor_insert) {
					ArrayList<Serializable[]> ret = new ArrayList<Serializable[]>(2);
					if (check) {
						Statement h2Stat = h2DBconn.createStatement();
						StorageManager gsnsm = Main.getStorage(vSensorConfig);
						String rootType = type.split("_")[0];
						ResultSet rs;
						if (pos < 0) {
							throw new Exception("position must be a positive integer");
						}

						if (begin == null && end == null) {
							throw new Exception("at least either begin or end has to specified");
						} else if (begin != null && end != null) {
							if (begin >= end) {
								throw new Exception("begin must be smaller than end");
							}

							// check for overlapping positions in this time period
							rs = h2Stat.executeQuery("SELECT pk FROM " + deployment + "_sensor WHERE position = " +
									pos + " AND sensortype LIKE '" +
									rootType + "%' AND ((end IS NOT null AND ((" +
									begin + " >= begin AND " + begin + " < end) OR (" +
									end + " > begin AND " + end + " <= end))) OR (end IS null AND (begin > " +
									begin + " AND begin < " + end + "))) LIMIT 1");

							if (rs.next()) {
								throw new Exception("new sensor mapping overlaps with existing entry");
							}
						} else if (begin == null) {
							rs = h2Stat.executeQuery("SELECT * FROM " + deployment + "_sensor WHERE position = " +
									pos + " AND sensortype LIKE '" +
									type + "' AND end IS null AND begin < " + end);

							if (rs.next()) {
								if (!rs.isLast()) {
									logger.error("there are too many result sets");
								}

								long h2pk = rs.getLong(1);
								long h2begin = rs.getLong(3);
								String h2comment = rs.getString(7);

								gsnsm.executeUpdate(new StringBuilder("DELETE FROM " + vSensorConfig.getName() +
										" WHERE position = " + pos + " AND sensortype LIKE '" +
										type + "' AND end IS null AND begin < " + end), gsnsm.getConnection());

								if (comment.isEmpty()) {
									h2Stat.executeUpdate("UPDATE " + deployment + "_sensor SET end = " +
											end + " WHERE pk = " + h2pk);
									ret.add(new Serializable[] { pos, h2begin, end, type, typeArgs, h2comment });
								} else {
									h2Stat.executeUpdate("UPDATE " + deployment + "_sensor SET end = " +
											end + ", comment = '" + comment + "' WHERE pk = " + h2pk);
									ret.add(new Serializable[] { pos, h2begin, end, type, typeArgs, comment });
								}

								return ret;
							} else {
								throw new Exception(
										"new sensor mapping does not end an existing entry or sensortype is not matching");
							}
						} else if (end == null) {
							// check for overlapping position in this open time period
							rs = h2Stat.executeQuery("SELECT pk FROM " + deployment + "_sensor WHERE position = " +
									pos + " AND sensortype LIKE '" +
									rootType + "%' AND ((end IS null AND " +
									begin + " < begin) OR (end IS NOT null AND " +
									begin + " < end)) LIMIT 1");

							if (rs.next()) {
								throw new Exception("new sensor mapping overlaps with existing entry");
							}

						}

						rs = h2Stat.executeQuery("SELECT pk FROM " + deployment + "_sensor WHERE position = " +
								pos + " AND sensortype LIKE '" +
								rootType + "%' AND sensortype NOT LIKE '" +
								type + "' AND end IS null AND begin = " + begin);
						if (rs.next()) {
							throw new Exception(
									"the same begin for this position is already existing with an other similar sensortype");
						}

						rs = h2Stat.executeQuery("SELECT * FROM " + deployment + "_sensor WHERE position = " +
								pos + " AND sensortype LIKE '" +
								type + "' AND end IS null AND begin = " + begin);
						if (rs.next()) {
							if (!rs.isLast()) {
								logger.error("there are too many result sets");
							}

							if (end == null) {
								throw new Exception("the same begin for this position is already existing");
							} else {
								long h2pk = rs.getLong(1);
								String h2comment = rs.getString(7);

								gsnsm.executeUpdate(new StringBuilder("DELETE FROM " + vSensorConfig.getName() +
										" WHERE position = " + pos + " AND sensortype LIKE '" +
										type + "' AND end IS null AND begin = " + begin), gsnsm.getConnection());

								if (comment.isEmpty()) {
									h2Stat.executeUpdate("UPDATE " + deployment + "_sensor SET end = " +
											end + " WHERE pk = " + h2pk);
									ret.add(new Serializable[] { pos, begin, end, type, typeArgs, h2comment });
								} else {
									h2Stat.executeUpdate("UPDATE " + deployment + "_sensor SET end = " +
											end + ", comment = '" + comment + "' WHERE pk = " + h2pk);
									ret.add(new Serializable[] { pos, begin, end, type, typeArgs, comment });
								}
							}

							return ret;
						}

						rs = h2Stat.executeQuery("SELECT * FROM " + deployment + "_sensor WHERE position = " +
								pos + " AND sensortype LIKE '" +
								rootType + "%' AND end IS null AND begin < " + begin);

						if (rs.next()) {
							if (!rs.isLast()) {
								logger.error("there are too many result sets");
							}

							long h2pk = rs.getLong(1);
							int h2pos = rs.getInt(2);
							long h2begin = rs.getLong(3);
							String h2type = rs.getString(5);
							long h2typeargs = rs.getLong(6);
							String h2comment = rs.getString(7);
							h2Stat.executeUpdate("UPDATE " + deployment + "_sensor SET end = " +
									(begin - 1) + " WHERE pk = " + h2pk);

							gsnsm.executeUpdate(new StringBuilder("DELETE FROM " + vSensorConfig.getName() +
									" WHERE position = " + pos + " AND sensortype LIKE '" +
									rootType + "%' AND end IS null AND begin < " + begin), gsnsm.getConnection());

							ret.add(new Serializable[] { h2pos, h2begin, begin - 1, h2type, h2typeargs, h2comment });
						}
					}

					sensor_insert.setInt(1, pos);
					sensor_insert.setLong(2, begin);
					if (end == null) {
						sensor_insert.setNull(3, java.sql.Types.BIGINT);
					} else {
						sensor_insert.setLong(3, end);
					}

					sensor_insert.setString(4, type);
					if (typeArgs == null) {
						sensor_insert.setNull(5, java.sql.Types.BIGINT);
					} else {
						sensor_insert.setLong(5, typeArgs);
					}

					sensor_insert.setString(6, comment);
					sensor_insert.executeUpdate();
					ret.add(new Serializable[] { pos, begin, end, type, typeArgs, comment });

					return ret;
				}
			}

		}

		/**
		 * Executes a sensor select query and returns the result as a formatted string.
		 * 
		 * @param pos            The position of the sensor.
		 * @param generationTime The generation time of the sensor data.
		 * @return A formatted string containing the sensor data.
		 * @throws SQLException If an error occurs while executing the query.
		 */
		public String executeSensorSelect(int pos, long generationTime) throws SQLException {
			String sensor = null;
			StringBuffer sb = new StringBuffer();
			if (sensor_select != null) {
				synchronized (sensor_select) {
					sensor_select.setInt(1, pos);
					sensor_select.setLong(2, generationTime);
					sensor_select.setLong(3, generationTime);
					ResultSet rs = sensor_select.executeQuery();
					if (rs.first()) {
						do {
							sensor = rs.getString(1);
							sb.append(" " + sensor.substring(sensor.indexOf('_') + 1) + ":" + rs.getString(2));
						} while (rs.next());
						sensor = sb.toString();
					}
				}
			}
			return sensor;
		}

		/**
		 * Executes a query to retrieve the serial ID associated with a specific
		 * position and generation time.
		 *
		 * @param pos            The position identifier.
		 * @param generationTime The timestamp representing the generation time.
		 * @return The serial ID corresponding to the provided position and generation
		 *         time, or {@code null} if not found.
		 * @throws SQLException If an error occurs while querying the database for the
		 *                      serial ID.
		 */
		public Long executeSerialIdSelect(int pos, long generationTime) throws SQLException {
			Long serialid = null;
			if (serialid_select != null) {
				synchronized (serialid_select) {
					serialid_select.setInt(1, pos);
					serialid_select.setLong(2, generationTime);
					serialid_select.setLong(3, generationTime);
					ResultSet rs = serialid_select.executeQuery();
					if (rs.next()) {
						serialid = rs.getLong(1);
						if (rs.wasNull()) {
							serialid = null;
						}
					}
				}
			}
			return serialid;
		}

		/**
		 * Sets the conversion query for retrieving sensor data mapping.
		 * The query selects the physical signal, conversion, input, and value from the
		 * database based on the given parameters.
		 * 
		 * @throws SQLException if there is an error executing the SQL query
		 */
		public synchronized void setConversionQuery() throws SQLException {
			conversion_select = h2DBconn.prepareStatement(
					"SELECT st.physical_signal AS physical_signal, st.conversion AS conversion, st.input as input, CASEWHEN(st.input IS NULL OR sm.sensortype_args IS NULL,NULL,sta.value) as value "
							+
							"FROM " + deployment
							+ "_sensor AS sm, sensortype AS st, sensortype_args AS sta WHERE sm.position = ? AND ((sm.end is null AND sm.begin <= ?) OR (? BETWEEN sm.begin AND sm.end)) AND sm.sensortype = st.sensortype "
							+
							"AND st.signal_name = ? AND CASEWHEN(st.input IS NULL OR sm.sensortype_args IS NULL,TRUE,sm.sensortype_args = sta.sensortype_args AND sta.physical_signal = st.physical_signal) LIMIT 1");
		}

		public synchronized void resetConversionQuery() {
			conversion_select = null;
		}

		/**
		 * Executes a conversion select query to retrieve values based on the given
		 * parameters.
		 * 
		 * @param pos            The position parameter for the query.
		 * @param generationTime The generation time parameter for the query.
		 * @param conv           The conversion parameter for the query.
		 * @return An array of strings containing the retrieved values, or null if no
		 *         values are found.
		 * @throws SQLException If an error occurs while executing the query.
		 */
		public String[] executeConversionSelect(int pos, long generationTime, String conv) throws SQLException {
			String[] values = null;
			if (conversion_select != null) {
				synchronized (conversion_select) {
					conversion_select.setInt(1, pos);
					conversion_select.setLong(2, generationTime);
					conversion_select.setLong(3, generationTime);
					conversion_select.setString(4, conv);
					ResultSet rs = conversion_select.executeQuery();
					if (rs.next()) {
						values = new String[] { rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4) };
					}
				}
			}
			return values;
		}

		public boolean isConversionAvailable() {
			return conversion_select!= null;
		}
	}

	public class MappedEntry {
		public Integer deviceId;
		public Short deviceType;
		public boolean spotted;

		public MappedEntry(Integer id, Short type) {
			deviceId = id;
			deviceType = type;
			spotted = false;
		}
	}

	class MappingFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(".csv");
		}
	}
}
