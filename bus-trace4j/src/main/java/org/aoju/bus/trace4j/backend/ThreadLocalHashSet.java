package org.aoju.bus.trace4j.backend;

import java.util.HashSet;
import java.util.Set;

public final class ThreadLocalHashSet<T> extends InheritableThreadLocal<Set<T>> {

	@Override
	protected Set<T> childValue(Set<T> parentValue) {
		return new HashSet<>(parentValue);
	}

	@Override
	protected Set<T> initialValue() {
		return new HashSet<>();
	}

}
