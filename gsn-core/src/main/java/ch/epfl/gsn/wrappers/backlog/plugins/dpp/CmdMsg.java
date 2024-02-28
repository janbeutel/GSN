package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import ch.epfl.gsn.beans.DataField;

public class CmdMsg extends AbstractMsg {

	private static final String DPP_CMD_TYPE = "type";
	private static final String DPP_CMD_VALUE = "value";

	private static final int DPP_CMD_TYPE_MAX = 255;

	private static final int CMD_SX1262_BASEBOARD_ENABLE = 0x14;
	private static final int CMD_SX1262_BASEBOARD_DISABLE = 0x15;
	private static final int CMD_SX1262_BASEBOARD_POWER_EXT3 = 0x17;

	private static final int WAKEUP_TYPE_POWER_ON = 3;

	private static final int CMD_BASEBOARD_POWEROFF = 0x01;
	private static final int CMD_BASEBOARD_MSG_ROUTING = 0x04;
	private static final int CMD_BASEBOARD_DUTYCYCLE = 0x05;
	
	private static final int CMD_GEOPHONE_REQ_ADCDATA = 0x0D;
	private static final int CMD_GEOPHONE_DEL_DATA = 0x0F;
	private static final int CMD_GEOPHONE_SCHED_ADD = 0x17;

	private Short component_id = null;
	private Short type = null;
	private byte[] command;
	
	private static DataField[] dataField = {
			new DataField("COMPONENT_ID", "SMALLINT"),		/* component id */
			new DataField("TYPE", "SMALLINT"),				/* command type */
			new DataField("VALUE", "BINARY")};				/* arguments */

	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		type = convertUINT8(payload);
		component_id = convertUINT8(payload);
        
