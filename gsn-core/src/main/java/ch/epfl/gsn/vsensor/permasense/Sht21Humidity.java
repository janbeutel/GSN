package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * This class implements the {@code Converter} interface and provides a method
 * to convert humidity values
 * from a Sht21 sensor to a formatted string representation.
 */
public class Sht21Humidity implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the humidity value from the Sht21 sensor to a formatted string
	 * representation.
	 *
	 * @param signal_name The name of the signal.
	 * @param value       The raw value of the humidity.
	 * @param input       The input object.
	 * @return The formatted string representation of the humidity value, or null if
	 *         the signal_name is null or the value is invalid.
	 */
	@Override
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		int v = ((Integer) signal_name).intValue();
		if (v == 0xffff) {
			return null;
		} else {
			return decimal3.format(-6.0 + 125.0 * v / 4096);
		}

	}

}
