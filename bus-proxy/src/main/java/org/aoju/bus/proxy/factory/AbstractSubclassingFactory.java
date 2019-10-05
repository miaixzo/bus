/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.proxy.factory;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.proxy.Factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 3.6.3
 * @since JDK 1.8
 */
public abstract class AbstractSubclassingFactory extends Factory {

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
                    throw new InstrumentException(
                            "Proxy class cannot extend " + superclass.getName() + " as it is final.");
                }
                if (!hasSuitableDefaultConstructor(superclass)) {
                    throw new InstrumentException("Proxy class cannot extend " + superclass.getName() +
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
                throw new InstrumentException(errorMessage.toString());
        }
    }

    public boolean canProxy(Class[] proxyClasses) {
        try {
            getSuperclass(proxyClasses);
            return true;
        } catch (InstrumentException e) {
            return false;
        }
    }

}

