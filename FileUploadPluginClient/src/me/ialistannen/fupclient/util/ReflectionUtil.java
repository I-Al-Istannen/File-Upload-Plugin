package me.ialistannen.fupclient.util;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * A small help with reflection
 */
public class ReflectionUtil {

	/**
	 * Invokes the method
	 *
	 * @param handle           The handle to invoke it on
	 * @param methodName       The name of the method
	 * @param parameterClasses The parameter types
	 * @param args             The arguments
	 *
	 * @return The resulting object or null if an error occurred / the method didn't return a thing
	 */
	@SuppressWarnings("unused")
	public static @Nullable Object invokeMethod(Object handle, String methodName, Class[] parameterClasses, Object...
			args) {
		return invokeMethod(handle.getClass(), handle, methodName, parameterClasses, args);
	}

	/**
	 * Invokes the method
	 *
	 * @param clazz            The class to invoke it from
	 * @param handle           The handle to invoke it on
	 * @param methodName       The name of the method
	 * @param parameterClasses The parameter types
	 * @param args             The arguments
	 *
	 * @return The resulting object or null if an error occurred / the method didn't return a thing
	 */
	private static @Nullable Object invokeMethod(Class<?> clazz, Object handle, String methodName, Class[]
			parameterClasses,
	                                             Object... args) {
		Optional<Method> methodOptional = getMethod(clazz, methodName, parameterClasses);

		if (!methodOptional.isPresent()) {
			return null;
		}

		Method method = methodOptional.get();

		try {
			return method.invoke(handle, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Invokes the method
	 *
	 * @param method The method to invoke
	 * @param handle The handle to invoke on
	 * @param args   The arguments
	 *
	 * @return The resulting object or null if an error occurred / the method didn't return a thing
	 */
	@SuppressWarnings("unused")
	public static Object invokeMethod(Method method, Object handle, Object... args) {
		try {
			return method.invoke(handle, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * Sets the value of an instance field
	 *
	 * @param handle The handle to invoke it on
	 * @param name   The name of the field
	 * @param value  The new value of the field
	 */
	@SuppressWarnings("unused")
	public static void setInstanceField(Object handle, String name, Object value) {
		Class<?> clazz = handle.getClass();
		Optional<Field> fieldOptional = getField(clazz, name);
		if (!fieldOptional.isPresent()) {
			return;
		}

		Field field = fieldOptional.get();
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		try {
			field.set(handle, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the value of an instance field
	 *
	 * @param handle The handle to invoke it on
	 * @param name   The name of the field
	 *
	 * @return The value
	 */
	@SuppressWarnings("unused")
	public static @Nullable
	Object getInstanceField(Object handle, String name) {
		Class<?> clazz = handle.getClass();
		Optional<Field> fieldOptional = getField(clazz, name);
		if (!fieldOptional.isPresent()) {
			return null;
		}

		Field field = fieldOptional.get();
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		try {
			return field.get(handle);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the constructor
	 *
	 * @param clazz  The class
	 * @param params The Constructor parameters
	 *
	 * @return The Constructor or null if there is none with these parameters
	 */
	@SuppressWarnings("unused")
	public static @Nullable Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
		try {
			return clazz.getConstructor(params);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * Instantiates the class. Will print the errors it gets
	 *
	 * @param constructor The constructor
	 * @param arguments   The initial arguments
	 *
	 * @return The resulting object, or null if an error occurred.
	 */
	@SuppressWarnings("unused")
	public static @Nullable Object instantiate(Constructor<?> constructor, Object... arguments) {
		try {
			return constructor.newInstance(arguments);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			System.out.println("Can't instantiate class " + constructor.getDeclaringClass());
			e.printStackTrace();
		}
		return null;
	}

	private static Optional<Method> getMethod(Class<?> clazz, String name, Class<?>... params) {
		try {
			return Optional.of(clazz.getMethod(name, params));
		} catch (NoSuchMethodException ignored) {
		}

		try {
			return Optional.of(clazz.getDeclaredMethod(name, params));
		} catch (NoSuchMethodException ignored) {
		}

		return Optional.empty();
	}

	private static Optional<Field> getField(Class<?> clazz, String name) {
		try {
			return Optional.of(clazz.getField(name));
		} catch (NoSuchFieldException ignored) {
		}

		try {
			return Optional.of(clazz.getDeclaredField(name));
		} catch (NoSuchFieldException ignored) {
		}

		return Optional.empty();
	}

}
