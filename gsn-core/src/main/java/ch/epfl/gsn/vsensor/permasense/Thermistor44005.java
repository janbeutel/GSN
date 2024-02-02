package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * This class represents a Thermistor44005 converter that implements the
 * {@code Converter} interface.
 * It provides a method to convert a signal value to temperature using the
 * Steinhart-Hart equation.
 */
public class Thermistor44005 implements Converter {

	private static final DecimalFormat decimal4 = new DecimalFormat("0.0000");

	/**
	 * Converts the given signal value to temperature using the Steinhart-Hart
	 * equation.
	 *
	 * @param signal_name the name of the signal (unused in this implementation)
	 * @param value       the signal value as a string
	 * @param input       additional input (unused in this implementation)
	 * @return the converted temperature as a string, or null if the signal name is
	 *         null or the conversion is not possible.
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		String result = null;
		int v = ((Integer) signal_name).intValue();
		if (v < 64000 && v != 0) {
			double cal = 0.0;
			if (value != null) {
				cal = Double.parseDouble(value);
			}
			double ln_res = Math.log(10000.0 / ((64000.0 / v) - 1.0));
			// Math.pow(v, 3.0) needs more CPU instructions than (v * v * v)
			// double steinhart_eq = 0.0014051 + 0.0002369 * ln_res + 0.0000001019 *
			// Math.pow(ln_res, 3);
			double tmp = 0.0014051 + (0.0002369 * ln_res) + (0.0000001019 * (ln_res * ln_res * ln_res));
			result = decimal4.format((1.0 / tmp) - 273.15 - cal);
		}
		return result;
	}

}
