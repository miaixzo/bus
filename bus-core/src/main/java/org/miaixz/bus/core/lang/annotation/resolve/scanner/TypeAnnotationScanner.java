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
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.xyz.SetKit;

/**
 * Scans annotations on {@link Class} elements, with support for traversing superclass and interface hierarchies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TypeAnnotationScanner extends AbstractTypeAnnotationScanner<TypeAnnotationScanner>
        implements AnnotationScanner {

    /**
     * Constructs a new {@code TypeAnnotationScanner}.
     *
     * @param includeSupperClass is whether to allow scanning superclasses
     * @param includeInterfaces  Whether to allow scanning interfaces
     * @param filter             The filter predicate
     * @param excludeTypes       Types to exclude from scanning
     */
    public TypeAnnotationScanner(final boolean includeSupperClass, final boolean includeInterfaces,
            final Predicate<Class<?>> filter, final Set<Class<?>> excludeTypes) {
        super(includeSupperClass, includeInterfaces, filter, excludeTypes);
    }

    /**
     * Constructs a new {@code TypeAnnotationScanner} that scans the superclass and interfaces by default.
     */
    public TypeAnnotationScanner() {
        this(true, true, t -> true, SetKit.ofLinked());
    }

    /**
     * Returns {@code true} only when the element is a {@link Class}.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return {@code true} if the element is a {@link Class}
     */
    @Override
    public boolean support(final AnnotatedElement annotatedEle) {
        return annotatedEle instanceof Class;
    }

    /**
     * Converts the annotated element to {@link Class}.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return The class to recursively scan
     */
    @Override
    protected Class<?> getClassFormAnnotatedElement(final AnnotatedElement annotatedEle) {
        return (Class<?>) annotatedEle;
    }

    /**
     * Returns the annotations from {@link Class#getAnnotations()}.
     *
     * @param source      The original annotated element
     * @param index       The hierarchy index of the class
     * @param targetClass The class to retrieve annotations from
     * @return The annotations directly declared on the class
     */
    @Override
    protected Annotation[] getAnnotationsFromTargetClass(
            final AnnotatedElement source,
            final int index,
            final Class<?> targetClass) {
        return targetClass.getAnnotations();
    }

    /**
     * Sets whether to include superclass scanning.
     *
     * @param includeSuperClass {@code true} to include superclass
     * @return This scanner instance
     */
    @Override
    public TypeAnnotationScanner setIncludeSuperClass(final boolean includeSuperClass) {
        return super.setIncludeSuperClass(includeSuperClass);
    }

    /**
     * Sets whether to include interface scanning.
     *
     * @param includeInterfaces {@code true} to include interfaces
     * @return This scanner instance
     */
    @Override
    public TypeAnnotationScanner setIncludeInterfaces(final boolean includeInterfaces) {
        return super.setIncludeInterfaces(includeInterfaces);
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
