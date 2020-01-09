/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.builtin.doc.annotation;

import org.aoju.bus.metric.magic.Result;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 文档方法，作用在方法上
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface ApiDocMethod {

    /**
     * @return 描述，接口简单描述，如：用户登录，发送验证码。支持html标签
     */
    String description() default "";

    /**
     * @return 备注，可写更详细的介绍，支持html标签
     */
    String remark() default "";

    /**
     * @return 自定义参数
     */
    ApiDocField[] params() default {};

    /**
     * @return 指定参数类型
     */
    Class<?> paramClass() default Object.class;

    /**
     * @return 自定义返回参数
     */
    ApiDocField[] results() default {};

    /**
     * @return 指定返回参数
     */
    Class<?> resultClass() default Object.class;

    /**
     * @return 数组元素class类型
     */
    Class<?> elementClass() default Object.class;

    /**
     * @return 指定模块下文档显示顺序，值越小越靠前
     */
    int order() default Integer.MAX_VALUE;

    /**
     * @return 最外部包装类class。
     * <pre>
     *     使用Void.class：文档显示结果不包装
     * </pre>
     */
    Class<? extends Result> wrapperClass() default Result.class;

}
