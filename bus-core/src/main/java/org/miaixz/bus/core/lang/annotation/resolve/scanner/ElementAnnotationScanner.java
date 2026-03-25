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
package org.miaixz.bus.core.lang.annotation.resolve.scanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.PredicateKit;

/**
 * Scans annotations on an {@link AnnotatedElement} without supporting hierarchy traversal.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ElementAnnotationScanner implements AnnotationScanner {

    /**
     * Returns {@code true} only when the annotated element is not null.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return {@code true} if the element is not null
     */
    @Override
    public boolean support(final AnnotatedElement annotatedEle) {
        return ObjectKit.isNotNull(annotatedEle);
    }

    /**
     * Scans annotations directly declared on the {@link AnnotatedElement}. Requires {@link #support(AnnotatedElement)}
     * to return {@code true} before calling.
     *
     * @param consumer     Consumer for each (index, annotation) pair
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @param filter       Annotation filter; annotations that fail this filter are skipped. May be {@code null}.
     */
    @Override
    public void scan(
            final BiConsumer<Integer, Annotation> consumer,
            final AnnotatedElement annotatedEle,
            Predicate<Annotation> filter) {
        filter = ObjectKit.defaultIfNull(filter, PredicateKit.alwaysTrue());
        Stream.of(annotatedEle.getAnnotations()).filter(filter).forEach(annotation -> consumer.accept(0, annotation));
    }

}
