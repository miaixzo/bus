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
package org.miaixz.bus.core.xyz;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotationMappingProxy;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotationProxy;
import org.miaixz.bus.core.lang.annotation.resolve.elements.CombinationAnnotatedElement;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Annotation utility class, providing encapsulated functions for quickly obtaining annotation objects, annotation
 * values, etc.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnoKit {

    /**
     * Constructs a new AnnoKit. Utility class constructor for static access.
     */
    private AnnoKit() {
    }

    /**
     * JDK annotation attribute field name.
     */
    private static final String JDK_MEMBER_ATTRIBUTE = "memberValues";
    /**
     * Spring annotation attribute field name.
     */
    private static final String SPRING_MEMBER_ATTRIBUTE = "valueCache";
    /**
     * Bus annotation attribute field name.
     */
    private static final String BUS_MEMBER_ATTRIBUTE = "valueCache";
    /**
     * Spring synthesized annotation handler class name.
     */
    private static final String SPRING_INVOCATION_HANDLER = "SynthesizedMergedAnnotationInvocationHandler";
    /**
     * Cache for directly declared annotations, stored using a weak-reference concurrent map.
     */
    private static final Map<AnnotatedElement, Annotation[]> DECLARED_ANNOTATIONS_CACHE = new WeakConcurrentMap<>();

    /**
     * Retrieves annotations directly declared on the given element. If a cached value exists, it is returned. This
     * method specifically:
     * <ul>
     * <li>Returns only annotations directly declared on the element.</li>
     * <li>Does not include annotations inherited from parent classes or interfaces.</li>
     * <li>Retrieves only annotations defined on the current class, method, field, etc.</li>
     * </ul>
     *
     * @param element The annotated element, which can be a Class, Method, Field, Constructor, etc.
     * @return An array of annotations.
     */
    public static Annotation[] getDeclaredAnnotations(final AnnotatedElement element) {
        return DECLARED_ANNOTATIONS_CACHE.computeIfAbsent(element, AnnotatedElement::getDeclaredAnnotations);
    }

    /**
     * Retrieves all annotations for the specified element. This method specifically:
     * <ul>
     * <li>Returns all annotations on the element.</li>
     * <li>Includes annotations inherited from parent classes or interfaces.</li>
     * <li>Retrieves all annotations from the current element and its inherited elements.</li>
     * </ul>
     *
     * @param annotationEle   The annotated element, which can be a Class, Method, Field, Constructor, etc.
     * @param isToCombination Whether to convert to a combination annotation, which supports recursively obtaining
     *                        annotations of annotations.
     * @return An array of annotations.
     */
    public static Annotation[] getAnnotations(final AnnotatedElement annotationEle, final boolean isToCombination) {
        return getAnnotations(annotationEle, isToCombination, (Predicate<Annotation>) null);
    }

    /**
     * Retrieves combination annotations of a specific type for the given element.
     *
     * @param <T>            The type of the annotation.
     * @param annotationEle  The annotated element.
     * @param annotationType The type of the annotation to retrieve.
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
     * @param annotationEle   The annotated element.
     * @param isToCombination Whether to convert to a combination annotation.
     * @param annotationType  The type of the annotation to retrieve.
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
     * @param predicate       A predicate to filter the annotations.
     * @return An array of annotations.
     */
    public static Annotation[] getAnnotations(
            final AnnotatedElement annotationEle,
            final boolean isToCombination,
            final Predicate<Annotation> predicate) {
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
     * Retrieves a specific type of annotation from the given element.
     *
     * @param <A>            The type of the annotation.
     * @param annotationEle  The annotated element.
     * @param annotationType The type of the annotation to retrieve.
     * @return The annotation object, or {@code null} if not found.
     */
    public static <A extends Annotation> A getAnnotation(
            final AnnotatedElement annotationEle,
            final Class<A> annotationType) {
        return (null == annotationEle) ? null : toCombination(annotationEle).getAnnotation(annotationType);
    }

    /**
     * Checks if the specified element contains a specific annotation, loading the annotation class by its fully
     * qualified name to avoid {@link ClassNotFoundException}.
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
     * Retrieves the default value of a specified annotation (usually the "value" attribute).
     *
     * @param <T>            The type of the annotation value.
     * @param annotationEle  The annotated element.
     * @param annotationType The type of the annotation.
     * @return The default annotation value, or {@code null} if no default value exists.
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
     * @return The attribute value, or {@code null} if the specified attribute does not exist.
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
     * @return The attribute value, or {@code null} if the specified attribute does not exist.
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
     * @return An array of element types, or all {@link ElementType} values if no {@code @Target} annotation is defined.
     */
    public static ElementType[] getTargetType(final Class<? extends Annotation> annotationType) {
        final Target target = annotationType.getAnnotation(Target.class);
        if (null == target) {
            // If @Target meta-annotation is not defined, it means all nodes are supported
            return ElementType.values();
        }
        return target.value();
    }

    /**
     * Checks if an annotation is a meta-annotation.
     *
     * @param annotationType The annotation type.
     * @return {@code true} if it is a meta-annotation, {@code false} otherwise.
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
     * Checks if an annotation class can be inherited.
     *
     * @param annotationType The annotation type.
     * @return {@code true} if it can be inherited, {@code false} otherwise.
     */
    public static boolean isInherited(final Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Inherited.class);
    }

    /**
     * Sets the value of an annotation attribute.
     * <p>
     * Note: This may throw an exception in JDK 9 and above. The JVM argument
     * {@code --add-opens=java.base/java.lang=ALL-UNNAMED} may be required.
     *
     * @param annotation      The annotation object.
     * @param annotationField The name of the attribute.
     * @param value           The value to set for the attribute.
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
     * Determines if a method is an annotation attribute method.
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
     * <li>Has a return value (not void)</li>
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
     * Clears annotation-related caches.
     */
    public static void clearCaches() {
        DECLARED_ANNOTATIONS_CACHE.clear();
    }

}
