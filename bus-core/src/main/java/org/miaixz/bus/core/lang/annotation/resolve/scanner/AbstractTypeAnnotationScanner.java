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
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.*;

/**
 * Base implementation of {@link AnnotationScanner} for scanning annotations from class hierarchies.
 *
 * @param <T> The type of the current instance (for fluent API support)
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractTypeAnnotationScanner<T extends AbstractTypeAnnotationScanner<T>>
        implements AnnotationScanner {

    /**
     * Whether to include superclass scanning.
     */
    private boolean includeSuperClass;

    /**
     * Whether to include interface scanning.
     */
    private boolean includeInterfaces;

    /**
     * Filter predicate; types that fail this filter (along with their tree structures) are not scanned.
     */
    private Predicate<Class<?>> filter;

    /**
     * Excluded types; these types and their tree structures are not scanned.
     */
    private final Set<Class<?>> excludeTypes;

    /**
     * Type converters applied to each class before processing.
     */
    private final List<UnaryOperator<Class<?>>> converters;

    /**
     * Whether any converters have been registered.
     */
    private boolean hasConverters;

    /**
     * Reference to the current typed instance (for fluent API support).
     */
    private final T typedThis;

    /**
     * Constructs a new {@code AbstractTypeAnnotationScanner}.
     *
     * @param includeSuperClass Whether to include superclass scanning
     * @param includeInterfaces Whether to include interface scanning
     * @param filter            The filter predicate
     * @param excludeTypes      Types to exclude from scanning
     */
    protected AbstractTypeAnnotationScanner(final boolean includeSuperClass, final boolean includeInterfaces,
            final Predicate<Class<?>> filter, final Set<Class<?>> excludeTypes) {
        Assert.notNull(filter, "filter must not null");
        Assert.notNull(excludeTypes, "excludeTypes must not null");
        this.includeSuperClass = includeSuperClass;
        this.includeInterfaces = includeInterfaces;
        this.filter = filter;
        this.excludeTypes = excludeTypes;
        this.converters = new ArrayList<>();
        this.typedThis = (T) this;
    }

    /**
     * Returns whether superclass scanning is enabled.
     *
     * @return {@code true} if superclass scanning is enabled
     */
    public boolean isIncludeSuperClass() {
        return includeSuperClass;
    }

    /**
     * Returns whether interface scanning is enabled.
     *
     * @return {@code true} if interface scanning is enabled
     */
    public boolean isIncludeInterfaces() {
        return includeInterfaces;
    }

    /**
     * Sets the filter predicate. Types that fail this filter are not scanned.
     *
     * @param filter The filter predicate
     * @return This scanner instance
     */
    public T setFilter(final Predicate<Class<?>> filter) {
        Assert.notNull(filter, "filter must not null");
        this.filter = filter;
        return typedThis;
    }

    /**
     * Adds types to exclude from scanning.
     *
     * @param excludeTypes The types to exclude
     * @return This scanner instance
     */
    public T addExcludeTypes(final Class<?>... excludeTypes) {
        CollKit.addAll(this.excludeTypes, excludeTypes);
        return typedThis;
    }

    /**
     * Adds a type converter.
     *
     * @param converter The converter to apply to each type
     * @return This scanner instance
     * @see JdkProxyClassConverter
     */
    public T addConverters(final UnaryOperator<Class<?>> converter) {
        Assert.notNull(converter, "converter must not null");
        this.converters.add(converter);
        if (!this.hasConverters) {
            this.hasConverters = CollKit.isNotEmpty(this.converters);
        }
        return typedThis;
    }

    /**
     * Sets whether to include superclass scanning.
     *
     * @param includeSuperClass {@code true} to scan superclasses
     * @return This scanner instance
     */
    protected T setIncludeSuperClass(final boolean includeSuperClass) {
        this.includeSuperClass = includeSuperClass;
        return typedThis;
    }

    /**
     * Sets whether to include interface scanning.
     *
     * @param includeInterfaces {@code true} to scan interfaces
     * @return This scanner instance
     */
    protected T setIncludeInterfaces(final boolean includeInterfaces) {
        this.includeInterfaces = includeInterfaces;
        return typedThis;
    }

    /**
     * Scans the class hierarchy using breadth-first traversal and processes each class/interface's annotations along
     * with their hierarchy index.
     *
     * @param consumer     Consumer for each (index, annotation) pair
     * @param annotatedEle The element to scan
     * @param filter       Annotation filter; annotations that fail this filter are skipped. May be {@code null}.
     */
    @Override
    public void scan(
            final BiConsumer<Integer, Annotation> consumer,
            final AnnotatedElement annotatedEle,
            Predicate<Annotation> filter) {
        filter = ObjectKit.defaultIfNull(filter, PredicateKit.alwaysTrue());
        final Class<?> sourceClass = getClassFormAnnotatedElement(annotatedEle);
        final Deque<List<Class<?>>> classDeque = ListKit.ofLinked(ListKit.of(sourceClass));
        final Set<Class<?>> accessedTypes = new LinkedHashSet<>();
        int index = 0;
        while (!classDeque.isEmpty()) {
            final List<Class<?>> currClassQueue = classDeque.removeFirst();
            final List<Class<?>> nextClassQueue = new ArrayList<>();
            for (Class<?> targetClass : currClassQueue) {
                targetClass = convert(targetClass);
                // Skip classes that do not need processing
                if (isNotNeedProcess(accessedTypes, targetClass)) {
                    continue;
                }
                accessedTypes.add(targetClass);
                // Scan superclass
                scanSuperClassIfNecessary(nextClassQueue, targetClass);
                // Scan interfaces
                scanInterfaceIfNecessary(nextClassQueue, targetClass);
                // Process hierarchy index and annotations
                final Annotation[] targetAnnotations = getAnnotationsFromTargetClass(annotatedEle, index, targetClass);
                for (final Annotation annotation : targetAnnotations) {
                    if (!AnnoKit.isMetaAnnotation(annotation.annotationType()) && filter.test(annotation)) {
                        consumer.accept(index, annotation);
                    }
                }
                index++;
            }
            if (CollKit.isNotEmpty(nextClassQueue)) {
                classDeque.addLast(nextClassQueue);
            }
        }
    }

    /**
     * Extracts the class type to recursively scan from the annotated element.
     *
     * @param annotatedElement The annotated element
     * @return The class type to recursively scan
     */
    protected abstract Class<?> getClassFormAnnotatedElement(AnnotatedElement annotatedElement);

    /**
     * Returns the target annotations from the given class.
     *
     * @param source      The original annotated element
     * @param index       The hierarchy index of the class
     * @param targetClass The class to retrieve annotations from
     * @return The target annotations
     */
    protected abstract Annotation[] getAnnotationsFromTargetClass(
            AnnotatedElement source,
            int index,
            Class<?> targetClass);

    /**
     * Returns whether the given class should be skipped during scanning.
     *
     * @param accessedTypes Already accessed types
     * @param targetClass   The target class to check
     * @return {@code true} if the class should be skipped
     */
    protected boolean isNotNeedProcess(final Set<Class<?>> accessedTypes, final Class<?> targetClass) {
        return ObjectKit.isNull(targetClass) || accessedTypes.contains(targetClass)
                || excludeTypes.contains(targetClass) || filter.negate().test(targetClass);
    }

    /**
     * If {@link #includeInterfaces} is {@code true}, adds the target class's interfaces to the next-class queue.
     *
     * @param nextClasses The next-round class list
     * @param targetClass The current target class
     */
    protected void scanInterfaceIfNecessary(final List<Class<?>> nextClasses, final Class<?> targetClass) {
        if (includeInterfaces) {
            final Class<?>[] interfaces = targetClass.getInterfaces();
            if (ArrayKit.isNotEmpty(interfaces)) {
                CollKit.addAll(nextClasses, interfaces);
            }
        }
    }

    /**
     * If {@link #includeSuperClass} is {@code true}, adds the target class's superclass to the next-class queue.
     *
     * @param nextClassQueue The next-round class queue
     * @param targetClass    The current target class
     */
    protected void scanSuperClassIfNecessary(final List<Class<?>> nextClassQueue, final Class<?> targetClass) {
        if (includeSuperClass) {
            final Class<?> superClass = targetClass.getSuperclass();
            if (!ObjectKit.equals(superClass, Object.class) && ObjectKit.isNotNull(superClass)) {
                nextClassQueue.add(superClass);
            }
        }
    }

    /**
     * Applies registered converters to the target class, if any.
     *
     * @param target The target class
     * @return The converted class
     */
    protected Class<?> convert(Class<?> target) {
        if (hasConverters) {
            for (final UnaryOperator<Class<?>> converter : converters) {
                target = converter.apply(target);
            }
        }
        return target;
    }

    /**
     * Converts a JDK proxy class to its original proxied class, if applicable.
     */
    public static class JdkProxyClassConverter implements UnaryOperator<Class<?>> {

        /**
         * If {@code sourceClass} is a JDK proxy class, recursively returns the superclass until a non-proxy class is
         * found; otherwise returns {@code sourceClass} unchanged.
         *
         * @param sourceClass the class to convert
         * @return the original non-proxy class
         */
        @Override
        public Class<?> apply(final Class<?> sourceClass) {
            return Proxy.isProxyClass(sourceClass) ? apply(sourceClass.getSuperclass()) : sourceClass;
        }
    }

}
