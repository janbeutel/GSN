package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class LwbHealthMsg extends AbstractMsg {

	private static DataField[] dataField = {
			new DataField("BOOTSTRAP_CNT", "SMALLINT"), /* Sync lost counter */
			new DataField("SLEEP_CNT", "SMALLINT"), /* Deepsleep counter */
			new DataField("FSR", "INTEGER"), /* Flood success rate [10^-2 %] */
			new DataField("T_TO_RX", "INTEGER"), /* Listen time to start of packet reception [us] */
			new DataField("T_FLOOD", "INTEGER"), /* Flood duration [us] */
			new DataField("N_TX", "SMALLINT"), /* TX count of last Glossy flood */
			new DataField("N_RX", "SMALLINT"), /* RX count of last Glossy flood */
			new DataField("N_HOPS", "SMALLINT"), /* Average hop count of first received packet of a flood */
			new DataField("N_HOPS_MAX", "SMALLINT"), /* Max. hop count of first received packet of a flood */
			new DataField("UNSYNCED_CNT", "SMALLINT"), /* Lost sync counter */
			new DataField("DRIFT", "SMALLINT"), /* Estimated drift [ppm] */
			new DataField("BUS_LOAD", "SMALLINT") /* Bus utilization [%] */
	};

	/**
	 * Receives the payload from a ByteBuffer and extracts the values of various
	 * parameters.
	 * 
	 * @param payload the ByteBuffer containing the payload
	 * @return an array of Serializable objects representing the extracted parameter
	 *         values
	 * @throws Exception if an error occurs during the extraction process
	 */
	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Short bootstrap_cnt = null;
		Short sleep_cnt = null;
		Integer fsr = null;
		Integer t_to_rx = null;
		Integer t_flood = null;
		Short n_tx = null;
		Short n_rx = null;
		Short n_hops = null;
		Short n_hops_max = null;
		Short unsynced_cnt = null;
		Short drift = null;
		Short bus_load = null;

		try {
			bootstrap_cnt = convertUINT8(payload);
			sleep_cnt = convertUINT8(payload);
			fsr = convertUINT16(payload);
			t_to_rx = convertUINT16(payload);
			t_flood = convertUINT16(payload);
			n_tx = convertUINT8(payload);
			n_rx = convertUINT8(payload);
			n_hops = convertUINT8(payload);
			n_hops_max = convertUINT8(payload);
			unsynced_cnt = convertUINT8(payload);
			drift = convertUINT8(payload);
			bus_load = convertUINT8(payload);
		} catch (Exception e) {
		}

		return new Serializable[] { bootstrap_cnt, sleep_cnt, fsr, t_to_rx, t_flood, n_tx, n_rx, n_hops, n_hops_max,
				unsynced_cnt, drift, bus_load };
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return ch.epfl.gsn.wrappers.backlog.plugins.dpp.MessageTypes.DPP_MSG_TYPE_LWB_HEALTH;
	}
}
