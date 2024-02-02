package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The Selfpotential class implements the {@code Converter} interface and
 * provides a method to convert a signal value.
 */
public class Selfpotential implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the given signal value based on the signal name and input.
	 *
	 * @param signal_name The name of the signal.
	 * @param value       The value of the signal.
	 * @param input       The input for the conversion.
	 * @return The converted value as a string, or null if the signal name is null
	 *         or the conversion is not possible.
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		String result = null;
		int v = ((Integer) signal_name).intValue();
		if (v <= 64000) {
			result = decimal3.format(v * 320.0 / 64000.0);
		}
		return result;
	}

}
