package chylex.hee.system.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReflectionUtils {

	public static Map<String, CachedField> mCachedFields = new LinkedHashMap<String, CachedField>();


	private static class CachedField {
		private final Field FIELD;

		public CachedField(Field aField) {
			FIELD = aField;
		}

		public Field get() {
			return FIELD;
		}
	}

	private static boolean cacheField(Class<?> aClass, Field aField) {		
		if (aField == null) {
			return false;
		}
		CachedField y = mCachedFields.get(aClass.getName()+"."+aField.getName());
		if (y == null) {
			mCachedFields.put(aClass.getName()+"."+aField.getName(), new CachedField(aField));
			return true;
		}		
		return false;
	}

	/**
	 * Returns a cached {@link Field} object.
	 * @param aClass - Class containing the Method.
	 * @param aFieldName - Field name in {@link String} form.
	 * @return - Valid, non-final, {@link Field} object, or {@link null}.
	 */
	public static Field getField(final Class<?> aClass, final String aFieldName) {
		if (aClass == null || aFieldName == null || aFieldName.length() <= 0) {
			return null;
		}		
		CachedField y = mCachedFields.get(aClass.getName()+"."+aFieldName);
		if (y == null) {
			Field u;
			try {
				u = getField_Internal(aClass, aFieldName);
				if (u != null) {
					cacheField(aClass, u);
					return u;
				}
			} catch (NoSuchFieldException e) {
			}
			return null;

		} else {
			return y.get();
		}
	}

	/**
	 * Returns a cached {@link Field} object.
	 * @param aInstance - {@link Object} to get the field instance from.
	 * @param aFieldName - Field name in {@link String} form.
	 * @return - Valid, non-final, {@link Field} object, or {@link null}.
	 */
	public static Field getField(final Object aInstance, final String aFieldName) {
		return getField(aInstance.getClass(), aFieldName);
	}


	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(final Object aInstance, final String aFieldName) {
		try {			
			return (T) getField(aInstance, aFieldName).get(aInstance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}

	public static void setFieldValue(final Object aInstance, final String aFieldName, Object aNewValue) {
		setFieldValue(aInstance, aFieldName, aInstance instanceof Class ? true : false, aNewValue);
	}

	public static void setFieldValue(final Object aInstance, final String aFieldName, boolean aStatic,  Object aNewValue) {
		Field f;
		try {
			if (aInstance instanceof Class && aStatic) {
				f = getField((Class) aInstance, aFieldName);
				f.set(null, aNewValue);
			}
			else {			
				f = getField(aInstance, aFieldName);
				f.set(aInstance, aNewValue);

			}	
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}	
	}


	private static Field getField_Internal(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
		try {
			Field k = clazz.getDeclaredField(fieldName);
			makeFieldAccessible(k);
			return k;
		} catch (final NoSuchFieldException e) {
			final Class<?> superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			}
			return getField_Internal(superClass, fieldName);
		}
	}	

	public static void makeFieldAccessible(final Field field) {
		if (!Modifier.isPublic(field.getModifiers()) ||
				!Modifier.isPublic(field.getDeclaringClass().getModifiers()))
		{
			field.setAccessible(true);
		}
	}


}
