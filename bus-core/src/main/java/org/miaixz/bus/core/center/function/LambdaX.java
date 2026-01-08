/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.function;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.ClassKit;

/**
 * Stores lambda information. This class extends and complements {@link SerializedLambda} information, including:
 * <ul>
 * <li>Instantiated object method parameter types, generally used for method references.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LambdaX {

    /**
     * An empty array of {@link Type} objects.
     */
    private static final Type[] EMPTY_TYPE = new Type[0];
    /**
     * The parameter types of the instantiated method.
     */
    private final Type[] instantiatedMethodParameterTypes;
    /**
     * The parameter types of the method or constructor.
     */
    private final Type[] parameterTypes;
    /**
     * The return type of the method or constructor.
     */
    private final Type returnType;
    /**
     * The name of the method or constructor.
     */
    private final String name;
    /**
     * The executable (method or constructor) object.
     */
    private final Executable executable;
    /**
     * The class where the method or constructor is declared.
     */
    private final Class<?> clazz;
    /**
     * The serialized lambda object.
     */
    private final SerializedLambda lambda;

    /**
     * Constructs a {@code LambdaX} instance.
     *
     * @param executable The constructor object ({@link Constructor}) or method object ({@link Method}).
     * @param lambda     The lambda expression that implements the serializable interface.
     */
    public LambdaX(final Executable executable, final SerializedLambda lambda) {
        Assert.notNull(executable, "executable must be not null!");
        // return type
        final boolean isMethod = executable instanceof Method;
        final boolean isConstructor = executable instanceof Constructor;
        Assert.isTrue(isMethod || isConstructor, "Unsupported executable type: " + executable.getClass());
        this.returnType = isMethod ? ((Method) executable).getGenericReturnType()
                : ((Constructor<?>) executable).getDeclaringClass();

        // lambda info
        this.parameterTypes = executable.getGenericParameterTypes();
        this.name = executable.getName();
        this.clazz = executable.getDeclaringClass();
        this.executable = executable;
        this.lambda = lambda;

        // types
        final String instantiatedMethodType = lambda.getInstantiatedMethodType();
        final int index = instantiatedMethodType.indexOf(";)");
        this.instantiatedMethodParameterTypes = (index > -1)
                ? getInstantiatedMethodParamTypes(instantiatedMethodType.substring(1, index + 1))
                : EMPTY_TYPE;
    }

    /**
     * Parses the actual parameter types based on the method signature information of the lambda object.
     *
     * @param className The class name string from the lambda's instantiated method type.
     * @return An array of {@link Type} objects representing the instantiated method parameter types.
     */
    private static Type[] getInstantiatedMethodParamTypes(final String className) {
        final String[] instantiatedTypeNames = className.split(Symbol.SEMICOLON);
        final Type[] types = new Type[instantiatedTypeNames.length];
        for (int i = 0; i < instantiatedTypeNames.length; i++) {
            final boolean isArray = instantiatedTypeNames[i].startsWith(Symbol.BRACKET_LEFT);
            if (isArray && !instantiatedTypeNames[i].endsWith(Symbol.SEMICOLON)) {
                // If it is an array, it needs to end with ";" to be loaded
                instantiatedTypeNames[i] += Symbol.SEMICOLON;
            } else {
                if (instantiatedTypeNames[i].startsWith("L")) {
                    // If it starts with "L", remove L
                    instantiatedTypeNames[i] = instantiatedTypeNames[i].substring(1);
                }
                if (instantiatedTypeNames[i].endsWith(Symbol.SEMICOLON)) {
                    // If it ends with ";", remove ";"
                    instantiatedTypeNames[i] = instantiatedTypeNames[i]
                            .substring(0, instantiatedTypeNames[i].length() - 1);
                }
            }
            types[i] = ClassKit.loadClass(instantiatedTypeNames[i]);
        }
        return types;
    }

    /**
     * Gets the instantiated method parameter types.
     *
     * @return An array of {@link Type} objects representing the instantiated method parameter types.
     */
    public Type[] getInstantiatedMethodParameterTypes() {
        return instantiatedMethodParameterTypes;
    }

    /**
     * Gets the parameter types of the constructor or method.
     *
     * @return An array of {@link Type} objects representing the parameter types.
     */
    public Type[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Gets the return type (for method references).
     *
     * @return The return type.
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Gets the name of the method or constructor.
     *
     * @return The name of the method or constructor.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the field name, mainly used for method name truncation. The method name must be getXXX, isXXX, or setXXX.
     *
     * @return The field name corresponding to the getter or setter.
     */
    public String getFieldName() {
        return BeanKit.getFieldName(getName());
    }

    /**
     * Gets the method or constructor object.
     *
     * @return The {@link Executable} object (method or constructor).
     */
    public Executable getExecutable() {
        return executable;
    }

    /**
     * Gets the class where the method or constructor is declared.
     *
     * @return The declaring {@link Class}.
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * Gets the serialized lambda expression object.
     *
     * @return The {@link SerializedLambda} object.
     */
    public SerializedLambda getLambda() {
        return lambda;
    }

}
