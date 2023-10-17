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
package org.aoju.bus.proxy.invoker;

import org.aoju.bus.proxy.Invoker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个仅为所有方法调用返回null的{@link Invoker}实现
 * 这个类对于需要“空对象”设计模式的场景非常有用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NullInvoker implements Invoker {

    private static Map primitiveValueMap = new HashMap();

    static {
        primitiveValueMap.put(Integer.TYPE, new Integer(0));
        primitiveValueMap.put(Long.TYPE, new Long(0));
        primitiveValueMap.put(Short.TYPE, new Short((short) 0));
        primitiveValueMap.put(Byte.TYPE, new Byte((byte) 0));
        primitiveValueMap.put(Float.TYPE, new Float(0.0f));
        primitiveValueMap.put(Double.TYPE, new Double(0.0));
        primitiveValueMap.put(Character.TYPE, new Character((char) 0));
        primitiveValueMap.put(Boolean.TYPE, Boolean.FALSE);
    }

    public Object invoke(Object proxy, Method method, Object[] args) {
        final Class returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            return primitiveValueMap.get(returnType);
        } else {
            return null;
        }
    }

}

