package ch.epfl.gsn.http.rest;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.storage.SQLValidator;

import java.io.IOException;
import java.sql.SQLException;

public class DefaultDistributionRequest implements DistributionRequest {

	private long startTime;
	private boolean continuous;

	private long lastVisitedPk = -1;

	private String query;

	private DeliverySystem deliverySystem;

	private VSensorConfig vSensorConfig;

	private DefaultDistributionRequest(DeliverySystem deliverySystem, VSensorConfig sensorConfig, String query,
			long startTime, boolean continuous) throws IOException, SQLException {
		this.deliverySystem = deliverySystem;
		vSensorConfig = sensorConfig;
		this.query = query;
		this.startTime = startTime;
		this.continuous = continuous;
		DataField[] selectedColmnNames = SQLValidator.getInstance().extractSelectColumns(query, vSensorConfig);
		deliverySystem.writeStructure(selectedColmnNames);
	}

	public static DefaultDistributionRequest create(DeliverySystem deliverySystem, VSensorConfig sensorConfig,
			String query, long startTime, boolean continuous) throws IOException, SQLException {
		DefaultDistributionRequest toReturn = new DefaultDistributionRequest(deliverySystem, sensorConfig, query,
				startTime, continuous);
		return toReturn;
	}

	public String toString() {
		return new StringBuilder("DefaultDistributionRequest Request[[ Delivery System: ")
				.append(deliverySystem.getClass().getName())
				.append("],[User: ").append(deliverySystem.getUser())
				.append("],[Query:").append(query)
				.append("],[startTime:").append(startTime)
				.append("],[continuous:").append(continuous)
				.append("],[VirtualSensorName:")
				.append(vSensorConfig.getName())
				.append("]]").toString();
	}

	public boolean deliverKeepAliveMessage() {
		return deliverySystem.writeKeepAliveStreamElement();
	}

	public boolean deliverStreamElement(StreamElement se) {
		boolean success = deliverySystem.writeStreamElement(se);
		// boolean success = true;
		if (success) {
			startTime = se.getTimeStamp();
			// lastVisitedPk = se.getInternalPrimayKey();
		}
		return success;
	}

	public long getStartTime() {
		return startTime;
	}

	public boolean isContinuous() {
		return continuous;
	}

	public long getLastVisitedPk() {
		return lastVisitedPk;
	}

	public String getQuery() {
		return query;
	}

	public VSensorConfig getVSensorConfig() {
		return vSensorConfig;
	}

	public void close() {
		deliverySystem.close();
	}

	public boolean isClosed() {
		return deliverySystem.isClosed();
	}

	public DeliverySystem getDeliverySystem() {
		return deliverySystem;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DefaultDistributionRequest that = (DefaultDistributionRequest) o;

		if (deliverySystem == null ? that.deliverySystem != null : !deliverySystem.equals(that.deliverySystem) ) {
			return false;
		}

		if (query == null ? that.query != null : !query.equals(that.query)) {
			return false;
		}
		if (vSensorConfig == null ? that.vSensorConfig != null :!vSensorConfig.equals(that.vSensorConfig)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = query == null ? 0 : query.hashCode();
		result = 31 * result + (deliverySystem == null ? 0 : deliverySystem.hashCode());
		result = 31 * result + (vSensorConfig == null ? 0 : vSensorConfig.hashCode());
		return result;
	}
}
