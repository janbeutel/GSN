package ch.epfl.gsn.utils;

/**
 * Represents a generic pair of values.
 *
 * @param <T> the type of the first value
 * @param <U> the type of the second value
 */
public class Pair<T, U> {
	T first;
	U second;

	public T getFirst() {
		return first;
	}

	public void setFisrt(T first) {
		this.first = first;
	}

	public U getSecond() {
		return second;
	}

	public void setSecond(U second) {
		this.second = second;
	}

	/**
	 * Constructs a new Pair object with the specified objects.
	 *
	 * @param t the first object in the pair
	 * @param u the second object in the pair
	 */
	public Pair(T t, U u) {
		this.first = t;
		this.second = u;
	}

}
