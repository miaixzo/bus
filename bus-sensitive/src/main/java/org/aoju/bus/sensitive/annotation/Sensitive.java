/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.sensitive.annotation;

import java.lang.annotation.*;

/**
 * 数据脱敏
 * 1. 所有的注解都要继承这个注解
 * 2. 如果一个字段上面有多个注解，则根据注解的顺序，依次执行。
 * 设计的考虑：
 * 本来想过将生效条件单独抽离为一个注解，这样可以达到条件注解的复用。
 * 但是有一个缺点，当指定多个策略时，条件的注解就会太宽泛，无法保证精细到每一个策略生效的场景。
 * 平衡的方式：
 * 在 support 注解中，可以指定策略。默认是全部，如果指定，则只针对其中的某个策略生效。
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    /**
     * 脱敏属性
     *
     * @return the object
     */
    String[] value() default {};

}
