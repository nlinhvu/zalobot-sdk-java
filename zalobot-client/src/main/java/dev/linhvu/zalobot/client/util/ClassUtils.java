package dev.linhvu.zalobot.client.util;

/**
 * Utility class for class-loading checks.
 */
public class ClassUtils {

	/**
	 * Checks whether a class with the given name is present on the classpath
	 * without initializing it.
	 *
	 * @param className the fully-qualified class name to check
	 * @param classLoader the class loader to use, or {@code null} to use
	 *                    this class's class loader
	 * @return {@code true} if the class is present, {@code false} otherwise
	 */
	public static boolean isPresent(String className, ClassLoader classLoader) {
		try {
			ClassLoader clToUse = (classLoader != null) ? classLoader : ClassUtils.class.getClassLoader();
			Class.forName(className, false, clToUse);
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}
}
