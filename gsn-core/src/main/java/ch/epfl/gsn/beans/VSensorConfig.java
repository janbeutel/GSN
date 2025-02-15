/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2016, Ecole Polytechnique Federale de Lausanne (EPFL)
* Copyright (c) 2020-2023, University of Innsbruck
* 
* This file is part of GSN.
* 
* GSN is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* GSN is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with GSN.  If not, see <http://www.gnu.org/licenses/>.
* 
* File: src/ch/epfl/gsn/beans/VSensorConfig.java
*
* @author Jerome Rousselot
* @author Mehdi Riahi
* @author gsn_devs
* @author Ali Salehi
* @author Timotee Maret
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.beans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.collections.KeyValue;
import org.slf4j.LoggerFactory;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.utils.CaseInsensitiveComparator;
import ch.epfl.gsn.utils.Pair;
import ch.epfl.gsn.utils.Utils;

import org.slf4j.Logger;

public class VSensorConfig implements Serializable {

	private static final long serialVersionUID = 1625382440863797197L;

	public static final int DEFAULT_PRIORITY = 100;

	public static final int NO_FIXED_RATE = 0;

	public static final int DEFAULT_POOL_SIZE = 10;

	public static final boolean DEFAULT_STATISTICS = false;

	private String name;

	private int priority = DEFAULT_PRIORITY;

	private boolean initPriority = false;

	private String mainClass;

	private String description;

	@Deprecated
	private int lifeCyclePoolSize = DEFAULT_POOL_SIZE;

	private int outputStreamRate;

	private KeyValue[] addressing;

	private DataField[] outputStructure;

	private String webParameterPassword = null;

	private String storageHistorySize = null;

	private final HashMap<String, InputStream> inputStreamNameToInputStreamObjectMapping = new HashMap<String, InputStream>();

	private InputStream inputStreams[];

	private ArrayList<KeyValue> mainClassInitialParams = new ArrayList<KeyValue>();

	private transient Long lastModified;

	private String fileName;

	private StorageConfig storage;

	private String timeZone;
	private SimpleDateFormat sdf = null;

	private transient final Logger logger = LoggerFactory.getLogger(VSensorConfig.class);

	private String directoryQuery;

	private WebInput[] webinput;

	private String sensorMap = "false";

	private String access_protected = "false";
	private Boolean stats = null;
	private String statistics = Boolean.toString(DEFAULT_STATISTICS);
	private boolean nameInitialized = false;
	private boolean isStorageCountBased = true;
	public static final int STORAGE_SIZE_NOT_SET = -1;
	private long parsedStorageSize = STORAGE_SIZE_NOT_SET;
	private transient Double cached_altitude = null;
	private transient Double cached_longitude = null;
	private transient Double cached_latitude = null;
	private boolean addressing_processed = false;

	private String chunkSize;

	private boolean isTimestampUnique = false;

	private boolean isGetMainClassInitParamsInitialized = false;

	private final TreeMap<String, String> mainClassInitParams = new TreeMap<String, String>(
			new CaseInsensitiveComparator());

	/**
	 * @return Returns the addressing.
	 */
	public KeyValue[] getAddressing() {
		return this.addressing;
	}

	/**
	 * Returns a two-dimensional array representing the RPC-friendly addressing of
	 * the VSensorConfig.
	 * Each row in the array contains two elements: the key and the value of a
	 * KeyValue object in the addressing array.
	 * 
	 * @return a two-dimensional array representing the RPC-friendly addressing
	 */
	public String[][] getRPCFriendlyAddressing() {
		String[][] toReturn = new String[this.addressing.length][2];
		for (int i = 0; i < toReturn.length; i++) {
			for (KeyValue val : this.addressing) {
				toReturn[i][0] = (String) val.getKey();
				toReturn[i][1] = (String) val.getValue();
			}
		}

		return toReturn;
	}

