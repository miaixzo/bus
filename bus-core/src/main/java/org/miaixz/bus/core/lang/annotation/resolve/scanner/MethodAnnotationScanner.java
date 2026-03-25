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
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.*;

/**
 * Scans annotations on {@link Method} elements, with support for traversing class hierarchies to find methods with the
 * same signature.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MethodAnnotationScanner extends AbstractTypeAnnotationScanner<MethodAnnotationScanner>
        implements AnnotationScanner {

    /**
     * Constructs a new {@code MethodAnnotationScanner} that only scans annotations directly declared on the method.
     */
    public MethodAnnotationScanner() {
        this(false);
    }

    /**
     * Constructs a new {@code MethodAnnotationScanner}.
     *
     * @param scanSameSignatureMethod Whether to scan methods with the same signature in the class hierarchy
     */
    public MethodAnnotationScanner(final boolean scanSameSignatureMethod) {
        this(scanSameSignatureMethod, targetClass -> true, SetKit.ofLinked());
    }

    /**
     * Constructs a new {@code MethodAnnotationScanner}.
     *
     * @param scanSameSignatureMethod Whether to scan methods with the same signature in the class hierarchy
     * @param filter                  The filter predicate
     * @param excludeTypes            Types to exclude from scanning
     */
    public MethodAnnotationScanner(final boolean scanSameSignatureMethod, final Predicate<Class<?>> filter,
            final Set<Class<?>> excludeTypes) {
        super(scanSameSignatureMethod, scanSameSignatureMethod, filter, excludeTypes);
    }

    /**
     * Constructs a new {@code MethodAnnotationScanner}.
     *
     * @param includeSuperClass Whether to scan methods with the same signature in superclasses
     * @param includeInterfaces Whether to scan methods with the same signature in interfaces
     * @param filter            The filter predicate
     * @param excludeTypes      Types to exclude from scanning
     */
    public MethodAnnotationScanner(final boolean includeSuperClass, final boolean includeInterfaces,
            final Predicate<Class<?>> filter, final Set<Class<?>> excludeTypes) {
        super(includeSuperClass, includeInterfaces, filter, excludeTypes);
    }

    /**
     * Returns {@code true} only when the annotated element is a {@link Method}.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return {@code true} if the element is a {@link Method}
     */
    @Override
    public boolean support(final AnnotatedElement annotatedEle) {
        return annotatedEle instanceof Method;
    }

    /**
     * Returns the class that declares the annotated method.
     *
     * @param annotatedElement The annotated element (a Method)
     * @return The class type to recursively scan
     * @see Method#getDeclaringClass()
     */
    @Override
    protected Class<?> getClassFormAnnotatedElement(final AnnotatedElement annotatedElement) {
        return ((Method) annotatedElement).getDeclaringClass();
    }

    /**
     * Returns annotations on methods in the class hierarchy that have the same signature as the source method.
     *
     * @param source      The original annotated element (a Method)
     * @param index       The hierarchy index of the class
     * @param targetClass The class to search for matching methods
     * @return The annotations from matching methods
     */
    @Override
    protected Annotation[] getAnnotationsFromTargetClass(
            final AnnotatedElement source,
            final int index,
            final Class<?> targetClass) {
        final Method sourceMethod = (Method) source;
        return Stream.of(MethodKit.getDeclaredMethods(targetClass)).filter(superMethod -> !superMethod.isBridge())
                .filter(superMethod -> hasSameSignature(sourceMethod, superMethod))
                .map(AnnotatedElement::getAnnotations).flatMap(Stream::of).toArray(Annotation[]::new);
    }

    /**
     * Sets whether to scan methods with the same signature in the class hierarchy.
     *
     * @param scanSuperMethodIfOverride {@code true} to scan same-signature methods
     * @return This scanner instance
     */
    public MethodAnnotationScanner setScanSameSignatureMethod(final boolean scanSuperMethodIfOverride) {
        setIncludeInterfaces(scanSuperMethodIfOverride);
        setIncludeSuperClass(scanSuperMethodIfOverride);
        return this;
    }

    /**
     * Returns whether the given method has the same signature as the source method. Signature equality requires the
     * same name, same parameter types, and a compatible return type.
     *
     * @param sourceMethod the source method to compare against
     * @param superMethod  the candidate method to check
     * @return {@code true} if both methods have the same signature
     */
    private boolean hasSameSignature(final Method sourceMethod, final Method superMethod) {
        if (!StringKit.equals(sourceMethod.getName(), superMethod.getName())) {
            return false;
        }
        final Class<?>[] sourceParameterTypes = sourceMethod.getParameterTypes();
        final Class<?>[] targetParameterTypes = superMethod.getParameterTypes();
        if (sourceParameterTypes.length != targetParameterTypes.length) {
            return false;
        }
        if (!ArrayKit.containsAll(sourceParameterTypes, targetParameterTypes)) {
            return false;
        }
        return ClassKit.isAssignable(superMethod.getReturnType(), sourceMethod.getReturnType());
    }

}
