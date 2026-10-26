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
package org.miaixz.bus.core.xyz;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.center.function.PredicateX;
import org.miaixz.bus.core.center.map.reference.SoftConcurrentMap;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotationLookupKey;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotationMappingProxy;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotationProxy;
import org.miaixz.bus.core.lang.annotation.resolve.elements.CombinationAnnotatedElement;
import org.miaixz.bus.core.lang.annotation.resolve.scanner.AnnotationScanner;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.GenericSynthesizedAggregateAnnotation;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAggregateAnnotation;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAnnotationProxy;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Provides direct, composite, and synthesized annotation lookup, annotation-attribute access, and annotation proxies.
 * Declared annotation arrays use a weak-reference cache; lookup and synthesis results use separate soft-reference
 * caches that also represent misses with a sentinel. Cached values may be reclaimed and resolved again.
 *
 * @author Kimi Liu
 */
public class AnnoKit {

    /**
     * Name of the member-value map on a JDK annotation invocation handler.
     */
    private static final String JDK_MEMBER_ATTRIBUTE = "memberValues";

    /**
     * Name of the member-value map on a recognized Spring synthesized-annotation handler.
     */
    private static final String SPRING_MEMBER_ATTRIBUTE = "valueCache";

    /**
     * Name of the member-value map on a Bus annotation-mapping proxy.
     */
    private static final String BUS_MEMBER_ATTRIBUTE = "valueCache";

    /**
     * Handler class-name fragment used to recognize Spring synthesized annotations without a Spring dependency.
     */
    private static final String SPRING_INVOCATION_HANDLER = "SynthesizedMergedAnnotationInvocationHandler";

    /**
     * Directly declared annotation arrays, with both annotated elements and arrays weakly held.
     */
    private static final Map<AnnotatedElement, Annotation[]> DECLARED_ANNOTATIONS_CACHE = new WeakConcurrentMap<>();

    /**
     * Internal non-null marker for a missing lookup or synthesis result; it is never returned as a user annotation.
     */
    private static final Annotation NULL_ANNOTATION_SENTINEL = () -> null;

    /**
     * Composite-annotation lookup results keyed by element and target annotation type. Keys and values are softly held;
     * an absent annotation is represented by {@link #NULL_ANNOTATION_SENTINEL}.
     */
    private static final Map<AnnotationLookupKey, Annotation> L1_ANNOTATION_CACHE = new SoftConcurrentMap<>();

    /**
     * Direct or synthesized lookup results keyed by element and target annotation type. Keys and values are softly
     * held; an absent annotation is represented by {@link #NULL_ANNOTATION_SENTINEL}.
     */
    private static final Map<AnnotationLookupKey, Annotation> L2_SYNTHESIZED_ANNOTATION_CACHE = new SoftConcurrentMap<>();

    /**
     * Creates an instance; annotation lookup and synthesis operations remain available through the static API.
     */
    public AnnoKit() {
        // No initialization required.
    }

    /**
     * Retrieves annotations directly declared on the given element. If a cached value exists, it is returned. This
     * method specifically:
     * <ul>
     * <li>Returns only annotations directly declared on the element.</li>
     * <li>Does not include annotations inherited from parent classes or interfaces.</li>
     * <li>Retrieves only annotations defined on the current class, method, field, etc.</li>
     * </ul>
     *
     * @param element The non-null annotated element, such as a class, method, field, or constructor.
     * @return The shared cached annotation array; callers must not modify it.
     * @throws NullPointerException If the annotated element is null.
     */
    public static Annotation[] getDeclaredAnnotations(final AnnotatedElement element) {
        return DECLARED_ANNOTATIONS_CACHE.computeIfAbsent(element, AnnotatedElement::getDeclaredAnnotations);
    }

    /**
     * Retrieves annotations exposed by the element, optionally expanding their meta-annotations. Without combination
     * expansion, inheritance follows {@link AnnotatedElement#getAnnotations()}; this does not independently traverse
     * every superclass or interface.
     *
     * @param annotationEle   The annotated element, which can be a Class, Method, Field, Constructor, etc.
     * @param isToCombination Whether to convert to a combination annotation, which supports recursively obtaining
     *                        annotations of annotations.
     * @return An array of annotations, or null if the element is null.
     */
    public static Annotation[] getAnnotations(final AnnotatedElement annotationEle, final boolean isToCombination) {
        return getAnnotations(annotationEle, isToCombination, (PredicateX<Annotation>) null);
    }

