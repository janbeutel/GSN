package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The Sht11Temperature class is responsible for converting temperature values
 * from the SHT11 sensor.
 * It implements the {@code Converter} interface.
 */
public class Sht11Temperature implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	/**
	 * Converts the raw temperature value from the SHT11 sensor to a formatted
	 * temperature value.
	 *
	 * @param signal_name The name of the signal.
	 * @param value       The raw temperature value.
	 * @param input       The input value.
	 * @return The formatted temperature value or null if the conversion is not
	 *         possible or the signal_name is null.
	 */
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null) {
			return null;
		}

		int v = ((Integer) signal_name).intValue();
		if (v == 0xffff) {
			return null;
		} else {
			return decimal3.format(0.01 * v - 39.63);
		}
	}

}
