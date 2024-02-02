package ch.epfl.gsn.wrappers.backlog.statistics;

import ch.epfl.gsn.wrappers.BackLogStatsWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DeploymentStatistics {

	protected final transient Logger logger = LoggerFactory.getLogger(DeploymentStatistics.class);

	private Map<String, CoreStationStatistics> coreStationToCoreStationStatsList = Collections
			.synchronizedMap(new Hashtable<String, CoreStationStatistics>());
	private BackLogStatsWrapper blstatswrapper = null;

	public DeploymentStatistics(BackLogStatsWrapper statswrapper) {
		blstatswrapper = statswrapper;
	}

	/**
	 * Creates a new instance of CoreStationStatistics if it doesn't already exist
	 * for the given core station address.
	 * 
	 * @param coreStationAddress The address of the core station.
	 * @return The CoreStationStatistics object associated with the given core
	 *         station address.
	 * @throws IOException If an I/O error occurs while creating the
	 *                     CoreStationStatistics object.
	 */
	protected CoreStationStatistics newStatisticsClass(String coreStationAddress) throws IOException {
		if (!coreStationToCoreStationStatsList.containsKey(coreStationAddress)) {
			coreStationToCoreStationStatsList.put(coreStationAddress, new CoreStationStatistics(coreStationAddress));
		}

		return coreStationToCoreStationStatsList.get(coreStationAddress);
	}

	/**
	 * Removes the instance of core station statistics associated with the given
	 * core station address.
	 * If the core station address does not exist in the statistics, an IOException
	 * is thrown.
	 *
	 * @param coreStationAddress the address of the core station
	 * @throws IOException if the core station address does not exist in the
	 *                     statistics
	 */
	protected void removeCoreStationStatsInstance(String coreStationAddress) throws IOException {
		if (!coreStationToCoreStationStatsList.containsKey(coreStationAddress)) {
			throw new IOException("CoreStation " + coreStationAddress + " does not exist in the statistics");
		}
		coreStationToCoreStationStatsList.remove(coreStationAddress);
	}

	public BackLogStatsWrapper getStatsWrapper() {
		return blstatswrapper;
	}

	public void setStatsWrapper(BackLogStatsWrapper statswrapper) {
		blstatswrapper = statswrapper;
	}

	/**
	 * Returns a map of device IDs and their connection status.
	 * 
	 * @return a map where the key is the device ID and the value is a boolean
	 *         indicating the connection status
	 */
	public Map<Integer, Boolean> isConnectedList() {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Boolean> map = new Hashtable<Integer, Boolean>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Boolean val = csstat.isConnected();
				if (id != null && val != null) {
					map.put(id, val);
				}
			}
		}
		return map;
	}

	/**
	 * Returns a map containing the total message receive counters for each core
	 * station.
	 * If the core station statistics list is empty, null is returned.
	 *
	 * @return a map with core station IDs as keys and total message receive
	 *         counters as values
	 */
	public Map<Integer, Long> getTotalMsgRecvCounter() {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getTotalMsgRecvCounter();
				if (id != null && val != null) {
					map.put(id, val);
				}
			}
		}
		return map;
	}

	/**
	 * Returns a map of message receive counters for a given type.
	 *
	 * @param type the type of message
	 * @return a map containing the device IDs as keys and the corresponding message
	 *         receive counters as values
	 */
	public Map<Integer, Long> getMsgRecvCounterList(int type) {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getMsgRecvCounter(type);
				if (id != null && val != null) {
					map.put(id, val);
				}

			}
		}
		return map;
	}

	/**
	 * Returns a map containing the total receive byte counter for each core
	 * station.
	 * If the core station statistics list is empty, null is returned.
	 *
	 * @return a map with core station IDs as keys and total receive byte counters
	 *         as values
	 */
	public Map<Integer, Long> getTotalRecvByteCounter() {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getTotalRecvByteCounter();
				if (id != null && val != null) {
					map.put(id, val);
				}
			}
		}
		return map;
	}

	/**
	 * Returns a map containing the total message receive byte counter for each core
	 * station.
	 * If the core station statistics list is empty, null is returned.
	 *
	 * @return a map with core station IDs as keys and total message receive byte
	 *         counters as values
	 */
	public Map<Integer, Long> getTotalMsgRecvByteCounter() {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getTotalMsgRecvByteCounter();
				if (id != null && val != null) {
					map.put(id, val);
				}
			}
		}
		return map;
	}

	/**
	 * Retrieves the message receive byte counter list for a given type.
	 * 
	 * @param type the type of message
	 * @return a map containing the device IDs as keys and the corresponding message
	 *         receive byte counters as values,
	 *         or null if the coreStationToCoreStationStatsList is empty
	 */
	public Map<Integer, Long> getMsgRecvByteCounterList(int type) {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getMsgRecvByteCounter(type);
				if (id != null && val != null) {
					map.put(id, val);
				}
			}
		}
		return map;
	}

	/**
	 * Returns a map containing the total message send counter for each core
	 * station.
	 * If the coreStationToCoreStationStatsList is empty, returns null.
	 *
	 * @return a map with core station IDs as keys and total message send counters
	 *         as values
	 */
	public Map<Integer, Long> getTotalMsgSendCounter() {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getTotalMsgSendCounter();
				if (id != null && val != null) {
					map.put(id, val);
				}
			}
		}
		return map;
	}

	/**
	 * Retrieves the message send counter list for a given type.
	 * 
	 * @param type the type of message
	 * @return a map containing the device IDs and their corresponding message send
	 *         counters
	 */
	public Map<Integer, Long> getMsgSendCounterList(int type) {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getMsgSendCounter(type);
				if (id != null && val != null) {
					map.put(id, val);
				}
			}
		}
		return map;
	}

	/**
	 * Returns a map containing the total send byte counter for each core station.
	 * If the core station statistics list is empty, null is returned.
	 *
	 * @return a map with core station IDs as keys and total send byte counters as
	 *         values
	 */
	public Map<Integer, Long> getTotalSendByteCounter() {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getTotalSendByteCounter();
				if (id != null && val != null) {
					map.put(id, val);
				}
			}
		}
		return map;
	}

	/**
	 * Returns a map containing the total message send byte counter for each core
	 * station.
	 * If the core station statistics list is empty, null is returned.
	 *
	 * @return a map with core station IDs as keys and total message send byte
	 *         counters as values
	 */
	public Map<Integer, Long> getTotalMsgSendByteCounter() {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getTotalMsgSendByteCounter();
				if (id != null && val != null) {
					map.put(id, val);
				}
			}
		}
		return map;
	}

	/**
	 * Retrieves the message send byte counter list for a given type.
	 * 
	 * @param type the type of message
	 * @return a map containing the device IDs as keys and the corresponding message
	 *         send byte counters as values,
	 *         or null if the coreStationToCoreStationStatsList is empty
	 */
	public Map<Integer, Long> getMsgSendByteCounterList(int type) {
		if (coreStationToCoreStationStatsList.isEmpty()) {
			return null;
		}
		Map<Integer, Long> map = new Hashtable<Integer, Long>();
		synchronized (coreStationToCoreStationStatsList) {
			for (Iterator<CoreStationStatistics> iter = coreStationToCoreStationStatsList.values().iterator(); iter
					.hasNext();) {
				CoreStationStatistics csstat = iter.next();
				Integer id = csstat.getDeviceId();
				Long val = csstat.getMsgSendByteCounter(type);
				if (id != null && val != null) {
					map.put(id, val);
				}

			}
		}
		return map;
	}
}
