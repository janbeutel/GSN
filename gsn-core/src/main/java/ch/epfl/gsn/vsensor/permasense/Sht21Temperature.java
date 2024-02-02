package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The Sht21Temperature class is responsible for converting temperature values
 * from a sensor.
 * It implements the {@code Converter} interface.
 */
public class Sht21Temperature implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the temperature value from the sensor to a formatted string.
	 *
	 * @param signal_name The name of the signal.
	 * @param value       The value of the temperature.
	 * @param input       The input value.
	 * @return The formatted temperature value as a string or null if the conversion
	 *         is not possible or the signal_name is null.
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		int v = ((Integer) signal_name).intValue();
		if (v == 0xffff) {
			return null;
		} else {
			return decimal3.format(-46.85 + 175.72 * v / 16384.0);
		}
	}
}
