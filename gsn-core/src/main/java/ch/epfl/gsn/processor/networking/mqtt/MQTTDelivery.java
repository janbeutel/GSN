package ch.epfl.gsn.networking.mqtt;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.delivery.DeliverySystem;

public class MQTTDelivery implements DeliverySystem {

	private final transient Logger logger = LoggerFactory.getLogger(MQTTDelivery.class);

	private MqttClient client;
	private String serverURI;
	private String topic;
	private String vsname;
	private boolean closed = false;
	private MqttConnectOptions options = new MqttConnectOptions();

	public MQTTDelivery(String serverURI, String clientID, String topic, String vsname) {
		try {
			client = new MqttClient(serverURI, clientID);
			options.setAutomaticReconnect(true);
			client.connect(options);
		} catch (Exception e) {
			logger.error("Unable to instanciate delivery system MQTT.", e);
		}
	}

	/**
	 * Writes the structure of the data fields to the MQTT broker.
	 * 
	 * @param fields the data fields to be written
	 * @throws IOException if an I/O error occurs while writing the structure
	 */
	@Override
	public void writeStructure(DataField[] fields) throws IOException {
		StreamElement se = new StreamElement(fields, new Integer[fields.length]);
		try {
			client.publish(topic, se.toJSON(vsname).getBytes(), 0, true);
		} catch (MqttException e) {
			logger.error("Unable to publish stream element to topic " + topic + " on " + serverURI);
		}
	}

	/**
	 * Writes a StreamElement to the MQTT topic.
	 * 
	 * @param se The StreamElement to be written.
	 * @return true if the StreamElement was successfully published, false
	 *         otherwise.
	 */
	@Override
	public boolean writeStreamElement(StreamElement se) {
		try {
			client.publish(topic, se.toJSON(vsname).getBytes(), 0, false);
		} catch (MqttException e) {
			logger.error("Unable to publish stream element to topic " + topic + " on " + serverURI);
			return false;
		}
		return true;
	}

	@Override
	public boolean writeKeepAliveStreamElement() {
		// The client takes care of keep-alive
		return true;
	}

	/**
	 * Closes the MQTT client connection and releases any resources associated with
	 * it.
	 * After calling this method, the client is no longer usable.
	 */
	@Override
	public void close() {
		try {
			client.disconnect();
			client.close();
			closed = true;
		} catch (MqttException e) {
			logger.warn("Error while closing the MQTT client.", e);
		}
	}

	/**
	 * Returns whether the MQTT delivery is closed or not.
	 *
	 * @return true if the MQTT delivery is closed, false otherwise.
	 */
	@Override
	public boolean isClosed() {
		return closed;
	}
}
