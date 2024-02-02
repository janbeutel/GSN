package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The Dilatation class is a {@code Converter} implementation that performs a
 * dilatation operation on a signal value.
 * It scales the value based on the signal name and input parameters.
 */
public class Dilatation implements Converter {

	private static final DecimalFormat decimal4 = new DecimalFormat("0.0000");

	/**
	 * Converts the given value based on the signal name and input parameters.
	 *
	 * @param signal_name the name of the signal
	 * @param value       the value to be converted
	 * @param input       the input parameter
	 * @return the converted value as a string or null if conversion is not possible
	 *         or signal_name is null
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		String result = null;
		int v = ((Integer) signal_name).intValue();
		if (v <= 64000) {
			result = decimal4.format((v / 64000.0) * Double.parseDouble(value));
		}
		return result;
	}

}
