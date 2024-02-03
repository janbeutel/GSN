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
* @author parobert
* @author cl3m
* @author Jerome Rousselot
* @author gsn_devs
* @author Mehdi Riahi
* @author Ali Salehi
* @author Behnaz Bostanipour
* @author Timotee Maret
* @author Julien Eberle
* @author Tonio Gsell
* @author Mustafa Yuecel
* @author Davide Desclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn;

import ch.epfl.gsn.config.GsnConf;
import ch.epfl.gsn.config.VsConf;
import ch.epfl.gsn.data.DataStore;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.SplashScreen;
import java.io.File;
// import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.zeromq.ZContext;

// import ch.epfl.gsn.ContainerImpl;
// import ch.epfl.gsn.DataDistributer;
// import ch.epfl.gsn.Mappings;
// import ch.epfl.gsn.VSensorLoader;
import ch.epfl.gsn.beans.BeansInitializer;
import ch.epfl.gsn.beans.ContainerConfig;
import ch.epfl.gsn.beans.StorageConfig;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.delivery.LocalDeliveryWrapper;
import ch.epfl.gsn.monitoring.MemoryMonitor;
import ch.epfl.gsn.monitoring.Monitorable;
import ch.epfl.gsn.monitoring.MonitoringServer;
import ch.epfl.gsn.networking.zeromq.ZeroMQDeliveryAsync;
import ch.epfl.gsn.networking.zeromq.ZeroMQDeliverySync;
import ch.epfl.gsn.networking.zeromq.ZeroMQProxy;
import ch.epfl.gsn.storage.SQLValidator;
import ch.epfl.gsn.storage.StorageManager;
import ch.epfl.gsn.storage.StorageManagerFactory;
import ch.epfl.gsn.storage.hibernate.DBConnectionInfo;
import ch.epfl.gsn.utils.ValidityTools;
import ch.epfl.gsn.vsensor.SQLValidatorIntegration;
import ch.epfl.gsn.wrappers.WrappersUtil;

//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;

public final class Main {

	public static final int DEFAULT_MAX_DB_CONNECTIONS = 128;
	public static final String DEFAULT_GSN_CONF_FOLDER = "../conf";
	public static final String DEFAULT_VIRTUAL_SENSOR_FOLDER = "../conf/virtual-sensors";
	public static transient Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * Mapping between the wrapper name (used in addressing of stream source)
	 * into the class implementing DataSource.
	 */
	private static Properties wrappers;
	private static Main singleton;
	public static String gsnConfFolder = DEFAULT_GSN_CONF_FOLDER;
	public static String virtualSensorDirectory = DEFAULT_VIRTUAL_SENSOR_FOLDER;
	private static ZeroMQProxy zmqproxy;
	private static StorageManager mainStorage;
	private static StorageManager windowStorage;
	private static StorageManager validationStorage;
	private static ZContext zmqContext = new ZContext();
	private static HashMap<Integer, StorageManager> storages = new HashMap<Integer, StorageManager>();
	private static HashMap<VSensorConfig, StorageManager> storagesConfigs = new HashMap<VSensorConfig, StorageManager>();
	private ContainerConfig containerConfig;
	private MonitoringServer monitoringServer;
	private static VSensorLoader vsLoader;
	private static GsnConf gsnConf;
	private static Map<String, VsConf> vsConf = new HashMap<String, VsConf>();
	private static ArrayList<Monitorable> toMonitor = new ArrayList<Monitorable>();

	/*
	 * Retrieving ThreadMXBean instance of JVM
	 * It would be used for monitoring CPU time of each virtual sensor
	 */

	private static ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

