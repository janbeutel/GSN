package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class GeophoneAdcDataMsg extends AbstractMsg {
	
	private static DataField[] dataField = {
			new DataField("ID", "BIGINT"),					/* acquisition ID */
			new DataField("OFFSET", "INTEGER"),				/* offset from the start of the waveform, in no. of packets */
			new DataField("PACKETS", "INTEGER"),			/* total no. of packets for this waveform */
			new DataField("ADC_DATA_FMT", "SMALLINT"),		/* data format for the adc samples */
			new DataField("ADC_DATA", "BINARY"),			/* ADC data */
			};

	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Long id = null;
		Integer offset = null;
		Integer packets = null;
		Short adc_data_fmt = null;
		byte[] adc_data = null;
		
		try {
			id = convertUINT32(payload);
			offset = convertUINT16(payload);
			packets = convertUINT16(payload);
			adc_data_fmt = convertUINT8(payload);
			adc_data = new byte[payload.remaining()];
			payload.get(adc_data);
		} catch (Exception e) {
		}
        
		return new Serializable[]{id, offset, packets, adc_data_fmt, adc_data};
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return MessageTypes.DPP_MSG_TYPE_GEOPHONE_ADCDATA;
	}

}
