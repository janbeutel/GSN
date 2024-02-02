package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The Voltage class implements the {@code Converter} interface and provides a
 * method to convert a voltage value.
 */
public class Voltage implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the given voltage value based on the signal name and input.
	 *
	 * @param signal_name The name of the signal.
	 * @param value       The value to be converted.
	 * @param input       The input value.
	 * @return The converted voltage value as a string. Null if the signal name is
	 *         null.
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		String result = null;
		int v = ((Integer) signal_name).intValue();
		result = decimal3.format(Double.parseDouble(value) * v);
		return result;
	}

}
