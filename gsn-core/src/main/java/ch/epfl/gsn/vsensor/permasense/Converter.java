package ch.epfl.gsn.vsensor.permasense;

import java.io.Serializable;

/**
 * The Converter interface represents a converter that converts a value from one
 * format to another.
 */
public interface Converter {

	/**
	 * Converts the given value from one format to another.
	 *
	 * @param signal_name the name of the signal being converted
	 * @param value       the value to be converted
	 * @param input       the input format of the value
	 * @return the converted value as a String
	 */
	public String convert(Serializable signal_name, String value, Serializable input);

}
