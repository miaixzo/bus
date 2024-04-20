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
package org.miaixz.bus.proxy.intercept;

import org.miaixz.bus.proxy.Interceptor;
import org.miaixz.bus.proxy.Invocation;

import java.io.*;

/**
 * 拦截器，它对所有参数和返回值进行序列化复制。
 * 这在测试远程服务以确保所有参数/返回类型实际上都是可序列化/反序列化的时候非常有用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SerializingInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = serializedCopy(arguments[i]);
        }
        return serializedCopy(invocation.proceed());
    }

    private Object serializedCopy(Object original) {
        try {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(original);
            oout.close();
            bout.close();
            final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            final ObjectInputStream oin = new ObjectInputStream(bin);
            final Object copy = oin.readObject();
            oin.close();
            bin.close();
            return copy;
        } catch (IOException e) {
            throw new RuntimeException("Unable to make serialized copy of " +
                    original.getClass().getName() + " object.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to make serialized copy of " +
                    original.getClass().getName() + " object.", e);
        }
    }

}
