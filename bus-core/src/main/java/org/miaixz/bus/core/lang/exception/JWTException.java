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

/**
 * 类型: JWT异常
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWTException extends UncheckedException {

    @Serial
    private static final long serialVersionUID = 2852257851297L;

    /**
     * 构造
     *
     * @param e 异常
     */
    public JWTException(final Throwable e) {
        super(e);
    }

    /**
     * 构造
     *
     * @param message 消息
     */
    public JWTException(final String message) {
        super(message);
    }

    /**
     * 构造
     *
     * @param messageTemplate 消息模板
     * @param params          参数
     */
    public JWTException(final String messageTemplate, final Object... params) {
        super(messageTemplate, params);
    }

    /**
     * 构造
     *
     * @param message 消息
     * @param cause   被包装的子异常
     */
    public JWTException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造
     *
     * @param message            消息
     * @param cause              被包装的子异常
     * @param enableSuppression  是否启用抑制
     * @param writableStackTrace 堆栈跟踪是否应该是可写的
     */
    public JWTException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * 构造
     *
     * @param cause           被包装的子异常
     * @param messageTemplate 消息模板
     * @param params          参数
     */
    public JWTException(final Throwable cause, final String messageTemplate, final Object... params) {
        super(cause, messageTemplate, params);
    }

}
