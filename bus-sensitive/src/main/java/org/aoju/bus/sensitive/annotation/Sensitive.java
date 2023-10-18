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
package org.aoju.bus.sensitive.annotation;

import org.aoju.bus.sensitive.Builder;

import java.lang.annotation.*;

/**
 * 数据脱敏,具体如下:
 * 1.数据库级别脱敏加密
 * SensitiveResultSetHandler 解密脱敏
 * SensitiveStatementHandler 脱敏加密
 * 2.访问请求级别加解密
 * RequestBodyAdvice 解密脱敏
 * ResponseBodyAdvice 脱敏加密
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    /**
     * 数据处理模式
     * 可选值
     * 1.Builder.ALL 全部开启
     * 2.Builder.SENS 开启脱敏
     * 3.Builder.SAFE 开启加解密
     *
     * @return the string
     */
    String value() default Builder.ALL;

    /**
     * 数据出入方向
     * 可选值
     * 1.Builder.ALL 全部开启
     * 2.Builder.IN  请求/写入
     * 3.Builder.OUT 查询/输出
     * 4.Builder.OVERALL 全局加密
     *
     * @return the string
     */
    String stage() default Builder.ALL;

    /**
     * 脱敏属性 {"id","name"}
     *
     * @return the array
     */
    String[] field() default {};

    /**
     * 忽略属性 {"created","creator"}
     *
     * @return the array
     */
    String[] skip() default {};

    /**
     * 内部脱敏:true/false
     *
     * @return the boolean
     */
    boolean inside() default true;

}
