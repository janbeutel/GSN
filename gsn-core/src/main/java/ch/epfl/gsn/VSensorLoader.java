/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2016, Ecole Polytechnique Federale de Lausanne (EPFL)
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
* File: src/ch/epfl/gsn/VSensorLoader.java
*
* @author Mehdi Riahi
* @author gsn_devs
* @author Sofiane Sarni
* @author Ali Salehi
* @author Mehdi Riahi
* @author Timotee Maret
* @author Julien Eberle
*
*/

package ch.epfl.gsn;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.InputStream;
import ch.epfl.gsn.beans.Modifications;
import ch.epfl.gsn.beans.StreamSource;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.wrappers.AbstractWrapper;
import ch.epfl.gsn.wrappers.WrappersUtil;

import ch.epfl.gsn.utils.graph.Graph;
import ch.epfl.gsn.utils.graph.Node;

import org.slf4j.Logger;

public class VSensorLoader extends Thread {

	public static final String VSENSOR_POOL = "VSENSOR-POOL";
	public static final String STREAM_SOURCE = "STREAM-SOURCE";
	public static final String INPUT_STREAM = "INPUT-STREAM";

	private String pluginsDir;
	private boolean isActive = true;
	private ArrayList<VSensorStateChangeListener> changeListeners = new ArrayList<VSensorStateChangeListener>();

	private static int VSENSOR_LOADER_THREAD_COUNTER = 0;
	private static VSensorLoader singleton = null;
	private static transient Logger logger = LoggerFactory.getLogger(VSensorLoader.class);

	/**
	 * Adds a VSensorStateChangeListener to the list of listeners.
	 * If the listener is not already in the list, it will be added.
	 *
	 * @param listener the VSensorStateChangeListener to be added
	 */
	public void addVSensorStateChangeListener(VSensorStateChangeListener listener) {
		if (!changeListeners.contains(listener)) {
			changeListeners.add(listener);
		}
	}

	/**
	 * Removes a VSensorStateChangeListener from the list of registered listeners.
	 *
	 * @param listener the VSensorStateChangeListener to be removed
	 */
	public void removeVSensorStateChangeListener(VSensorStateChangeListener listener) {
		changeListeners.remove(listener);
	}

