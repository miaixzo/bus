/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.exception;

import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import lombok.Getter;
import lombok.Setter;

/**
 * 类型: IO相关异常
 */
@Getter
@Setter
public class RelevantException extends IOException {

    @Serial
    private static final long serialVersionUID = 2852263392627L;

    /**
     * 错误码
     */
    protected String errcode;
    /**
     * 错误信息
     */
    protected String errmsg;

    /**
     * 构造
     */
    protected RelevantException() {
        super();
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param message 打印信息
     */
    protected RelevantException(final String message) {
        super(message);
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param cause 抛出对象
     */
    protected RelevantException(final Throwable cause) {
        super(cause);
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param message 打印信息
     * @param cause   抛出对象
     */
    protected RelevantException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param errcode 错误编码
     * @param errmsg  错误提示
     */
    protected RelevantException(final String errcode, final String errmsg) {
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
    protected RelevantException(final String format, final Object... args) {
        super(String.format(format, args));
    }

    /**
     * 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param e    抛出对象
     * @param fmt  格式
     * @param args 参数
     */
    protected RelevantException(final Throwable e, String fmt, final Object... args) {
        super(String.format(fmt, args), e);
    }

    /**
     * 生成一个未实现的运行时异常
     *
     * @return 一个未实现的运行时异常
     */
    protected static RelevantException noImplement() {
        return new RelevantException("Not implement yet!");
    }

    /**
     * 生成一个不可能的运行时异常
     *
     * @return 一个不可能的运行时异常
     */
    protected static RelevantException impossible() {
        return new RelevantException("r u kidding me?! It is impossible!");
    }

    protected static Throwable unwrapThrow(final Throwable e) {
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

    protected static boolean isCauseBy(final Throwable e, final Class<? extends Throwable> causeType) {
        if (e.getClass() == causeType)
            return true;
        Throwable cause = e.getCause();
        if (null == cause)
            return false;
        return isCauseBy(cause, causeType);
    }

}
