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
package org.aoju.bus.limiter.metadata;

import org.aoju.bus.limiter.resource.LimitedResource;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kimi Liu
 * @version 5.2.2
 * @since JDK 1.8+
 */
public class LimitedResourceMetadataCache {

    private final Map<LimitedResourceKey, LimitedResourceMetadata> metadataCache = new ConcurrentHashMap<>(1024);

    private BeanFactory beanFactory;

    public LimitedResourceMetadataCache(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public LimitedResourceMetadata getLimitedResourceMetadata(LimitedResource limitedResource, Method method, Class<?> targetClass) {
        LimitedResourceKey limitedResourceKey = new LimitedResourceKey(limitedResource, method, targetClass);
        LimitedResourceMetadata metadata = this.metadataCache.get(limitedResourceKey);
        if (metadata == null) {
            metadata = limitedResource.createMetadata(this.beanFactory, targetClass, method);
            this.metadataCache.put(limitedResourceKey, metadata);
        }
        return metadata;
    }

}
