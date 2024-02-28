package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.beans.DataField;

public class AckCommandMsg extends AbstractMsg {
	
	private static DataField[] dataField = {
			new DataField("ACK_SEQ_NO", "INTEGER"),				/* sequence no. of the acknowledged packet */
			new DataField("NUM_CMDS", "INTEGER"),				/* number of node ID + command tuples (note: use 16 bits for better alignment) */
			new DataField("COMMANDS", "BINARY")					/* list of commands */
			};

	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Integer ack_seq_no = null;
		Integer num_cmds = null;
		byte[]  commands = null;
		
		try {
			ack_seq_no = convertUINT16(payload); 			// uint16_t
			num_cmds = convertUINT16(payload); 				// uint16_t
			commands = new byte[payload.remaining()];		// num_cmds * dpp_cmd_min_t
			payload.get(commands);
		} catch (Exception e) {
		}
        
		return new Serializable[]{ack_seq_no, num_cmds, commands};
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return MessageTypes.DPP_MSG_TYPE_ACK_COMMAND;
	}
}
