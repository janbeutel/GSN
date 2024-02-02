package ch.epfl.gsn.wrappers;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.wrappers.backlog.BackLogMessage;
import ch.epfl.gsn.wrappers.backlog.statistics.DeploymentStatistics;
import ch.epfl.gsn.wrappers.backlog.statistics.StatisticListener;
import ch.epfl.gsn.wrappers.backlog.statistics.StatisticsMain;

public class BackLogStatsWrapper extends AbstractWrapper implements StatisticListener {
	private final transient Logger logger = LoggerFactory.getLogger(BackLogStatsWrapper.class);

	private final static int DEFAULT_SAMPLING_RATE_MS = 30000;

	private final static DataField[] outputStructure = new DataField[] {
			new DataField("generation_time", DataTypes.BIGINT),
			new DataField("device_id", DataTypes.INTEGER),

			new DataField("connected", DataTypes.TINYINT),

			new DataField("in_total_counter", DataTypes.BIGINT),
			new DataField("in_total_stuffed", DataTypes.BIGINT),
			new DataField("in_total_unstuffed", DataTypes.BIGINT),
			new DataField("out_total_counter", DataTypes.BIGINT),
			new DataField("out_total_stuffed", DataTypes.BIGINT),
			new DataField("out_total_unstuffed", DataTypes.BIGINT),

			new DataField("in_ack_counter", DataTypes.BIGINT),
			new DataField("in_ack_volume", DataTypes.BIGINT),
			new DataField("in_ping_counter", DataTypes.BIGINT),
			new DataField("in_ping_volume", DataTypes.BIGINT),
			new DataField("in_ping_ack_counter", DataTypes.BIGINT),
			new DataField("in_ping_ack_volume", DataTypes.BIGINT),

			new DataField("out_ack_counter", DataTypes.BIGINT),
			new DataField("out_ack_volume", DataTypes.BIGINT),
			new DataField("out_ping_counter", DataTypes.BIGINT),
			new DataField("out_ping_volume", DataTypes.BIGINT),
			new DataField("out_ping_ack_counter", DataTypes.BIGINT),
			new DataField("out_ping_ack_volume", DataTypes.BIGINT),

			new DataField("out_queue_limit_counter", DataTypes.BIGINT),
			new DataField("out_queue_limit_volume", DataTypes.BIGINT),
			new DataField("out_queue_ready_counter", DataTypes.BIGINT),
			new DataField("out_queue_ready_volume", DataTypes.BIGINT)
	};

	private int sampling_rate = DEFAULT_SAMPLING_RATE_MS;
	private boolean stopped = false;
	private Object event = new Object();
	private DeploymentStatistics stats;

	/**
	 * Initializes the BackLogStatsWrapper.
	 * This method retrieves the deployment name from the active address bean,
	 * initializes the statistics instance for the deployment,
	 * and sets the sampling rate based on the predicate value.
	 * If the sampling rate is not parsable, it is set to the default value.
	 * 
	 * @return true if the initialization is successful, false otherwise.
	 */
	@Override
	public boolean initialize() {
		String deployment = getActiveAddressBean().getVirtualSensorName().split("_")[0].toLowerCase();

		stats = StatisticsMain.getDeploymentStatsInstance(deployment, this);

		String predicate = getActiveAddressBean().getPredicateValue("sampling-rate");
		if (predicate != null) {
			try {
				sampling_rate = Integer.parseInt(predicate) * 1000;
			} catch (NumberFormatException e) {
				logger.warn("sampling-rate is not parsable, set to default (" + DEFAULT_SAMPLING_RATE_MS + "ms)");
			}
		}

		return true;
	}

