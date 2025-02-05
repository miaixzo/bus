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
package org.miaixz.bus.cache.support;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArgNameGenerator {

    private static final String X_ARGS_PREFIX = "args";
    private static final ConcurrentMap<Method, String[]> methodParameterNames = new ConcurrentHashMap<>();
    private static String[] X_ARGS = { X_ARGS_PREFIX + 0, X_ARGS_PREFIX + 1, X_ARGS_PREFIX + 2, X_ARGS_PREFIX + 3,
            X_ARGS_PREFIX + 4, X_ARGS_PREFIX + 5, X_ARGS_PREFIX + 6, X_ARGS_PREFIX + 7, X_ARGS_PREFIX + 8,
            X_ARGS_PREFIX + 9, X_ARGS_PREFIX + 10, X_ARGS_PREFIX + 11, X_ARGS_PREFIX + 12, X_ARGS_PREFIX + 13,
            X_ARGS_PREFIX + 14, X_ARGS_PREFIX + 15, X_ARGS_PREFIX + Normal._16, X_ARGS_PREFIX + 17, X_ARGS_PREFIX + 18,
            X_ARGS_PREFIX + 19 };
    private static boolean isFirst = true;

    public static String[] getArgNames(Method method) {
        return methodParameterNames.computeIfAbsent(method, ArgNameGenerator::doGetArgNamesWithJava8);
    }

    // 由于编译参数–parameters的影响, 开启了该参数, 获取到的参数名为真是的方法参数Name; 没有开启: 则是获取到argN这种.
    // 为了方便用户, 我们统一生成xArgN这种方式来填充, 同时也兼容原先的这种生成方式¬
    public static String[] getXArgNames(int valueSize) {
        if (valueSize == 0) {
            return Normal.EMPTY_STRING_ARRAY;
        }

        String[] xArgs = new String[valueSize];
        for (int i = 0; i < valueSize; ++i) {
            xArgs[i] = i < X_ARGS.length ? X_ARGS[i] : X_ARGS_PREFIX + i;
        }

        return xArgs;
    }

    // Java 8 之后提供了获取参数名方法, 但需要编译时添加`–parameters`参数支持, 如`javac –parameters`, 不然参数名为'arg0'
    private static String[] doGetArgNamesWithJava8(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] argNames = Arrays.stream(parameters).map(Parameter::getName).toArray(String[]::new);
        if (isFirst && argNames.length != 0 && argNames[0].equals("arg0")) {
            Logger.warn("compile not set '–parameters', used default method parameter names");
            isFirst = false;
        }

        return argNames;
    }

}
