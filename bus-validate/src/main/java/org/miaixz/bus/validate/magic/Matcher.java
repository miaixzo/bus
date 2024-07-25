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
package org.miaixz.bus.validate.magic;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumMap;
import org.miaixz.bus.validate.Context;

/**
 * 校验器接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Matcher<T, K> extends Provider {

    /**
     * 将Validator转为Matcher
     *
     * @param validator Validator对象
     * @param <T>       Validator泛型
     * @return ComplexValidator对象
     */
    static <T> Matcher<T, ?> of(Validator<T> validator) {
        return (object, annotation, context) -> validator.on(object, context);
    }

    /**
     * 根据校验器,创建相对立的一个校验器
     *
     * @param matcher 校验器
     * @param <T>     校验对象泛型
     * @param <K>     校验器注解泛型
     * @return 新的校验器, 永远与传入参数的校验器的校验结果相反
     */
    static <T, K> Matcher<T, K> not(Matcher<T, K> matcher) {
        return (object, anno, context) -> !matcher.on(object, anno, context);
    }

    /**
     * 校验对象
     *
     * @param object     被校验的对象
     * @param annotation 被校验对象的注解
     * @param context    校验环境上下文
     * @return 校验结果, true：校验通过
     */
    boolean on(T object, K annotation, Context context);

    @Override
    default Object type() {
        return EnumMap.Povider.VALIDATE;
    }

}
