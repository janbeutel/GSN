package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * This class represents a Thermistor44006 converter that implements the
 * {@code Converter} interface.
 * It provides a method to convert a given signal value to temperature in
 * Celsius.
 */
public class Thermistor44006 implements Converter {

	private static final DecimalFormat decimal4 = new DecimalFormat("0.0000");

	/**
	 * Converts the given signal value to temperature in Celsius.
	 *
	 * @param signal_name The name of the signal.
	 * @param value       The value of the signal.
	 * @param input       The input value.
	 * @return The converted temperature value in Celsius, or null if the
	 *         signal_name is null.
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
			double ln_res = Math.log(27000.0 / ((64000.0 / v) - 1.0));
			double steinhart_eq = 0.00103348 + (0.000238465 * ln_res) + (0.000000158948 * (ln_res * ln_res * ln_res));
			result = decimal4.format((1.0 / steinhart_eq) - 273.15 - cal);
		}
		return result;
	}

}
