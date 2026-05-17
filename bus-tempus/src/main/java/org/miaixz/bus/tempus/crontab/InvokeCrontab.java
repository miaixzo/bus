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
package org.miaixz.bus.tempus.crontab;

import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

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
 * @since Java 21+
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
        Logger.debug(true, "Tempus", "Invoke crontab binding started: target={}", classNameWithMethodName);
        int splitIndex = classNameWithMethodName.lastIndexOf(Symbol.C_HASH);
        if (splitIndex <= 0) {
            splitIndex = classNameWithMethodName.lastIndexOf('.');
        }
        if (splitIndex <= 0) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Invoke crontab binding rejected: target={}, reason=invalidTarget",
                    classNameWithMethodName);
            throw new InternalException("Invalid classNameWithMethodName [{}]!", classNameWithMethodName);
        }

        // Load the class
        final String className = classNameWithMethodName.substring(0, splitIndex);
        if (StringKit.isBlank(className)) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Invoke crontab binding rejected: target={}, reason=blankClassName",
                    classNameWithMethodName);
            throw new IllegalArgumentException("Class name is blank !");
        }
        final Class<?> clazz = ClassKit.loadClass(className);
        if (null == clazz) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Invoke crontab binding rejected: className={}, reason=classNotFound",
                    className);
            throw new IllegalArgumentException("Load class with name of [" + className + "] fail !");
        }
        this.object = ReflectKit.newInstanceIfPossible(clazz);

        // Find the method
        final String methodName = classNameWithMethodName.substring(splitIndex + 1);
        if (StringKit.isBlank(methodName)) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Invoke crontab binding rejected: className={}, reason=blankMethodName",
                    className);
            throw new IllegalArgumentException("Method name is blank !");
        }
        this.method = MethodKit.getPublicMethod(clazz, false, methodName);
        if (null == this.method) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Invoke crontab binding rejected: className={}, methodName={}, reason=methodNotFound",
                    className,
                    methodName);
            throw new IllegalArgumentException("No method with name of [" + methodName + "] !");
        }
        Logger.debug(
                false,
                "Tempus",
                "Invoke crontab binding completed: className={}, methodName={}, objectType={}",
                className,
                methodName,
                this.object == null ? null : this.object.getClass().getName());
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
            Logger.debug(
                    true,
                    "Tempus",
                    "Invoke crontab execution started: declaringClass={}, methodName={}",
                    this.method.getDeclaringClass().getName(),
                    this.method.getName());
            MethodKit.invoke(this.object, this.method);
            Logger.debug(
                    false,
                    "Tempus",
                    "Invoke crontab execution completed: declaringClass={}, methodName={}",
                    this.method.getDeclaringClass().getName(),
                    this.method.getName());
        } catch (final InternalException e) {
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Invoke crontab execution failed: declaringClass={}, methodName={}, exception={}",
                    this.method.getDeclaringClass().getName(),
                    this.method.getName(),
                    e.getClass().getSimpleName());
            throw new CrontabException(e.getCause());
        }
    }

}