    /**
     * Retrieves combination annotations of a specific type for the given element.
     *
     * @param <T>            The type of the annotation.
     * @param annotationEle  The non-null annotated element.
     * @param annotationType The non-null type of the annotation to retrieve.
     * @return An array of annotations of the specified type.
     */
    public static <T> T[] getCombinationAnnotations(
            final AnnotatedElement annotationEle,
            final Class<T> annotationType) {
        return getAnnotations(annotationEle, true, annotationType);
    }

    /**
     * Retrieves annotations of a specific type for the given element.
     *
     * @param <T>             The type of the annotation.
     * @param annotationEle   The non-null annotated element.
     * @param isToCombination Whether to convert to a combination annotation.
     * @param annotationType  The non-null type of the annotation to retrieve.
     * @return An array of annotations of the specified type.
     */
    public static <T> T[] getAnnotations(
            final AnnotatedElement annotationEle,
            final boolean isToCombination,
            final Class<T> annotationType) {
        final Annotation[] annotations = getAnnotations(
                annotationEle,
                isToCombination,
                (annotation -> null == annotationType || annotationType.isAssignableFrom(annotation.getClass())));

        final T[] result = ArrayKit.newArray(annotationType, annotations.length);
        for (int i = 0; i < annotations.length; i++) {
            result[i] = (T) annotations[i];
        }
        return result;
    }

    /**
     * Retrieves annotations for the specified element, with optional filtering.
     *
     * @param annotationEle   The annotated element.
     * @param isToCombination Whether to convert to a combination annotation.
     * @param predicate       A predicate to filter the annotations, or null to retain all annotations.
     * @return An array of annotations, or null if the element is null.
     */
    public static Annotation[] getAnnotations(
            final AnnotatedElement annotationEle,
            final boolean isToCombination,
            final PredicateX<Annotation> predicate) {
        if (null == annotationEle) {
            return null;
        }

        if (isToCombination) {
            if (null == predicate) {
                return toCombination(annotationEle).getAnnotations();
            }
            return CombinationAnnotatedElement.of(annotationEle, predicate).getAnnotations();
        }

        final Annotation[] result = annotationEle.getAnnotations();
        if (null == predicate) {
            return result;
        }
        return ArrayKit.filter(result, predicate);
    }

    /**
     * Retrieves the specified annotation through a combination-annotation view. Both found annotations and misses are
     * cached by element and annotation type while the soft references remain live. Meta-annotations are included, but
     * the lookup does not introduce an independent traversal of all superclass and interface declarations.
     *
     * @param <A>            The type of the annotation.
     * @param annotationEle  The annotated element.
     * @param annotationType The type of the annotation to retrieve.
     * @return The annotation object, or null if either input is null or the annotation is not found.
     */
    public static <A extends Annotation> A getAnnotation(
            final AnnotatedElement annotationEle,
            final Class<A> annotationType) {
        if (null == annotationEle || null == annotationType) {
            return null;
        }

        // Check L1 cache first
        final A result = (A) L1_ANNOTATION_CACHE
                .computeIfAbsent(new AnnotationLookupKey(annotationEle, annotationType), (lookupKey) -> {
                    // Resolve a cache miss through the same combination view used by uncached lookup.
                    final A annotation = toCombination(annotationEle).getAnnotation(annotationType);
                    // Store in cache: replace null with sentinel
                    return (null == annotation) ? NULL_ANNOTATION_SENTINEL : annotation;
                });
        return (result == NULL_ANNOTATION_SENTINEL) ? null : result;
    }

    /**
     * Checks for an annotation identified by its fully qualified class name. A missing annotation class is treated as
     * an absent annotation; other class-loading failures are not suppressed.
     *
     * @param annotationEle      The annotated element.
     * @param annotationTypeName The fully qualified class name of the annotation type.
     * @return {@code true} if the element contains the specified annotation, {@code false} otherwise.
     */
    public static boolean hasAnnotation(final AnnotatedElement annotationEle, final String annotationTypeName) {
        Class aClass = null;
        try {
            aClass = Class.forName(annotationTypeName);
        } catch (final ClassNotFoundException e) {
            // Ignore exception
        }
        if (null != aClass) {
            return hasAnnotation(annotationEle, aClass);
        }
        return false;
    }

