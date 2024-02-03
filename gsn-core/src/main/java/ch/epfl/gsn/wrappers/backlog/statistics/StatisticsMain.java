package ch.epfl.gsn.wrappers.backlog.statistics;

import ch.epfl.gsn.wrappers.BackLogStatsWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class StatisticsMain {

	protected final transient Logger logger = LoggerFactory.getLogger(StatisticsMain.class);

	private static Map<String, DeploymentStatistics> deploymentToDeploymentStatsList = Collections
			.synchronizedMap(new Hashtable<String, DeploymentStatistics>());

	private static StatisticsMain singletonObject = null;

	private StatisticsMain() {
	}

	/**
	 * Retrieves or creates an instance of DeploymentStatistics associated with the
	 * specified deploymentName.
	 * This method ensures thread safety by synchronizing the creation and retrieval
	 * process.
	 *
	 * @param deploymentName The name of the deployment for which to get or create
	 *                       statistics.
	 * @param statswrapper   The BackLogStatsWrapper containing statistics
	 *                       information.
	 * @return A synchronized instance of DeploymentStatistics for the specified
	 *         deployment.
	 */
	public synchronized static DeploymentStatistics getDeploymentStatsInstance(String deploymentName,
			BackLogStatsWrapper statswrapper) {
		if (singletonObject == null) {
			singletonObject = new StatisticsMain();
		}

		if (deploymentToDeploymentStatsList.containsKey(deploymentName)) {
			deploymentToDeploymentStatsList.get(deploymentName).setStatsWrapper(statswrapper);
		} else {
			deploymentToDeploymentStatsList.put(deploymentName, new DeploymentStatistics(statswrapper));
		}

		return deploymentToDeploymentStatsList.get(deploymentName);
	}

	/**
	 * Retrieves or creates an instance of CoreStationStatistics associated with the
	 * specified deploymentName
	 * and CoreStation address. This method ensures thread safety by synchronizing
	 * the creation and retrieval process.
	 *
	 * @param deploymentName     The name of the deployment for which to get or
	 *                           create CoreStation statistics.
	 * @param coreStationAddress The address of the CoreStation for which to get or
	 *                           create statistics.
	 * @return A synchronized instance of CoreStationStatistics for the specified
	 *         deployment and CoreStation.
	 * @throws IOException If an error occurs during the creation of
	 *                     CoreStationStatistics.
	 */
	public synchronized static CoreStationStatistics getCoreStationStatsInstance(String deploymentName,
			String coreStationAddress) throws IOException {
		if (singletonObject == null) {
			singletonObject = new StatisticsMain();
		}

		if (!deploymentToDeploymentStatsList.containsKey(deploymentName)) {
			deploymentToDeploymentStatsList.put(deploymentName, new DeploymentStatistics(null));
		}

		return deploymentToDeploymentStatsList.get(deploymentName).newStatisticsClass(coreStationAddress);
	}

	/**
	 * Notifies the statistics module of a change in the connection status for a
	 * specific device in the specified deployment.
	 * This method updates the statistics based on the connection status change.
	 *
	 * @param deploymentName The name of the deployment for which the connection
	 *                       status changed.
	 * @param deviceId       The identifier of the device whose connection status
	 *                       changed.
	 * @throws IOException If the specified deployment does not exist in the
	 *                     statistics.
	 */
	public static void connectionStatusChanged(String deploymentName, int deviceId) throws IOException {
		if (singletonObject == null) {
			singletonObject = new StatisticsMain();
		}

		if (!deploymentToDeploymentStatsList.containsKey(deploymentName)) {
			throw new IOException("deployment " + deploymentName + " does not exist in the statistics");
		}

		BackLogStatsWrapper deplstats = deploymentToDeploymentStatsList.get(deploymentName).getStatsWrapper();
		if (deplstats != null) {
			deplstats.connectionStatusChanged(deviceId);
		}

	}

	/**
	 * Removes the statistics instance associated with the specified CoreStation
	 * address
	 * from the deployment's statistics. This method ensures thread safety by
	 * synchronizing
	 * the removal process.
	 *
	 * @param deploymentName     The name of the deployment from which to remove
	 *                           CoreStation statistics.
	 * @param coreStationAddress The address of the CoreStation for which to remove
	 *                           statistics.
	 * @throws IOException If the specified deployment does not exist in the
	 *                     statistics.
	 */
	public synchronized static void removeCoreStationStatsInstance(String deploymentName, String coreStationAddress)
			throws IOException {
		if (singletonObject == null) {
			singletonObject = new StatisticsMain();
		}

		if (!deploymentToDeploymentStatsList.containsKey(deploymentName)) {
			throw new IOException("deployment " + deploymentName + " does not exist in the statistics");
		}

		deploymentToDeploymentStatsList.get(deploymentName).removeCoreStationStatsInstance(coreStationAddress);
	}
}
