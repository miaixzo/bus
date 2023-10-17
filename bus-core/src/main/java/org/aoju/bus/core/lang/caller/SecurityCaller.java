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
package org.aoju.bus.core.lang.caller;

import org.aoju.bus.core.toolkit.ArrayKit;

/**
 * 方式获取调用者
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SecurityCaller extends SecurityManager implements Caller {

    private static final int OFFSET = 1;

    @Override
    public Class<?> getCaller() {
        final Class<?>[] context = getClassContext();
        if (null != context && (OFFSET + 1) < context.length) {
            return context[OFFSET + 1];
        }
        return null;
    }

    @Override
    public Class<?> getCallers() {
        final Class<?>[] context = getClassContext();
        if (null != context && (OFFSET + 2) < context.length) {
            return context[OFFSET + 2];
        }
        return null;
    }

    @Override
    public Class<?> getCaller(int depth) {
        final Class<?>[] context = getClassContext();
        if (null != context && (OFFSET + depth) < context.length) {
            return context[OFFSET + depth];
        }
        return null;
    }

    @Override
    public boolean isCalledBy(Class<?> clazz) {
        final Class<?>[] classes = getClassContext();
        if (ArrayKit.isNotEmpty(classes)) {
            for (Class<?> contextClass : classes) {
                if (contextClass.equals(clazz)) {
                    return true;
                }
            }
        }
        return false;
    }

}
