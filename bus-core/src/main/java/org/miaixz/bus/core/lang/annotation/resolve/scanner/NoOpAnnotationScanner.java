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
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * A no-op annotation scanner that does not scan any elements. Implements the null-object pattern for
 * {@link AnnotationScanner}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NoOpAnnotationScanner implements AnnotationScanner {

    /**
     * Returns {@code true} for all annotated elements.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return {@code true} always
     */
    @Override
    public boolean support(final AnnotatedElement annotatedEle) {
        return true;
    }

    /**
     * Always returns an empty list; this scanner does not scan any annotations.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return an empty list
     */
    @Override
    public List<Annotation> getAnnotations(final AnnotatedElement annotatedEle) {
        return Collections.emptyList();
    }

    /**
     * Does nothing; this scanner does not scan any annotations.
     *
     * @param consumer     consumer for each (index, annotation) pair (unused)
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor (unused)
     * @param filter       annotation filter (unused)
     */
    @Override
    public void scan(
            final BiConsumer<Integer, Annotation> consumer,
            final AnnotatedElement annotatedEle,
            final Predicate<Annotation> filter) {
        // do nothing
    }

}
