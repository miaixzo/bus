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
package org.miaixz.bus.starter.aot;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.ReflectionHints;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * A Spring AOT (Ahead-Of-Time) {@link org.springframework.aot.hint.annotation.ReflectiveProcessor}.
 *
 * <p>
 * This processor is designed to be used with a custom annotation that is meta-annotated with
 * {@code @Reflective(ReflectiveProcessor.class)}.
 * </p>
 * <p>
 * When the AOT engine encounters an element marked with such an annotation, it invokes the
 * {@link #registerReflectionHints(ReflectionHints, AnnotatedElement)} method. This specific implementation checks if
 * the annotated element is a {@link Method} and registers it for reflective invocation ({@link ExecutableMode#INVOKE}).
 * This is crucial for ensuring that methods intended for dynamic invocation function correctly in a GraalVM native
 * image.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReflectiveProcessor implements org.springframework.aot.hint.annotation.ReflectiveProcessor {

    /**
     * Registers reflection hints for the annotated element.
     * <p>
     * This method is called by the Spring AOT framework during context processing. It checks if the provided
     * {@link AnnotatedElement} is an instance of {@link Method}. If it is, the method is registered with the
     * {@link ReflectionHints} using {@link ExecutableMode#INVOKE} to make it reflectively callable in the native image.
     * </p>
     * 
     * @param hints   the {@link ReflectionHints} instance to register hints against
     * @param element the annotated element (expected to be a {@link Method} in this implementation)
     */
    @Override
    public void registerReflectionHints(ReflectionHints hints, AnnotatedElement element) {
        if (element instanceof Method method) {
            // If the annotated element is a method, register it for invocation
            hints.registerMethod(method, ExecutableMode.INVOKE);
        }
    }

}
