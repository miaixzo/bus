/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.tracer.backend;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
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
        return null != key && TraceKeys.get().contains(key) && null != MDC.get(key);
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
        if ((null != key) && TraceKeys.get().contains(key))
            return MDC.get(key);
        else
            return null;
    }

    @Override
    public void put(String key, String value) throws IllegalArgumentException {
        if (null == key) throw new IllegalArgumentException("null keys are not allowed.");
        if (null == value) throw new IllegalArgumentException("null values are not allowed.");
        final Set<String> registeredKeys = TraceKeys.get();
        if (!registeredKeys.contains(key)) {
            registeredKeys.add(key);
        }
        MDC.put(key, value);
    }

    @Override
    public void remove(String key) throws IllegalArgumentException {
        if (null == key) throw new IllegalArgumentException("null keys are not allowed.");
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
            if (null != value) {
                TraceMap.put(TraceKey, value);
            }
        }
        return TraceMap;
    }
}
