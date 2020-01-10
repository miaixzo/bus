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
package org.aoju.bus.metric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标记类具有接口提供能力，该注解同样具备SpringBean管理功能，因为继承了@Service
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@org.springframework.stereotype.Service
public @interface Service {

    /**
     * @return 忽略验证签名，默认false，为true则仅仅忽略ApiService下面所有的接口的验签操作，但其它验证会执行。
     */
    boolean sign() default false;

    /**
     * @return 忽略所有验证，默认false，为true则忽略ApiService下面所有接口的验证操作。
     */
    boolean validate() default false;

    /**
     * @return 是否对返回结果进行包装，影响其下所有接口。
     */
    boolean wrap() default true;

}
