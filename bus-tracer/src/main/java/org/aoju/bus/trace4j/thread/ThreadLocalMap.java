package org.aoju.bus.trace4j.thread;

import java.util.HashMap;
import java.util.Map;

class ThreadLocalMap<K, V> extends InheritableThreadLocal<Map<K, V>> {

	@Override
	protected final Map<K, V> initialValue() {
		return new HashMap<>();
	}

	@Override
	protected final Map<K, V> childValue(Map<K, V> parentValue) {
		if (parentValue == null) {
			return null;
		} else {
			return new HashMap<>(parentValue);
		}
	}

}
