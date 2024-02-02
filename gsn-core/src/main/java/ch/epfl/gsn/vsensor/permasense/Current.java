package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Implements the {@code Converter} interface and
 * provides functionality
 * for converting signals related to current measurements.
 */
public class Current implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the provided signal value based on the given signal name and input.
	 *
	 * @param signal_name The name of the signal (as a Serializable object).
	 * @param value       The signal value to be converted.
	 * @param input       Additional input information (as a Serializable object).
	 * @return The converted signal value as a formatted string, or null if
	 *         conversion is not possible or signal_name equals null.
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		int v = ((Integer) signal_name).intValue();
		if (value.trim().isEmpty() || v == 0xffff) {
			return null;
		} else {
			return decimal3.format(Double.parseDouble(value) * v);
		}

	}

}
