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
package org.miaixz.bus.limiter.support.rate;

import org.miaixz.bus.limiter.Limiter;
import org.miaixz.bus.limiter.Parser;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractParser<T extends Limiter<?>, V extends Annotation> implements Parser<T> {

    private Class<Annotation> supportAnnotation;

    // 不需要同步
    private synchronized Class<Annotation> computeSupportAnnotation() {
        if (null != supportAnnotation) {
            return supportAnnotation;
        }
        supportAnnotation = (Class<Annotation>) ((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments()[1];
        return supportAnnotation;
    }

    @Override
    public Class<Annotation> getSupportAnnotation() {
        if (null != supportAnnotation) {
            return supportAnnotation;
        }
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
