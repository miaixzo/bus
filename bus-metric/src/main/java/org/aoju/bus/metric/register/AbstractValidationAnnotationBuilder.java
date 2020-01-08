/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.register;

import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public abstract class AbstractValidationAnnotationBuilder<T extends Annotation>
        implements ValidationAnnotationBuilder<T> {

    @Override
    public ValidationAnnotationDefinition build(T jsr303Annotation) {
        ValidationAnnotationDefinition validationAnnotationDefinition = new ValidationAnnotationDefinition();

        validationAnnotationDefinition.setAnnotationClass(jsr303Annotation.annotationType());
        Map<String, Object> properties = AnnotationUtils.getAnnotationAttributes(jsr303Annotation);
        properties = this.formatProperties(properties);
        validationAnnotationDefinition.setProperties(properties);
        return validationAnnotationDefinition;
    }

    protected Map<String, Object> formatProperties(Map<String, Object> properties) {
        Set<Map.Entry<String, Object>> entrySet = properties.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            Object value = entry.getValue();
            if (value.getClass().isArray()) {
                Object[] arr = (Object[]) value;
                if (arr.length == 0) {
                    properties.put(entry.getKey(), null);
                }
            }
        }
        return properties;
    }

}
