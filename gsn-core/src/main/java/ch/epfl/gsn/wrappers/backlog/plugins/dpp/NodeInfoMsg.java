package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import ch.epfl.gsn.beans.DataField;

public class NodeInfoMsg extends AbstractMsg {

	private static DataField[] dataField = {
			new DataField("COMPONENT_ID", "SMALLINT"), /* application specific component id */
			new DataField("RST_FLAG", "SMALLINT"), /* reset cause / source */
			new DataField("RST_CNT", "INTEGER"), /* reset counter */
			new DataField("MCU_DESC", "VARCHAR(12)"), /* MCU description, i.g. 'CC430F5147' */
			new DataField("COMPILER_DESC", "VARCHAR(4)"), /* compiler abbreviation (e.g. 'GCC') */
			new DataField("COMPILER_VER", "BIGINT"), /*
														 * compiler version: human-readable (e.g. AAABBBCCC where A:
														 * major, B: minor, C: patch
														 */
			new DataField("COMPILE_DATE", "BIGINT"), /* compilation time (seconds since 1970) */
			new DataField("FW_NAME", "VARCHAR(8)"), /* name of the firmware/application */
			new DataField("FW_VER", "INTEGER"), /*
												 * firmware version: human-readable (e.g. ABBCC where A: major, B:
												 * minor, C: patch
												 */
			new DataField("SW_REV_ID", "BIGINT"), /* repository revision number (GIT or SVN) */
			new DataField("CONFIG", "BIGINT") /*
												 * bitfield for custom (application specific) compile-time configuration
												 * settings
												 */
	};

	/**
	 * Receives the payload from a ByteBuffer and extracts the necessary
	 * information.
	 * 
	 * @param payload The ByteBuffer containing the payload data.
	 * @return An array of Serializable objects containing the extracted
	 *         information.
	 * @throws Exception If an error occurs while receiving the payload.
	 */
	@Override
	public Serializable[] receivePayload(ByteBuffer payload) throws Exception {
		Short component_id = null;
		Short rst_flag = null;
		Integer rst_cnt = null;
		byte[] mcuDesc = new byte[12];
		String mcu_desc = null;
		byte[] compilerDesc = new byte[4];
		String compiler_desc = null;
		Long compiler_ver = null;
		Long compile_date = null;
		byte[] fwName = new byte[8];
		String fw_name = null;
		Integer fw_ver = null;
		Long sw_rev_id = null;
		Long config = null;

		try {
			component_id = convertUINT8(payload);
			rst_flag = convertUINT8(payload);
			rst_cnt = convertUINT16(payload);
			payload.get(mcuDesc); // char[12]
			mcu_desc = byteArrayToString(mcuDesc);
			payload.get(compilerDesc); // char[3]
			compiler_desc = byteArrayToString(compilerDesc);
			compiler_ver = convertUINT32(payload);
			compile_date = convertUINT32(payload);
			payload.get(fwName); // char[8]
			fw_name = byteArrayToString(fwName);
			fw_ver = convertUINT16(payload);
			sw_rev_id = convertUINT32(payload);
			config = convertUINT32(payload);
		} catch (Exception e) {
		}

		return new Serializable[] { component_id, rst_flag, rst_cnt, mcu_desc, compiler_desc, compiler_ver,
				compile_date, fw_name, fw_ver, sw_rev_id, config };
	}

	/**
	 * Converts a byte array to a string using the UTF-8 character encoding.
	 * 
	 * @param array the byte array to be converted
	 * @return the resulting string
	 */
	private String byteArrayToString(byte[] array) {
		int i;
		for (i = 0; i < array.length && array[i] != 0; i++) {
		}
		return new String(Arrays.copyOfRange(array, 0, i), Charset.forName("UTF-8"));
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public int getType() {
		return MessageTypes.DPP_MSG_TYPE_NODE_INFO;
	}
}