	/**
	 * Returns the output structure of the sensor in a format suitable for RPC
	 * communication.
	 * The output structure is represented as a 2D array of strings, where each row
	 * contains the name and type of an output.
	 *
	 * @return the output structure of the sensor in a RPC-friendly format
	 */
	public String[][] getRPCFriendlyOutputStructure() {
		String[][] toReturn = new String[this.outputStructure.length][2];
		for (int i = 0; i < outputStructure.length; i++) {
			toReturn[i][0] = (String) outputStructure[i].getName();
			toReturn[i][1] = (String) outputStructure[i].getType();
		}
		return toReturn;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return Returns the inputStreams.
	 */
	public Collection<InputStream> getInputStreams() {
		return this.inputStreamNameToInputStreamObjectMapping.values();
	}

	public InputStream getInputStream(final String inputStreamName) {
		return this.inputStreamNameToInputStreamObjectMapping.get(inputStreamName);
	}

	/**
	 * @Deprecated
	 * @return Returns the lifeCyclePoolSize.
	 */
	public int getLifeCyclePoolSize() {
		return this.lifeCyclePoolSize;
	}

	/**
	 * @return Returns the mainClass.
	 */
	public String getProcessingClass() {
		if (this.mainClass == null) {
			this.mainClass = "ch.epfl.gsn.vsensor.BridgeVirtualSensor";
		}
		return this.mainClass;
	}

	/**
	 * Returns the name of the VSensorConfig.
	 * If the name has not been initialized, it removes spaces, trims leading and
	 * trailing whitespace,
	 * and converts the name to lowercase before returning it.
	 *
	 * @return the name of the VSensorConfig
	 */
	public String getName() {
		if (!this.nameInitialized) {
			this.name = this.name.replace(" ", "").trim().toLowerCase();
			this.nameInitialized = true;
		}
		return this.name;
	}

	/**
	 * @return Returns the outputStreamRate.
	 */
	public int getOutputStreamRate() {
		return this.outputStreamRate;
	}

	/**
	 * @return Returns the outputStructure.
	 */
	public DataField[] getOutputStructure() {
		return this.outputStructure;
	}

	/**
	 * @return Returns the priority.
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * @return Returns the initPriority.
	 */
	public boolean hasInitPriority() {
		return this.initPriority;
	}

	public Long getLastModified() {
		return this.lastModified;
	}

	/**
	 * @param addressing The addressing to set.
	 */
	public void setAddressing(KeyValue[] addressing) {
		this.addressing = addressing;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(final Long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @Deprecated
	 * @param lifeCyclePoolSize The lifeCyclePoolSize to set.
	 */
	public void setLifeCyclePoolSize(final int lifeCyclePoolSize) {
		this.lifeCyclePoolSize = lifeCyclePoolSize;
	}

	/**
	 * @param mainClass The mainClass to set.
	 */
	public void setMainClass(final String mainClass) {
		this.mainClass = mainClass;
	}

	/**
	 * @param virtualSensorName The name to set.
	 */
	public void setName(final String virtualSensorName) {
		this.name = virtualSensorName;
	}

	/**
	 * @param outputStreamRate The outputStreamRate to set.
	 */
	public void setOutputStreamRate(final int outputStreamRate) {
		this.outputStreamRate = outputStreamRate;
	}

	/**
	 * @param outputStructure The outputStructure to set.
	 */
	public void setOutputStructure(DataField[] outputStructure) {
		this.outputStructure = outputStructure;
	}

	/**
	 * @param priority The priority to set.
	 */
	public void setPriority(final int priority) {
		this.priority = priority;
	}

	/**
	 * @param initPriority set if the file has an initPriority
	 */
	public void setInitPriority(final boolean initPriority) {
		this.initPriority = initPriority;
	}

	/**
	 * Returns an array of addressing keys.
	 *
	 * @return the array of addressing keys
	 */
	public String[] getAddressingKeys() {
		final String result[] = new String[this.getAddressing().length];
		int counter = 0;
		for (final KeyValue predicate : this.getAddressing()) {
			result[counter++] = (String) predicate.getKey();
		}

		return result;
	}

	/**
	 * Returns an array of addressing values.
	 *
	 * @return the array of addressing values
	 */
	public String[] getAddressingValues() {
		final String result[] = new String[this.getAddressing().length];
		int counter = 0;
		for (final KeyValue predicate : this.getAddressing()) {
			result[counter++] = (String) predicate.getValue();
		}

		return result;
	}

	/**
	 * Note that the key and value both are trimmed before being inserted into
	 * the data strcture.
	 * 
	 * @return
	 */
	public TreeMap<String, String> getMainClassInitialParams() {
		if (!this.isGetMainClassInitParamsInitialized) {
			this.isGetMainClassInitParamsInitialized = true;
			for (final KeyValue param : this.mainClassInitialParams) {
				this.mainClassInitParams.put(param.getKey().toString().toLowerCase(), param.getValue().toString());
			}
		}
		return this.mainClassInitParams;
	}

	public void setMainClassInitialParams(final ArrayList<KeyValue> mainClassInitialParams) {
		this.mainClassInitialParams = mainClassInitialParams;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return Returns the storageHistorySize.
	 */
	public String getStorageHistorySize() {
		if (storageHistorySize == null) {
			if (storage == null || storage.getStorageSize() == null || storage.getStorageSize().trim().equals("")) {
				storageHistorySize = "" + STORAGE_SIZE_NOT_SET;
			} else {
				storageHistorySize = storage.getStorageSize();
			}

		}
		return storageHistorySize;
	}

	/**
	 * Validates the configuration of the virtual sensor.
	 * This method checks if the storage size is valid and sets the parsed storage
	 * size and storage count based flag accordingly.
	 * 
	 * @return true if the configuration is valid, false otherwise.
	 */
	public boolean validate() {
		for (final InputStream inputStream : this.inputStreams) {
			this.inputStreamNameToInputStreamObjectMapping.put(inputStream.getInputStreamName(), inputStream);
		}
		try {
			Pair<Boolean, Long> p = Utils.parseWindowSize(this.getStorageHistorySize());
			this.parsedStorageSize = p.getSecond();
			this.isStorageCountBased = !p.getFirst();
		} catch (final NumberFormatException e) {
			logger.error("The storage size, " + storageHistorySize + ", specified for the virtual sensor : " + name
					+ " is not valid.", e);
			return false;
		}
		return true;
	}

	public StorageConfig getStorage() {
		return storage;
	}

	public void setStorage(StorageConfig sc) {
		this.storage = sc;
	}

	public boolean isStorageCountBased() {
		return this.isStorageCountBased;
	}

	public long getParsedStorageSize() {
		return this.parsedStorageSize;
	}

	public String getChunkSize(){
		return chunkSize;
	}

	public void setChunkSize(String chunkSize){
		this.chunkSize = chunkSize;
	}

	public String getDirectoryQuery() {
		return directoryQuery;
	}

	/**
	 * @return the securityCode
	 */
	public String getWebParameterPassword() {
		return webParameterPassword;
	}

	public void setWebParameterPassword(String p) {
		webParameterPassword = p;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("Input Stream [");
		for (final InputStream inputStream : this.getInputStreams()) {
			builder.append("Input-Stream-Name").append(inputStream.getInputStreamName());
			builder.append("Input-Stream-Query").append(inputStream.getQuery());
			builder.append(" Stream-Sources ( ");
			if (inputStream.getSources() == null) {
				builder.append("null");
			} else {
				for (final StreamSource ss : inputStream.getSources()) {
					builder.append("Stream-Source Alias : ").append(ss.getAlias());
					for (final AddressBean addressing : ss.getAddressing()) {
						builder.append("Stream-Source-wrapper >").append(addressing.getWrapper())
								.append("< with addressign predicates : ");
						for (final KeyValue keyValue : addressing.getPredicates()) {
							builder.append("Key=").append(keyValue.getKey()).append("Value=")
									.append(keyValue.getValue());
						}

					}
					builder.append(" , ");
				}
			}

			builder.append(")");
		}
		builder.append("]");
		return "VSensorConfig{" + "name='" + this.name + '\'' + ", priority=" + this.priority + ", mainClass='"
				+ this.mainClass + '\''
				+ ", description='" + this.description + '\'' + ", outputStreamRate=" + this.outputStreamRate
				+ ", addressing=" + this.addressing + ", outputStructure=" + this.outputStructure
				+ ", storageHistorySize='" + this.storageHistorySize + '\'' + builder.toString()
				+ ", mainClassInitialParams=" + this.mainClassInitialParams + ", lastModified=" + this.lastModified
				+ ", fileName='" + this.fileName + '\'' + ", logger=" + this.logger + ", nameInitialized="
				+ this.nameInitialized + ", isStorageCountBased=" + this.isStorageCountBased + ", parsedStorageSize="
				+ this.parsedStorageSize + '}';
	}

	public boolean equals(Object obj) {
		if (obj instanceof VSensorConfig) {
			VSensorConfig vSensorConfig = (VSensorConfig) obj;
			return name.equals(vSensorConfig.getName());
		}
		return false;
	}

	public int hashCode() {
		if (name == null) {
			return super.hashCode();
		} else {
			return name.hashCode();
		}
	}

	// time zone

	public SimpleDateFormat getSDF() {
		if (timeZone == null) {
			return null;
		} else {
			if (sdf == null) {
				sdf = new SimpleDateFormat(Main.getContainerConfig().getTimeFormat());
				sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
			}
		}
		return sdf;
	}

	/**
	 * @return if statistics should be produced
	 */
	public boolean isProducingStatistics() {
		if (stats == null) {
			if (statistics == null) {
				stats = DEFAULT_STATISTICS;
			} else {
				stats = Boolean.parseBoolean(statistics.trim());
			}

		}
		return stats;
	}

	/**
	 * @return the webinput
	 */
	public WebInput[] getWebinput() {
		return webinput;
	}

	public void setWebInput(WebInput[] webInput) {
		this.webinput = webInput;
	}

	public void setInputStreams(InputStream... inputStreams) {
		this.inputStreams = inputStreams;
	}

	public void setStorageHistorySize(String storageHistorySize) {
		this.storageHistorySize = storageHistorySize;
	}

	public boolean getPublishToSensorMap() {
		if (sensorMap == null) {
			return false;
		}

		return Boolean.parseBoolean(sensorMap);
	}

	/**
	 * Preprocesses the addressing information of the VSensorConfig.
	 * This method parses the addressing key-value pairs and extracts the altitude,
	 * longitude, and latitude values.
	 * The extracted values are stored in the corresponding cached variables.
	 * This method should be called before accessing the cached variables.
	 */
	public void preprocess_addressing() {
		if (!addressing_processed) {
			for (KeyValue kv : getAddressing()) {
				if (kv.getKey().toString().equalsIgnoreCase("altitude")) {
					cached_altitude = Double.parseDouble(kv.getValue().toString());
				} else if (kv.getKey().toString().equalsIgnoreCase("longitude")) {
					cached_longitude = Double.parseDouble(kv.getValue().toString());
				} else if (kv.getKey().toString().equalsIgnoreCase("latitude")) {
					cached_latitude = Double.parseDouble(kv.getValue().toString());
				}

			}

			addressing_processed = true;
		}
	}

	public Double getAltitude() {
		preprocess_addressing();
		return cached_altitude;
	}

	public Double getLatitude() {
		preprocess_addressing();
		return cached_latitude;
	}

	public Double getLongitude() {
		preprocess_addressing();
		return cached_longitude;
	}

	public boolean getIsTimeStampUnique() {
		return isTimestampUnique;
	}

	public void setIsTimeStampUnique(boolean unique) {
		this.isTimestampUnique = unique;
	}

	public boolean isAccess_protected() {
		try {
			return Boolean.parseBoolean(access_protected.trim());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

}