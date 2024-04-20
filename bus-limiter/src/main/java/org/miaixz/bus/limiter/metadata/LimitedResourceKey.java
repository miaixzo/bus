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
package org.miaixz.bus.limiter.metadata;

import org.miaixz.bus.limiter.resource.LimitedResource;
import org.springframework.context.expression.AnnotatedElementKey;

import java.lang.reflect.Method;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class LimitedResourceKey implements Comparable<LimitedResourceKey> {

    private final LimitedResource limitedResource;
    private final AnnotatedElementKey methodCacheKey;

    public LimitedResourceKey(LimitedResource limitedResource, Method method, Class<?> targetClass) {
        this.limitedResource = limitedResource;
        this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
    }

    public LimitedResource getLimitedResource() {
        return limitedResource;
    }

    public AnnotatedElementKey getMethodCacheKey() {
        return methodCacheKey;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof LimitedResourceKey)) {
            return false;
        } else {
            LimitedResourceKey otherKey = (LimitedResourceKey) other;
            return this.limitedResource.equals(otherKey.limitedResource) && this.methodCacheKey.equals(otherKey.methodCacheKey);
        }
    }

    public int hashCode() {
        return this.limitedResource.hashCode() * 31 + this.methodCacheKey.hashCode();
    }

    public String toString() {
        return this.limitedResource + " on " + this.methodCacheKey;
    }

    @Override
    public int compareTo(LimitedResourceKey o) {
        return 0;
    }

}
