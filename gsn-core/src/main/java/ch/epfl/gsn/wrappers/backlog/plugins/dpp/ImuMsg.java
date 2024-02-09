package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class ImuMsg extends AbstractMsg {

	private static DataField[] dataField = {
			new DataField("ACC_X", "INTEGER"), /* Accelerometer X-axis raw data */
			new DataField("ACC_Y", "INTEGER"), /* Accelerometer Y-axis raw data */
			new DataField("ACC_Z", "INTEGER"), /* Accelerometer Z-axis raw data */
			new DataField("MAG_X", "INTEGER"), /* Magnetometer X-axis raw data */
			new DataField("MAG_Y", "INTEGER"), /* Magnetometer Y-axis raw data */
			new DataField("MAG_Z", "INTEGER") /* Magnetometer Z-axis raw data */
	};

	/**
	 * Receives the payload from a ByteBuffer and converts it into an array of
	 * Serializable objects.
	 * 
	 * @param payload the ByteBuffer containing the payload data
	 * @return an array of Serializable objects representing the received payload
	 * @throws Exception if an error occurs during the conversion process
	 */
	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Integer acc_x = null;
		Integer acc_y = null;
		Integer acc_z = null;
		Integer mag_x = null;
		Integer mag_y = null;
		Integer mag_z = null;

		try {
			acc_x = convertINT16(payload);
			acc_y = convertINT16(payload);
			acc_z = convertINT16(payload);
			mag_x = convertINT16(payload);
			mag_y = convertINT16(payload);
			mag_z = convertINT16(payload);
		} catch (Exception e) {
		}

		return new Serializable[] { acc_x, acc_y, acc_z, mag_x, mag_y, mag_z };
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return MessageTypes.DPP_MSG_TYPE_IMU;
	}
}
