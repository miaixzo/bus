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

import javassist.*;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.proxy.Builder;

import java.util.HashSet;
import java.util.Set;

/**
 * 处理Javassist的实用方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class JavassistKit {

    public static final String DEFAULT_BASE_NAME = "JavassistKit";
    private static final ClassPool classPool = new ClassPool();
    private static final Set classLoaders = new HashSet();
    private static int classNumber = 0;

    static {
        classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
    }

    public static void addField(Class fieldType, String fieldName, CtClass enclosingClass)
            throws CannotCompileException {
        enclosingClass.addField(new CtField(resolve(fieldType), fieldName, enclosingClass));
    }

    public static CtClass resolve(Class clazz) {
        synchronized (classLoaders) {
            try {
                final ClassLoader loader = clazz.getClassLoader();
                if (null != loader && !classLoaders.contains(loader)) {
                    classLoaders.add(loader);
                    classPool.appendClassPath(new LoaderClassPath(loader));
                }
                return classPool.get(Builder.getJavaClassName(clazz));
            } catch (NotFoundException e) {
                throw new InternalException(
                        "Unable to find class " + clazz.getName() + " in default Javassist class pool.", e);
            }
        }
    }

    public static void addInterfaces(CtClass ctClass, Class[] proxyClasses) {
        for (int i = 0; i < proxyClasses.length; i++) {
            Class proxyInterface = proxyClasses[i];
            ctClass.addInterface(resolve(proxyInterface));
        }
    }

    public static CtClass createClass(Class superclass) {
        return createClass(DEFAULT_BASE_NAME, superclass);
    }

    public synchronized static CtClass createClass(String baseName, Class superclass) {
        return classPool.makeClass(baseName + Symbol.UNDERLINE + classNumber++, resolve(superclass));
    }

    public static CtClass[] resolve(Class[] classes) {
        final CtClass[] ctClasses = new CtClass[classes.length];
        for (int i = 0; i < ctClasses.length; ++i) {
            ctClasses[i] = resolve(classes[i]);
        }
        return ctClasses;
    }

}

