package org.epragati.hsrp.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Class having basic utility methods for processing on objects.
 * 
 */
public final class ObjectsUtil {

	public static <T> boolean isNull(T t) {
		return Objects.isNull(t);
	}

	public static <T> boolean isNullOrEmpty(Collection<T> t) {
		return isNull(t) || t.isEmpty();
	}

	public static <K, V> boolean isNullOrEmpty(Map<K, V> t) {
		return isNull(t) || t.isEmpty();
	}

}
