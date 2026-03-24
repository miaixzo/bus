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
package org.miaixz.bus.core.lang.annotation.resolve;

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

/**
 * Annotation lookup key for fine-grained caching, used to uniquely identify an annotation lookup operation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AnnotationLookupKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852289137230L;

    /**
     * The annotated element (Class, Method, Field, etc.).
     */
    private final AnnotatedElement element;
    /**
     * The target annotation type.
     */
    private final Class<? extends Annotation> annotationType;

    /**
     * Constructs a new {@code AnnotationLookupKey}.
     *
     * @param element        the annotated element
     * @param annotationType the target annotation type
     */
    public AnnotationLookupKey(final AnnotatedElement element, final Class<? extends Annotation> annotationType) {
        this.element = element;
        this.annotationType = annotationType;
    }

    /**
     * Returns whether this key is equal to the given object. Two keys are equal if they have the same annotated element
     * and the same annotation type.
     *
     * @param o the object to compare with
     * @return {@code true} if the given object is equal to this key
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AnnotationLookupKey that = (AnnotationLookupKey) o;
        return Objects.equals(element, that.element) && Objects.equals(annotationType, that.annotationType);
    }

    /**
     * Returns the hash code of this key, computed from the annotated element and the annotation type.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(element, annotationType);
    }

}
