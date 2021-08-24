package chylex.hee.system.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.launchwrapper.Launch;

public class ReflectionUtils {

	public static Boolean obf;
	public static Map<String, CachedField> mCachedFields = new LinkedHashMap<String, CachedField>();
	
	private static HashMap<String, String> obfFieldNames = new HashMap<String, String>();
	
	static {
		obfFieldNames.put("registryObjects", "field_82596_a");
		obfFieldNames.put("underlyingIntegerMap", "field_148759_a");
		obfFieldNames.put("block", "field_150939_a");
		obfFieldNames.put("lastDamage", "field_110153_bc");
		obfFieldNames.put("equipmentDropChances", "field_82174_bp");
		obfFieldNames.put("fire", "field_70151_c");
		obfFieldNames.put("targetedEntity", "field_70792_g");
		obfFieldNames.put("tagList", "field_74747_a");
		obfFieldNames.put("floatingTickCount", "field_147365_f");
		obfFieldNames.put("lastPosX", "field_147373_o");
		obfFieldNames.put("lastPosY", "field_147382_p");
		obfFieldNames.put("lastPosZ", "field_147381_q");
		obfFieldNames.put("endRNG", "field_73204_i");
		obfFieldNames.put("sky", "field_76779_k");
		obfFieldNames.put("chunkProvider", "field_73020_y");
		obfFieldNames.put("mcMusicTicker", "field_147126_aw");
		obfFieldNames.put("xSize", "field_146999_f");
		obfFieldNames.put("ySize", "field_147000_g");
		obfFieldNames.put("mapSoundPositions", "field_147593_P");
	}

	public static boolean checkObfuscated() {
		if (obf != null) {
			return obf;
		}		
		boolean obfuscated = false;
		try {
			obfuscated = !(boolean) ReflectionHelper.findField(CoreModManager.class, "deobfuscatedEnvironment").get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			byte[] bs;
			try {
				bs = Launch.classLoader.getClassBytes("net.minecraft.world.World");
				if (bs != null) {
					obfuscated = false;
				} else {
					obfuscated = true;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				obfuscated = false;
			}
		}
		return obfuscated;
	}	
	
	public static String getCorrectFieldName(String aFieldName) {
		if (checkObfuscated()) {
			aFieldName = obfFieldNames.get(aFieldName);
		}
		return aFieldName;
	}
	

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
	public static Field getField(final Class<?> aClass, String aFieldName) {
		if (aClass == null || aFieldName == null || aFieldName.length() <= 0) {
			return null;
		}		
		aFieldName = getCorrectFieldName(aFieldName);
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
	public static Field getField(final Object aInstance, String aFieldName) {
		return getField(aInstance.getClass(), aFieldName);
	}


	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(final Object aInstance, String aFieldName) {		
		aFieldName = getCorrectFieldName(aFieldName);
		try {			
			return (T) getField(aInstance, aFieldName).get(aInstance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}

	public static void setFieldValue(final Object aInstance, String aFieldName, Object aNewValue) {
		setFieldValue(aInstance, aFieldName, aInstance instanceof Class ? true : false, aNewValue);
	}

	public static void setFieldValue(final Object aInstance, String aFieldName, boolean aStatic,  Object aNewValue) {
		Field f;		
		aFieldName = getCorrectFieldName(aFieldName);
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
