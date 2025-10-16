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
package org.miaixz.bus.core.lang.annotation.resolve.elements;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.*;

/**
 * <p>
 * Represents a group of {@link AnnotatedElement}s that have an associated hierarchical relationship. When an instance
 * is created, it scans all {@link AnnotatedElement}s within the hierarchical structure of the specified
 * {@link AnnotatedElement} and wraps them as {@link MetaAnnotatedElement}s. For example, if element <em>A</em> has a
 * superclass <em>B</em> and an interface <em>C</em>, then a {@code HierarchicalAnnotatedElements} instance generated
 * from <em>A</em> will include <em>A</em>, <em>B</em>, and <em>C</em>. This instance supports accessing annotations
 * directly declared on these three elements, as well as their meta-annotations.
 *
 * <p>
 * <strong>Annotation Search Scope</strong>
 * <p>
 * In this instance, methods with and without the <em>declared</em> keyword are defined as follows:
 * <ul>
 * <li>When a method includes the <em>declared</em> keyword, the search scope is limited to annotations directly
 * declared on all saved {@link AnnotatedElement}s.</li>
 * <li>When a method does not include the <em>declared</em> keyword, the search scope includes:
 * <ol>
 * <li>Annotations directly declared on all saved {@link AnnotatedElement}s, and their meta-annotations.</li>
 * <li>If the element is a class, it includes annotations and meta-annotations declared on all its superclasses and
 * superinterfaces.</li>
 * <li>If the element is a method, and it is not static, private, or final, it additionally includes annotations and
 * meta-annotations on methods with the same signature in all superclasses and superinterfaces of its declaring
 * class.</li>
 * </ol>
 * </li>
 * </ul>
 *
 * <p>
 * <strong>Scanning Order</strong>
 * <p>
 * When an {@link AnnotatedElement} has a hierarchical structure, it is scanned in a breadth-first manner. This applies
 * to the element itself (if it is a {@link Class}) or its declaring class (if it is a {@link Method}). During this
 * process, superclasses are always scanned before superinterfaces. If there are multiple superinterfaces, their
 * scanning order follows the order in which they are returned by {@link Class#getInterfaces()}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HierarchicalAnnotatedElements implements AnnotatedElement, Iterable<AnnotatedElement> {

    /**
     * A factory method for creating {@link AnnotatedElement}s. If the factory returns {@code null}, the element will be
     * ignored.
     */
    protected final BiFunction<Set<AnnotatedElement>, AnnotatedElement, AnnotatedElement> elementFactory;
    /**
     * The wrapped {@link AnnotatedElement} object.
     */
    protected final AnnotatedElement source;
    /**
     * A lazy-loaded set of all {@link AnnotatedElement}s in the hierarchy. This set is initialized upon the first call
     * to {@link #getElementMappings()}. The elements in this collection are ordered by their distance from the wrapped
     * {@link AnnotatedElement} and by the order of breadth-first scanning.
     */
    private volatile Set<AnnotatedElement> elementMappings;

    /**
     * Constructs a new {@code HierarchicalAnnotatedElements} instance.
     *
     * @param element        The element to be wrapped.
     * @param elementFactory A factory method for creating {@link AnnotatedElement}s. If it returns {@code null}, the
     *                       element will be ignored.
     */
    HierarchicalAnnotatedElements(final AnnotatedElement element,
            final BiFunction<Set<AnnotatedElement>, AnnotatedElement, AnnotatedElement> elementFactory) {
        this.source = Objects.requireNonNull(element);
        // Lazy initialization
        this.elementMappings = null;
        this.elementFactory = Objects.requireNonNull(elementFactory);
    }

    /**
     * Creates a new {@code HierarchicalAnnotatedElements} instance. If the provided {@code element} is already a
     * {@code HierarchicalAnnotatedElements} instance, it is returned directly.
     *
     * @param element The element to be wrapped. If it is already a {@code HierarchicalAnnotatedElements}, it is
     *                returned as is.
     * @return A {@code HierarchicalAnnotatedElements} instance.
     */
    public static HierarchicalAnnotatedElements of(final AnnotatedElement element) {
        return of(element, (es, e) -> e);
    }

    /**
     * Creates a new {@code HierarchicalAnnotatedElements} instance with a custom element factory. If the provided
     * {@code element} is already a {@code HierarchicalAnnotatedElements} instance, it is returned directly.
     *
     * @param element        The element to be wrapped. If it is already a {@code HierarchicalAnnotatedElements}, it is
     *                       returned as is.
     * @param elementFactory A factory method for creating {@link AnnotatedElement}s. If it returns {@code null}, the
     *                       element will be ignored.
     * @return A {@code HierarchicalAnnotatedElements} instance.
     */
    public static HierarchicalAnnotatedElements of(
            final AnnotatedElement element,
            final BiFunction<Set<AnnotatedElement>, AnnotatedElement, AnnotatedElement> elementFactory) {
        return element instanceof HierarchicalAnnotatedElements ? (HierarchicalAnnotatedElements) element
                : new HierarchicalAnnotatedElements(element, elementFactory);
    }

    /**
     * Checks if the specified annotation type is present on any annotation or meta-annotation within the hierarchical
     * structure.
     *
     * @param annotationType The type of the annotation to check for.
     * @return {@code true} if the annotation is found, {@code false} otherwise.
     */
    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
        return getElementMappings().stream().anyMatch(element -> element.isAnnotationPresent(annotationType));
    }

    /**
     * Retrieves all annotations and meta-annotations present on any {@link AnnotatedElement} within the hierarchical
     * structure.
     *
     * @return An array of all annotation objects found.
     */
    @Override
    public Annotation[] getAnnotations() {
        return getElementMappings().stream().map(AnnotatedElement::getAnnotations).filter(ArrayKit::isNotEmpty)
                .flatMap(Stream::of).toArray(Annotation[]::new);
    }

    /**
     * Retrieves the first occurrence of the specified annotation type from any {@link AnnotatedElement} within the
     * hierarchical structure.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return The annotation object, or {@code null} if not found.
     */
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        return getElementMappings().stream().map(e -> e.getAnnotation(annotationType)).filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    /**
     * Retrieves all occurrences of the specified annotation type from any {@link AnnotatedElement} within the
     * hierarchical structure.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return An array of all annotation objects of the specified type found.
     */
    public <A extends Annotation> A[] getAnnotationsByType(final Class<A> annotationType) {
        return getElementMappings().stream().map(element -> element.getAnnotationsByType(annotationType))
                .filter(ArrayKit::isNotEmpty).flatMap(Stream::of)
                .toArray(size -> ArrayKit.newArray(annotationType, size));
    }

    /**
     * Retrieves all annotations directly declared on any {@link AnnotatedElement} within the hierarchical structure.
     *
     * @return An array of all directly declared annotation objects found.
     */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getElementMappings().stream().map(AnnoKit::getDeclaredAnnotations).filter(ArrayKit::isNotEmpty)
                .flatMap(Stream::of).toArray(Annotation[]::new);
    }

    /**
     * Retrieves the first occurrence of the specified annotation type directly declared on any {@link AnnotatedElement}
     * within the hierarchical structure.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return The directly declared annotation object, or {@code null} if not found.
     */
    @Override
    public <A extends Annotation> A getDeclaredAnnotation(final Class<A> annotationType) {
        return getElementMappings().stream().map(element -> element.getDeclaredAnnotation(annotationType))
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Retrieves all occurrences of the specified annotation type directly declared on any {@link AnnotatedElement}
     * within the hierarchical structure.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return An array of all directly declared annotation objects of the specified type found.
     */
    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(final Class<A> annotationType) {
        return getElementMappings().stream().map(element -> element.getDeclaredAnnotationsByType(annotationType))
                .filter(ArrayKit::isNotEmpty).flatMap(Stream::of)
                .toArray(size -> ArrayKit.newArray(annotationType, size));
    }

    /**
     * Returns an iterator over the {@link AnnotatedElement} objects within this hierarchical structure.
     *
     * @return An iterator over {@link AnnotatedElement} objects.
     */
    @Override
    public Iterator<AnnotatedElement> iterator() {
        return getElementMappings().iterator();
    }

    /**
     * Retrieves the original wrapped {@link AnnotatedElement} object.
     *
     * @return The original annotated element.
     */
    public AnnotatedElement getElement() {
        return source;
    }

    /**
     * Compares this instance with the specified object for equality.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HierarchicalAnnotatedElements that = (HierarchicalAnnotatedElements) o;
        return elementFactory.equals(that.elementFactory) && source.equals(that.source);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(elementFactory, source);
    }

    /**
     * Retrieves the set of mapped {@link AnnotatedElement} objects, including the current element and all associated
     * elements in its hierarchy. The returned set is unmodifiable.
     *
     * @return An unmodifiable set of {@link AnnotatedElement} mappings.
     */
    public final Set<AnnotatedElement> getElementMappings() {
        initElementMappingsIfNecessary();
        return this.elementMappings;
    }

    /**
     * Checks if the method signature of the target method matches that of the source method.
     *
     * @param source The source method.
     * @param target The method to compare.
     * @return {@code true} if the method signatures match, {@code false} otherwise.
     */
    protected boolean isMatchMethod(final Method source, final Method target) {
        return CharsBacker.equals(source.getName(), target.getName())
                // Cannot be a bridge method or a synthetic method.
                && !target.isBridge() && !target.isSynthetic()
                // The return type must be assignable from the source method's return type.
                && ClassKit.isAssignable(target.getReturnType(), source.getReturnType())
                // The number of parameters must be the same, and their types must be strictly identical, but generics
                // are not checked.
                && Arrays.equals(source.getParameterTypes(), target.getParameterTypes());
    }

    /**
     * Converts an element to a {@link MetaAnnotatedElement} and adds it to the collection of elements.
     *
     * @param elements The collection of elements to add to.
     * @param element  The element to convert and add.
     */
    private void collectElement(final Set<AnnotatedElement> elements, final AnnotatedElement element) {
        final AnnotatedElement target = elementFactory.apply(elements, element);
        if (Objects.nonNull(target)) {
            elements.add(target);
        }
    }

    /**
     * Initializes the {@link #elementMappings} set if it has not already been initialized. This method uses
     * double-checked locking to ensure thread-safe lazy initialization.
     */
    private void initElementMappingsIfNecessary() {
        if (Objects.isNull(elementMappings)) {
            synchronized (this) {
                if (Objects.isNull(elementMappings)) {
                    final Set<AnnotatedElement> mappings = initElementMappings();
                    elementMappings = SetKit.view(mappings);
                }
            }
        }
    }

    /**
     * Traverses the hierarchical structure to collect all associated {@link AnnotatedElement}s and adds them to
     * {@link #elementMappings}.
     *
     * @return A set of collected {@link AnnotatedElement}s.
     */
    private Set<AnnotatedElement> initElementMappings() {
        final Set<AnnotatedElement> mappings = new LinkedHashSet<>();
        // If the original element is a Class.
        if (source instanceof Class) {
            scanHierarchy(mappings, (Class<?>) source, false, source);
        }
        // If the original element is a Method.
        else if (source instanceof final Method methodSource) {
            // Static, private, and final methods cannot be overridden by subclasses, so they do not have a hierarchical
            // structure.
            if (Modifier.isPrivate(methodSource.getModifiers()) || Modifier.isFinal(methodSource.getModifiers())
                    || Modifier.isStatic(methodSource.getModifiers())) {
                collectElement(mappings, methodSource);
            } else {
                scanHierarchy(mappings, methodSource.getDeclaringClass(), true, methodSource);
            }
        }
        return mappings;
    }

    /**
     * Traverses the superclasses and superinterfaces of the given {@code type} in a breadth-first manner, collecting
     * annotations from the class itself or from specified methods within the class.
     *
     * @param mappings The set to collect the {@link AnnotatedElement}s into.
     * @param type     The class to start scanning from.
     * @param isMethod {@code true} if scanning for methods, {@code false} if scanning for classes.
     * @param source   The original {@link AnnotatedElement} (either a Class or a Method) that initiated the scan.
     */
    private void scanHierarchy(
            final Set<AnnotatedElement> mappings,
            Class<?> type,
            final boolean isMethod,
            final AnnotatedElement source) {
        final Method methodSource = isMethod ? (Method) source : null;
        final Deque<Class<?>> deque = new LinkedList<>();
        deque.addLast(type);
        final Set<Class<?>> accessed = new HashSet<>();
        while (!deque.isEmpty()) {
            type = deque.removeFirst();
            // Skip already visited classes.
            if (!isNeedMapping(type, accessed)) {
                continue;
            }
            // Collect elements.
            if (!isMethod) {
                collectElement(mappings, type);
            } else {
                Stream.of(MethodKit.getDeclaredMethods(type)).filter(method -> isMatchMethod(methodSource, method))
                        .forEach(method -> collectElement(mappings, method));
            }
            // Get superclass and superinterfaces.
            accessed.add(type);
            deque.addLast(type.getSuperclass());
            CollKit.addAll(deque, type.getInterfaces());
        }
    }

    /**
     * Checks if a given class needs to be processed. A class needs processing if it is not {@code null}, has not been
     * accessed yet, and is not {@link Object.class}.
     *
     * @param type          The class to check.
     * @param accessedTypes A set of classes that have already been accessed.
     * @return {@code true} if the class needs processing, {@code false} otherwise.
     */
    private boolean isNeedMapping(final Class<?> type, final Set<Class<?>> accessedTypes) {
        return Objects.nonNull(type) && !accessedTypes.contains(type) && !Objects.equals(type, Object.class);
    }

}