	/**
	 * The Main class represents the entry point of the GSN (Global Sensor Networks)
	 * application.
	 * It initializes the necessary components, loads the configuration, and starts
	 * the application.
	 * 
	 * @throws Exception if an error occurs during the initialization process
	 */
	private Main() throws Exception {

		ValidityTools.checkAccessibilityOfFiles(WrappersUtil.DEFAULT_WRAPPER_PROPERTIES_FILE,
				gsnConfFolder + "/gsn.xml");
		ValidityTools.checkAccessibilityOfDirs(virtualSensorDirectory);
		containerConfig = loadContainerConfiguration();
		updateSplashIfNeeded(
				new String[] { "GSN is starting...", "All GSN logs are available at: logs/ch.epfl.gsn.log" });
		System.out.println("Global Sensor Networks (GSN) is starting...");

		int maxDBConnections = containerConfig.getMaxDBConnections();
		int maxSlidingDBConnections = containerConfig.getMaxSlidingDBConnections();

		DataStore ds = new DataStore(gsnConf);

		mainStorage = StorageManagerFactory.getInstance(containerConfig.getStorage().getJdbcDriver(),
				containerConfig.getStorage().getJdbcUsername(), containerConfig.getStorage().getJdbcPassword(),
				containerConfig.getStorage().getJdbcURL(), maxDBConnections);

		StorageConfig sc = containerConfig.getSliding() == null ? containerConfig.getStorage(): containerConfig.getSliding().getStorage();
		windowStorage = StorageManagerFactory.getInstance(sc.getJdbcDriver(), sc.getJdbcUsername(),
				sc.getJdbcPassword(), sc.getJdbcURL(), maxSlidingDBConnections);

		validationStorage = StorageManagerFactory.getInstance("org.h2.Driver", "sa", "", "jdbc:h2:mem:validator",
				Main.DEFAULT_MAX_DB_CONNECTIONS);

		logger.trace("The Container Configuration file loaded successfully.");

		// starting the monitoring socket
		toMonitor.add(new MemoryMonitor());
		monitoringServer = new MonitoringServer(containerConfig.getMonitorPort());
		monitoringServer.start();

		if (containerConfig.isZMQEnabled()) {
			// start the 0MQ proxy
			zmqproxy = new ZeroMQProxy(containerConfig.getZMQProxyPort(), containerConfig.getZMQMetaPort());
		}

		VSensorLoader vsloader = VSensorLoader.getInstance(virtualSensorDirectory);
		File vsDir = new File(virtualSensorDirectory);
		for (File f : vsDir.listFiles()) {
			if (f.getName().endsWith(".xml")) {
				VsConf vs = VsConf.load(f.getPath());
				vsConf.put(vs.name(), vs);
			}
		}
		Main.vsLoader = vsloader;

		vsloader.addVSensorStateChangeListener(new SQLValidatorIntegration(SQLValidator.getInstance()));
		vsloader.addVSensorStateChangeListener(DataDistributer.getInstance(LocalDeliveryWrapper.class));
		if (containerConfig.isZMQEnabled()) {
			vsloader.addVSensorStateChangeListener(DataDistributer.getInstance(ZeroMQDeliverySync.class));
			vsloader.addVSensorStateChangeListener(DataDistributer.getInstance(ZeroMQDeliveryAsync.class));
		}

		ContainerImpl.getInstance().addVSensorDataListener(DataDistributer.getInstance(LocalDeliveryWrapper.class));
		ContainerImpl.getInstance().addVSensorDataListener(DataDistributer.getInstance(ZeroMQDeliverySync.class));
		ContainerImpl.getInstance().addVSensorDataListener(DataDistributer.getInstance(ZeroMQDeliveryAsync.class));
		vsloader.startLoading();

	}

	/**
	 * Closes the splash screen if it is visible.
	 * If the application is running in headless mode or no splash screen is
	 * specified, this method does nothing.
	 */
	private static void closeSplashIfneeded() {
		if (isHeadless()) {
			return;
		}
		SplashScreen splash = SplashScreen.getSplashScreen();
		// Check if we have specified any splash screen
		if (splash == null) {
			return;
		}
		if (splash.isVisible()) {
			splash.close();
		}

	}

