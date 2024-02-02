package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The Pressure class is responsible for converting pressure values.
 * Implements the {@code Converter} interface.
 */
public class Pressure implements Converter {

	private static final DecimalFormat decimal1 = new DecimalFormat("0.0");

	/**
	 * Converts the pressure value based on the given signal name and input.
	 *
	 * @param signal_name the name of the signal
	 * @param value       the value to be converted
	 * @param input       the input value
	 * @return the converted pressure value as a string or null if conversion is not
	 *         possible or signal_name is null
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		String result = null;
		int v = ((Integer) signal_name).intValue();
		if (v <= 64000) {
			result = decimal1.format((v / 64000.0) * 5000.0);
		}
		return result;
	}

}
