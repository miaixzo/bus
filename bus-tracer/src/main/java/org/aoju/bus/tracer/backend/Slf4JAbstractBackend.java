package org.aoju.bus.tracer.backend;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Slf4JAbstractBackend extends AbstractBackend {

    /**
     * This set contains all MDC-Keys managed by Builder.
     * This bookkeeping is required to ensure that operations like {@link Slf4JAbstractBackend#clear()} do not remove
     * Builder unrelated keys from the MDC.
     */
    protected final ThreadLocal<Set<String>> TraceKeys;

    Slf4JAbstractBackend(ThreadLocal<Set<String>> TraceKeys) {
        this.TraceKeys = TraceKeys;
    }

    @Override
    public boolean containsKey(String key) {
        return key != null && TraceKeys.get().contains(key) && MDC.get(key) != null;
    }

    @Override
    public int size() {
        return TraceKeys.get().size();
    }

    @Override
    public boolean isEmpty() {
        return TraceKeys.get().isEmpty();
    }

    @Override
    public String get(String key) {
        if ((key != null) && TraceKeys.get().contains(key))
            return MDC.get(key);
        else
            return null;
    }

    @Override
    public void put(String key, String value) throws IllegalArgumentException {
        if (key == null) throw new IllegalArgumentException("null keys are not allowed.");
        if (value == null) throw new IllegalArgumentException("null values are not allowed.");
        final Set<String> registeredKeys = TraceKeys.get();
        if (!registeredKeys.contains(key)) {
            registeredKeys.add(key);
        }
        MDC.put(key, value);
    }

    @Override
    public void remove(String key) throws IllegalArgumentException {
        if (key == null) throw new IllegalArgumentException("null keys are not allowed.");
        if (TraceKeys.get().remove(key)) {
            MDC.remove(key);
        }
    }

    @Override
    public void clear() {
        for (String key : TraceKeys.get()) {
            MDC.remove(key);
        }
        TraceKeys.remove();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> entries) {
        for (Map.Entry<? extends String, ? extends String> entry : entries.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Map<String, String> copyToMap() {
        final Map<String, String> TraceMap = new HashMap<>();
        final Set<String> keys = TraceKeys.get();
        for (String TraceKey : keys) {
            final String value = MDC.get(TraceKey);
            if (value != null) {
                TraceMap.put(TraceKey, value);
            }
        }
        return TraceMap;
    }
}
