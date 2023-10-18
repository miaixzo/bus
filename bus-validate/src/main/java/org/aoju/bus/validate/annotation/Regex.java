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
package org.aoju.bus.validate.annotation;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.strategy.RegexStrategy;

import java.lang.annotation.*;

/**
 * 正则表达式校验
 * <p>
 * 默认被校验对象是null时,通过校验
 * </P>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Complex(value = Builder._REGEX, clazz = RegexStrategy.class)
public @interface Regex {

    @Filler("pattern")
    String pattern() default Normal.EMPTY;

    /**
     * 如果长度为0,判断能否通过校验
     * 默认为false
     * true：表示长度为零,默认通过校验；false：表示长度为0,仍然要进行正则验证
     *
     * @return the boolean
     */
    boolean zeroAble() default false;

    /**
     * 默认使用的异常码
     *
     * @return the string
     */
    String errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * 默认使用的异常信息
     *
     * @return the string
     */
    String errmsg() default "${field}验证失败,请检查数据格式";

    /**
     * 校验器组
     *
     * @return the array
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     *
     * @return the string
     */
    String field() default Builder.DEFAULT_FIELD;

}
