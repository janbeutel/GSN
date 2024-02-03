package ch.epfl.gsn.networking.mqtt;

import java.io.Serializable;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.wrappers.AbstractWrapper;

public class MQTTWrapper extends AbstractWrapper implements MqttCallbackExtended {

	private final transient Logger logger = LoggerFactory.getLogger(MQTTWrapper.class);

	private MqttAsyncClient client;
	private AddressBean addressBean;
	private String serverURI;
	private String clientID;
	private String topic;
	private int qos;

	/**
	 * Returns the output format of the MQTTWrapper.
	 * 
	 * @return an array of DataField objects representing the output format.
	 */
	@Override
	public DataField[] getOutputFormat() {
		return new DataField[] {
				new DataField("raw_packet", "BINARY", "The packet contains raw data received in the MQTT payload.") };
	}

	/**
	 * Initializes the MQTTWrapper by setting up the necessary parameters and
	 * connecting to the MQTT broker.
	 * 
	 * @return true if the initialization is successful, false otherwise.
	 */
	@Override
	public boolean initialize() {
		try {
			addressBean = getActiveAddressBean();
			serverURI = addressBean.getPredicateValue("uri");
			if (serverURI == null || serverURI.trim().length() == 0) {
				logger.error("The uri parameter is missing from the MQTT wrapper, initialization failed.");
				return false;
			}
			clientID = addressBean.getPredicateValue("client_id");
			if (clientID == null || clientID.trim().length() == 0) {
				logger.error("The client_id parameter is missing from the MQTT wrapper, initialization failed.");
				return false;
			}
			topic = addressBean.getPredicateValue("topic");
			if (topic == null || topic.trim().length() == 0) {
				logger.error("The topic parameter is missing from the MQTT wrapper, initialization failed.");
				return false;
			}
			qos = addressBean.getPredicateValueAsInt("qos", 0);
			if (qos < 0 || qos > 2) {
				logger.error("The qos parameter from MQTT wrapper can be 0, 1 or 2 (found " + qos
						+ "), initialization failed.");
				return false;
			}
			client = new MqttAsyncClient(serverURI, clientID);
			client.setCallback(this);
			client.connect();
		} catch (Exception e) {
			logger.error("Error in instanciating MQTT broker with " + topic + " @ " + serverURI, e);
			return false;
		}
		return true;
	}

	/**
	 * Disposes the MQTTWrapper by unsubscribing from the topic, disconnecting from
	 * the MQTT client,
	 * and closing the client connection.
	 */
	@Override
	public void dispose() {
		try {
			if (client.isConnected()) {
				client.unsubscribe(topic);
				client.disconnect();
			}
			client.close();
		} catch (MqttException e) {
			logger.warn("Error while closing the MQTT client.", e);
		}
	}

	@Override
	public String getWrapperName() {
		return "MQTTWrapper[" + topic + "]";
	}

	/**
	 * This method is called when the connection to the MQTT server is lost.
	 * It attempts to reconnect to the server after a 3-second delay.
	 * If the reconnection fails, an error message is logged.
	 *
	 * @param e the Throwable object representing the cause of the connection loss
	 */
	@Override
	public void connectionLost(Throwable e) {
		logger.warn("Connection to MQTT server lost. Reconnecting in 3s...", e);
		try {
			Thread.sleep(3000);
			client.reconnect();
		} catch (Exception e1) {
			logger.error("Error while reconnecting to server " + serverURI, e1);
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// MQTTWrapper doesn't publish
	}

	/**
	 * This method is called when a message is received from the MQTT broker.
	 * It logs the received message and creates a StreamElement object to post the
	 * message to the stream.
	 * 
	 * @param s The topic on which the message was received.
	 * @param m The MQTT message received.
	 * @throws Exception if an error occurs while processing the message.
	 */
	@Override
	public void messageArrived(String s, MqttMessage m) throws Exception {
		logger.info("Message received on topic " + s + ": " + new String(m.getPayload()));
		StreamElement streamElement = new StreamElement(getOutputFormat(), new Serializable[] { m.getPayload() },
				System.currentTimeMillis());
		postStreamElement(streamElement);
	}

	/**
	 * This method is called when the MQTT client successfully completes the
	 * connection to the server.
	 * If it is not a reconnection, it subscribes to the specified topic with the
	 * specified quality of service (QoS).
	 * If it is a reconnection, it logs a debug message indicating that the MQTT
	 * server has been reconnected.
	 *
	 * @param reconnect a boolean indicating whether it is a reconnection or not
	 * @param s         the server URI that the client has connected to
	 */
	@Override
	public void connectComplete(boolean reconnect, String s) {
		if (reconnect) {
			if(logger.isDebugEnabled()){
				logger.debug("MQTT server reconnected " + s);
			}
		} else {
			try {
				client.subscribe(topic, qos);
			} catch (MqttException e) {
				logger.error("Error while subscribing to topic " + topic + " with qos " + qos, e);
			}
		}
	}
}