    /**
     * Checks if the specified element contains a specific annotation.
     *
     * @param annotationEle  The annotated element.
     * @param annotationType The type of the annotation to check for.
     * @return {@code true} if the element contains the specified annotation, {@code false} otherwise.
     */
    public static boolean hasAnnotation(
            final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType) {
        return null != getAnnotation(annotationEle, annotationType);
    }

    /**
     * Retrieves the resolved annotation's {@code value} attribute. This is the annotation instance's value, which may
     * be explicitly supplied or supplied by the annotation declaration's default.
     *
     * @param <T>            The type of the annotation value.
     * @param annotationEle  The annotated element.
     * @param annotationType The type of the annotation.
     * @return The attribute value, or null if the annotation or its {@code value} attribute is absent.
     * @throws InternalException If an error occurs while invoking the annotation method.
     */
    public static <T> T getAnnotationValue(
            final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType) throws InternalException {
        return getAnnotationValue(annotationEle, annotationType, "value");
    }

    /**
     * Retrieves the value of a specified annotation attribute, identified by a Lambda expression.
     *
     * @param <A>           The type of the annotation.
     * @param <R>           The type of the attribute value.
     * @param annotationEle The annotated element.
     * @param propertyName  The Lambda expression representing the property name.
     * @return The attribute value, or null if the property expression, annotation, or attribute is absent.
     * @throws InternalException If an error occurs while invoking the annotation method.
     */
    public static <A extends Annotation, R> R getAnnotationValue(
            final AnnotatedElement annotationEle,
            final FunctionX<A, R> propertyName) {
        if (propertyName == null) {
            return null;
        } else {
            final LambdaX lambda = LambdaKit.resolve(propertyName);
            final String instantiatedMethodType = lambda.getLambda().getInstantiatedMethodType();
            final Class<A> annotationClass = ClassKit.loadClass(
                    StringKit.sub(
                            instantiatedMethodType,
                            2,
                            StringKit.indexOf(instantiatedMethodType, Symbol.C_SEMICOLON)));
            return getAnnotationValue(annotationEle, annotationClass, lambda.getLambda().getImplMethodName());
        }
    }

    /**
     * Retrieves the value of a specified annotation attribute.
     *
     * @param <T>            The type of the annotation value.
     * @param annotationEle  The annotated element.
     * @param annotationType The type of the annotation.
     * @param propertyName   The name of the attribute.
     * @return The attribute value, or null if the annotation or specified attribute is absent.
     * @throws InternalException If an error occurs while invoking the annotation method.
     */
    public static <T> T getAnnotationValue(
            final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType,
            final String propertyName) throws InternalException {
        final Annotation annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }

        final Method method = MethodKit.getMethodOfObject(annotation, propertyName);
        if (null == method) {
            return null;
        }
        return MethodKit.invoke(annotation, method);
    }

    /**
     * Retrieves all attribute values of a specified annotation.
     *
     * @param annotationEle  The annotated element.
     * @param annotationType The type of the annotation.
     * @return A map of attribute names to attribute values, or {@code null} if no annotation is found.
     * @throws InternalException If an error occurs while invoking the annotation methods.
     */
    public static Map<String, Object> getAnnotationValueMap(
            final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType) throws InternalException {
        final Annotation annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }

        final Method[] methods = MethodKit.getMethods(annotationType, t -> {
            if (ArrayKit.isEmpty(t.getParameterTypes())) {
                // Only read methods with no parameters
                final String name = t.getName();
                // Skip several self-owned methods
                return (!Normal.HASHCODE.equals(name)) && (!Normal.TOSTRING.equals(name))
                        && (!"annotationType".equals(name));
            }
            return false;
        });

        final HashMap<String, Object> result = new HashMap<>(methods.length, 1);
        for (final Method method : methods) {
            result.put(method.getName(), MethodKit.invoke(annotation, method));
        }
        return result;
    }

    /**
     * Retrieves the retention policy of an annotation class.
     *
     * @param annotationType The annotation type.
     * @return The retention policy, defaults to {@link RetentionPolicy#CLASS} if not specified.
     */
    public static RetentionPolicy getRetentionPolicy(final Class<? extends Annotation> annotationType) {
        final Retention retention = annotationType.getAnnotation(Retention.class);
        if (null == retention) {
            return RetentionPolicy.CLASS;
        }
        return retention.value();
    }

    /**
     * Retrieves the program element types supported by an annotation class.
     *
     * @param annotationType The annotation type.
     * @return The declared target types, or all {@link ElementType} values as this method's fallback when
     *         {@code @Target} is absent; the fallback is not a validation of Java source placement rules.
     */
    public static ElementType[] getTargetType(final Class<? extends Annotation> annotationType) {
        final Target target = annotationType.getAnnotation(Target.class);
        if (null == target) {
            // Return the all-types fallback rather than enforce Java annotation placement rules.
            return ElementType.values();
        }
        return target.value();
    }

    /**
     * Checks whether a type belongs to the predefined core meta-annotation set in {@link Normal#META_ANNOTATIONS}. This
     * does not detect every application-defined annotation that can annotate another annotation.
     *
     * @param annotationType The annotation type.
     * @return Whether the type belongs to the predefined set.
     */
    public static boolean isMetaAnnotation(final Class<? extends Annotation> annotationType) {
        return Normal.META_ANNOTATIONS.contains(annotationType);
    }

    /**
     * Checks if an annotation class will be documented in Javadoc.
     *
     * @param annotationType The annotation type.
     * @return {@code true} if it will be documented, {@code false} otherwise.
     */
    public static boolean isDocumented(final Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Documented.class);
    }

    /**
     * Checks whether an annotation type declares {@link Inherited}. Java inheritance of such annotations applies to
     * class declarations, not arbitrary method, field, or interface annotations.
     *
     * @param annotationType The annotation type.
     * @return {@code true} if it can be inherited, {@code false} otherwise.
     */
    public static boolean isInherited(final Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Inherited.class);
    }

    /**
     * Mutates the member-value map of a recognized annotation invocation handler. This changes the supplied proxy
     * instance; it does not rewrite annotation metadata stored in a class file.
     * <p>
     * Reflective access must be permitted by the runtime. For a JDK annotation handler, this may require opening
     * {@code java.base/sun.reflect.annotation} to the caller, for example with
     * {@code --add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED} for classpath applications. Other handlers may
     * require access to their own packages. This method does not perform annotation-member type validation.
     *
     * @param annotation      The annotation object.
     * @param annotationField The name of the attribute.
     * @param value           The value to set for the attribute.
     * @throws IllegalArgumentException If the annotation is not a proxy instance.
     */
    public static void setValue(final Annotation annotation, final String annotationField, final Object value) {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        String memberAttributeName = JDK_MEMBER_ATTRIBUTE;
        // Spring synthesized annotation
        if (StringKit.contains(invocationHandler.getClass().getName(), SPRING_INVOCATION_HANDLER)) {
            memberAttributeName = SPRING_MEMBER_ATTRIBUTE;
        }
        // Synthesized annotation
        else if (invocationHandler instanceof AnnotationMappingProxy) {
            memberAttributeName = BUS_MEMBER_ATTRIBUTE;
        }
        final Map<String, Object> memberValues = (Map<String, Object>) FieldKit
                .getFieldValue(invocationHandler, memberAttributeName);
        memberValues.put(annotationField, value);
    }

    /**
     * Retrieves an annotation proxy object that supports aliases.
     *
     * @param <T>            The type of the annotation.
     * @param annotationEle  The annotated element.
     * @param annotationType The type of the annotation.
     * @return The annotation proxy object, or {@code null} if no annotation is found.
     */
    public static <T extends Annotation> T getAnnotationAlias(
            final AnnotatedElement annotationEle,
            final Class<T> annotationType) {
        final T annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }
        return (T) Proxy.newProxyInstance(
                annotationType.getClassLoader(),
                new Class[] { annotationType },
                new AnnotationProxy<>(annotation));
    }

    /**
     * Retrieves the attribute methods of an annotation class.
     *
     * @param annotationType The annotation type.
     * @return An array of attribute methods.
     */
    public static Method[] getAnnotationAttributes(final Class<? extends Annotation> annotationType) {
        return Stream.of(MethodKit.getDeclaredMethods(annotationType)).filter(AnnoKit::isAnnotationAttribute)
                .toArray(Method[]::new);
    }

    /**
     * Applies the annotation-attribute method-shape filter when inspecting annotation types. This is not a complete
     * validation of every Java annotation declaration rule.
     *
     * <ul>
     * <li>Not {@link Object#equals(Object)} method</li>
     * <li>Not {@link Object#hashCode()} method</li>
     * <li>Not {@link Object#toString()} method</li>
     * <li>Not a bridge method</li>
     * <li>Not a synthetic method</li>
     * <li>Not a static method</li>
     * <li>A public method</li>
     * <li>Has no parameters</li>
     * <li>Its return type is not the boxed {@link Void} class</li>
     * </ul>
     *
     * @param attribute The method object.
     * @return {@code true} if it is an annotation attribute method, {@code false} otherwise.
     */
    public static boolean isAnnotationAttribute(final Method attribute) {
        return !MethodKit.isEqualsMethod(attribute) && !MethodKit.isHashCodeMethod(attribute)
                && !MethodKit.isToStringMethod(attribute) && ArrayKit.isEmpty(attribute.getParameterTypes())
                && ObjectKit.notEquals(attribute.getReturnType(), Void.class)
                && !Modifier.isStatic(attribute.getModifiers()) && Modifier.isPublic(attribute.getModifiers())
                && !attribute.isBridge() && !attribute.isSynthetic();
    }

    /**
     * Converts the specified annotated element into a combination annotated element, which supports recursively
     * obtaining annotations of annotations.
     *
     * @param annotationEle The annotated element.
     * @return The combination annotated element.
     */
    public static CombinationAnnotatedElement toCombination(final AnnotatedElement annotationEle) {
        if (annotationEle instanceof CombinationAnnotatedElement) {
            return (CombinationAnnotatedElement) annotationEle;
        }
        return new CombinationAnnotatedElement(annotationEle);
    }

    /**
     * Converts the given annotation instances and their meta-annotations into a synthesized annotation.
     *
     * @param annotationType the annotation type
     * @param annotations    the annotation instances
     * @param <T>            the annotation type
     * @return The synthesized annotation, or null if no supplied annotation can produce the requested type.
     * @see SynthesizedAggregateAnnotation
     */
    public static <T extends Annotation> T getSynthesizedAnnotation(
            final Class<T> annotationType,
            final Annotation... annotations) {
        return Optional.ofNullable(annotations).filter(ArrayKit::isNotEmpty)
                .map(AnnoKit::aggregatingFromAnnotationWithMeta).map(a -> a.synthesize(annotationType)).getOrNull();
    }

    /**
     * Retrieves an annotation directly exposed by the element, returning that instance unchanged if present. Otherwise,
     * scans the element with {@link AnnotationScanner#DIRECTLY} and returns the first root annotation whose
     * meta-annotation aggregate can synthesize the requested type. Class annotations include those exposed by JDK
     * {@link Inherited} rules; this method does not independently scan every superclass or interface. Found results and
     * misses are cached by element and annotation type while the soft references remain live.
     *
     * <p>
     * Annotation synthesis rules: suppose {@code AnnotatedEle} declares annotations A, B, C in order from top to
     * bottom, with the following meta-annotation hierarchies:
     *
     * <pre>
     *    A -&gt; M3
     *    B -&gt; M1 -&gt; M2 -&gt; M3
     *    C -&gt; M2 -&gt; M3
     * </pre>
     * <p>
     * If {@code annotationType} is {@code M2}, the synthesized annotation derived from root annotation B is returned
     * first.
     *
     * @param annotatedEle   The annotated element, such as a class, method, field, or constructor.
     * @param annotationType the annotation type
     * @param <T>            the annotation type
     * @return The direct or synthesized annotation, or null if either input is null or no match is found.
     * @see SynthesizedAggregateAnnotation
     */
    public static <T extends Annotation> T getSynthesizedAnnotation(
            final AnnotatedElement annotatedEle,
            final Class<T> annotationType) {
        if (null == annotatedEle || null == annotationType) {
            return null;
        }

        // Check L2 cache first
        final AnnotationLookupKey key = new AnnotationLookupKey(annotatedEle, annotationType);
        final Annotation cached = L2_SYNTHESIZED_ANNOTATION_CACHE.get(key);

        // Cache hit
        if (null != cached) {
            return (cached == NULL_ANNOTATION_SENTINEL) ? null : (T) cached;
        }

        // Prefer an annotation directly exposed by the element before synthesizing from its roots.
        T result = annotatedEle.getAnnotation(annotationType);
        if (ObjectKit.isNotNull(result)) {
            L2_SYNTHESIZED_ANNOTATION_CACHE.put(key, result);
            return result;
        }

        result = AnnotationScanner.DIRECTLY.getAnnotationsIfSupport(annotatedEle).stream()
                .map(annotation -> getSynthesizedAnnotation(annotationType, annotation)).filter(Objects::nonNull)
                .findFirst().orElse(null);

        // Store in cache: replace null with sentinel
        L2_SYNTHESIZED_ANNOTATION_CACHE.put(key, (null == result) ? NULL_ANNOTATION_SENTINEL : result);
        return result;
    }

    /**
     * Scans the element with {@link AnnotationScanner#DIRECTLY} and attempts synthesis from each root annotation and
     * its meta-annotations. Results follow root scan order and are not deduplicated. Class annotations include those
     * exposed by JDK {@link Inherited} rules; this does not independently scan every superclass or interface.
     *
     * <p>
     * Annotation synthesis rules: suppose {@code AnnotatedEle} declares annotations A, B, C in order from top to
     * bottom, with the following meta-annotation hierarchies:
     *
     * <pre>
     *    A -&gt; M1 -&gt; M2
     *    B -&gt; M3 -&gt; M1 -&gt; M2
     *    C -&gt; M2
     * </pre>
     * <p>
     * If {@code annotationType} is {@code M1}, the synthesized annotations derived from root annotations A and B are
     * both returned.
     *
     * @param annotatedEle   The annotated element, such as a class, method, field, or constructor.
     * @param annotationType the annotation type
     * @param <T>            the annotation type
     * @return A list of successfully synthesized annotations, empty if the scanner finds no matching roots.
     * @see SynthesizedAggregateAnnotation
     */
    public static <T extends Annotation> List<T> getAllSynthesizedAnnotations(
            final AnnotatedElement annotatedEle,
            final Class<T> annotationType) {
        return AnnotationScanner.DIRECTLY.getAnnotationsIfSupport(annotatedEle).stream()
                .map(annotation -> getSynthesizedAnnotation(annotationType, annotation)).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Aggregates the given annotation instances into a {@link SynthesizedAggregateAnnotation} without scanning
     * meta-annotations.
     *
     * @param annotations the annotation instances to aggregate
     * @return the aggregate annotation
     */
    public static SynthesizedAggregateAnnotation aggregatingFromAnnotation(final Annotation... annotations) {
        return new GenericSynthesizedAggregateAnnotation(Arrays.asList(annotations), AnnotationScanner.NOTHING);
    }

    /**
     * Aggregates the given annotation instances and their meta-annotations into a
     * {@link SynthesizedAggregateAnnotation}.
     *
     * @param annotations the annotation instances to aggregate
     * @return the aggregate annotation
     */
    public static SynthesizedAggregateAnnotation aggregatingFromAnnotationWithMeta(final Annotation... annotations) {
        return new GenericSynthesizedAggregateAnnotation(Arrays.asList(annotations),
                AnnotationScanner.DIRECTLY_AND_META_ANNOTATION);
    }

    /**
     * Returns whether the given annotation was generated by a proxy class as a synthesized annotation.
     *
     * @param annotation the annotation instance
     * @return {@code true} if the annotation is a synthesized proxy annotation
     * @see SynthesizedAnnotationProxy#isProxyAnnotation(Class)
     */
    public static boolean isSynthesizedAnnotation(final Annotation annotation) {
        return SynthesizedAnnotationProxy.isProxyAnnotation(annotation.getClass());
    }

    /**
     * Clears the declared-annotation, lookup, and synthesized-annotation caches owned by this class. Concurrent queries
     * can populate them again; clearing does not form a global barrier against annotation lookups.
     */
    public static void clearCaches() {
        DECLARED_ANNOTATIONS_CACHE.clear();
        L1_ANNOTATION_CACHE.clear();
        L2_SYNTHESIZED_ANNOTATION_CACHE.clear();
    }

}
