package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * This class implements the {@code Converter} interface and provides a method
 * to convert humidity values
 * obtained from an SHT11 sensor.
 */
public class Sht11Humidity implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the humidity value obtained from the SHT11 sensor.
	 *
	 * @param signal_name The name of the signal.
	 * @param value       The value of the signal.
	 * @param input       The input value.
	 * @return The converted humidity value as a string, or null if the input is
	 *         invalid or the signal name is null.
	 */
	@Override
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null || input == null) {
			return null;
		}

		int v = ((Integer) signal_name).intValue();
		if (v == 0xffff) {
			return null;
		} else {
			int i = ((Integer) input).intValue();
			if (i == 0xffff) {
				return null;
			} else {
				return decimal3.format(
						((0.01 * i) - 64.63) * (0.01 + (0.00008 * v)) + ((0.0405 * v) - 4 - (0.0000028 * v * v)));
			}

		}
	}

}
