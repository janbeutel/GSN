package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * This class represents a converter for MspTemperature values.
 * It implements the {@code Converter} Interface.
 */
public class MspTemperature implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the MspTemperature signal value to a formatted string
	 * representation.
	 *
	 * @param signal_name The name of the signal.
	 * @param value       The value of the signal as a string.
	 * @param input       The input object.
	 * @return The formatted string representation of the converted value, or null
	 *         if the signal_name is null or the value is invalid.
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		int v = ((Integer) signal_name).intValue();
		if (v == 65535) {
			return null;
		} else {
			return decimal3.format((new Double(v) * (1.5 / 4095) - 0.986) / 0.00355);
		}
	}
}
