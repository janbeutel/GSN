package ch.epfl.gsn.wrappers.backlog.plugins;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.InputInfo;
import ch.epfl.gsn.wrappers.BackLogWrapper;

/**
 * This plugin listens for incoming LWB DPP messages.
 * 
 * @author Tonio Gsell
 */
public class DPPMessagePlugin extends AbstractPlugin {

	private static final String DPP_MESSAGE_CLASS = "message-classname";

	private static final String DPP_HEADER_TARGET_ID = "target_id";

	private static DataField[] headerDataField = {
			new DataField("TIMESTAMP", "BIGINT"),
			new DataField("GENERATION_TIME", "BIGINT"),
			new DataField("GENERATION_TIME_MICROSEC", "BIGINT"),
			new DataField("DEVICE_ID", "INTEGER"),
			new DataField("MESSAGE_TYPE", "INTEGER"),
			new DataField("TARGET_ID", "INTEGER"),
			new DataField("SEQNR", "INTEGER"),
			new DataField("PAYLOAD_LENGTH", "INTEGER") };

	private DataField[] msgDataField;

	private final transient Logger logger = LoggerFactory.getLogger(DPPMessagePlugin.class);

	private Constructor<?> messageConstructor = null;

	private DPPMessageMultiplexer dppMsgMultiplexer = null;

	private ch.epfl.gsn.wrappers.backlog.plugins.dpp.Message msgClass;