	/**
	 * Fires the loading of a virtual sensor with the given configuration.
	 * Notifies all registered VSensorStateChangeListeners and returns true if all
	 * listeners
	 * successfully handle the loading event, otherwise returns false.
	 *
	 * @param config the configuration of the virtual sensor to be loaded
	 * @return true if all listeners handle the loading event successfully, false
	 *         otherwise
	 */
	public boolean fireVSensorLoading(VSensorConfig config) {
		for (VSensorStateChangeListener listener : changeListeners) {
			if (!listener.vsLoading(config)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Fires the event to unload a virtual sensor.
	 * 
	 * @param config the configuration of the virtual sensor to be unloaded
	 * @return true if the unloading is successful, false otherwise
	 */
	public boolean fireVSensorUnLoading(VSensorConfig config) {
		for (VSensorStateChangeListener listener : changeListeners) {
			if (!listener.vsUnLoading(config)) {
				logger.error("Unloading failed !",
						new RuntimeException("Unloading : " + config.getName() + " is failed."));
				return false;
			}
		}
		return true;
	}

	public VSensorLoader() {
	}

	public VSensorLoader(String pluginsPath) {
		this.pluginsDir = pluginsPath;
	}

	/**
	 * Gets the singleton instance of the VSensorLoader based on the specified path.
	 *
	 * If the singleton instance is not already created, a new VSensorLoader
	 * instance is instantiated using the provided path.
	 *
	 * @param path The path used to initialize the VSensorLoader instance.
	 * @return The singleton instance of the VSensorLoader.
	 */
	public static VSensorLoader getInstance(String path) {
		if (singleton == null) {
			singleton = new VSensorLoader(path);
		}

		return singleton;
	}

	/**
	 * Starts the loading process by creating a new thread and starting it.
	 */
	public void startLoading() {
		Thread thread = new Thread(this);
		thread.setName("VSensorLoader-Thread" + VSENSOR_LOADER_THREAD_COUNTER++);
		thread.start();
	}

	/**
	 * Executes the continuous loading of the plugin in a loop.
	 *
	 * This method runs in a loop, attempting to load the plugin at regular
	 * intervals. It checks if the default storage
	 * and window storage are defined before attempting to load the plugin. If
	 * either of them is null, an error message is logged.
	 *
	 * The loop continues until the {@code isActive} flag is set to false.
	 */
	public void run() {
		if (Main.getStorage((VSensorConfig) null) == null || Main.getWindowStorage() == null) { // Checks only if the
																								// default storage and
																								// the window storage
																								// are defined.
			logger.error("The Storage Manager shouldn't be null, possible a BUG.");
			return;
		}
		while (isActive) {
			try {
				loadPlugin();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Loads a Virtual Sensor from the given configuration content and file name.
	 *
	 * This method synchronizes the loading process to ensure thread safety. It
	 * first checks if a Virtual Sensor configuration
	 * file with the specified file name already exists. If not, it creates the
	 * configuration file and writes the provided
	 * configuration content. Next, it attempts to load the Virtual Sensor plugin
	 * using the specified file name. If the loading
	 * fails due to a syntax error in the configuration file, an exception is
	 * thrown, and the newly created configuration file
	 * is deleted.
	 *
	 * @param vsConfigurationFileContent The content of the Virtual Sensor
	 *                                   configuration file.
	 * @param fileName                   The name of the Virtual Sensor
	 *                                   configuration file.
	 * @throws Exception If an error occurs during the loading process, such as a
	 *                   syntax error in the configuration file.
	 */
	public synchronized void loadVirtualSensor(String vsConfigurationFileContent, String fileName) throws Exception {
		String filePath = getVSConfigurationFilePath(fileName);
		File file = new File(filePath);
		if (file.exists()) {
			logger.warn("The configuration file:" + filePath + " already exist.");
			throw new Exception("The configuration file:" + filePath + " already exist.");
		} else {
			try {
				// Create the VS configuration file
				Writer fw = new BufferedWriter(new FileWriter(filePath, true));
				fw.write(vsConfigurationFileContent);
				fw.flush();
				fw.close();
				// Try to load it
				if (!loadPlugin(fileName)) {
					throw new Exception("Failed to load the Virtual Sensor: " + fileName
							+ " because there is syntax error in the configuration file. Please check the configuration file and try again.");
				}
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
				if (file.exists()) {
					file.delete();
				}
				throw e;
			}
		}
	}

	/**
	 * Returns the file path for the virtual sensor configuration file with the
	 * specified file name.
	 *
	 * @param fileName the name of the virtual sensor configuration file (without
	 *                 the file extension)
	 * @return the file path for the virtual sensor configuration file
	 */
	public static String getVSConfigurationFilePath(String fileName) {
		return Main.virtualSensorDirectory + File.separator + fileName + ".xml";
	}

	/**
	 * Loads the plugins by updating the sensor configuration based on the
	 * modifications obtained from the plugins directory.
	 * This method synchronizes the loading process to ensure thread safety.
	 * It removes the virtual sensors specified in the remove list and adds the
	 * virtual sensors specified in the add list.
	 * If a virtual sensor has an initialization priority and is a root node in the
	 * sensor graph, it is added first.
	 *
	 * @throws SQLException if there is an error accessing the database
	 */
	public synchronized void loadPlugin() throws SQLException {

		Modifications modifications = getUpdateStatus(pluginsDir);
		ArrayList<VSensorConfig> removeIt = modifications.getRemove();
		ArrayList<VSensorConfig> addIt = modifications.getAdd();
		Graph<VSensorConfig> sensorGraph = modifications.getGraph();
		ArrayList<Node<VSensorConfig>> rootNodes = sensorGraph.getRootNodes();
		ArrayList<VSensorConfig> newadd = new ArrayList<VSensorConfig>();
		ArrayList<VSensorConfig> elementsToRemove = new ArrayList<>();

		for (VSensorConfig config : addIt) {
			if (config.hasInitPriority() && rootNodes.contains(sensorGraph.findNode(config))) {
				newadd.add(config);
				elementsToRemove.add(config);
			}
		}

		addIt.removeAll(elementsToRemove);
		newadd.addAll(addIt);

		for (VSensorConfig configFile : removeIt) {
			removeVirtualSensor(configFile);
		}

		for (VSensorConfig vs : newadd) {
			try {
				loadPlugin(vs);
			} catch (Exception e) {
				logger.error("Unable to load VSensor " + vs.getName() + ", retrying later... : " + e.getMessage());
			}
		}
	}

	/**
	 * Loads a plugin with the specified file filter name.
	 * 
	 * @param fileFilterName the name of the file filter
	 * @return true if the plugin was successfully loaded, false otherwise
	 * @throws SQLException if there is an error during the loading process
	 */
	public synchronized boolean loadPlugin(String fileFilterName) throws SQLException {
		Modifications modifications = getUpdateStatus(pluginsDir, fileFilterName);
		ArrayList<VSensorConfig> addIt = modifications.getAdd();

		boolean found = false;
		for (VSensorConfig config : addIt) {
			if (config.getName().equals(fileFilterName)) {
				found = true;
				break;
			}
		}
		if (found) {
			return loadPlugin(addIt.get(0));
		} else {
			return false;
		}

	}

	/**
	 * Loads a Virtual Sensor based on the provided VirtualSensorConfig.
	 *
	 * This method synchronizes the loading process to ensure thread safety. It
	 * validates the given VirtualSensorConfig
	 * to ensure it is in a valid state. If the configuration is valid, it creates a
	 * VirtualSensor instance and prepares
	 * input streams for it. It then checks if the corresponding table already
	 * exists in the database specified in the
	 * container configuration. If the table does not exist, it creates a new table
	 * using the output structure specified
	 * in the VirtualSensorConfig. If the table already exists and the
	 * "overwrite-tables" option is not set to true,
	 * an error message is logged. Finally, it adds the VirtualSensor instance to
	 * the Mappings and starts the VirtualSensor.
	 *
	 * @param vs The VirtualSensorConfig containing the configuration details for
	 *           the Virtual Sensor.
	 * @return {@code true} if the Virtual Sensor is successfully loaded,
	 *         {@code false} otherwise.
	 * @throws SQLException If an SQL error occurs during table creation or
	 *                      validation.
	 */
	private synchronized boolean loadPlugin(VSensorConfig vs) throws SQLException {

		if (!isVirtualSensorValid(vs)) {
			return false;
		}

		VirtualSensor pool = new VirtualSensor(vs);
		try {
			if (!createInputStreams(pool)) {
				logger.error("loading the >" + vs.getName()
						+ "< virtual sensor is stoped due to error(s) in preparing the input streams.");
				return false;
			}
		} catch (InstantiationException e2) {
			logger.error(e2.getMessage(), e2);
		} catch (IllegalAccessException e2) {
			logger.error(e2.getMessage(), e2);
		}
		try {
			if (!Main.getStorage(vs).tableExists(vs.getName(), vs.getOutputStructure())) {
				Main.getStorage(vs).executeCreateTable(vs.getName(), vs.getOutputStructure(),
						pool.getConfig().getIsTimeStampUnique());
			} else {
				logger.info("Reusing the existing " + vs.getName() + " table.");
			}

		} catch (Exception e) {
			removeAllVSResources(pool);
			if (e.getMessage().toLowerCase().contains("table already exists")) {
				logger.error("Loading the virtual sensor from " + vs.getFileName() + " failed, because the table "
						+ vs.getName() + " already exists in the database specified in "
						+ Main.getContainerConfig().getContainerFileName() + ".");
				logger.info("Solutions : ");
				logger.info("1. Change the virtual sensor name, in the : " + vs.getFileName());
				logger.info("2. Change the URL of the database in " + Main.getContainerConfig().getContainerFileName()
						+ " and choose another database.");
				logger.info("3. Rename/Move the table with the name : "
						+ Main.getContainerConfig().getContainerFileName() + " in the database.");
				logger.info(
						"4. Change the overwrite-tables=\"true\" (be careful, this will overwrite all the data previously saved in "
								+ vs.getName() + " table )");
			} else {
				logger.error(e.getMessage(), e);
			}
			return false;
		}
		logger.info("adding : " + vs.getName() + " virtual sensor[" + vs.getFileName() + "]");
		if (Mappings.addVSensorInstance(pool)) {
			try {
				fireVSensorLoading(pool.getConfig());
				pool.start();
			} catch (VirtualSensorInitializationFailedException e1) {
				logger.error("Creating the virtual sensor >" + vs.getName() + "< failed.", e1);
				removeVirtualSensor(vs);
				return false;
			}
		} else {
			removeAllVSResources(pool);
		}
		return true;

	}

	/**
	 * Removes a virtual sensor from the system.
	 * This method removes the specified virtual sensor configuration file,
	 * along with its associated resources.
	 *
	 * @param configFile The virtual sensor configuration file to be removed.
	 */
	private void removeVirtualSensor(VSensorConfig configFile) {
		logger.info("removing : " + configFile.getName());
		VirtualSensor sensorInstance = Mappings.getVSensorInstanceByFileName(configFile.getFileName());
		Mappings.removeFilename(configFile.getFileName());
		removeAllVSResources(sensorInstance);
	}

	/**
	 * Checks the validity of a Virtual Sensor configuration.
	 *
	 * This method validates the given VirtualSensorConfig to ensure its
	 * InputStreams are valid and do not contain
	 * any configuration errors. It also checks if the Virtual Sensor name is a
	 * valid Java identifier, ensuring it follows
	 * the specified requirements. Additionally, it checks if the Virtual Sensor
	 * name is unique within the existing
	 * Virtual Sensors. If any validation checks fail, appropriate error messages
	 * are logged.
	 *
	 * @param configuration The VirtualSensorConfig to be validated.
	 * @return {@code true} if the Virtual Sensor configuration is valid,
	 *         {@code false} otherwise.
	 */
	public boolean isVirtualSensorValid(VSensorConfig configuration) {
		for (InputStream is : configuration.getInputStreams()) {
			if (!is.validate()) {
				logger.error("Adding the virtual sensor specified in " + configuration.getFileName()
						+ " failed because of one or more problems in configuration file.");
				logger.info("Please check the file and try again");
				return false;
			}
		}
		String vsName = configuration.getName();
		if (Mappings.getVSensorConfig(vsName) != null) {
			logger.error("Adding the virtual sensor specified in " + configuration.getFileName()
					+ " failed because the virtual sensor name used by " +
					configuration.getFileName() + " is already used by : "
					+ Mappings.getVSensorConfig(vsName).getFileName());
			logger.info(
					"Note that the virtual sensor name is case insensitive and all the spaces in it's name will be removed automatically.");
			return false;
		}

		if (!isValidJavaIdentifier(vsName)) {
			logger.error("Adding the virtual sensor specified in " + configuration.getFileName()
					+ " failed because the virtual sensor name is not following the requirements : ");
			logger.info(
					"The virtual sensor name is case insensitive and all the spaces in it's name will be removed automatically.");
			logger.info(
					"That the name of the virutal sensor should starting by alphabetical character and they can contain numerical characters afterwards.");
			return false;
		}
		return true;
	}

	/**
	 * Checks if the given string is a valid Java identifier.
	 *
	 * @param name the string to be checked
	 * @return true if the string is a valid Java identifier, false otherwise
	 */
	static protected boolean isValidJavaIdentifier(final String name) {
		boolean valid = false;
		while (true) {
			if (!Character.isJavaIdentifierStart(name.charAt(0))) {
				break;
			}
			valid = true;
			final int count = name.length();
			for (int i = 1; i < count; i++) {
				if (!Character.isJavaIdentifierPart(name.charAt(i))) {
					valid = false;
					break;
				}
			}
			break;
		}
		return valid;
	}

	/**
	 * Removes all the resources associated with the given VirtualSensor.
	 * This method closes the pool, releases input streams, and fires the
	 * VSensorUnLoading event.
	 * 
	 * @param pool The VirtualSensor for which the resources need to be removed.
	 */
	public void removeAllVSResources(VirtualSensor pool) {
		VSensorConfig config = pool.getConfig();
		pool.closePool();
		final String vsensorName = config.getName();
		logger.info("Releasing previously used resources used by [" + vsensorName + "].");
		for (InputStream inputStream : config.getInputStreams()) {
			for (StreamSource streamSource : inputStream.getSources()) {
				releaseStreamSource(streamSource);
			}
			inputStream.release();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Total change Listeners:" + changeListeners.size());
		}
		
		fireVSensorUnLoading(pool.getConfig());
	}

	/**
	 * Releases the resources associated with the given StreamSource.
	 * This method removes the renaming mapping for the StreamSource's alias
	 * and removes the StreamSource as a listener from its wrapper.
	 *
	 * @param streamSource the StreamSource to release
	 */
	public void releaseStreamSource(StreamSource streamSource) {
		final AbstractWrapper wrapper = streamSource.getWrapper();
		streamSource.getInputStream().getRenamingMapping().remove(streamSource.getAlias());
		try {
			wrapper.removeListener(streamSource);
		} catch (SQLException e) {
			logger.error("Release the resources failed !" + e.getMessage());
		}
	}

	/**
	 * Retrieves the update status for Virtual Sensors from the specified path.
	 *
	 * @param virtualSensorsPath The path where Virtual Sensors are located. If
	 *                           {@code null}, the default path is used.
	 * @return The Modifications object containing information about the update
	 *         status.
	 */
	public static Modifications getUpdateStatus(String virtualSensorsPath) {
		return getUpdateStatus(virtualSensorsPath, null);
	}

	/**
	 * Retrieves the update status for Virtual Sensors from the specified path with
	 * an optional filter on the file name.
	 *
	 * This method checks for updates in the Virtual Sensors located at the given
	 * path. If the path is not specified,
	 * it uses the default path. The update status includes information about
	 * modifications made to the Virtual Sensors,
	 * such as additions, updates, or deletions. The optional {@code filterFileName}
	 * parameter allows filtering the
	 * update status based on a specific file name.
	 *
	 * @param virtualSensorsPath The path where Virtual Sensors are located. If
	 *                           {@code null}, the default path is used.
	 * @param filterFileName     Optional filter for the file name. If not
	 *                           {@code null}, only updates related to the
	 *                           specified file name are considered.
	 * @return The Modifications object containing information about the update
	 *         status.
	 */
	public static Modifications getUpdateStatus(String virtualSensorsPath, String filterFileName) {
		ArrayList<String> remove = new ArrayList<String>();
		ArrayList<String> add = new ArrayList<String>();

		String[] previous = Mappings.getAllKnownFileName();

		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {
				if (!file.isDirectory() && file.getName().endsWith(".xml") && !file.getName().startsWith(".")) {
					return true;
				}
				return false;
			}
		};

		File files[] = new File(virtualSensorsPath).listFiles(filter);

		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File a, File b) {
				return a.getName().compareTo(b.getName());
			}
		});

		// --- preparing the remove list
		// Removing those in the previous which are not existing the new files
		// or modified.
		main: for (String pre : previous) {
			for (File curr : files) {
				if (pre.equals(curr.getAbsolutePath()) && (Mappings.getLastModifiedTime(pre) == curr.lastModified())) {
					continue main;
				}
			}
			remove.add(pre);
		}
		// ---adding the new files to the Add List a new file should added if
		//
		// 1. it's just deployed.
		// 2. it's modification time changed.

		main: for (File cur : files) {
			for (String pre : previous) {
				if (cur.getAbsolutePath().equals(pre) && (cur.lastModified() == Mappings.getLastModifiedTime(pre))) {
					continue main;
				}
			}
			add.add(cur.getAbsolutePath());
		}
		return new Modifications(add, remove);

	}

	/**
	 * The properties file contains information on wrappers for stream sources.
	 * FIXME : The body of CreateInputStreams is incomplete b/c in the case of an
	 * error it should remove the resources.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public boolean createInputStreams(VirtualSensor pool) throws InstantiationException, IllegalAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug(new StringBuilder().append("Preparing input streams for: ").append(pool.getConfig().getName())
				.toString());
		}
		if (pool.getConfig().getInputStreams().size() == 0) {
			logger.warn(new StringBuilder("There is no input streams defined for *").append(pool.getConfig().getName())
					.append("*").toString());
		}
		ArrayList<StreamSource> sources = new ArrayList<StreamSource>();
		ArrayList<InputStream> streams = new ArrayList<InputStream>();
		for (Iterator<InputStream> inputStreamIterator = pool.getConfig().getInputStreams()
				.iterator(); inputStreamIterator.hasNext();) {
			InputStream inputStream = inputStreamIterator.next();
			for (StreamSource dataSouce : inputStream.getSources()) {
				if (!prepareStreamSource(pool.getConfig(), inputStream, dataSouce)) {
					for (StreamSource ss : sources) {
						releaseStreamSource(ss);
					}
					for (InputStream is : streams) {
						is.release();
					}
					return false;
				}
				sources.add(dataSouce);
			}
			streams.add(inputStream);
			inputStream.setPool(pool);
		}
		return true;
	}

	/**
	 * Instantiate the wrapper from its addressBean.
	 * if it doesn't return null, the calling class is responsible for releasing the
	 * resources of the wrapper.
	 * 
	 * @param addressBean
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 *                                FIXME: COPIED_FOR_SAFE_STOAGE
	 */
	public AbstractWrapper createWrapper(AddressBean addressBean)
			throws InstantiationException, IllegalAccessException {

		if (Main.getWrapperClass(addressBean.getWrapper()) == null) {
			logger.error("The wrapper >" + addressBean.getWrapper() + "< is not defined in the >"
					+ WrappersUtil.DEFAULT_WRAPPER_PROPERTIES_FILE + "< file.");
			return null;
		}
		AbstractWrapper wrapper = (AbstractWrapper) Main.getWrapperClass(addressBean.getWrapper()).newInstance();
		wrapper.setActiveAddressBean(addressBean);
		boolean initializationResult = wrapper.initialize_wrapper();
		if (!initializationResult) {
			return null;
		}
		try {
			if(logger.isDebugEnabled()){
				logger.debug("Wrapper name: " + wrapper.getWrapperName() + " -- view name " + wrapper.getDBAliasInStr());
			}
			if (!Main.getWindowStorage().tableExists(wrapper.getDBAliasInStr(), wrapper.getOutputFormat())) {
				Main.getWindowStorage().executeCreateTable(wrapper.getDBAliasInStr(), wrapper.getOutputFormat(),
						wrapper.isTimeStampUnique());
			}
		} catch (Exception e) {
			try {
				wrapper.releaseResources(); // releasing resources
			} catch (SQLException sql) {
				logger.error(sql.getMessage(), sql);
			}
			logger.error(e.getMessage(), e);
			return null;
		}
		return wrapper;
	}

	/**
	 * Prepares a StreamSource associated with an InputStream in the context of a
	 * VirtualSensor configuration.
	 *
	 * This method initializes the provided StreamSource by associating it with the
	 * given InputStream and
	 * configuring addressing details. It iterates over the addressing information
	 * of the StreamSource,
	 * creating a corresponding wrapper using the provided AddressBean, and attempts
	 * to prepare the StreamSource
	 * with the wrapper's output format. If the preparation is successful, it breaks
	 * out of the loop, and the
	 * associated wrapper is retained. If any errors occur during the preparation,
	 * the method logs an error message
	 * and releases acquired resources.
	 *
	 * @param vsensorConfig The configuration of the VirtualSensor.
	 * @param inputStream   The InputStream associated with the StreamSource.
	 * @param streamSource  The StreamSource to be prepared.
	 * @return {@code true} if the preparation is successful, {@code false}
	 *         otherwise.
	 * @throws InstantiationException If an instantiation error occurs during the
	 *                                process.
	 * @throws IllegalAccessException If an access error occurs during the process.
	 */
	public boolean prepareStreamSource(VSensorConfig vsensorConfig, InputStream inputStream, StreamSource streamSource)
			throws InstantiationException, IllegalAccessException {
		streamSource.setInputStream(inputStream);
		AbstractWrapper wrapper = null;
		for (AddressBean addressBean : streamSource.getAddressing()) {
			addressBean.setInputStreamName(inputStream.getInputStreamName());
			addressBean.setVirtualSensorName(vsensorConfig.getName());
			addressBean.setVirtualSensorConfig(vsensorConfig);
			wrapper = createWrapper(addressBean);
			try {
				if (wrapper != null && prepareStreamSource(streamSource, wrapper.getOutputFormat(), wrapper)) {
					break;
				} else {
					if (wrapper != null) {
						wrapper.releaseResources();
					}
					wrapper = null;
				}

			} catch (Exception e) {
				if (wrapper != null) {
					try {
						wrapper.releaseResources();
					} catch (SQLException sql) {
						logger.error(sql.getMessage(), sql);
					}
				}
				logger.error("Preparation of the stream source failed : " + streamSource.getAlias()
						+ " from the input stream : " + inputStream.getInputStreamName() + ". " + e.getMessage(), e);
			}
		}
		return wrapper != null;
	}

	/**
	 * Prepares the stream source by setting the wrapper, adding renaming mapping,
	 * and validating the output format.
	 * 
	 * @param streamSource the stream source to prepare
	 * @param outputformat the desired output format
	 * @param wrapper      the wrapper to use
	 * @return true if the stream source is prepared successfully, false otherwise
	 * @throws InstantiationException if an error occurs during instantiation
	 * @throws IllegalAccessException if an error occurs due to illegal access
	 * @throws SQLException           if an error occurs during SQL operations
	 */
	public boolean prepareStreamSource(StreamSource streamSource, DataField[] outputformat, AbstractWrapper wrapper)
			throws InstantiationException, IllegalAccessException, SQLException {
		if (outputformat == null) {
			logger.error("Preparing the stream source failed because the wrapper : " + wrapper.getWrapperName()
					+ " returns null for the >getOutputStructure< method!");
			return false;
		}
		streamSource.setWrapper(wrapper);
		streamSource.getInputStream().addToRenamingMapping(streamSource.getAlias(), streamSource.getUIDStr());
		return true;
	}

	/**
	 * Stops the loading process of the VSensorLoader.
	 * This method sets the isActive flag to false, interrupts the thread, and
	 * removes all associated resources for each virtual sensor.
	 * It also shuts down the window storage and storage for each VSensorConfig.
	 * 
	 * @throws SQLException if there is an error during the shutdown of the storage.
	 */
	public void stopLoading() {
		this.isActive = false;
		this.interrupt();
		for (String configFile : Mappings.getAllKnownFileName()) {
			VirtualSensor sensorInstance = Mappings.getVSensorInstanceByFileName(configFile);
			removeAllVSResources(sensorInstance);
			logger.info("Removing the resources associated with : " + sensorInstance.getConfig().getFileName()
					+ " [done].");
		}
		try {
			Main.getWindowStorage().shutdown();
			Iterator<VSensorConfig> iter = Mappings.getAllVSensorConfigs();
			while (iter.hasNext()) {
				Main.getStorage(iter.next()).shutdown();
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
