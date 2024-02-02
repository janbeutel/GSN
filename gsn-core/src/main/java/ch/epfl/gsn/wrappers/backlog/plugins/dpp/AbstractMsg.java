package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

import java.io.Serializable;
import java.nio.ByteBuffer;

import ch.epfl.gsn.wrappers.backlog.plugins.DPPMessagePlugin;

public abstract class AbstractMsg implements Message {

	@Override
	public boolean initialize(DPPMessagePlugin dppMessagePlugin, String coreStationName, String deploymentName) {
		return true;
	}

	@Override
	public ByteBuffer sendPayload(String action, String[] paramNames, Object[] paramValues) throws Exception {
		throw new Exception("sendPayload not implemented");
	}

	@Override
	public boolean isMinimal() {
		return false;
	}

	@Override
	public Serializable[] sendPayloadSuccess(boolean success) {
		return null;
	}

	/**
	 * Converts a byte buffer to a Short value.
	 * 
	 * @param buf the byte buffer to convert
	 * @return the converted Short value, or null if the value is 0xFF
	 * @throws Exception if an error occurs during the conversion
	 */
	protected static Short convertUINT8(ByteBuffer buf) throws Exception {
		Short val = (short) (buf.get() & 0xFF);
		if (val == 0xFF) {
			return null;
		} else {
			return val;
		}

	}

	/**
	 * Converts an INT8 value from a ByteBuffer to a Short.
	 * 
	 * @param buf the ByteBuffer containing the INT8 value
	 * @return the converted Short value, or null if the value is -128
	 * @throws Exception if an error occurs during the conversion
	 */
	protected static Short convertINT8(ByteBuffer buf) throws Exception {
		Short val = (short) buf.get();
		if (val == -128) {
			return null;
		} else {
			return val;
		}
	}

	/**
	 * Converts a 16-bit unsigned integer value from a ByteBuffer to an Integer.
	 * 
	 * @param buf the ByteBuffer containing the 16-bit unsigned integer value
	 * @return the converted Integer value, or null if the value is 0xFFFF
	 * @throws Exception if an error occurs during the conversion
	 */
	protected static Integer convertUINT16(ByteBuffer buf) throws Exception {
		Integer val = buf.getShort() & 0xFFFF;
		if (val == 0xFFFF) {
			return null;
		} else {
			return val;
		}
	}

	/**
	 * Converts a 16-bit signed integer value from a ByteBuffer to an Integer.
	 * If the value is -32768, it returns null.
	 *
	 * @param buf the ByteBuffer containing the 16-bit signed integer value
	 * @return the converted Integer value or null if the value is -32768
	 * @throws Exception if there is an error during the conversion
	 */
	protected static Integer convertINT16(ByteBuffer buf) throws Exception {
		Integer val = (int) buf.getShort();
		if (val == -32768) {
			return null;
		} else {
			return val;
		}
	}

	/**
	 * Converts a 32-bit unsigned integer value from a ByteBuffer to a Long.
	 * 
	 * @param buf the ByteBuffer containing the unsigned integer value
	 * @return the converted Long value, or null if the value is equal to
	 *         0xFFFFFFFFL
	 * @throws Exception if there is an error during the conversion
	 */
	protected static Long convertUINT32(ByteBuffer buf) throws Exception {
		Long val = buf.getInt() & 0xFFFFFFFFL;
		if (val == 0xFFFFFFFFL) {
			return null;
		} else {
			return val;
		}
	}

	/**
	 * Converts a ByteBuffer to a Long value.
	 * 
	 * @param buf the ByteBuffer to convert
	 * @return the converted Long value, or null if the ByteBuffer value is
	 *         -2147483648L
	 * @throws Exception if an error occurs during the conversion
	 */
	protected static Long convertINT32(ByteBuffer buf) throws Exception {
		Long val = (long) buf.getInt();
		if (val == -2147483648L) {
			return null;
		} else {
			return val;
		}
	}

	/**
	 * Converts a ByteBuffer to a Long value.
	 *
	 * @param buf the ByteBuffer to convert
	 * @return the converted Long value
	 * @throws Exception if an error occurs during conversion
	 */
	protected static Long convertUINT64(ByteBuffer buf) throws Exception {
		return buf.getLong();
	}
}
