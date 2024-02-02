package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class InclinometerMsg extends AbstractMsg {

	private static DataField[] dataField = {
			new DataField("ACC_X", "INTEGER"), /* Accelerometer X-axis raw data */
			new DataField("ACC_Y", "INTEGER"), /* Accelerometer Y-axis raw data */
			new DataField("ACC_Z", "INTEGER"), /* Accelerometer Z-axis raw data */
			new DataField("ANG_X", "INTEGER"), /* Angle X-axis raw data */
			new DataField("ANG_Y", "INTEGER"), /* Angle Y-axis raw data */
			new DataField("ANG_Z", "INTEGER"), /* Angle Z-axis raw data */
			new DataField("TEMPERATURE", "INTEGER") /* Temperature */
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
		Integer ang_x = null;
		Integer ang_y = null;
		Integer ang_z = null;
		Integer temperature = null;

		try {
			acc_x = convertINT16(payload);
			acc_y = convertINT16(payload);
			acc_z = convertINT16(payload);
			ang_x = convertINT16(payload);
			ang_y = convertINT16(payload);
			ang_z = convertINT16(payload);
			temperature = convertINT16(payload);
		} catch (Exception e) {
		}

		return new Serializable[] { acc_x, acc_y, acc_z, ang_x, ang_y, ang_z, temperature };
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return ch.epfl.gsn.wrappers.backlog.plugins.dpp.MessageTypes.DPP_MSG_TYPE_INCLINO;
	}
}
