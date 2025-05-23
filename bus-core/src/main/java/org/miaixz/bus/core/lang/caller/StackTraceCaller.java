/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.caller;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * 通过StackTrace方式获取调用者。此方式效率最低，不推荐使用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StackTraceCaller implements Caller, Serializable {

    @Serial
    private static final long serialVersionUID = 2852299505765L;

    private static final int OFFSET = 2;

    @Override
    public Class<?> getCaller() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (OFFSET + 1 >= stackTrace.length) {
            return null;
        }
        final String className = stackTrace[OFFSET + 1].getClassName();
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new InternalException(e, "[{}] not found!", className);
        }
    }

    @Override
    public Class<?> getCallers() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (OFFSET + 2 >= stackTrace.length) {
            return null;
        }
        final String className = stackTrace[OFFSET + 2].getClassName();
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new InternalException(e, "[{}] not found!", className);
        }
    }

    @Override
    public Class<?> getCaller(final int depth) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (OFFSET + depth >= stackTrace.length) {
            return null;
        }
        final String className = stackTrace[OFFSET + depth].getClassName();
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new InternalException(e, "[{}] not found!", className);
        }
    }

    @Override
    public boolean isCalledBy(final Class<?> clazz) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (final StackTraceElement element : stackTrace) {
            if (element.getClassName().equals(clazz.getName())) {
                return true;
            }
        }
        return false;
    }

}
