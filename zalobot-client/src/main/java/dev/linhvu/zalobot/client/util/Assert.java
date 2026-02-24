package dev.linhvu.zalobot.client.util;

/**
 * Utility class for common argument validation checks.
 */
public class Assert {

	/**
	 * Asserts that the given object is not {@code null}.
	 *
	 * @param object the object to check
	 * @param message the exception message if the assertion fails
	 * @throws IllegalArgumentException if {@code object} is {@code null}
	 */
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
}
