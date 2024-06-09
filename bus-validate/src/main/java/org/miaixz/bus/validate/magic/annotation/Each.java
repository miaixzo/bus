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
package org.miaixz.bus.validate.magic.annotation;

import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.metric.EachMatcher;

import java.lang.annotation.*;

/**
 * 对数组、集合、Map元素进行校验, 注意,Map对象,只校验内部的值列表
 *
 * <p>
 * 对象为null, 忽略校验
 * </P>
 * <p>
 * 如果不是数组或集合、Map,则忽略校验
 * </P>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Complex(value = Builder._EACH, clazz = EachMatcher.class)
public @interface Each {

    /**
     * 校验器名称数组,优先使用校验器名称中的校验器,并忽略校验器类中的校验器
     *
     * @return the array
     */
    String[] value() default {};

    /**
     * 校验器类数组, 当校验器名称数组为空时,使用校验器类数组中的校验器
     *
     * @return the object
     */
    Class<? extends Matcher>[] classes() default {};

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
    String errmsg() default "${field}参数校验失败";

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
