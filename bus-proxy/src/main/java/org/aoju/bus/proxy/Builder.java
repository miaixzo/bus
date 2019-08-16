/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.proxy;

import org.aoju.bus.proxy.invoker.NullInvoker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.0.6
 * @since JDK 1.8
 */
public class Builder {

    public static final Object[] EMPTY_ARGUMENTS = new Object[0];
    public static final Class[] EMPTY_ARGUMENT_TYPES = new Class[0];
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
        return interfaces == null ? null : (Class[]) interfaces.toArray(new Class[interfaces.size()]);
    }

    private static List getAllInterfacesImpl(Class cls, List list) {
        if (cls == null) {
            return null;
        }
        while (cls != null) {
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
            return getJavaClassName(clazz.getComponentType()) + "[]";
        }
        return clazz.getName();
    }

    public static Class getWrapperClass(Class primitiveType) {
        return (Class) wrapperClassMap.get(primitiveType);
    }

}