	/**
	 * Initializes the DPPMessagePlugin with the specified parameters.
	 * 
	 * @param backlogwrapper  The BackLogWrapper instance.
	 * @param coreStationName The name of the core station.
	 * @param deploymentName  The name of the deployment.
	 * @return true if the initialization is successful, false otherwise.
	 */
	@Override
	public boolean initialize(BackLogWrapper backlogwrapper, String coreStationName, String deploymentName) {
		activeBackLogWrapper = backlogwrapper;
		String p = getActiveAddressBean().getPredicateValue("priority");
		if (p == null) {
			priority = null;
		} else {
			priority = Integer.valueOf(p);
		}

		try {
			dppMsgMultiplexer = DPPMessageMultiplexer.getInstance(coreStationName,
					backlogwrapper.getBLMessageMultiplexer());

			// get the DPP message class for the specified DPP packet
			Class<?> classTemplate = Class
					.forName(getActiveAddressBean().getPredicateValueWithException(DPP_MESSAGE_CLASS));
			messageConstructor = classTemplate.getConstructor();

			msgClass = ((ch.epfl.gsn.wrappers.backlog.plugins.dpp.Message) messageConstructor.newInstance());

			if (!msgClass.initialize(this, coreStationName, deploymentName)) {
				return false;
			}

			msgDataField = (DataField[]) ArrayUtils.addAll(headerDataField, msgClass.getOutputFormat());

			dppMsgMultiplexer.registerListener(msgClass.getType(), this);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	@Override
	public String getPluginName() {
		return "DPPMessagPlugin-" + messageConstructor.getName();
	}

	/**
	 * This method is called when a message is received.
	 * It processes the received message and performs necessary actions.
	 *
	 * @param deviceId  The ID of the device that received the message.
	 * @param timestamp The timestamp of the received message.
	 * @param data      The data array containing the message information.
	 * @return True if the message was successfully processed, false otherwise.
	 */
	@Override
	public boolean messageReceived(int deviceId, long timestamp, Serializable[] data) {
		try {
			int device_id = toInteger(data[0]);
			boolean min_msg = (Boolean) data[1];
			int type = toInteger(data[2]);
			int payload_len = toInteger(data[3]);
			ByteBuffer payload;
			Serializable[] header;
			if (min_msg) {
				payload = ByteBuffer.wrap((byte[]) data[4]);
				header = new Serializable[] { timestamp, null, null, device_id, type, null, null, payload_len };
			} else {
				int target_id = toInteger(data[4]);
				int seqnr = toInteger(data[5]);
				long generation_time = toLong(data[6]);
				payload = ByteBuffer.wrap((byte[]) data[7]);
				header = new Serializable[] { timestamp, (long) (generation_time / 1000.0), generation_time, device_id,
						type, target_id, seqnr, payload_len };

			}
			payload.order(ByteOrder.LITTLE_ENDIAN);
			Serializable[] msg = (Serializable[]) ArrayUtils.addAll(header, msgClass.receivePayload(payload));

			if (dataProcessed(System.currentTimeMillis(), msg)) {
				ackMessage(timestamp, super.priority);
				return true;
			} else {
				logger.warn("The message with timestamp >" + timestamp + "< could not be stored in the database.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * Sends a message to a plugin with the specified action, parameter names, and
	 * parameter values.
	 * The method constructs a message header and payload based on the provided
	 * parameters and sends
	 * the message to the remote plugin. It also handles the response and returns an
	 * InputInfo object
	 * indicating the success or failure of the message upload.
	 *
	 * @param action      The action to be performed by the plugin.
	 * @param paramNames  An array of parameter names associated with the action.
	 * @param paramValues An array of parameter values corresponding to the
	 *                    parameter names.
	 * @return An InputInfo object representing the result of the message upload.
	 * @throws Exception If an error occurs during the message creation or upload
	 *                   process.
	 */
	@Override
	public InputInfo sendToPlugin(String action, String[] paramNames, Object[] paramValues) {
		Long timestamp = System.currentTimeMillis();
		InputInfo inputInfo;
		Serializable[] header;
		Serializable[] processPayload;
		if (msgClass.isMinimal()) {
			header = new Serializable[] { timestamp, null, null, getDeviceID(), null };
		} else {
			header = new Serializable[] { timestamp, null, null, getDeviceID(), null, null, null, null };
		}

		if (getDeviceID() != null) {
			processPayload = msgClass.sendPayloadSuccess(false);
			inputInfo = new InputInfo(getActiveAddressBean().toString(), "device ID is null ", false);
		} else {
			boolean ret = false;
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("action: " + action);
				}

				Serializable[] message;
				if (msgClass.isMinimal()) {
					message = new Serializable[5];
				} else {
					message = new Serializable[8];
				}

				int device_id = getDeviceID(); // device_id
				boolean min_msg = msgClass.isMinimal(); // min_msg
				int type = msgClass.getType(); // type
				Integer target_id = null;
				Integer seqnr = null;
				Long generation_time = null;

				if (!msgClass.isMinimal()) {
					for (int i = 0; i < paramNames.length; i++) {
						if (paramNames[i].trim().compareToIgnoreCase(DPP_HEADER_TARGET_ID) == 0) {
							target_id = new Integer((String) paramValues[i]); // target_id
						}
					}
					if (target_id == null) {
						throw new Exception("target_id missing");
					}

					seqnr = dppMsgMultiplexer.getNextSequenceNumber(); // seqnr
					generation_time = timestamp * 1000; // generation_time
				}

				byte[] payload;
				try {
					payload = msgClass.sendPayload(action, paramNames, paramValues).array();
				} catch (Exception e) {
					return inputInfo = new InputInfo(getActiveAddressBean().toString(),
							"DPP message upload not successfull: " + e.getMessage(), false);
				}

				int payload_len = (byte) (payload.length & 0xff); // payload_len

				message[0] = device_id;
				message[1] = min_msg;
				message[2] = type;
				message[3] = payload_len;
				if (msgClass.isMinimal()) {
					message[4] = payload;
					header = new Serializable[] { timestamp, null, null, device_id, type, null, null, payload_len };
				} else {
					message[4] = target_id;
					message[5] = seqnr;
					message[6] = generation_time;
					message[7] = payload;
					header = new Serializable[] { timestamp, (long) (generation_time / 1000.0), generation_time,
							device_id, type, target_id, seqnr, payload_len };
				}

				ret = sendRemote(timestamp, message, super.priority);
				processPayload = msgClass.sendPayloadSuccess(ret);

				if (ret) {
					inputInfo = new InputInfo(getActiveAddressBean().toString(), "MIG message upload successfull", ret);
				} else {
					inputInfo = new InputInfo(getActiveAddressBean().toString(), "MIG message upload not successfull",
							ret);
				}

			} catch (Exception e) {
				processPayload = msgClass.sendPayloadSuccess(false);
				inputInfo = new InputInfo(getActiveAddressBean().toString(),
						"DPP message upload not successfull: " + e.getMessage(), false);
			}
		}

		if (processPayload != null) {
			dataProcessed(timestamp, (Serializable[]) ArrayUtils.addAll(header, processPayload));
		}

		return inputInfo;
	}

	@Override
	public short getMessageType() {
		return ch.epfl.gsn.wrappers.backlog.BackLogMessage.DPP_MESSAGE_TYPE;
	}

	@Override
	public void dispose() {
		dppMsgMultiplexer.deregisterListener(msgClass.getType(), this);
	}

	@Override
	public DataField[] getOutputFormat() {
		return msgDataField;
	}

}
