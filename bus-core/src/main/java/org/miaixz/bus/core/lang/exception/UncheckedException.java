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

import java.io.Serial;

import lombok.Getter;
import lombok.Setter;

/**
 * 类型: 未受检异常
 */
@Getter
@Setter
public class UncheckedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2852302528719L;

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
    protected UncheckedException() {
        super();
    }

    /**
     * 构造 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param message 打印信息
     */
    protected UncheckedException(final String message) {
        super(message);
    }

    /**
     * 构造 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param cause 抛出对象
     */
    protected UncheckedException(final Throwable cause) {
        super(cause);
    }

    /**
     * 构造 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param message 打印信息
     * @param cause   抛出对象
     */
    protected UncheckedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param errcode 错误编码
     * @param errmsg  错误提示
     */
    protected UncheckedException(final String errcode, final String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    /**
     * 构造 根据格式化字符串,生成运行时异常
     *
     * @param format 格式
     * @param args   参数
     */
    protected UncheckedException(final String format, final Object... args) {
        super(String.format(format, args));
    }

    /**
     * 构造 将抛出对象包裹成运行时异常,并增加自己的描述
     *
     * @param cause  抛出对象
     * @param format 格式
     * @param args   参数
     */
    protected UncheckedException(final Throwable cause, final String format, final Object... args) {
        super(String.format(format, args), cause);
    }

    /**
     * 构造 运行时异常，其中包含指定的详细信息消息，原因，启用或禁用抑制，可写堆栈跟踪启用或禁用
     *
     * @param message            详细信息
     * @param cause              原因,（允许使用{@code null}值，表示原因不存在或未知)
     * @param enableSuppression  是否启用抑制whether or not suppression is enabled or disabled
     * @param writableStackTrace 堆栈跟踪是否应该可写
     */
    protected UncheckedException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
