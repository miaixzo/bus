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

import org.aoju.bus.core.exception.ValidateException;

import java.lang.annotation.*;

/**
 * 校验异常注解,校验失败时将ValidateException替换为指定的异常并抛出.
 * <p>
 * 在被拦截方法的入参上使用,表明为全局校验异常.
 * 在对象内部校验的字段上标记,表明为字段异常.
 * 在校验器注解的定义上标记,表明为校验器异常.
 * 校验异常说明：
 * 当校验失败时,如果定义了全局校验异常,则抛出全局校验异常；
 * 然后判断如果定义了字段异常,则抛出字段异常；
 * 最后判断如果定义了校验器注解异常,则抛出校验器注解上定义的异常;
 * 如果都没定义,则抛出{@link ValidateException}
 * </P>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.FIELD})
public @interface ValidEx {

    /**
     * 异常类
     *
     * @return the object
     */
    Class<? extends ValidateException> value() default ValidateException.class;

}
