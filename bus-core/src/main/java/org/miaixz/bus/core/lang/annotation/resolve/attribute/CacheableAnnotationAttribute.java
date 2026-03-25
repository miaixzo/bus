/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.annotation.resolve.attribute;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Base implementation of {@link AnnotationAttribute} with value caching support.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CacheableAnnotationAttribute implements AnnotationAttribute {

    /**
     * Whether the attribute value has been invoked and cached.
     */
    private volatile boolean valueInvoked;

    /**
     * The cached attribute value.
     */
    private volatile Object value;

    /**
     * Whether the default value has been fetched.
     */
    private boolean defaultValueInvoked;

    /**
     * The cached default value of the attribute.
     */
    private Object defaultValue;

    /**
     * The annotation instance owning this attribute.
     */
    private final Annotation annotation;

    /**
     * The method representing the annotation attribute.
     */
    private final Method attribute;

    /**
     * Constructs a new {@code CacheableAnnotationAttribute}.
     *
     * @param annotation The annotation instance
     * @param attribute  The annotation method (attribute)
     */
    public CacheableAnnotationAttribute(final Annotation annotation, final Method attribute) {
        Assert.notNull(annotation, "annotation must not null");
        Assert.notNull(attribute, "attribute must not null");
        this.annotation = annotation;
        this.attribute = attribute;
        this.valueInvoked = false;
        this.defaultValueInvoked = false;
    }

    /**
     * Returns the annotation instance that declares this attribute.
     *
     * @return the annotation instance
     */
    @Override
    public Annotation getAnnotation() {
        return this.annotation;
    }

    /**
     * Returns the method representing this annotation attribute.
     *
     * @return the attribute method
     */
    @Override
    public Method getAttribute() {
        return this.attribute;
    }

    /**
     * Returns the value of this annotation attribute, invoking the attribute method on the annotation instance on the
     * first call and caching the result for subsequent calls.
     *
     * @return the attribute value
     */
    @Override
    public Object getValue() {
        if (!valueInvoked) {
            synchronized (this) {
                if (!valueInvoked) {
                    valueInvoked = true;
                    value = MethodKit.invoke(annotation, attribute);
                }
            }
        }
        return value;
    }

    /**
     * Returns whether the current attribute value is equal to the attribute's default value.
     *
     * @return {@code true} if the attribute value equals its default value
     */
    @Override
    public boolean isValueEquivalentToDefaultValue() {
        if (!defaultValueInvoked) {
            defaultValue = attribute.getDefaultValue();
            defaultValueInvoked = true;
        }
        return ObjectKit.equals(getValue(), defaultValue);
    }

}
