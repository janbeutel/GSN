package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class GeophoneAcqMsg extends AbstractMsg {

	private static DataField[] dataField = {
			new DataField("START_TIME", "BIGINT"), /* Timestamp of trigger */
			new DataField("FIRST_TIME", "BIGINT"), /* Timestamp of first ADC sample */
			new DataField("SAMPLES", "BIGINT"), /* Total no. of samples */
			new DataField("PEAK_POS_VAL", "BIGINT"), /* Positive peak value (can be pos or neg!) of ADC */
			new DataField("PEAK_POS_SAMPLE", "BIGINT"), /* Positive peak location: sample no. of peak value of ADC */
			new DataField("PEAK_NEG_VAL", "BIGINT"), /* Negative peak value (can be pos or neg!) of ADC */
			new DataField("PEAK_NEG_SAMPLE", "BIGINT"), /* Negative peak location: sample no. of peak value of ADC */
			new DataField("TRG_COUNT_POS", "BIGINT"), /* Count of positive triggers */
			new DataField("TRG_COUNT_NEG", "BIGINT"), /* Count of negative triggers */
			new DataField("TRG_LAST_POS_SAMPLE", "BIGINT"), /* Last positive TRG location: sample no. of ADC */
			new DataField("TRG_LAST_NEG_SAMPLE", "BIGINT"), /* Last negative TRG location: sample no. of ADC */
			new DataField("TRG_GAIN", "INTEGER"), /* Amplification gain value of TRG */
			new DataField("TRG_TH_POS", "INTEGER"), /* Positive TRG threshold value [mV] */
			new DataField("TRG_TH_NEG", "INTEGER"), /* Negative TRG threshold value [mV] */
			new DataField("TRG_SOURCE", "SMALLINT"), /*
														 * Source of initial trigger. 0: external | 1: positive
														 * threshold, 2: negative threshold
														 */
			new DataField("ADC_PGA", "SMALLINT"), /* ADC PGA value. 0: PGA is off. */
			new DataField("ID", "BIGINT"), /* Acquire # Identifier */
			new DataField("ADC_SPS", "SMALLINT") /*
													 * ADC rate (sampling frequency), 0 = 1kHz, 1 = 500Hz, 2 = 250Hz, 3
													 * = 125Hz
													 */
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
		Long start_time = null;
		Long first_time = null;
		Long samples = null;
		Long peak_pos_val = null;
		Long peak_pos_sample = null;
		Long peak_neg_val = null;
		Long peak_neg_sample = null;
		Long trg_count_pos = null;
		Long trg_count_neg = null;
		Long trg_last_pos_sample = null;
		Long trg_last_neg_sample = null;
		Integer trg_gain = null;
		Integer trg_th_pos = null;
		Integer trg_th_neg = null;
		Short trg_source = null;
		Short adc_pga = null;
		Long id = null;
		Short adc_sps = null;

		try {
			start_time = convertUINT64(payload);
			first_time = convertUINT64(payload);
			samples = convertUINT32(payload);
			peak_pos_val = convertUINT32(payload);
			peak_pos_sample = convertUINT32(payload);
			peak_neg_val = convertUINT32(payload);
			peak_neg_sample = convertUINT32(payload);
			trg_count_pos = convertUINT32(payload);
			trg_count_neg = convertUINT32(payload);
			trg_last_pos_sample = convertUINT32(payload);
			trg_last_neg_sample = convertUINT32(payload);
			trg_gain = convertUINT16(payload);
			trg_th_pos = convertUINT16(payload);
			trg_th_neg = convertUINT16(payload);
			trg_source = convertUINT8(payload);
			adc_pga = convertUINT8(payload);
			id = convertUINT32(payload);
			adc_sps = convertUINT8(payload);
		} catch (Exception e) {
		}

		return new Serializable[] { start_time, first_time, samples, peak_pos_val, peak_pos_sample, peak_neg_val,
				peak_neg_sample, trg_count_pos, trg_count_neg, trg_last_pos_sample, trg_last_neg_sample, trg_gain,
				trg_th_pos, trg_th_neg, trg_source, adc_pga, id, adc_sps };
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return MessageTypes.DPP_MSG_TYPE_GEOPHONE_ACQ;
	}

}
