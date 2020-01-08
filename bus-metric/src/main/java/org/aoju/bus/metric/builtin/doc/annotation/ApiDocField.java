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

import org.aoju.bus.metric.builtin.doc.DataType;
import org.aoju.bus.metric.builtin.doc.IEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 文档字段
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8++
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface ApiDocField {

    /**
     * @return 字段描述
     */
    String description() default "";

    /**
     * @return 字段名
     */
    String name() default "";

    /**
     * @return 数据类型
     */
    DataType dataType() default DataType.UNKNOW;

    /**
     * @return 是否必填
     */
    boolean required() default false;

    /**
     * @return 示例值
     */
    String example() default "";

    /**
     * @return 指定文档类
     */
    Class<?> beanClass() default Void.class;

    /**
     * @return 数组元素class类型
     */
    Class<?> elementClass() default Void.class;

    /**
     * @return 枚举类class，指定的枚举类必须实现{@link IEnum} 接口
     * 文档页会自动显示枚举信息（getCode() + ":" + getDescription()）
     * @since 1.12.5
     */
    Class<?> enumClass() default Void.class;

}
