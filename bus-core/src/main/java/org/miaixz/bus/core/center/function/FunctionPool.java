/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.function;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.xyz.ReflectKit;

import java.lang.reflect.Constructor;
import java.util.function.BiFunction;

/**
 * 常用Lambda函数封装 提供常用对象方法的Lambda包装，减少Lambda初始化时间。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionPool {

    /**
     * 通过{@code String(char[] value, boolean share)}这个内部构造生成一个Lambda函数
     * 此函数通过传入char[]，实现zero-copy的String创建，效率很高。但是要求传入的char[]不可以在其他地方修改。 此函数只支持JKDK8
     */
    public static final BiFunction<char[], Boolean, String> STRING_CREATOR_JDK8;

    static {
        final Constructor<String> constructor = ReflectKit.getConstructor(String.class, char[].class, boolean.class);
        STRING_CREATOR_JDK8 = Keys.IS_JDK8 ? LambdaFactory.build(BiFunction.class, constructor) : null;
    }

    /**
     * 通过{@code String(char[] value, boolean share)}这个内部构造创建String对象。
     * 此函数通过传入char[]，实现zero-copy的String创建，效率很高。但是要求传入的char[]不可以在其他地方修改。
     *
     * @param value char[]值，注意这个数组不可修改！！
     * @return String
     */
    public static String createString(final char[] value) {
        if (Keys.IS_JDK8) {
            return STRING_CREATOR_JDK8.apply(value, true);
        } else {
            return new String(value);
        }
    }

}