	/**
	 * Updates the splash screen if needed with the given message.
	 * 
	 * @param message the array of messages to be displayed on the splash screen
	 */
	private static void updateSplashIfNeeded(String message[]) {
		boolean headless_check = isHeadless();

		if (!headless_check) {
			SplashScreen splash = SplashScreen.getSplashScreen();
			if (splash == null) {
				return;
			}

			if (splash.isVisible()) {
				// Get a graphics overlay for the splash screen
				Graphics2D g = splash.createGraphics();
				// Do some drawing on the graphics object
				// Now update to the splash screen

				g.setComposite(AlphaComposite.Clear);
				g.fillRect(0, 0, 400, 70);
				g.setPaintMode();
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.BLACK);
				g.setFont(new Font("Arial", Font.BOLD, 11));
				for (int i = 0; i < message.length; i++) {
					g.drawString(message[i], 13, 16 * i + 10);
				}
				splash.update();
			}
		}
	}

	private static boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

	/**
	 * Returns the singleton instance of the Main class.
	 * If the singleton instance does not exist, it is created.
	 * 
	 * This method is thread-safe. It uses the "double-checked locking" idiom
	 * to ensure that only one instance of Main is ever created.
	 *
	 * @return The singleton instance of the Main class.
	 * @throws RuntimeException if an exception occurs while creating the singleton
	 *                          instance.
	 */
	public synchronized static Main getInstance() {
		if (singleton == null) {
			try {
				singleton = new Main();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
		return singleton;
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			Main.gsnConfFolder = args[0];
		}
		if (args.length > 1) {
			Main.virtualSensorDirectory = args[1];
		}
		updateSplashIfNeeded(new String[] { "GSN is trying to start.", "All GSN logs are available at: logs/gsn.log" });
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("GSN is stopping...");
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
						} finally {
							logger.warn("Forced exit...");
							System.out.println("GSN is stopped (forced).");
							Runtime.getRuntime().halt(1);
						}
					}
				}).start();

				try {
					logger.info("Shutting down GSN...");
					if (vsLoader != null) {
						vsLoader.stopLoading();
						logger.info("All virtual sensors have been stopped, shutting down virtual machine.");
					} else {
						logger.warn(
								"Could not shut down virtual sensors properly. We are probably exiting GSN before it has been completely initialized.");
					}
				} catch (Exception e) {
					logger.warn("Error while reading from or writing to control connection: " + e.getMessage(), e);
				} finally {
					System.out.println("GSN is stopped.");
				}
			}
		});

		try {
			Main.getInstance();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			updateSplashIfNeeded(new String[] { "Starting GSN failed! Look at logs/gsn.log for more information." });
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e1) {
			}
		}
		closeSplashIfneeded();
	}

	/**
	 * Loads the container configuration from the default configuration files.
	 * 
	 * This method first checks the accessibility of the necessary files and
	 * directories.
	 * Then, it loads the container configuration from the 'gsn.xml' file and the
	 * wrappers from the
	 * 'wrappers.properties' file.
	 *
	 * @return The loaded container configuration.
	 * @throws RuntimeException If the 'wrappers.properties' file refers to one or
	 *                          more classes
	 *                          which don't exist in the classpath.
	 */
	public static ContainerConfig loadContainerConfiguration() {
		ValidityTools.checkAccessibilityOfFiles(WrappersUtil.DEFAULT_WRAPPER_PROPERTIES_FILE,
				gsnConfFolder + "/gsn.xml");
		ValidityTools.checkAccessibilityOfDirs(virtualSensorDirectory);
		ContainerConfig toReturn = null;
		try {
			toReturn = loadContainerConfig(gsnConfFolder + "/gsn.xml");
			logger.info("Loading wrappers.properties at : " + WrappersUtil.DEFAULT_WRAPPER_PROPERTIES_FILE);
			wrappers = WrappersUtil.loadWrappers(new HashMap<String, Class<?>>());
			logger.info("Wrappers initialization ...");
		} catch (ClassNotFoundException e) {
			logger.error("The file wrapper.properties refers to one or more classes which don't exist in the classpath"
					+ e.getMessage());
			System.exit(1);
		}
		return toReturn;

	}

	/**
	 * Loads the container configuration from the specified gsn.xml file.
	 *
	 * @param gsnXMLpath The path to the gsn.xml file.
	 * @return The loaded ContainerConfig object.
	 * @throws ClassNotFoundException If the specified gsn.xml file is not found.
	 */
	public static ContainerConfig loadContainerConfig(String gsnXMLpath) throws ClassNotFoundException {
		if (!new File(gsnXMLpath).isFile()) {
			logger.error("Couldn't find the gsn.xml file @: " + (new File(gsnXMLpath).getAbsolutePath()));
			System.exit(1);
		}
		GsnConf gsn = GsnConf.load(gsnXMLpath);
		gsnConf = gsn;
		ContainerConfig conf = BeansInitializer.container(gsn);
		Class.forName(conf.getStorage().getJdbcDriver());
		conf.setContainerConfigurationFileName(gsnXMLpath);
		// return conf;
		// Create a JDBC connection using the URL approach
		// String jdbcUrl = conf.getStorage().getJdbcURL(); // Get the JDBC URL from
		// your configuration
		// String username = conf.getStorage().getJdbcUsername(); // Get the username
		// String password = conf.getStorage().getJdbcPassword(); // Get the password

		// try{
		// Connection connection = DriverManager.getConnection(jdbcUrl, username,
		// password);
		// Perform any necessary operations with the connection
		// ...
		// } catch (SQLException e) {
		// logger.error("Error creating database connection: " + e.getMessage());
		// Handle the exception as needed
		// }

		// conf.setContainerConfigurationFileName(gsnXMLpath);
		return conf;
	}

	/**
	 * Retrieves the wrappers.
	 *
	 * @return the wrappers as a Properties object.
	 */
	public static Properties getWrappers() {
		if (singleton == null) {
			return WrappersUtil.loadWrappers(new HashMap<String, Class<?>>());
		}
		return Main.wrappers;
	}

	/**
	 * Retrieves the wrapper class associated with the given ID.
	 * 
	 * @param id the ID of the wrapper class to retrieve
	 * @return the wrapper class associated with the given ID, or null if it doesn't
	 *         exist
	 */
	public static Class<?> getWrapperClass(String id) {
		try {
			String className = getWrappers().getProperty(id);
			if (className == null) {
				logger.error("The requested wrapper: " + id + " doesn't exist in the wrappers.properties file.");
				return null;
			}

			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Get's the GSN configuration without starting GSN.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static ContainerConfig getContainerConfig() {
		if (singleton == null) {
			try {
				return loadContainerConfig(Main.gsnConfFolder + "/gsn.xml");
			} catch (Exception e) {
				return null;
			}
		} else {
			return singleton.containerConfig;
		}
	}

	/**
	 * Returns the validation storage manager.
	 *
	 * @return the validation storage manager
	 */
	public static StorageManager getValidationStorage() {
		return validationStorage;
	}

	/**
	 * Retrieves the StorageManager based on the provided VSensorConfig.
	 * If the StorageManager is already cached, it is returned.
	 * Otherwise, a new StorageManager is created based on the configuration and
	 * cached for future use.
	 *
	 * @param config The VSensorConfig used to determine the StorageManager.
	 * @return The StorageManager instance.
	 */
	public static StorageManager getStorage(VSensorConfig config) {
		StorageManager sm = storagesConfigs.get(config == null ? null : config);
		if (sm != null) {
			return sm;
		}

		DBConnectionInfo dci = null;
		if (config == null || config.getStorage() == null || !config.getStorage().isDefined()) {
			sm = mainStorage;
		} else {
			if (config.getStorage().isIdentifierDefined()) {
				throw new IllegalArgumentException("Identifiers for storage is not supported yet.");
			} else {
				dci = new DBConnectionInfo(config.getStorage().getJdbcDriver(),
						config.getStorage().getJdbcURL(),
						config.getStorage().getJdbcUsername(),
						config.getStorage().getJdbcPassword());
			}
			sm = storages.get(dci.hashCode());
			if (sm == null) {
				sm = StorageManagerFactory.getInstance(config.getStorage().getJdbcDriver(),
						config.getStorage().getJdbcUsername(), config.getStorage().getJdbcPassword(),
						config.getStorage().getJdbcURL(), DEFAULT_MAX_DB_CONNECTIONS);
				storages.put(dci.hashCode(), sm);
				storagesConfigs.put(config, sm);
			}
		}
		return sm;

	}

	/**
	 * Retrieves the storage manager for a given virtual sensor name.
	 *
	 * @param vsName the name of the virtual sensor
	 * @return the storage manager for the virtual sensor
	 */
	public static StorageManager getStorage(String vsName) {
		return getStorage(Mappings.getVSensorConfig(vsName));
	}

	/**
	 * Returns the default StorageManager.
	 *
	 * @return the default StorageManager
	 */
	public static StorageManager getDefaultStorage() {
		return getStorage((VSensorConfig) null);
	}

	/**
	 * Returns the window storage manager.
	 *
	 * @return the window storage manager
	 */
	public static StorageManager getWindowStorage() {
		return windowStorage;
	}

	/**
	 * Returns the ZMQ context.
	 *
	 * @return the ZMQ context
	 */
	public static ZContext getZmqContext() {
		return zmqContext;
	}

	/**
	 * Returns the ZeroMQProxy instance.
	 *
	 * @return the ZeroMQProxy instance
	 */
	public static ZeroMQProxy getZmqProxy() {
		return zmqproxy;
	}

	/**
	 * Returns the GsnConf object.
	 *
	 * @return the GsnConf object
	 */
	public GsnConf getGsnConf() {
		return gsnConf;
	}

	/**
	 * Returns the map of VsConf objects.
	 *
	 * @return the map of VsConf objects
	 */
	public Map<String, VsConf> getVsConf() {
		return vsConf;
	}

	/**
	 * Returns the list of objects to be monitored.
	 *
	 * @return the list of objects to be monitored
	 */
	public ArrayList<Monitorable> getToMonitor() {
		return toMonitor;
	}

	/**
	 * Returns the ThreadMXBean instance.
	 *
	 * @return the ThreadMXBean instance
	 */
	public static ThreadMXBean getThreadMXBean() {
		return threadBean;
	}

}
