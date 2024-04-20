/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.proxy.factory.javassist;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.proxy.Builder;
import org.miaixz.bus.proxy.Invocation;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 这个类实际上是所有基于Javassist方法调用的超类
 * 动态创建子类来处理特定的接口方法(它们是硬连接的)
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class JavassistInvocation implements Invocation {

    private static WeakHashMap loaderToClassCache = new WeakHashMap();
    protected final Method method;
    protected final Object target;
    protected final Object[] arguments;

    public JavassistInvocation(Method method, Object target, Object[] arguments) {
        this.method = method;
        this.target = target;
        this.arguments = arguments;
    }

    private static String createCastExpression(Class type, String objectToCast) {
        if (!type.isPrimitive()) {
            return "( " + Builder.getJavaClassName(type) + " )" + objectToCast;
        } else {
            return "( ( " + Builder.getWrapperClass(type).getName() + " )" + objectToCast + " )." +
                    type.getName() + "Value()";
        }
    }

    private static Class createInvocationClass(ClassLoader classLoader, Method interfaceMethod)
            throws CannotCompileException {
        Class invocationClass;
        final CtClass ctClass = JavassistKit.createClass(
                getSimpleName(interfaceMethod.getDeclaringClass()) + Symbol.UNDERLINE + interfaceMethod.getName() +
                        "_invocation",
                JavassistInvocation.class);
        final CtConstructor constructor = new CtConstructor(
                JavassistKit.resolve(new Class[]{Method.class, Object.class, Object[].class}),
                ctClass);
        constructor.setBody("{\n\tsuper($$);\n}");
        ctClass.addConstructor(constructor);
        final CtMethod proceedMethod = new CtMethod(JavassistKit.resolve(Object.class), "proceed",
                JavassistKit.resolve(Normal.EMPTY_CLASS_ARRAY), ctClass);
        final Class[] argumentTypes = interfaceMethod.getParameterTypes();
        final StringBuffer proceedBody = new StringBuffer("{\n");
        if (!Void.TYPE.equals(interfaceMethod.getReturnType())) {
            proceedBody.append("\treturn ");
            if (interfaceMethod.getReturnType().isPrimitive()) {
                proceedBody.append("new ");
                proceedBody.append(Builder.getWrapperClass(interfaceMethod.getReturnType()).getName());
                proceedBody.append("( ");
            }
        } else {
            proceedBody.append(Symbol.HT);
        }
        proceedBody.append("( (");
        proceedBody.append(Builder.getJavaClassName(interfaceMethod.getDeclaringClass()));
        proceedBody.append(" )target ).");
        proceedBody.append(interfaceMethod.getName());
        proceedBody.append(Symbol.PARENTHESE_LEFT);
        for (int i = 0; i < argumentTypes.length; ++i) {
            final Class argumentType = argumentTypes[i];
            proceedBody.append(createCastExpression(argumentType, "arguments[" + i + "]"));
            if (i != argumentTypes.length - 1) {
                proceedBody.append(", ");
            }
        }
        if (!Void.TYPE.equals(interfaceMethod.getReturnType()) && interfaceMethod.getReturnType().isPrimitive()) {
            proceedBody.append(") );\n");
        } else {
            proceedBody.append(");\n");
        }
        if (Void.TYPE.equals(interfaceMethod.getReturnType())) {
            proceedBody.append("\treturn null;\n");
        }
        proceedBody.append(Symbol.BRACE_RIGHT);
        final String body = proceedBody.toString();
        proceedMethod.setBody(body);
        ctClass.addMethod(proceedMethod);
        invocationClass = ctClass.toClass(classLoader, null);
        return invocationClass;
    }

    private static Map getClassCache(ClassLoader classLoader) {
        Map cache = (Map) loaderToClassCache.get(classLoader);
        if (null == cache) {
            cache = new HashMap();
            loaderToClassCache.put(classLoader, cache);
        }
        return cache;
    }

    synchronized static Class getMethodInvocationClass(ClassLoader classLoader,
                                                       Method interfaceMethod)
            throws CannotCompileException {
        final Map classCache = getClassCache(classLoader);
        final String key = toClassCacheKey(interfaceMethod);
        final WeakReference invocationClassRef = (WeakReference) classCache.get(key);
        Class invocationClass;
        if (null == invocationClassRef) {
            invocationClass = createInvocationClass(classLoader, interfaceMethod);
            classCache.put(key, new WeakReference(invocationClass));
        } else {
            synchronized (invocationClassRef) {
                invocationClass = (Class) invocationClassRef.get();
                if (null == invocationClass) {
                    invocationClass = createInvocationClass(classLoader, interfaceMethod);
                    classCache.put(key, new WeakReference(invocationClass));
                }
            }
        }
        return invocationClass;
    }

    private static String getSimpleName(Class c) {
        final String name = c.getName();
        final int ndx = name.lastIndexOf(Symbol.C_DOT);
        return ndx == -1 ? name : name.substring(ndx + 1);
    }

    private static String toClassCacheKey(Method method) {
        return String.valueOf(method);
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Method getMethod() {
        return method;
    }

    public Object getProxy() {
        return target;
    }

}

