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
package org.aoju.bus.core.lang.caller;

import org.aoju.bus.core.lang.exception.CommonException;

/**
 * 通过StackTrace方式获取调用者。此方式效率最低，不推荐使用
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public class StackTraceCaller implements Caller {

    private static final int OFFSET = 2;

    @Override
    public Class<?> getCaller() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (null == stackTrace || (OFFSET + 1) >= stackTrace.length) {
            return null;
        }
        final String className = stackTrace[OFFSET + 1].getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new CommonException(className + " not found!");
        }
    }

    @Override
    public Class<?> getCallers() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (null == stackTrace || (OFFSET + 2) >= stackTrace.length) {
            return null;
        }
        final String className = stackTrace[OFFSET + 2].getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new CommonException(className + "not found!");
        }
    }

    @Override
    public Class<?> getCaller(int depth) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (null == stackTrace || (OFFSET + depth) >= stackTrace.length) {
            return null;
        }
        final String className = stackTrace[OFFSET + depth].getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new CommonException(className + " not found!");
        }
    }

    @Override
    public boolean isCalledBy(Class<?> clazz) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (null != stackTrace) {
            for (final StackTraceElement element : stackTrace) {
                if (element.getClassName().equals(clazz.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

}
