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
package org.miaixz.bus.cron.crontab;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.lang.reflect.Method;

/**
 * A {@link Crontab} implementation that executes a method on a specified class using reflection.
 * <p>
 * The target method is specified by a string in the format {@code com.example.MyClass#myMethod} or
 * {@code com.example.MyClass.myMethod}.
 * </p>
 * <p>
 * If the target method is static, it is invoked directly. If it is an instance method, a new instance of the class is
 * created using its default (no-argument) constructor.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InvokeCrontab implements Crontab {

    /**
     * The target object on which to invoke the method. For static methods, this is {@code null}.
     */
    private final Object object;
    /**
     * The method to be invoked.
     */
    private final Method method;

    /**
     * Constructs a new InvokeCrontab.
     *
     * @param classNameWithMethodName The fully qualified class name and method name, separated by '#' or '.' (e.g.,
     *                                "com.example.MyClass#myMethod").
     */
    public InvokeCrontab(final String classNameWithMethodName) {
        int splitIndex = classNameWithMethodName.lastIndexOf(Symbol.C_HASH);
        if (splitIndex <= 0) {
            splitIndex = classNameWithMethodName.lastIndexOf('.');
        }
        if (splitIndex <= 0) {
            throw new InternalException("Invalid classNameWithMethodName [{}]!", classNameWithMethodName);
        }

        // Load the class
        final String className = classNameWithMethodName.substring(0, splitIndex);
        if (StringKit.isBlank(className)) {
            throw new IllegalArgumentException("Class name is blank !");
        }
        final Class<?> clazz = ClassKit.loadClass(className);
        if (null == clazz) {
            throw new IllegalArgumentException("Load class with name of [" + className + "] fail !");
        }
        this.object = ReflectKit.newInstanceIfPossible(clazz);

        // Find the method
        final String methodName = classNameWithMethodName.substring(splitIndex + 1);
        if (StringKit.isBlank(methodName)) {
            throw new IllegalArgumentException("Method name is blank !");
        }
        this.method = MethodKit.getPublicMethod(clazz, false, methodName);
        if (null == this.method) {
            throw new IllegalArgumentException("No method with name of [" + methodName + "] !");
        }
    }

    /**
     * Executes the target method using reflection.
     * <p>
     * If the method is static, it is invoked directly on the class. If it is an instance method, it is invoked on the
     * target object.
     * </p>
     *
     * @throws CrontabException if the method invocation fails.
     */
    @Override
    public void execute() {
        try {
            MethodKit.invoke(this.object, this.method);
        } catch (final InternalException e) {
            throw new CrontabException(e.getCause());
        }
    }

}
