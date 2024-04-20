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
package org.miaixz.bus.tracer.thread;

import org.miaixz.bus.tracer.backend.AbstractBackend;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
class ThreadLocalBackend extends AbstractBackend {

    private final ThreadLocalMap<String, String> threadLocalMap;

    public ThreadLocalBackend() {
        this.threadLocalMap = new ThreadLocalMap<>();
    }


    @Override
    public boolean containsKey(String key) {
        return threadLocalMap.get().containsKey(key);
    }

    @Override
    public int size() {
        return threadLocalMap.get().size();
    }

    @Override
    public boolean isEmpty() {
        return threadLocalMap.get().isEmpty();
    }

    @Override
    public String get(String key) {
        return threadLocalMap.get().get(key);
    }

    @Override
    public void put(String key, String value) {
        threadLocalMap.get().put(key, value);
    }

    @Override
    public void remove(String key) {
        threadLocalMap.get().remove(key);
    }

    @Override
    public void clear() {
        threadLocalMap.get().clear();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> entries) {
        threadLocalMap.get().putAll(entries);
    }

    @Override
    public Map<String, String> copyToMap() {
        return new HashMap<>(threadLocalMap.get());
    }

    ThreadLocalMap<String, String> getThreadLocalMap() {
        return threadLocalMap;
    }

}
