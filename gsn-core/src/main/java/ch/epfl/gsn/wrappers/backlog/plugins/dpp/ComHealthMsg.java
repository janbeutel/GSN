package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class ComHealthMsg extends AbstractMsg {

	private static DataField[] dataField = {
			/* all values are from the last health period, unless otherwise stated */
			new DataField("UPTIME", "BIGINT"), /* Uptime [seconds] */
			new DataField("MSG_CNT", "INTEGER"), /* Number of received messages */
			new DataField("CORE_VCC", "INTEGER"), /* Core voltage [10^-3 V] */
			new DataField("CORE_TEMP", "INTEGER"), /* Core temperature [10^-2 °C] */
			new DataField("CPU_DC", "INTEGER"), /* CPU duty cycle [10^-2 %] */
			new DataField("STACK", "SMALLINT"), /* Stack [watermark over the last period in %] */

			new DataField("RADIO_SNR", "SMALLINT"), /* average signal-to-noise ratio [dBm] */
			new DataField("RADIO_RSSI", "SMALLINT"), /* average RSSI value [-dBm] */
			new DataField("RADIO_TX_PWR", "SMALLINT"), /* Transmit power [dBm] */
			new DataField("RADIO_RX_DC", "INTEGER"), /* radio transmit duty cycle [10^-2 %] */
			new DataField("RADIO_TX_DC", "INTEGER"), /* radio listen duty cycle [10^-2 %] */
			new DataField("RADIO_PER", "INTEGER"), /* radio packet error rate [10^-2 %] */

			new DataField("RX_CNT", "INTEGER"), /* Number of successfully received packets */
			new DataField("TX_QUEUE", "SMALLINT"), /* Number of packets in the transmit buffer */
			new DataField("RX_QUEUE", "SMALLINT"), /* Number of packets in the receive buffer */
			new DataField("TX_DROPPED", "SMALLINT"), /* Dropped packets due to TX queue full */
			new DataField("RX_DROPPED", "SMALLINT") /* Dropped packets due to RX queue full */
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

		Short radio_snr = null;
		Short radio_rssi = null;
		Short radio_tx_pwr = null;
		Integer radio_rx_dc = null;
		Integer radio_tx_dc = null;
		Integer radio_per = null;

		Integer rx_cnt = null;
		Short tx_queue = null;
		Short rx_queue = null;
		Short tx_dropped = null;
		Short rx_dropped = null;

		try {
			uptime = convertUINT32(payload); // uint32_t
			msg_cnt = convertUINT16(payload); // uint16_t
			core_vcc = convertUINT16(payload); // uint16_t
			core_temp = convertINT16(payload); // int16_t
			cpu_dc = convertUINT16(payload); // uint16_t
			stack = convertUINT8(payload); // uint8_t

			radio_snr = convertUINT8(payload); // uint8_t
			radio_rssi = convertUINT8(payload); // uint8_t
			radio_tx_pwr = convertINT8(payload); // int8_t
			radio_rx_dc = convertUINT16(payload); // uint16_t
			radio_tx_dc = convertUINT16(payload); // uint16_t
			radio_per = convertUINT16(payload); // uint16_t

			rx_cnt = convertUINT16(payload); // uint16_t
			tx_queue = convertUINT8(payload); // uint8_t
			rx_queue = convertUINT8(payload); // uint8_t
			tx_dropped = convertUINT8(payload); // uint8_t
			rx_dropped = convertUINT8(payload); // uint8_t
		} catch (Exception e) {
		}

		return new Serializable[] { uptime, msg_cnt, core_vcc, core_temp, cpu_dc, stack,
				radio_snr, radio_rssi, radio_tx_pwr, radio_rx_dc, radio_tx_dc, radio_per,
				rx_cnt, tx_queue, rx_queue, tx_dropped, rx_dropped };
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return ch.epfl.gsn.wrappers.backlog.plugins.dpp.MessageTypes.DPP_MSG_TYPE_COM_HEALTH;
	}
}
