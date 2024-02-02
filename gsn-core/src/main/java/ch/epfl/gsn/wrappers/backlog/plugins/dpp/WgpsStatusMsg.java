package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class WgpsStatusMsg extends AbstractMsg {

	private static DataField[] dataField = {
			new DataField("INC_X", "INTEGER"), /* Inclinometer X raw data */
			new DataField("INC_Y", "INTEGER"), /* Inclinometer Y raw data */
			new DataField("GNSS_SV_QUEUE", "INTEGER"), /* Number of gnss_sv messages in queue */
			new DataField("WGPS_STATUS_QUEUE", "INTEGER"), /* Number of wgps_status messages in queue */
			new DataField("APP_HEALTH_QUEUE", "INTEGER"), /* Number of app_health messages in queue */
			new DataField("EVENT_QUEUE", "INTEGER"), /* Number of events in queue */
			new DataField("CARD_USAGE", "BIGINT"), /* Card usage [kB] */
			new DataField("STATUS", "INTEGER") /* Bit0: gps power state (on/off) */
	};

	/**
	 * Receives the payload from a ByteBuffer and converts it into an array of
	 * Serializable objects.
	 * 
	 * @param payload the ByteBuffer containing the payload
	 * @return an array of Serializable objects representing the received payload
	 * @throws Exception if an error occurs during the conversion process
	 */
	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Integer inc_x = null;
		Integer inc_y = null;
		Integer gnss_sv_queue = null;
		Integer wgps_status_queue = null;
		Integer app_health_queue = null;
		Integer event_queue = null;
		Long card_usage = null;
		Integer status = null;

		try {
			inc_x = convertINT16(payload);
			inc_y = convertINT16(payload);
			gnss_sv_queue = convertUINT16(payload);
			wgps_status_queue = convertUINT16(payload);
			app_health_queue = convertUINT16(payload);
			event_queue = convertUINT16(payload);
			card_usage = convertUINT32(payload);
			status = convertUINT16(payload);
		} catch (Exception e) {
		}

		return new Serializable[] { inc_x, inc_y, gnss_sv_queue, wgps_status_queue, app_health_queue, event_queue,
				card_usage, status };
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return ch.epfl.gsn.wrappers.backlog.plugins.dpp.MessageTypes.DPP_MSG_TYPE_WGPS_STATUS;
	}
}
