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
package org.aoju.bus.core.utils;

import org.aoju.bus.core.lang.caller.Caller;
import org.aoju.bus.core.lang.caller.SecurityManagerCaller;
import org.aoju.bus.core.lang.caller.StackTraceCaller;

/**
 * 调用者。可以通过此类的方法获取调用者、多级调用者以及判断是否被调用
 *
 * @author Kimi Liu
 * @version 3.1.0
 * @since JDK 1.8
 */
public class CallerUtils {

    private static final Caller INSTANCE;

    static {
        INSTANCE = tryCreateCaller();
    }

    /**
     * 获得调用者
     *
     * @return 调用者
     */
    public static Class<?> getCaller() {
        return INSTANCE.getCaller();
    }

    /**
     * 获得调用者的调用者
     *
     * @return 调用者的调用者
     */
    public static Class<?> getCallers() {
        return INSTANCE.getCallers();
    }

    /**
     * 获得调用者，指定第几级调用者
     * 调用者层级关系：
     *
     * <pre>
     * 0 {@link CallerUtils}
     * 1 调用{@link CallerUtils}中方法的类
     * 2 调用者的调用者
     * ...
     * </pre>
     *
     * @param depth 层级。0表示{@link CallerUtils}本身，1表示调用{@link CallerUtils}的类，2表示调用者的调用者，依次类推
     * @return 第几级调用者
     */
    public static Class<?> getCaller(int depth) {
        return INSTANCE.getCaller(depth);
    }

    /**
     * 是否被指定类调用
     *
     * @param clazz 调用者类
     * @return 是否被调用
     */
    public static boolean isCalledBy(Class<?> clazz) {
        return INSTANCE.isCalledBy(clazz);
    }

    /**
     * 尝试创建{@link Caller}实现
     *
     * @return {@link Caller}实现
     */
    private static Caller tryCreateCaller() {
        try {
            return new SecurityManagerCaller();
        } catch (Throwable e) {
        }
        return new StackTraceCaller();
    }

}
