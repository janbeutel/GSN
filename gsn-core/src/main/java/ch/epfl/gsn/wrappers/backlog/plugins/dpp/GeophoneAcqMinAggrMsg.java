package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class GeophoneAcqMinAggrMsg extends AbstractMsg {
	
	private static DataField[] dataField = {
			new DataField("BLOCK_CNT", "SMALLINT"),					/* block count */
			new DataField("BLOCK_SIZE", "SMALLINT"),				/* block size */
			new DataField("GEOPHONE_ACQ_BLOCKS", "BINARY")			/* geophone acquisition blocks */
			};

	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Short block_cnt = null;
		Short block_size = null;
		byte[] geophone_acq_blocks = null;
		
		try {
			block_cnt = convertUINT8(payload);						// uint8_t
			block_size = convertUINT8(payload);						// uint8_t
			geophone_acq_blocks = new byte[payload.remaining()];	// block_cnt * [node_id (uint16_t), geo_acq_min (dpp_geophone_acq_min_t)]
			payload.get(geophone_acq_blocks);
		} catch (Exception e) {
		}
        
		return new Serializable[]{block_cnt, block_size, geophone_acq_blocks};
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return MessageTypes.DPP_MSG_TYPE_GEO_ACQ_AGGR;
	}
}
