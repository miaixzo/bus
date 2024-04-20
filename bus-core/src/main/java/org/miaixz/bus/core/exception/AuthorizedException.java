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

/**
 * 自定义异常: 认证异常
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AuthorizedException extends UncheckedException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造
     */
    public AuthorizedException() {
        super();
    }

    /**
     * 构造
     *
     * @param message 消息
     */
    public AuthorizedException(String message) {
        super(message);
    }

    /**
     * 构造
     *
     * @param format 格式
     * @param args   参数
     */
    public AuthorizedException(String format, Object... args) {
        super(format, args);
    }

    /**
     * 构造
     *
     * @param message 消息
     * @param cause   异常
     */
    public AuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造
     *
     * @param cause 异常
     */
    public AuthorizedException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造
     *
     * @param errcode 错误码
     * @param errmsg  消息
     */
    public AuthorizedException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}
