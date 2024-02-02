package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class GnssSvMsg extends AbstractMsg {

	private static DataField[] dataField = {
			new DataField("RCVTOW", "DOUBLE"), /* Receiver time of week [s] (converted from IEEE-754 to integer) */
			new DataField("WEEK", "INTEGER"), /* GPS week number */
			new DataField("LEAPS", "SMALLINT"), /* GPS leap seconds */
			new DataField("NUMMEAS", "SMALLINT"), /* Number of measurements */
			new DataField("RECSTAT", "SMALLINT"), /* Receiver tracking status bitfield */
			new DataField("PRMES", "DOUBLE"), /* Pseudo-range measurement (converted from IEEE-754 to integer */
			new DataField("CPMES", "DOUBLE"), /* Carrier-phase measurement (converted from IEEE-754 to integer) */
			new DataField("DOMES", "DOUBLE"), /* Doppler measurement (converted from IEEE-754 to integer) */
			new DataField("GNSSID", "SMALLINT"), /* GNSS identifier */
			new DataField("SVID", "SMALLINT"), /* Satellite identifier */
			new DataField("CNO", "SMALLINT"), /* Carrier-to-noise density ratio [db-Hz] */
			new DataField("LOCKTIME", "INTEGER"), /* Carrier phase locktime counter [ms] */
			new DataField("PRSTDEV", "SMALLINT"), /* Estimated pseudo-range std. deviation */
			new DataField("CPSTDEV", "SMALLINT"), /* Estimated carrier-phase std. deviation */
			new DataField("DOSTDEV", "SMALLINT"), /* Estimated doppler std. deviation */
			new DataField("TRKSTAT", "SMALLINT") /* Tracking status bitfield */
	};

	/**
	 * Receives the payload from a ByteBuffer and extracts the necessary data
	 * fields.
	 * 
	 * @param payload The ByteBuffer containing the payload data.
	 * @return An array of Serializable objects representing the extracted data
	 *         fields.
	 * @throws Exception If an error occurs while extracting the data fields.
	 */
	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Double rcvTow = null;
		Integer week = null;
		Short leapS = null;
		Short numMeas = null;
		Short recStat = null;
		Double prMes = null;
		Double cpMes = null;
		Float doMes = null;
		Short gnssId = null;
		Short svId = null;
		Short cno = null;
		Integer locktime = null;
		Short prStDev = null;
		Short cpStDev = null;
		Short doStDev = null;
		Short trkStat = null;

		try {
			rcvTow = payload.getDouble(); // uint8_t[8] IEEE-754
			week = convertUINT16(payload);
			leapS = convertINT8(payload);
			numMeas = convertUINT8(payload);
			recStat = convertUINT8(payload);
			prMes = payload.getDouble(); // uint8_t[8] IEEE-754
			cpMes = payload.getDouble(); // uint8_t[8] IEEE-754
			doMes = payload.getFloat(); // uint8_t[4] IEEE-754
			gnssId = convertUINT8(payload);
			svId = convertUINT8(payload);
			cno = convertUINT8(payload);
			locktime = convertUINT16(payload);
			prStDev = convertUINT8(payload);
			cpStDev = convertUINT8(payload);
			doStDev = convertUINT8(payload);
			trkStat = convertUINT8(payload);
		} catch (Exception e) {
		}

		return new Serializable[] { rcvTow, week, leapS, numMeas, recStat, prMes, cpMes, doMes, gnssId, svId, cno,
				locktime, prStDev, cpStDev, doStDev, trkStat };
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return ch.epfl.gsn.wrappers.backlog.plugins.dpp.MessageTypes.DPP_MSG_TYPE_GNSS_SV;
	}
}
