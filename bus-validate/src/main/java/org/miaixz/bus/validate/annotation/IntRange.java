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
package org.miaixz.bus.validate.annotation;

import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.strategy.IntRangeStrategy;

import java.lang.annotation.*;

/**
 * 判断数字在int范围内
 *
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
@Complex(value = Builder._INT_RANGE, clazz = IntRangeStrategy.class)
public @interface IntRange {

    /**
     * 小于等于
     *
     * @return the int
     */
    @Filler("min")
    int min() default Integer.MIN_VALUE;

    /**
     * 大于等于
     *
     * @return the int
     */
    @Filler("max")
    int max() default Integer.MAX_VALUE;

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
    String errmsg() default "${field}必须在指定范围内, 最小: ${min}, 最大: ${max}";

    /**
     * 校验器组
     *
     * @return the string
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     *
     * @return the string
     */
    String field() default Builder.DEFAULT_FIELD;

}
