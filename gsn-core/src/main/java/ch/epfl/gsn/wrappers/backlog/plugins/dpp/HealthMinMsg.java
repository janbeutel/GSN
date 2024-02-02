package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class HealthMinMsg extends AbstractMsg {

	private static DataField[] dataField = {
			/* all values are from the last health period, unless otherwise stated */
			new DataField("UPTIME", "INTEGER"), /* system uptime (either COM or APP) [h] */
			new DataField("CPU_DC_APP", "INTEGER"), /* CPU duty cycle (APP) [10^-2 %] */
			new DataField("CPU_DC_COM", "INTEGER"), /* CPU duty cycle (COM) [10^-2 %] */
			new DataField("SUPPLY_VCC", "SMALLINT"), /* system supply voltage [10^-1 V] */
			new DataField("NV_MEM", "SMALLINT"), /* Non-volatile memory usage (typically SD card of APP) [%] */
			new DataField("STACK_WM", "SMALLINT"), /* Stack watermark (max value of APP and COM) [%] */

			new DataField("TEMPERATURE", "SMALLINT"), /* Ambient temperature [°C] */
			new DataField("HUMIDITY", "SMALLINT"), /* Ambient humidity [%] */

			new DataField("RADIO_PRR", "SMALLINT"), /* Average packet reception rate [%] */
			new DataField("RADIO_RSSI", "SMALLINT"), /* Average receive signal strength indicator [dBm] */
			new DataField("HOP_CNT", "SMALLINT"), /* Average hop count on first reception [hops * 10] */
			new DataField("RADIO_RX_DC", "INTEGER"), /* RX duty cycle (COM) [10^-2 %] */
			new DataField("RADIO_TX_DC", "INTEGER"), /* TX duty cycle (COM) [10^-2 %] */

			new DataField("EVENTS", "INTEGER"), /* bit field to indicate events that occurred */
			new DataField("CONFIG", "INTEGER") /* contains a part of the current node configuration (bit field) */
	};

	/**
	 * Receives the payload from a ByteBuffer and extracts the necessary data
	 * fields.
	 * 
	 * @param payload the ByteBuffer containing the payload data
	 * @return an array of Serializable objects containing the extracted data fields
	 * @throws Exception if an error occurs during the extraction process
	 */
	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Integer uptime = null;
		Integer cpu_dc_app = null;
		Integer cpu_dc_com = null;
		Short supply_vcc = null;
		Short nv_mem = null;
		Short stack_wm = null;

		Short temperature = null;
		Short humidity = null;

		Short radio_prr = null;
		Short radio_rssi = null;
		Short radio_hop_cnt = null;
		Integer radio_rx_dc = null;
		Integer radio_tx_dc = null;

		Integer events = null;
		Integer config = null;

		try {
			uptime = convertUINT16(payload); // uint16_t
			cpu_dc_app = convertUINT16(payload); // uint16_t
			cpu_dc_com = convertUINT16(payload); // uint16_t
			supply_vcc = convertUINT8(payload); // uint8_t
			nv_mem = convertUINT8(payload); // uint8_t
			stack_wm = convertUINT8(payload); // uint8_t

			temperature = convertINT8(payload); // int8_t
			humidity = convertUINT8(payload); // uint8_t

			radio_prr = convertUINT8(payload); // uint8_t
			radio_rssi = convertINT8(payload); // int8_t
			radio_hop_cnt = convertUINT8(payload); // uint8_t
			radio_rx_dc = convertUINT16(payload); // uint16_t
			radio_tx_dc = convertUINT16(payload); // uint16_t

			events = convertUINT16(payload); // uint16_t
			config = convertUINT16(payload); // uint16_t
		} catch (Exception e) {
		}

		return new Serializable[] { uptime, cpu_dc_app, cpu_dc_com, supply_vcc, nv_mem, stack_wm,
				temperature, humidity, radio_prr, radio_rssi, radio_hop_cnt, radio_rx_dc,
				radio_tx_dc, events, config };
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return ch.epfl.gsn.wrappers.backlog.plugins.dpp.MessageTypes.DPP_MSG_TYPE_HEALTH_MIN;
	}
}
