package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class AppHealthMsg extends AbstractMsg {

	private static DataField[] dataField = {
		new DataField("UPTIME", "BIGINT"), /* Uptime [seconds] */
		new DataField("MSG_CNT", "INTEGER"), /* Number of received messages */
		new DataField("CORE_VCC", "INTEGER"), /* Core voltage [10^-3 V] */
		new DataField("CORE_TEMP", "INTEGER"), /* Core temperature [10^-2 °C] */
		new DataField("CPU_DC", "INTEGER"), /* CPU duty cycle [10^-2 %] */
		new DataField("STACK", "SMALLINT"), /* Stack [watermark over the last period in %] */
		new DataField("NV_MEM", "SMALLINT"), /* Non-volatile memory usage [%] */
		new DataField("SUPPLY_VCC", "INTEGER"), /* Supply voltage [10^-3 V] */
		new DataField("SUPPLY_CURRENT", "INTEGER"), /* Supply [10^-5 A] */
		new DataField("TEMPERATURE", "INTEGER"), /* Temperature [10^-2 °C] */
		new DataField("HUMIDITY", "INTEGER") /* Humidity [10^-2 %] */
	};

	/**
	 * Receives the payload from a ByteBuffer and extracts the necessary data
	 * fields.
	 * 
	 * @param payload The ByteBuffer containing the payload data.
	 * @return An array of Serializable objects containing the extracted data
	 *         fields.
	 * @throws Exception If an error occurs while extracting the data fields.
	 */
	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Long uptime = null;
		Integer msg_cnt = null;
		Integer core_vcc = null;
		Integer core_temp = null;
		Integer cpu_dc = null;
		Short stack = null;
		Short nv_mem = null;
		Integer supply_vcc = null;
		Integer supply_current = null;
		Integer temperature = null;
		Integer humidity = null;

		try {
			uptime = convertUINT32(payload);
			msg_cnt = convertUINT16(payload);
			core_vcc = convertUINT16(payload);
			core_temp = convertINT16(payload);
			cpu_dc = convertUINT16(payload);
			stack = convertUINT8(payload);
			nv_mem = convertUINT8(payload);
			supply_vcc = convertUINT16(payload);
			supply_current = convertUINT16(payload);
			temperature = convertINT16(payload);
			humidity = convertUINT16(payload);
		} catch (Exception e) {
		}

		return new Serializable[] { uptime, msg_cnt, core_vcc, core_temp, cpu_dc, stack, nv_mem, supply_vcc,
				supply_current, temperature, humidity };
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return MessageTypes.DPP_MSG_TYPE_APP_HEALTH;
	}

}