		byte[] cmd = new byte[payload.remaining()];
		payload.get(cmd);
		return new Serializable[]{component_id, type, cmd};
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public ByteBuffer sendPayload(String action, String[] paramNames, Object[] paramValues) throws Exception {
		ByteBuffer bb;
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		for (int i=0; i<paramNames.length;i++)
			params.put(paramNames[i].toLowerCase().trim(), paramValues[i]);
		
		if (action.compareToIgnoreCase("SX1262_BASEBOARD_POWER_CMD") == 0) {
			if (new Integer((String)params.get("status")) == 1) {
				bb = ByteBuffer.wrap(new byte[9]);
				type = CMD_SX1262_BASEBOARD_ENABLE;
			}
			else {
				bb = ByteBuffer.wrap(new byte[7]);
				type = CMD_SX1262_BASEBOARD_DISABLE;
			}
		}
		else if (action.compareToIgnoreCase("SX1262_BASEBOARD_POWER_EXT3") == 0) {
			bb = ByteBuffer.wrap(new byte[3]);
			type = CMD_SX1262_BASEBOARD_POWER_EXT3;
		}
		else if (action.compareToIgnoreCase("BASEBOARD_PWR_CMD") == 0) {
			bb = ByteBuffer.wrap(new byte[3]);
			type = new Short((String)params.get("port"));
		}
		else if (action.compareToIgnoreCase("BASEBOARD_POWEROFF_CMD") == 0) {
			bb = ByteBuffer.wrap(new byte[3]);
			type = CMD_BASEBOARD_POWEROFF;
		}
		else if (action.compareToIgnoreCase("BASEBOARD_DUTYCYCLE_CMD") == 0) {
			bb = ByteBuffer.wrap(new byte[3]);
			type = CMD_BASEBOARD_DUTYCYCLE;
		}
		else if (action.compareToIgnoreCase("CMD_BASEBOARD_MSG_ROUTING") == 0) {
			bb = ByteBuffer.wrap(new byte[3]);
			type = CMD_BASEBOARD_MSG_ROUTING;
		}
		else if (action.compareToIgnoreCase("GEOPHONE_REQ_ADCDATA_CMD") == 0) {
			bb = ByteBuffer.wrap(new byte[7]);
			type = CMD_GEOPHONE_REQ_ADCDATA;
		}
		else if (action.compareToIgnoreCase("GEOPHONE_DEL_DATA_CMD") == 0) {
			bb = ByteBuffer.wrap(new byte[10]);
			type = CMD_GEOPHONE_DEL_DATA;
		}
		else if (action.compareToIgnoreCase("GEOPHONE_SCHED_ADD_CMD") == 0) {
			bb = ByteBuffer.wrap(new byte[10]);
			type = CMD_GEOPHONE_SCHED_ADD;
		}
		else {
			bb = ByteBuffer.wrap(new byte[4]);
			try {
	        	type = new Short((String)params.get(DPP_CMD_TYPE));
			} catch (NumberFormatException e) {
				throw new Exception("type field has to be a short");
			}
		}
		bb = bb.order(ByteOrder.LITTLE_ENDIAN);
		command = new byte[bb.capacity()-2];
		
		if (type == null)
			throw new Exception("type field has to be specified");
		else if (type < 0 || type > DPP_CMD_TYPE_MAX)
			throw new Exception("type field has to be an integer between 0 and " + DPP_CMD_TYPE_MAX);
		
		
		if( action.compareToIgnoreCase("SX1262_CMD") == 0 || action.compareToIgnoreCase("SX1262_BASEBOARD_POWER_CMD") == 0 || action.compareToIgnoreCase("SX1262_BASEBOARD_POWER_EXT3") == 0 )
			component_id = ComponentId.DPP_COMPONENT_ID_SX1262;
		else if( action.compareToIgnoreCase("BASEBOARD_PWR_CMD") == 0 || action.compareToIgnoreCase("BASEBOARD_POWEROFF_CMD") == 0 || action.compareToIgnoreCase("BASEBOARD_DUTYCYCLE_CMD") == 0 || action.compareToIgnoreCase("CMD_BASEBOARD_MSG_ROUTING") == 0 )
			component_id = ComponentId.DPP_COMPONENT_ID_BASEBOARD;
		else if( action.compareToIgnoreCase("CC430_CMD") == 0 )
			component_id = ComponentId.DPP_COMPONENT_ID_CC430;
		else if( action.compareToIgnoreCase("GEOPHONE_CMD") == 0 || action.compareToIgnoreCase("GEOPHONE_REQ_ADCDATA_CMD") == 0 || action.compareToIgnoreCase("GEOPHONE_DEL_DATA_CMD") == 0 || action.compareToIgnoreCase("GEOPHONE_SCHED_ADD_CMD") == 0 )
			component_id = ComponentId.DPP_COMPONENT_ID_GEOPHONE;
		else if( action.compareToIgnoreCase("BOLTBRIDGE_CMD") == 0 )
			component_id = ComponentId.DPP_COMPONENT_ID_DEVBOARD;
		else
			throw new Exception("Unknown action");

		bb.putShort((short) ((component_id & 0xff) << 8 | (type & 0xff)));

		if (action.compareToIgnoreCase("SX1262_BASEBOARD_POWER_CMD") == 0) {
			try {
				Long val = new Long((String)params.get("time"));
				if (val < 0 || val >= 4294967296L)
					throw new Exception("time field has to be an usigned int (0 to 2^32-1)");
				bb.putInt((int) (val & 0xffffffffL));
			} catch (NumberFormatException e) {
				throw new Exception("time field has to be an integer");
			}
			bb.put(new Byte((String)params.get("flag")));
			if (new Integer((String)params.get("status")) == 1)
				bb.putShort((short) WAKEUP_TYPE_POWER_ON);
		}
		else if (action.compareToIgnoreCase("SX1262_BASEBOARD_POWER_EXT3") == 0 || action.compareToIgnoreCase("BASEBOARD_PWR_CMD") == 0 || action.compareToIgnoreCase("BASEBOARD_POWEROFF_CMD") == 0 || action.compareToIgnoreCase("BASEBOARD_DUTYCYCLE_CMD") == 0 ) {
			bb.put(new Byte((String)params.get("status")));
		}
		else if (action.compareToIgnoreCase("CMD_BASEBOARD_MSG_ROUTING") == 0) {
			bb.put(new Byte((String)params.get("route")));
		}
		else if (action.compareToIgnoreCase("GEOPHONE_REQ_ADCDATA_CMD") == 0) {
			try {
				Long val = new Long((String)params.get("id"));
				if (val < 0 || val >= 4294967296L)
					throw new Exception("id field has to be an usigned int (0 to 2^32-1)");
				bb.putInt((int) (val & 0xffffffffL));
			} catch (NumberFormatException e) {
				throw new Exception("id field has to be an integer");
			}
			byte fmt_type = new Byte((String)params.get("format_type"));
			byte res = new Byte((String)params.get("bits"));
			byte sub = new Byte((String)params.get("subsampling"));
			byte fmt = (byte) ((fmt_type << 4) | (sub << 2) | res);
			bb.put(fmt);
		}
		else if (action.compareToIgnoreCase("GEOPHONE_DEL_DATA_CMD") == 0) {
			Long val;
			try {
				val = new Long((String)params.get("start_time"));
				if (val < 0 || val >= 4294967296L)
					throw new Exception("start_time field has to be an usigned int (0 to 2^32-1)");
				bb.putInt((int) (val & 0xffffffffL));
			} catch (NumberFormatException e) {
				throw new Exception("start_time field has to be an integer");
			}
			try {
				val = new Long((String)params.get("end_time"));
				if (val < 0 || val >= 4294967296L)
					throw new Exception("end_time field has to be an usigned int (0 to 2^32-1)");
				bb.putInt((int) (val & 0xffffffffL));
			} catch (NumberFormatException e) {
				throw new Exception("end_time field has to be an integer");
			}
		}
		else if (action.compareToIgnoreCase("GEOPHONE_SCHED_ADD_CMD") == 0) {
			Long val;
			try {
				val = new Long((String)params.get("start_time"));
				if (val < 0 || val >= 4294967296L)
					throw new Exception("start_time field has to be an usigned int (0 to 2^32-1)");
				bb.putInt((int) (val & 0xffffffffL));
			} catch (NumberFormatException e) {
				throw new Exception("start_time field has to be an integer");
			}
			try {
				val = new Long((String)params.get("period_duration_task"));
				if (val < 0 || val >= 4294967296L)
					throw new Exception("period_duration_task field has to be an usigned int (0 to 2^32-1)");
				bb.putInt((int) (val & 0xffffffffL));
			} catch (NumberFormatException e) {
				throw new Exception("period_duration_task field has to be an integer");
			}
		}
		else {
			try {
				Integer val = new Integer((String)params.get(DPP_CMD_VALUE));
				if (val >= 0 && val < 65536)
					bb.putShort((short) (val & 0xffff));
				else if (val < 0 && val > -32768)
					bb.putShort(val.shortValue());
				else
					throw new Exception("value field has to be either a signed or unsigned short (-32767 to 2^16-1)");
			} catch (NumberFormatException e) {
				throw new Exception("value field has to be a short");
			}
		}
		
		bb.position(2);
		bb.get(command);
		
		return bb;
	}

	@Override
	public Serializable[] sendPayloadSuccess(boolean success) {
		if (success) {
			return new Serializable[] {component_id, type, command};
		}
		else
			return null;
	}

	@Override
	public int getType() {
		return MessageTypes.DPP_MSG_TYPE_CMD;
	}
}