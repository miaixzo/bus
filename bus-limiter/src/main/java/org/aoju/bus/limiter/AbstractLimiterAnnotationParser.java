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
package org.aoju.bus.limiter;

import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public abstract class AbstractLimiterAnnotationParser<T extends Limiter<?>, V extends Annotation> implements LimiterAnnotationParser<T> {

    private Class<Annotation> supportAnnotation;

    // 不需要同步
    private synchronized Class<Annotation> computeSupportAnnotation() {
        if (supportAnnotation != null) {
            return supportAnnotation;
        }
        supportAnnotation = (Class<Annotation>) ((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments()[1];
        return supportAnnotation;
    }

    @Override
    public Class<Annotation> getSupportAnnotation() {
        if (supportAnnotation != null) return supportAnnotation;
        return computeSupportAnnotation();
    }

    public String getLimiter(AnnotationAttributes attributes) {
        return attributes.getString("limiter");
    }

    public String getKey(AnnotationAttributes attributes) {
        return attributes.getString("key");
    }

    public String getFallback(AnnotationAttributes attributes) {
        return attributes.getString("fallback");
    }

    public String getErrorHandler(AnnotationAttributes attributes) {
        return attributes.getString("errorHandler");
    }

    public Collection<String> getArgumentInjectors(AnnotationAttributes attributes) {
        return Arrays.asList(attributes.getStringArray("argumentInjectors"));
    }

}
