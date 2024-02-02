package ch.epfl.gsn.statistics;

import java.util.Iterator;
import java.util.Vector;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class StatisticsHandler {

	private final transient Logger logger = LoggerFactory.getLogger(StatisticsHandler.class);

	private static StatisticsHandler instance = new StatisticsHandler();

	private Vector<StatisticsListener> statisticsListeners = new Vector<StatisticsListener>();

	private StatisticsHandler() {
	}

	public static StatisticsHandler getInstance() {
		return instance;
	}

	/**
	 * Registers a statistics listener.
	 * 
	 * @param listener the statistics listener to register
	 */
	public void registerListener(StatisticsListener listener) {
		statisticsListeners.add(listener);
		logger.info("statistics listener >" + listener.listenerName() + "< registered");
	}

	/**
	 * Removes the specified listener from the list of statistics listeners.
	 * 
	 * @param listener the listener to be deregistered
	 */
	public void deregisterListener(StatisticsListener listener) {
		statisticsListeners.remove(listener);
		logger.info("statistics listener >" + listener.listenerName() + "< deregistered");
	}

	/**
	 * Processes an input event from a producer virtual sensor.
	 * 
	 * @param producerVS        the name of the producer virtual sensor
	 * @param statisticsElement the statistics element associated with the event
	 * @return true if the event was successfully processed, false otherwise
	 */
	public boolean inputEvent(String producerVS, StatisticsElement statisticsElement) {
		if (producerVS == null) {
			logger.error("producer virtual sensor name should not be null");
			return false;
		}
		if (statisticsListeners.isEmpty()) {
			return false;
		}

		boolean ret = false;
		for (Iterator<StatisticsListener> iter = statisticsListeners.iterator(); iter.hasNext();) {
			if (iter.next().inputEvent(producerVS.toLowerCase().trim(), statisticsElement)) {
				ret = true;
			}
		}

		return ret;
	}

	/**
	 * Outputs an event for the given producer virtual sensor and statistics
	 * element.
	 * 
	 * @param producerVS        The name of the producer virtual sensor.
	 * @param statisticsElement The statistics element to be output.
	 * @return true if the event was successfully output, false otherwise.
	 */
	public boolean outputEvent(String producerVS, StatisticsElement statisticsElement) {
		if (producerVS == null) {
			logger.error("producer virtual sensor name should not be null");
			return false;
		}
		if (statisticsListeners.isEmpty()) {
			return false;
		}

		boolean ret = false;
		for (Iterator<StatisticsListener> iter = statisticsListeners.iterator(); iter.hasNext();) {
			if (iter.next().outputEvent(producerVS.toLowerCase().trim(), statisticsElement)) {
				ret = true;
			}
		}

		return ret;
	}
}
