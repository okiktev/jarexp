package com.delfin.jarexp.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.delfin.jarexp.Version;

final class FieldHelper {

	static void makeNonFinal(Field field) {
		try {
			int modifiers = field.getModifiers();
			if (Version.JAVA_MAJOR_VER < 12) {
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
			} else if (Modifier.isFinal(modifiers)) {
				java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles
						.privateLookupIn(Field.class, java.lang.invoke.MethodHandles.lookup());
				lookup.findVarHandle(Field.class, "modifiers", int.class).set(field, modifiers & ~Modifier.FINAL);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Unable to make " + field.getName() + " non final", e);
		}
	}

}