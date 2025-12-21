/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.annotation.resolve;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.annotation.Alias;
import org.miaixz.bus.core.lang.annotation.resolve.elements.HierarchicalAnnotatedElements;
import org.miaixz.bus.core.lang.annotation.resolve.elements.MetaAnnotatedElement;
import org.miaixz.bus.core.lang.annotation.resolve.elements.RepeatableMetaAnnotatedElement;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A utility class for {@link AnnotatedElement} that provides support for accessing annotations and meta-annotations
 * within a hierarchical structure. It also offers special attribute mapping mechanisms, such as attribute aliasing via
 * {@link Alias} and attribute value overrides between parent and child annotations.
 *
 * <p>
 * <b>Hierarchical Search:</b> Inspired by Spring's {@code AnnotatedElementUtils}, this utility provides two search
 * semantics:
 * <ul>
 * <li><em>get</em>: The search scope is limited to the specified {@link AnnotatedElement} itself.</li>
 * <li><em>find</em>: The search scope includes the specified {@link AnnotatedElement} and, if it's a class, its entire
 * hierarchy of superclasses and interfaces. If it's a method, it also searches for methods with the same signature in
 * the hierarchy of the declaring class.</li>
 * </ul>
 *
 * <p>
 * <b>Meta-Annotation Search:</b> The utility supports searching for meta-annotations. Methods without the
 * {@code directly} keyword will search for meta-annotations, while those with it will not. Note: The effect of
 * {@link Inherited} is not considered by {@code directly} methods.
 *
 * <p>
 * <b>Annotation Attribute Mapping:</b> The utility supports special mechanisms for mapping annotation attributes:
 * <ul>
 * <li><b>Attribute Aliasing with {@link Alias}:</b> If attributes are linked via {@link Alias}, setting a value for one
 * is equivalent to setting it for all linked attributes.
 * 
 * <pre>{@code
 * &#64;Alias("alias")
 * String value() default "";
 * &#64;Alias("value")
 * String alias() default "";
 * }</pre>
 * 
 * </li>
 * <li><b>Attribute Overrides:</b> If an annotation has an attribute with the same name and type as an attribute on its
 * meta-annotation, its value will override the meta-annotation's attribute value.</li>
 * </ul>
 *
 * <p>
 * <b>Repeatable Annotation Support:</b> Methods like {@code findAllXXX} or {@code getAllXXX} support finding repeatable
 * annotations, including those declared within a container annotation or multiple annotations sharing the same
 * meta-annotation.
 *
 * <p>
 * <b>Caching:</b> To avoid excessive reflection, this utility caches {@link AnnotatedElement} and meta-annotation
 * information. The cache is backed by a {@link WeakConcurrentMap} and can be cleared manually by calling
 * {@link #clearCaches()}.
 *
 * @author Kimi Liu
 * @see ResolvedAnnotationMapping
 * @see GenericAnnotationMapping
 * @see HierarchicalAnnotatedElements
 * @see RepeatableMetaAnnotatedElement
 * @see MetaAnnotatedElement
 * @see RepeatableAnnotationCollector
 * @since Java 17+
 */
public class AnnotatedElements {

    /**
     * Constructs a new AnnotatedElements. Utility class constructor for static access.
     */
    private AnnotatedElements() {
    }

    /**
     * Cache for {@link MetaAnnotatedElement} with attribute resolution enabled.
     */
    private static final Map<AnnotatedElement, MetaAnnotatedElement<ResolvedAnnotationMapping>> RESOLVED_ELEMENT_CACHE = new WeakConcurrentMap<>();
    /**
     * Cache for {@link MetaAnnotatedElement} with attribute resolution disabled.
     */
    private static final Map<AnnotatedElement, MetaAnnotatedElement<GenericAnnotationMapping>> ELEMENT_CACHE = new WeakConcurrentMap<>();
    /**
     * Cache for {@link RepeatableMetaAnnotatedElement} with attribute resolution enabled.
     */
    private static final Map<AnnotatedElement, RepeatableMetaAnnotatedElement<ResolvedAnnotationMapping>> RESOLVED_REPEATABLE_ELEMENT_CACHE = new WeakConcurrentMap<>();
    /**
     * Cache for {@link RepeatableMetaAnnotatedElement} with attribute resolution disabled.
     */
    private static final Map<AnnotatedElement, RepeatableMetaAnnotatedElement<GenericAnnotationMapping>> REPEATABLE_ELEMENT_CACHE = new WeakConcurrentMap<>();

    /**
     * Checks if an annotation of the specified type is present on the element or within its hierarchy, including on
     * meta-annotations.
     *
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return {@code true} if the annotation is present.
     */
    public static boolean isAnnotated(
            final AnnotatedElement element,
            final Class<? extends Annotation> annotationType) {
        return toHierarchyMetaElement(element, false).isAnnotationPresent(annotationType);
    }

    /**
     * Finds the first annotation of the specified type on the element or within its hierarchy.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return The annotation instance, or null if not found.
     */
    public static <T extends Annotation> T findAnnotation(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toHierarchyMetaElement(element, false).getAnnotation(annotationType);
    }

    /**
     * Finds all annotations of the specified type on the element and its hierarchy, including repeatable annotations
     * and meta-annotations.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return An array of found annotations.
     */
    public static <T extends Annotation> T[] findAllAnnotations(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toHierarchyRepeatableMetaElement(element, false).getAnnotationsByType(annotationType);
    }

    /**
     * Finds all annotations and meta-annotations on the element and within its hierarchy.
     *
     * @param element The {@link AnnotatedElement}.
     * @return An array of all found annotations.
     */
    public static Annotation[] findAnnotations(final AnnotatedElement element) {
        return toHierarchyMetaElement(element, false).getAnnotations();
    }

    /**
     * Finds the first annotation of the specified type on the element or within its hierarchy, applying attribute alias
     * and override rules.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return The resolved annotation instance, or null if not found.
     */
    public static <T extends Annotation> T findResolvedAnnotation(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toHierarchyMetaElement(element, true).getAnnotation(annotationType);
    }

    /**
     * Finds all annotations and meta-annotations on the element and within its hierarchy, applying attribute alias and
     * override rules.
     *
     * @param element The {@link AnnotatedElement}.
     * @return An array of all found and resolved annotations.
     */
    public static Annotation[] findResolvedAnnotations(final AnnotatedElement element) {
        return toHierarchyMetaElement(element, true).getAnnotations();
    }

    /**
     * Finds all annotations of the specified type on the element and its hierarchy, including repeatable annotations
     * and meta-annotations, applying attribute alias and override rules.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return An array of found and resolved annotations.
     */
    public static <T extends Annotation> T[] findAllResolvedAnnotations(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toHierarchyRepeatableMetaElement(element, true).getAnnotationsByType(annotationType);
    }

    /**
     * Finds the first annotation of the specified type that is directly present on the element or any element in its
     * hierarchy (no meta-annotation search).
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return The annotation instance, or null if not found.
     */
    public static <T extends Annotation> T findDirectlyAnnotation(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toHierarchyMetaElement(element, false).getDeclaredAnnotation(annotationType);
    }

    /**
     * Finds all directly present annotations of the specified type on the element and its hierarchy, including
     * repeatable annotations (no meta-annotation search).
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return An array of found annotations.
     */
    public static <T extends Annotation> T[] findAllDirectlyAnnotations(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toHierarchyRepeatableMetaElement(element, false).getDeclaredAnnotationsByType(annotationType);
    }

    /**
     * Finds all annotations directly present on the element and its hierarchy.
     *
     * @param element The {@link AnnotatedElement}.
     * @return An array of all directly present annotations.
     */
    public static Annotation[] findDirectlyAnnotations(final AnnotatedElement element) {
        return toHierarchyMetaElement(element, false).getDeclaredAnnotations();
    }

    /**
     * Finds the first directly present annotation of the specified type on the element or its hierarchy, applying
     * attribute alias and override rules.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return The resolved annotation instance, or null if not found.
     */
    public static <T extends Annotation> T findDirectlyResolvedAnnotation(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toHierarchyMetaElement(element, true).getDeclaredAnnotation(annotationType);
    }

    /**
     * Finds all directly present annotations on the element and its hierarchy, applying attribute alias and override
     * rules.
     *
     * @param element The {@link AnnotatedElement}.
     * @return An array of all found and resolved annotations.
     */
    public static Annotation[] findDirectlyResolvedAnnotations(final AnnotatedElement element) {
        return toHierarchyMetaElement(element, true).getDeclaredAnnotations();
    }

    /**
     * Finds all directly present annotations of the specified type on the element and its hierarchy, including
     * repeatable annotations, and applies attribute alias and override rules.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return An array of found and resolved annotations.
     */
    public static <T extends Annotation> T[] findAllDirectlyResolvedAnnotations(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toHierarchyRepeatableMetaElement(element, true).getDeclaredAnnotationsByType(annotationType);
    }

    /**
     * Checks if an annotation of the specified type is present on the element itself or as a meta-annotation.
     *
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return {@code true} if the annotation is present.
     */
    public static boolean isAnnotationPresent(
            final AnnotatedElement element,
            final Class<? extends Annotation> annotationType) {
        return toMetaElement(element, false).isAnnotationPresent(annotationType);
    }

    /**
     * Gets an annotation of the specified type from the element, searching meta-annotations if necessary.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return The annotation instance, or null if not found.
     */
    public static <T extends Annotation> T getAnnotation(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toMetaElement(element, false).getAnnotation(annotationType);
    }

    /**
     * Gets all annotations and meta-annotations present on the element.
     *
     * @param element The {@link AnnotatedElement}.
     * @return An array of all annotations.
     */
    public static Annotation[] getAnnotations(final AnnotatedElement element) {
        return toMetaElement(element, false).getAnnotations();
    }

    /**
     * Gets all annotations of the specified type from the element, including repeatable annotations and
     * meta-annotations.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return An array of all found annotations.
     */
    public static <T extends Annotation> T[] getAllAnnotations(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toRepeatableMetaElement(element, false).getAnnotationsByType(annotationType);
    }

    /**
     * Gets an annotation of the specified type from the element, searching meta-annotations and applying attribute
     * alias and override rules.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return The resolved annotation instance, or null if not found.
     */
    public static <T extends Annotation> T getResolvedAnnotation(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toMetaElement(element, true).getAnnotation(annotationType);
    }

    /**
     * Gets all annotations and meta-annotations on the element, applying attribute alias and override rules.
     *
     * @param element The {@link AnnotatedElement}.
     * @return An array of all resolved annotations.
     */
    public static Annotation[] getResolvedAnnotations(final AnnotatedElement element) {
        return toMetaElement(element, true).getAnnotations();
    }

    /**
     * Gets all annotations of the specified type from the element, including repeatable and meta-annotations, and
     * applies attribute alias and override rules.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return An array of all found and resolved annotations.
     */
    public static <T extends Annotation> T[] getAllResolvedAnnotations(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toRepeatableMetaElement(element, true).getAnnotationsByType(annotationType);
    }

    /**
     * Gets an annotation that is directly present on the element (no meta-annotation search).
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return The annotation instance, or null if not directly present.
     */
    public static <T extends Annotation> T getDirectlyAnnotation(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toMetaElement(element, false).getDeclaredAnnotation(annotationType);
    }

    /**
     * Gets all directly present annotations of the specified type, including repeatable annotations.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return An array of all directly present annotations.
     */
    public static <T extends Annotation> T[] getAllDirectlyAnnotations(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toRepeatableMetaElement(element, false).getDeclaredAnnotationsByType(annotationType);
    }

    /**
     * Gets all annotations directly present on the element.
     *
     * @param element The {@link AnnotatedElement}.
     * @return An array of directly present annotations.
     */
    public static Annotation[] getDirectlyAnnotations(final AnnotatedElement element) {
        return toMetaElement(element, false).getDeclaredAnnotations();
    }

    /**
     * Gets a directly present annotation, applying attribute alias and override rules.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return The resolved annotation instance, or null if not directly present.
     */
    public static <T extends Annotation> T getDirectlyResolvedAnnotation(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toMetaElement(element, true).getDeclaredAnnotation(annotationType);
    }

    /**
     * Gets all directly present annotations, applying attribute alias and override rules.
     *
     * @param element The {@link AnnotatedElement}.
     * @return An array of resolved annotations.
     */
    public static Annotation[] getDirectlyResolvedAnnotations(final AnnotatedElement element) {
        return toMetaElement(element, true).getDeclaredAnnotations();
    }

    /**
     * Gets all directly present annotations of the specified type, including repeatable annotations, and applies
     * attribute alias and override rules.
     *
     * @param <T>            The type of the annotation.
     * @param element        The {@link AnnotatedElement}.
     * @param annotationType The type of the annotation.
     * @return An array of resolved annotations.
     */
    public static <T extends Annotation> T[] getAllDirectlyResolvedAnnotations(
            final AnnotatedElement element,
            final Class<T> annotationType) {
        return toRepeatableMetaElement(element, true).getDeclaredAnnotationsByType(annotationType);
    }

    /**
     * Scans the hierarchy of the given element and wraps each level in a {@link MetaAnnotatedElement}, then combines
     * them into a {@link HierarchicalAnnotatedElements} instance.
     *
     * @param element  The element to scan.
     * @param resolved If {@code true}, enables attribute alias and override mechanisms.
     * @return A {@link HierarchicalAnnotatedElements} instance representing the element's hierarchy.
     */
    public static AnnotatedElement toHierarchyMetaElement(final AnnotatedElement element, final boolean resolved) {
        if (Objects.isNull(element)) {
            return emptyElement();
        }
        if (resolved) {
            return HierarchicalAnnotatedElements.of(element, (es, e) -> getResolvedMetaElementCache(e));
        }
        return HierarchicalAnnotatedElements.of(element, (es, e) -> getMetaElementCache(e));
    }

    /**
     * Scans the hierarchy of the given element, wraps each level in a {@link RepeatableMetaAnnotatedElement}, and
     * combines them into a {@link HierarchicalAnnotatedElements} instance.
     *
     * @param element  The element to scan.
     * @param resolved If {@code true}, enables attribute alias and override mechanisms.
     * @return A {@link HierarchicalAnnotatedElements} instance.
     */
    public static AnnotatedElement toHierarchyRepeatableMetaElement(
            final AnnotatedElement element,
            final boolean resolved) {
        if (Objects.isNull(element)) {
            return emptyElement();
        }
        if (resolved) {
            return HierarchicalAnnotatedElements.of(element, (es, e) -> getResolvedRepeatableMetaElementCache(e));
        }
        return HierarchicalAnnotatedElements.of(element, (es, e) -> getRepeatableMetaElementCache(e));
    }

    /**
     * Scans the hierarchy of the given element and combines them into a {@link HierarchicalAnnotatedElements}. This
     * version does not perform meta-annotation searches.
     *
     * @param element The element to scan.
     * @return A {@link HierarchicalAnnotatedElements} instance.
     */
    public static AnnotatedElement toHierarchyElement(final AnnotatedElement element) {
        return ObjectKit
                .defaultIfNull(element, ele -> HierarchicalAnnotatedElements.of(ele, (es, e) -> e), emptyElement());
    }

    /**
     * Wraps an {@link AnnotatedElement} as a {@link MetaAnnotatedElement} to enable meta-annotation searching.
     *
     * @param element  The element to wrap.
     * @param resolved If {@code true}, enables attribute alias and override mechanisms.
     * @return A {@link MetaAnnotatedElement} instance.
     */
    public static AnnotatedElement toMetaElement(final AnnotatedElement element, final boolean resolved) {
        return ObjectKit.defaultIfNull(
                element,
                e -> resolved ? getResolvedMetaElementCache(e) : getMetaElementCache(e),
                emptyElement());
    }

    /**
     * Wraps an {@link AnnotatedElement} as a {@link RepeatableMetaAnnotatedElement} to enable searching for repeatable
     * annotations and meta-annotations.
     *
     * @param element  The element to wrap.
     * @param resolved If {@code true}, enables attribute alias and override mechanisms.
     * @return A {@link RepeatableMetaAnnotatedElement} instance.
     */
    public static AnnotatedElement toRepeatableMetaElement(final AnnotatedElement element, final boolean resolved) {
        return ObjectKit.defaultIfNull(
                element,
                e -> resolved ? getResolvedRepeatableMetaElementCache(e) : getRepeatableMetaElementCache(e),
                emptyElement());
    }

    /**
     * Wraps an {@link AnnotatedElement} as a {@link RepeatableMetaAnnotatedElement} with a custom collector. This
     * method does not use caching.
     *
     * @param collector The custom repeatable annotation collector.
     * @param element   The element to wrap.
     * @param resolved  If {@code true}, enables attribute alias and override mechanisms.
     * @return A new {@link RepeatableMetaAnnotatedElement} instance.
     */
    public static AnnotatedElement toRepeatableMetaElement(
            final AnnotatedElement element,
            RepeatableAnnotationCollector collector,
            final boolean resolved) {
        if (Objects.isNull(element)) {
            return emptyElement();
        }
        collector = ObjectKit.defaultIfNull(collector, RepeatableAnnotationCollector.none());
        if (resolved) {
            return RepeatableMetaAnnotatedElement.create(
                    collector,
                    element,
                    (source, annotation) -> ResolvedAnnotationMapping
                            .create((ResolvedAnnotationMapping) source, annotation, true));
        }
        return RepeatableMetaAnnotatedElement.create(
                collector,
                element,
                (source, annotation) -> GenericAnnotationMapping.create(annotation, Objects.isNull(source)));
    }

    /**
     * Creates an {@link AnnotatedElement} from a given array of annotations.
     *
     * @param annotations The annotations to include in the element.
     * @return A new {@link AnnotatedElement} instance.
     */
    public static AnnotatedElement asElement(Annotation... annotations) {
        annotations = ArrayKit.filter(annotations, Objects::nonNull);
        return ArrayKit.isEmpty(annotations) ? emptyElement() : new ConstantElement(annotations);
    }

    /**
     * Gets a singleton {@link AnnotatedElement} that has no annotations.
     *
     * @return An empty {@link AnnotatedElement} instance.
     */
    public static AnnotatedElement emptyElement() {
        return EmptyElement.INSTANCE;
    }

    /**
     * Gets a cached (or creates a new) {@link MetaAnnotatedElement} for the given element, with attribute resolution
     * enabled.
     *
     * @param element The {@link AnnotatedElement}.
     * @return A {@link MetaAnnotatedElement} instance.
     */
    public static MetaAnnotatedElement<ResolvedAnnotationMapping> getResolvedMetaElementCache(
            final AnnotatedElement element) {
        return RESOLVED_ELEMENT_CACHE.computeIfAbsent(
                element,
                ele -> MetaAnnotatedElement.create(
                        element,
                        (source, annotation) -> ResolvedAnnotationMapping.create(source, annotation, true)));
    }

    /**
     * Gets a cached (or creates a new) {@link MetaAnnotatedElement} for the given element, without attribute
     * resolution.
     *
     * @param element The {@link AnnotatedElement}.
     * @return A {@link MetaAnnotatedElement} instance.
     */
    public static MetaAnnotatedElement<GenericAnnotationMapping> getMetaElementCache(final AnnotatedElement element) {
        return ELEMENT_CACHE.computeIfAbsent(
                element,
                ele -> MetaAnnotatedElement.create(
                        element,
                        (source, annotation) -> GenericAnnotationMapping.create(annotation, Objects.isNull(source))));
    }

    /**
     * Gets a cached (or creates a new) {@link RepeatableMetaAnnotatedElement} for the given element, with attribute
     * resolution enabled.
     *
     * @param element The {@link AnnotatedElement}.
     * @return A {@link RepeatableMetaAnnotatedElement} instance.
     */
    public static RepeatableMetaAnnotatedElement<ResolvedAnnotationMapping> getResolvedRepeatableMetaElementCache(
            final AnnotatedElement element) {
        return RESOLVED_REPEATABLE_ELEMENT_CACHE.computeIfAbsent(
                element,
                ele -> RepeatableMetaAnnotatedElement.create(
                        element,
                        (source, annotation) -> ResolvedAnnotationMapping.create(source, annotation, true)));
    }

    /**
     * Gets a cached (or creates a new) {@link RepeatableMetaAnnotatedElement} for the given element, without attribute
     * resolution.
     *
     * @param element The {@link AnnotatedElement}.
     * @return A {@link RepeatableMetaAnnotatedElement} instance.
     */
    public static RepeatableMetaAnnotatedElement<GenericAnnotationMapping> getRepeatableMetaElementCache(
            final AnnotatedElement element) {
        return REPEATABLE_ELEMENT_CACHE.computeIfAbsent(
                element,
                ele -> RepeatableMetaAnnotatedElement.create(
                        element,
                        (source, annotation) -> GenericAnnotationMapping.create(annotation, Objects.isNull(source))));
    }

    /**
     * Clears all related annotation caches. This includes caches in this class, {@link AnnoKit}, and
     * {@link RepeatableAnnotationCollector}.
     *
     * @see AnnoKit#clearCaches()
     * @see RepeatableAnnotationCollector#clearSingletonCaches()
     */
    public static void clearCaches() {
        ELEMENT_CACHE.clear();
        RESOLVED_ELEMENT_CACHE.clear();
        REPEATABLE_ELEMENT_CACHE.clear();
        RESOLVED_REPEATABLE_ELEMENT_CACHE.clear();
        RepeatableAnnotationCollector.clearSingletonCaches();
        AnnoKit.clearCaches();
    }

    /**
     * An {@link AnnotatedElement} implementation backed by a constant array of annotations.
     */
    private static class ConstantElement implements AnnotatedElement {

        private final Annotation[] annotations;

        ConstantElement(final Annotation[] annotations) {
            this.annotations = Objects.requireNonNull(annotations);
        }

        @Override
        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
            return Stream.of(annotations)
                    .filter(annotation -> Objects.equals(annotation.annotationType(), annotationClass)).findFirst()
                    .map(annotationClass::cast).orElse(null);
        }

        @Override
        public Annotation[] getAnnotations() {
            return annotations.clone();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return annotations.clone();
        }
    }

    /**
     * An {@link AnnotatedElement} implementation that contains no annotations.
     */
    private static class EmptyElement implements AnnotatedElement {

        static final EmptyElement INSTANCE = new EmptyElement();

        @Override
        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }
    }

}
