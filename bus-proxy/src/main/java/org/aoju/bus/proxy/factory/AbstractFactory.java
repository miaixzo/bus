/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.proxy.factory;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.toolkit.ClassKit;
import org.aoju.bus.core.toolkit.ReflectKit;
import org.aoju.bus.proxy.Factory;
import org.aoju.bus.proxy.aspects.Aspectj;
import org.aoju.bus.proxy.factory.cglib.CglibFactory;
import org.aoju.bus.proxy.factory.javassist.JavassistFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 超类为{@link Factory}，它支持子类化而不仅仅是实现接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractFactory extends Factory {

    private static boolean hasSuitableDefaultConstructor(Class superclass) {
        final Constructor[] declaredConstructors = superclass.getDeclaredConstructors();
        for (int i = 0; i < declaredConstructors.length; i++) {
            Constructor constructor = declaredConstructors[i];
            if (constructor.getParameterTypes().length == 0 && (Modifier.isPublic(constructor.getModifiers()) ||
                    Modifier.isProtected(constructor.getModifiers()))) {
                return true;
            }
        }
        return false;
    }

    protected static Class[] toInterfaces(Class[] proxyClasses) {
        final Collection interfaces = new LinkedList();
        for (int i = 0; i < proxyClasses.length; i++) {
            Class proxyInterface = proxyClasses[i];
            if (proxyInterface.isInterface()) {
                interfaces.add(proxyInterface);
            }
        }
        return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
    }

    private static Class[] toNonInterfaces(Class[] proxyClasses) {
        final List superclasses = new LinkedList();
        for (int i = 0; i < proxyClasses.length; i++) {
            Class proxyClass = proxyClasses[i];
            if (!proxyClass.isInterface()) {
                superclasses.add(proxyClass);
            }
        }
        return (Class[]) superclasses.toArray(new Class[superclasses.size()]);
    }

    public static Class getSuperclass(Class[] proxyClasses) {
        final Class[] superclasses = toNonInterfaces(proxyClasses);
        switch (superclasses.length) {
            case 0:
                return Object.class;
            case 1:
                final Class superclass = superclasses[0];
                if (Modifier.isFinal(superclass.getModifiers())) {
                    throw new InternalException(
                            "Proxy class cannot extend " + superclass.getName() + " as it is final.");
                }
                if (!hasSuitableDefaultConstructor(superclass)) {
                    throw new InternalException("Proxy class cannot extend " + superclass.getName() +
                            ", because it has no visible \"default\" constructor.");
                }
                return superclass;
            default:
                final StringBuffer errorMessage = new StringBuffer("Proxy class cannot extend ");
                for (int i = 0; i < superclasses.length; i++) {
                    Class c = superclasses[i];
                    errorMessage.append(c.getName());
                    if (i != superclasses.length - 1) {
                        errorMessage.append(", ");
                    }
                }
                errorMessage.append("; multiple inheritance not allowed.");
                throw new InternalException(errorMessage.toString());
        }
    }

    /**
     * 根据用户引入Cglib与否自动创建代理对象
     *
     * @param <T>         切面对象类型
     * @param target      目标对象
     * @param aspectClass 切面对象类
     * @return 代理对象
     */
    public static <T> T createProxy(T target, Class<? extends Aspectj> aspectClass) {
        return createProxy(target, ReflectKit.newInstance(aspectClass));
    }

    /**
     * 根据用户引入Cglib与否自动创建代理对象
     *
     * @param <T>     切面对象类型
     * @param target  被代理对象
     * @param aspectj 切面实现
     * @return 代理对象
     */
    public static <T> T createProxy(T target, Aspectj aspectj) {
        return create().proxy(target, aspectj);
    }

    /**
     * 根据用户引入Cglib与否创建代理工厂
     *
     * @return 代理工厂
     */
    public static AbstractFactory create() {
        try {
            return new CglibFactory();
        } catch (NoClassDefFoundError e) {
            // ignore
        }
        return new JavassistFactory();
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
        return createProxy(target, aspectClass);
    }

    /**
     * 创建动态代理对象
     * 动态代理对象的创建原理是：
     * 假设创建的代理对象名为 $Proxy0
     * 1、根据传入的interfaces动态生成一个类,实现interfaces中的接口
     * 2、通过传入的classloder将刚生成的类加载到jvm中 即将$Proxy0类load
     * 3、调用$Proxy0的$Proxy0(InvocationHandler)构造函数 创建$Proxy0的对象,并且用interfaces参数遍历其所有接口的方法,这些实现方法的实现本质上是通过反射调用被代理对象的方法
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

    public boolean canProxy(Class[] proxyClasses) {
        try {
            getSuperclass(proxyClasses);
            return true;
        } catch (InternalException e) {
            return false;
        }
    }

    /**
     * 创建代理
     *
     * @param <T>     代理对象类型
     * @param target  被代理对象
     * @param aspectj 切面实现
     * @return 代理对象
     */
    public abstract <T> T proxy(T target, Aspectj aspectj);

}

