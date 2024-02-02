package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The Resistivity class is responsible for converting a signal value to
 * resistivity.
 * Implements the {@code Converter} interface.
 */
public class Resistivity implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the given signal value to resistivity.
	 *
	 * @param signal_name the name of the signal (unused in this implementation)
	 * @param value       the value of the signal
	 * @param input       additional input (unused in this implementation)
	 * @return the resistivity value as a formatted string, or null if the
	 *         signal_name is null
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		String result = null;
		int v = ((Integer) signal_name).intValue();
		if (v <= 64000 && v != 0) {
			result = decimal3.format(((64000.0 / v) - 1.0));
		}
		return result;
	}

}
