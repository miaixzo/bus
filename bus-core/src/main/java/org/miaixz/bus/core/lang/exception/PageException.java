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

/**
 * 类型: 分页插件异常
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageException extends UncheckedException {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     */
    public PageException() {
        super();
    }

    /**
     * 构造
     *
     * @param e 异常
     */
    public PageException(final Throwable e) {
        super(e);
    }

    /**
     * 构造
     *
     * @param message 消息
     */
    public PageException(final String message) {
        super(message);
    }

    /**
     * 构造
     *
     * @param format 消息模板
     * @param args   参数
     */
    public PageException(final String format, final Object... args) {
        super(format, args);
    }

    /**
     * 构造
     *
     * @param errcode 错误码
     * @param errmsg  消息
     */
    public PageException(final String errcode, final String errmsg) {
        super(errcode, errmsg);
    }

    /**
     * 构造
     *
     * @param errcode   错误码
     * @param throwable 异常
     */
    public PageException(final String errcode, final Throwable throwable) {
        super(errcode, throwable);
    }

    /**
     * 构造
     *
     * @param cause  被包装的子异常
     * @param format 消息模板
     * @param args   参数
     */
    public PageException(final Throwable cause, final String format, final Object... args) {
        super(cause, format, args);
    }

    /**
     * @param errcode   错误码
     * @param errmsg    消息
     * @param throwable 异常
     */
    public PageException(final String errcode, final String errmsg, final Throwable throwable) {
        super(errcode, errmsg, throwable);
    }

    /**
     * 构造
     *
     * @param message            消息
     * @param cause              被包装的子异常
     * @param enableSuppression  是否启用抑制
     * @param writableStackTrace 堆栈跟踪是否应该是可写的
     */
    public PageException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
