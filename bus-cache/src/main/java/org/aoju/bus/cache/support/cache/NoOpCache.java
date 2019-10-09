/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cache.support.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public class NoOpCache implements Cache {

    @Override
    public Object read(String key) {
        return null;
    }

    @Override
    public void write(String key, Object value, long expire) {
        // no op
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        return Collections.emptyMap();
    }

    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        // no op
    }

    @Override
    public void remove(String... keys) {
        // no op
    }

}
