package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The Multiplication class implements the {@code Converter} interface and provides a
 * method to convert a value by multiplying it with a given factor.
 */
public class Multiplication implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the value by multiplying it with the given factor.
	 *
	 * @param signal_name the name of the signal (unused)
	 * @param value       the value to be converted
	 * @param input       the input (unused)
	 * @return the converted value as a formatted string or null if conversion is
	 *         not possible or signal_name is null
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
