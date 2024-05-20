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
package org.miaixz.bus.proxy;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.proxy.aspects.Aspectj;
import org.miaixz.bus.proxy.factory.AbstractFactory;
import org.miaixz.bus.proxy.invoker.NullInvoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    public static final Object[] EMPTY_ARGUMENTS = Normal.EMPTY_OBJECT_ARRAY;
    public static final Class[] EMPTY_ARGUMENT_TYPES = Normal.EMPTY_CLASS_ARRAY;
    private static final Map wrapperClassMap = new HashMap();

    static {
        wrapperClassMap.put(Integer.TYPE, Integer.class);
        wrapperClassMap.put(Character.TYPE, Character.class);
        wrapperClassMap.put(Boolean.TYPE, Boolean.class);
        wrapperClassMap.put(Short.TYPE, Short.class);
        wrapperClassMap.put(Long.TYPE, Long.class);
        wrapperClassMap.put(Float.TYPE, Float.class);
        wrapperClassMap.put(Double.TYPE, Double.class);
        wrapperClassMap.put(Byte.TYPE, Byte.class);
    }

    public static Object createNullObject(Factory factory, Class[] proxyClasses) {
        return factory.createInvokerProxy(new NullInvoker(), proxyClasses);
    }

    public static Object createNullObject(Factory factory, ClassLoader classLoader, Class[] proxyClasses) {
        return factory.createInvokerProxy(classLoader, new NullInvoker(), proxyClasses);
    }

    public static Class[] getAllInterfaces(Class cls) {
        final List interfaces = getAllInterfacesImpl(cls, new LinkedList());
        return null == interfaces ? null : (Class[]) interfaces.toArray(new Class[interfaces.size()]);
    }

    private static List getAllInterfacesImpl(Class cls, List list) {
        if (null == cls) {
            return null;
        }
        while (null != cls) {
            Class[] interfaces = cls.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (!list.contains(interfaces[i])) {
                    list.add(interfaces[i]);
                }
                getAllInterfacesImpl(interfaces[i], list);
            }
            cls = cls.getSuperclass();
        }
        return list;
    }

    public static String getJavaClassName(Class clazz) {
        if (clazz.isArray()) {
            return getJavaClassName(clazz.getComponentType()) + Symbol.BRACKET;
        }
        return clazz.getName();
    }

    public static Class getWrapperClass(Class primitiveType) {
        return (Class) wrapperClassMap.get(primitiveType);
    }


    /**
     * 使用切面代理对象
     *
     * @param <T>         切面对象类型
     * @param target      目标对象
     * @param aspectClass 切面对象类
     * @return 代理对象
     */
    public static <T> T proxy(T target, Class<? extends Aspectj> aspectClass) {
        return AbstractFactory.createProxy(target, aspectClass);
    }

    /**
     * 使用切面代理对象
     *
     * @param <T>     被代理对象类型
     * @param target  被代理对象
     * @param aspectj 切面对象
     * @return 代理对象
     */
    public static <T> T proxy(T target, Aspectj aspectj) {
        return AbstractFactory.createProxy(target, aspectj);
    }

    /**
     * 创建动态代理对象
     * 动态代理对象的创建原理是：
     * 假设创建的代理对象名为 $Proxy0
     * 1、根据传入的interfaces动态生成一个类,实现interfaces中的接口
     * 2、通过传入的classloder将刚生成的类加载到jvm中 即将$Proxy0类load
     * 3、调用$Proxy0的$Proxy0(InvocationHandler)构造函数 创建$Proxy0的对象,
     * 并且用interfaces参数遍历其所有接口的方法,这些实现方法的实现本质上是通过反射调用被代理对象的方法
     * 4、将$Proxy0的实例返回给客户端
     * 5、当调用代理类的相应方法时,相当于调用 {@link InvocationHandler#invoke(Object, java.lang.reflect.Method, Object[])} 方法
     *
     * @param <T>               被代理对象类型
     * @param classloader       被代理类对应的ClassLoader
     * @param invocationHandler {@link InvocationHandler} ,被代理类通过实现此接口提供动态代理功能
     * @param interfaces        代理类中需要实现的被代理类的接口方法
     * @return 代理类
     */
    public static <T> T newProxyInstance(ClassLoader classloader, InvocationHandler invocationHandler, Class<?>... interfaces) {
        return (T) Proxy.newProxyInstance(classloader, interfaces, invocationHandler);
    }

    /**
     * 创建动态代理对象
     *
     * @param <T>               被代理对象类型
     * @param invocationHandler {@link InvocationHandler} ,被代理类通过实现此接口提供动态代理功能
     * @param interfaces        代理类中需要实现的被代理类的接口方法
     * @return 代理类
     */
    public static <T> T newProxyInstance(InvocationHandler invocationHandler, Class<?>... interfaces) {
        return newProxyInstance(ClassKit.getClassLoader(), invocationHandler, interfaces);
    }

}

