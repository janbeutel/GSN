package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@code Converter} interface and provides a method
 * to convert a signal value based on rotation around the X-axis.
 */
public class RotationX implements Converter {

	private static final DecimalFormat decimal3 = new DecimalFormat("0.000");

	private static final transient Logger logger = LoggerFactory.getLogger(RotationX.class);

	/**
	 * Converts the signal value based on rotation around the X-axis.
	 *
	 * @param signal_name The name of the signal.
	 * @param value       The rotation angle in degrees.
	 * @param input       The input value to be converted.
	 * @return The converted value as a string, or null if the conversion fails or
	 *         the signal_name is null.
	 */
	@Override
	public String convert(Serializable signal_name, String value, Serializable input) {
		if (signal_name == null || input == null) {
			return null;
		}

		Double x, 
			   y;

		if (signal_name instanceof Integer ||
				signal_name instanceof Double ||
				signal_name instanceof Float ||
				signal_name instanceof Short) {

			x = (Double) signal_name;
		} else if (signal_name instanceof String) {
			x = Double.parseDouble((String) signal_name);
		} else {
			logger.error("signal_name is not of supported type");
			return null;
		}

		if (input instanceof Integer) {
			y = (Double) input;
		} else if (input instanceof String) {
			y = Double.parseDouble((String) input);
		} else {
			logger.error("input is not of supported type");
			return null;
		}

		double a;
		try {
			a = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return null;
		}

		return decimal3.format(x * Math.cos(Math.toRadians(a)) - y * Math.sin(Math.toRadians(a)));
	}

}