	/**
	 * Executes the run method in a separate thread.
	 * This method continuously checks the connection status of devices and
	 * generates stream elements accordingly.
	 * It uses the sampling rate to control the frequency of checking and generating
	 * stream elements.
	 */
	public void run() {
		long timestamp;

		while (!stopped) {
			timestamp = System.currentTimeMillis();

			Map<Integer, Boolean> connected = stats.isConnectedList();

			if (connected != null) {
				Iterator<Integer> iter = connected.keySet().iterator();

				if (iter != null) {
					for (; iter.hasNext();) {
						int deviceid = iter.next();

						generateStreamElement(timestamp, deviceid, connected);
					}
				}
			}

			try {
				synchronized (event) {
					event.wait(sampling_rate);
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	/**
	 * Generates a stream element with the given timestamp, device ID, and connected
	 * list.
	 * The stream element is then posted using the postStreamElement method.
	 *
	 * @param timestamp     the timestamp of the stream element
	 * @param deviceid      the ID of the device
	 * @param connectedList a map representing the connected status of devices
	 */
	private void generateStreamElement(long timestamp, int deviceid, Map<Integer, Boolean> connectedList) {
		Serializable[] output = new Serializable[outputStructure.length];
		output[0] = timestamp;
		output[1] = deviceid;

		if (connectedList.get(deviceid)) {
			output[2] = (byte) 1;
		} else {
			output[2] = (byte) 0;
		}

		int counter = 3;
		for (Iterator<Map<Integer, Long>> it = getStatsList().iterator(); it.hasNext();) {
			output[counter++] = it.next().get(deviceid);
		}
		postStreamElement(new StreamElement(outputStructure, output));
	}

	/**
	 * Returns a list of maps containing various statistics related to message
	 * counters and byte counters.
	 *
	 * @return The list of maps containing the statistics.
	 */
	@SuppressWarnings("unchecked")
	private List<Map<Integer, Long>> getStatsList() {
		return Arrays.asList(
				stats.getTotalMsgRecvCounter(),
				stats.getTotalRecvByteCounter(),
				stats.getTotalMsgRecvByteCounter(),
				stats.getTotalMsgSendCounter(),
				stats.getTotalSendByteCounter(),
				stats.getTotalMsgSendByteCounter(),

				stats.getMsgRecvCounterList(BackLogMessage.ACK_MESSAGE_TYPE),
				stats.getMsgRecvByteCounterList(BackLogMessage.ACK_MESSAGE_TYPE),
				stats.getMsgRecvCounterList(BackLogMessage.PING_MESSAGE_TYPE),
				stats.getMsgRecvByteCounterList(BackLogMessage.PING_MESSAGE_TYPE),
				stats.getMsgRecvCounterList(BackLogMessage.PING_ACK_MESSAGE_TYPE),
				stats.getMsgRecvByteCounterList(BackLogMessage.PING_ACK_MESSAGE_TYPE),

				stats.getMsgSendCounterList(BackLogMessage.ACK_MESSAGE_TYPE),
				stats.getMsgSendByteCounterList(BackLogMessage.ACK_MESSAGE_TYPE),
				stats.getMsgSendCounterList(BackLogMessage.PING_MESSAGE_TYPE),
				stats.getMsgSendByteCounterList(BackLogMessage.PING_MESSAGE_TYPE),
				stats.getMsgSendCounterList(BackLogMessage.PING_ACK_MESSAGE_TYPE),
				stats.getMsgSendByteCounterList(BackLogMessage.PING_ACK_MESSAGE_TYPE),
				stats.getMsgSendCounterList(BackLogMessage.MESSAGE_QUEUE_LIMIT_MESSAGE_TYPE),
				stats.getMsgSendByteCounterList(BackLogMessage.MESSAGE_QUEUE_LIMIT_MESSAGE_TYPE),
				stats.getMsgSendCounterList(BackLogMessage.MESSAGE_QUEUE_READY_MESSAGE_TYPE),
				stats.getMsgSendByteCounterList(BackLogMessage.MESSAGE_QUEUE_READY_MESSAGE_TYPE));
	}

	@Override
	public void connectionStatusChanged(int deviceId) {
		generateStreamElement(System.currentTimeMillis(), deviceId, stats.isConnectedList());
	}

	@Override
	public DataField[] getOutputFormat() {
		return outputStructure;
	}

	@Override
	public String getWrapperName() {
		return "StatisticsWrapper";
	}

	@Override
	public void dispose() {
		stopped = true;
		synchronized (event) {
			event.notify();
		}
	}
}
