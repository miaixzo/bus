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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.map.multiple.ListValueMap;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.PredicateKit;

/**
 * Composite annotation scanner that dispatches to the appropriate scanner based on the element type, supporting
 * annotations on {@link AnnotatedElement} objects according to different hierarchy structures.
 * <p>
 * The meaning of "hierarchy structure" depends on the element type:
 * <ul>
 * <li>When the element is a {@link Method}, the hierarchy refers to the declaring class hierarchy. The scanner will
 * look for methods with the same signature in the hierarchy and scan their annotations.</li>
 * <li>When the element is a {@link Class}, the hierarchy refers to the class itself together with its superclasses and
 * implemented interfaces.</li>
 * <li>For other element types, the hierarchy contains only the element itself.</li>
 * </ul>
 * In addition, the scanner supports recursively scanning the meta-annotations of each annotation found in the
 * hierarchy.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CompositeAnnotationScanner implements AnnotationScanner {

    /**
     * Scanner for class-type annotated elements.
     */
    private final AnnotationScanner typeScanner;

    /**
     * Scanner for method-type annotated elements.
     */
    private final AnnotationScanner methodScanner;

    /**
     * Scanner for meta-annotations.
     */
    private final AnnotationScanner metaScanner;

    /**
     * Scanner for general annotated elements.
     */
    private final AnnotationScanner elementScanner;

    /**
     * Returns {@code true} for all {@link AnnotatedElement} types.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return {@code true} always
     */
    @Override
    public boolean support(final AnnotatedElement annotatedEle) {
        return true;
    }

    /**
     * Constructs a composite annotation scanner.
     *
     * @param enableScanMetaAnnotation  whether to scan meta-annotations of found annotations
     * @param enableScanSupperClass     whether to scan superclasses
     * @param enableScanSupperInterface whether to scan superinterfaces
     */
    public CompositeAnnotationScanner(final boolean enableScanMetaAnnotation, final boolean enableScanSupperClass,
            final boolean enableScanSupperInterface) {

        this.metaScanner = enableScanMetaAnnotation ? new MetaAnnotationScanner() : new NoOpAnnotationScanner();
        this.typeScanner = new TypeAnnotationScanner(enableScanSupperClass, enableScanSupperInterface, a -> true,
                Collections.emptySet());
        this.methodScanner = new MethodAnnotationScanner(enableScanSupperClass, enableScanSupperInterface, a -> true,
                Collections.emptySet());
        this.elementScanner = new ElementAnnotationScanner();
    }

    /**
     * Dispatches scanning to the appropriate sub-scanner based on the element type, then processes each annotation
     * along with its corresponding hierarchy index.
     *
     * @param consumer     consumer for each (index, annotation) pair
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @param filter       annotation filter; annotations that fail this filter are skipped. May be {@code null}.
     */
    @Override
    public void scan(
            final BiConsumer<Integer, Annotation> consumer,
            final AnnotatedElement annotatedEle,
            Predicate<Annotation> filter) {
        filter = ObjectKit.defaultIfNull(filter, PredicateKit.alwaysTrue());
        if (ObjectKit.isNull(annotatedEle)) {
            return;
        }
        // Element is a class
        if (annotatedEle instanceof Class) {
            scanElements(typeScanner, consumer, annotatedEle, filter);
        }
        // Element is a method
        else if (annotatedEle instanceof Method) {
            scanElements(methodScanner, consumer, annotatedEle, filter);
        }
        // Element is another type
        else {
            scanElements(elementScanner, consumer, annotatedEle, filter);
        }
    }

    /**
     * Scans the hierarchy structure of the annotated element using the given scanner, collects annotations, then scans
     * their meta-annotations and processes each (index, annotation) pair.
     *
     * @param scanner      the scanner to use
     * @param consumer     consumer for each (index, annotation) pair
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @param filter       annotation filter; annotations that fail this filter are skipped
     */
    private void scanElements(
            final AnnotationScanner scanner,
            final BiConsumer<Integer, Annotation> consumer,
            final AnnotatedElement annotatedEle,
            final Predicate<Annotation> filter) {
        // Scan annotations on the element
        final ListValueMap<Integer, Annotation> classAnnotations = new ListValueMap<>(new LinkedHashMap<>());
        scanner.scan((index, annotation) -> {
            if (filter.test(annotation)) {
                classAnnotations.putValue(index, annotation);
            }
        }, annotatedEle, filter);

        // Scan meta-annotations
        classAnnotations.forEach((index, annotations) -> annotations.forEach(annotation -> {
            consumer.accept(index, annotation);
            metaScanner.scan(consumer, annotation.annotationType(), filter);
        }));
    }

}
