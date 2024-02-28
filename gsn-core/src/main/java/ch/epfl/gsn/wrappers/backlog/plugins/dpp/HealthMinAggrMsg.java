package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class HealthMinAggrMsg extends AbstractMsg {
	
	private static DataField[] dataField = {
			new DataField("BLOCK_CNT", "SMALLINT"),			/* block count */
			new DataField("BLOCK_SIZE", "SMALLINT"),		/* block size */
			new DataField("HEALTH_BLOCKS", "BINARY")		/* health blocks */
			};

	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Short block_cnt = null;
		Short block_size = null;
		byte[] health_blocks = null;
		
		try {
			block_cnt = convertUINT8(payload);				// uint8_t
			block_size = convertUINT8(payload);				// uint8_t
			health_blocks = new byte[payload.remaining()];	// block_cnt * [node_id (uint16_t), health_min (dpp_health_min_t)]
			payload.get(health_blocks);
		} catch (Exception e) {
		}
        
		return new Serializable[]{block_cnt, block_size, health_blocks};
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return MessageTypes.DPP_MSG_TYPE_HEALTH_AGGR;
	}
}
