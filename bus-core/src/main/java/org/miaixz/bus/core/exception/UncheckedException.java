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
package org.miaixz.bus.core.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.InvocationTargetException;

/**
 * 类型: 未受检异常
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class UncheckedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    protected String errcode;
    /**
     * 错误信息
     */
    protected String errmsg;

    protected UncheckedException() {
        super();
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param message 打印信息
     */
    protected UncheckedException(String message) {
        super(message);
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param cause 抛出对象
     */
    protected UncheckedException(Throwable cause) {
        super(cause);
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param message 打印信息
     * @param cause   抛出对象
     */
    protected UncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param errcode 错误编码
     * @param errmsg  错误提示
     */
    protected UncheckedException(String errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    /**
     * 根据格式化字符串,生成运行时异常
     *
     * @param format 格式
     * @param args   参数
     */
    protected UncheckedException(String format, Object... args) {
        super(String.format(format, args));
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param cause  抛出对象
     * @param format 格式
     * @param args   参数
     */
    protected UncheckedException(Throwable cause, String format, Object... args) {
        super(String.format(format, args), cause);
    }

    /**
     * 构造一个新的运行时异常，其中包含指定的详细信息消息，原因，启用或禁用抑制，可写堆栈跟踪启用或禁用
     *
     * @param message            详细信息
     * @param cause              原因,（允许使用{@code null}值，表示原因不存在或未知)
     * @param enableSuppression  是否启用抑制whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace 堆栈跟踪是否应该可写
     */
    protected UncheckedException(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * 生成一个未实现的运行时异常
     *
     * @return 一个未实现的运行时异常
     */
    protected static UncheckedException noImplement() {
        return new UncheckedException("Not implement yet!");
    }

    /**
     * 生成一个不可能的运行时异常
     *
     * @return 一个不可能的运行时异常
     */
    protected static UncheckedException impossible() {
        return new UncheckedException("r u kidding me?! It is impossible!");
    }

    protected static Throwable unwrapThrow(Throwable e) {
        if (null == e) {
            return null;
        }
        if (e instanceof InvocationTargetException) {
            InvocationTargetException itE = (InvocationTargetException) e;
            if (null != itE.getTargetException())
                return unwrapThrow(itE.getTargetException());
        }
        if (e instanceof RuntimeException && null != e.getCause()) {
            return unwrapThrow(e.getCause());
        }
        return e;
    }

    protected static boolean isCauseBy(Throwable e, Class<? extends Throwable> causeType) {
        if (e.getClass() == causeType)
            return true;
        Throwable cause = e.getCause();
        if (null == cause)
            return false;
        return isCauseBy(cause, causeType);
    }

}
