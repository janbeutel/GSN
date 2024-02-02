package ch.epfl.gsn.wrappers.backlog.statistics;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class CoreStationStatistics {

	private final transient Logger logger = LoggerFactory.getLogger(CoreStationStatistics.class);

	private String coreStationAddress = null;
	private Boolean isConnected = null;
	private Integer deviceId = null;
	Long recvTotal = null;
	Long sendTotal = null;
	private Map<Integer, Long> msgRecvCounterMap = Collections.synchronizedMap(new Hashtable<Integer, Long>());
	private Map<Integer, Long> msgRecvByteCounterMap = Collections.synchronizedMap(new Hashtable<Integer, Long>());
	private Map<Integer, Long> msgSendCounterMap = Collections.synchronizedMap(new Hashtable<Integer, Long>());
	private Map<Integer, Long> msgSendByteCounterMap = Collections.synchronizedMap(new Hashtable<Integer, Long>());

	public CoreStationStatistics(String corestation) {
		coreStationAddress = corestation;
	}

	public void setConnected(boolean conn) {
		isConnected = conn;
	}

	public Boolean isConnected() {
		return isConnected;
	}

	/**
	 * Sets the device ID for the CoreStation.
	 * If the device ID has changed, all statistics are reset.
	 * 
	 * @param id the new device ID
	 */
	public void setDeviceId(int id) {
		if (deviceId != null && id != deviceId) {
			logger.warn("device id for CoreStation " + coreStationAddress + " has changed => reseting all statistics");
			recvTotal = null;
			sendTotal = null;
			msgRecvCounterMap.clear();
			msgRecvByteCounterMap.clear();
			msgSendCounterMap.clear();
			msgSendByteCounterMap.clear();
		}
		deviceId = id;
	}

	public Integer getDeviceId() {
		return deviceId;
	}

	/**
	 * Updates the message received counters for the specified message type and
	 * size.
	 *
	 * @param type The type of the received message.
	 * @param size The size of the received message.
	 */
	public void msgReceived(int type, long size) {
		Long val = msgRecvCounterMap.get(type);
		if (val == null) {
			msgRecvCounterMap.put(type, new Long(1));
		} else {
			msgRecvCounterMap.put(type, val + 1);
		}

		val = msgRecvByteCounterMap.get(type);
		if (val == null) {
			msgRecvByteCounterMap.put(type, size);
		} else {
			msgRecvByteCounterMap.put(type, val + size);
		}
	}

	/**
	 * Updates the total number of bytes received by the core station.
	 * 
	 * @param size the size of the received bytes
	 */
	public void bytesReceived(long size) {
		if (recvTotal == null) {
			recvTotal = size;
		} else {
			recvTotal += size;
		}
	}

	public Long getTotalRecvByteCounter() {
		return recvTotal;
	}

	/**
	 * Returns the total message receive counter.
	 *
	 * @return the total message receive counter
	 */
	public Long getTotalMsgRecvCounter() {
		long total = 0;
		synchronized (msgRecvCounterMap) {
			for (Iterator<Long> iter = msgRecvCounterMap.values().iterator(); iter.hasNext();) {
				total += iter.next();
			}
		}
		return total;
	}

	public Long getMsgRecvCounter(int type) {
		return msgRecvCounterMap.get(type);
	}

	/**
	 * Returns the total message receive byte counter.
	 *
	 * @return the total message receive byte counter
	 */
	public Long getTotalMsgRecvByteCounter() {
		long total = 0;
		synchronized (msgRecvByteCounterMap) {
			for (Iterator<Long> iter = msgRecvByteCounterMap.values().iterator(); iter.hasNext();) {
				total += iter.next();
			}
		}
		return total;
	}

	public Long getMsgRecvByteCounter(int type) {
		return msgRecvByteCounterMap.get(type);
	}

	/**
	 * Updates the message send counters and byte counters for the specified message
	 * type and size.
	 * 
	 * @param type the type of the message
	 * @param size the size of the message in bytes
	 */
	public void msgSent(int type, long size) {
		Long val = msgSendCounterMap.get(type);
		if (val == null) {
			msgSendCounterMap.put(type, new Long(1));
		} else {
			msgSendCounterMap.put(type, val + 1);
		}

		val = msgSendByteCounterMap.get(type);
		if (val == null) {
			msgSendByteCounterMap.put(type, size);
		} else {
			msgSendByteCounterMap.put(type, val + size);
		}
	}

	public void bytesSent(long size) {
		if (sendTotal == null) {
			sendTotal = size;
		} else {
			sendTotal += size;
		}

	}

	public Long getTotalSendByteCounter() {
		return sendTotal;
	}

	/**
	 * Returns the total message send counter.
	 *
	 * @return the total message send counter
	 */
	public Long getTotalMsgSendCounter() {
		long total = 0;
		synchronized (msgSendCounterMap) {
			for (Iterator<Long> iter = msgSendCounterMap.values().iterator(); iter.hasNext();) {
				total += iter.next();
			}
		}
		return total;
	}

	public Long getMsgSendCounter(int type) {
		return msgSendCounterMap.get(type);
	}

	/**
	 * Returns the total number of bytes sent in messages.
	 *
	 * @return the total number of bytes sent in messages
	 */
	public Long getTotalMsgSendByteCounter() {
		long total = 0;
		synchronized (msgSendByteCounterMap) {
			for (Iterator<Long> iter = msgSendByteCounterMap.values().iterator(); iter.hasNext();) {
				total += iter.next();
			}
		}
		return total;
	}

	public Long getMsgSendByteCounter(int type) {
		return msgSendByteCounterMap.get(type);
	}
}
