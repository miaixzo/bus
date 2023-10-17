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
package org.aoju.bus.proxy.intercept;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.proxy.Builder;
import org.aoju.bus.proxy.Interceptor;
import org.aoju.bus.proxy.Invocation;

/**
 * 记录每个方法调用的拦截器。
 * 注意:这个类的实现是从HiveMind的日志拦截器中借来的
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LoggingInterceptor implements Interceptor {

    private static final int BUFFER_SIZE = 100;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final String methodName = invocation.getMethod().getName();
        entry(methodName, invocation.getArguments());
        try {
            Object result = invocation.proceed();
            if (Void.TYPE.equals(invocation.getMethod().getReturnType())) {
                voidExit(methodName);
            } else {
                exit(methodName, result);
            }
            return result;
        } catch (Throwable t) {
            exception(methodName, t);
            throw t;
        }
    }

    private void entry(String methodName, Object[] args) {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("BEGIN ");
        buffer.append(methodName);
        buffer.append(Symbol.PARENTHESE_LEFT);
        int count = args.length;
        for (int i = 0; i < count; i++) {
            Object arg = args[i];
            if (i > 0) {
                buffer.append(", ");
            }
            convert(buffer, arg);
        }
        buffer.append(Symbol.PARENTHESE_RIGHT);
        Logger.debug(buffer.toString());
    }

    private void convert(StringBuffer buffer, Object input) {
        if (null == input) {
            buffer.append("<null>");
            return;
        }

        if (!(input instanceof Object[])) {
            buffer.append(input.toString());
            return;
        }
        buffer.append(Symbol.PARENTHESE_LEFT);
        buffer.append(Builder.getJavaClassName(input.getClass()));
        buffer.append("){");
        Object[] array = (Object[]) input;
        int count = array.length;
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            convert(buffer, array[i]);
        }
        buffer.append(Symbol.BRACE_RIGHT);
    }

    private void exception(String methodName, Throwable t) {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("EXCEPTION ");
        buffer.append(methodName);
        buffer.append("() -- ");
        buffer.append(t.getClass().getName());
        Logger.debug(buffer.toString(), t);
    }

    private void exit(String methodName, Object result) {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("END ");
        buffer.append(methodName);
        buffer.append("() [");
        convert(buffer, result);
        buffer.append(Symbol.BRACKET_RIGHT);
        Logger.debug(buffer.toString());
    }

    private void voidExit(String methodName) {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("END ");
        buffer.append(methodName);
        buffer.append("()");
        Logger.debug(buffer.toString());
    }

}

