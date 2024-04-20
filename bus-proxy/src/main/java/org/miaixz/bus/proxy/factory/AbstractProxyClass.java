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
package org.miaixz.bus.proxy.factory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 用于{@link ProxyClass}实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProxyClass implements ProxyClass {

    public static Method[] getImplementationMethods(Class[] proxyClasses) {
        final Map signatureMethodMap = new HashMap();
        final Set finalizedSignatures = new HashSet();
        for (int i = 0; i < proxyClasses.length; i++) {
            Class proxyInterface = proxyClasses[i];
            final Method[] methods = proxyInterface.getMethods();
            for (int j = 0; j < methods.length; j++) {
                final MethodSignature signature = new MethodSignature(methods[j]);
                if (Modifier.isFinal(methods[j].getModifiers())) {
                    finalizedSignatures.add(signature);
                } else if (!signatureMethodMap.containsKey(signature)) {
                    signatureMethodMap.put(signature, methods[j]);
                }
            }
        }
        final Collection resultingMethods = signatureMethodMap.values();
        for (Iterator i = finalizedSignatures.iterator(); i.hasNext(); ) {
            MethodSignature signature = (MethodSignature) i.next();
            resultingMethods.remove(signatureMethodMap.get(signature));
        }
        return (Method[]) resultingMethods.toArray(new Method[resultingMethods.size()]);
    }

}

