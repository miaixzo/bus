/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.base.spring;


import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.base.entity.Message;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;

/**
 * <p>
 * 基础输出封装
 * </p>
 *
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
public class Controller {

    public static Object write(ErrorCode respCode) {
        return write(respCode, null);
    }

    public static Object write(ErrorCode respCode, Object data) {
        return write(respCode.getErrcode(), respCode.getErrmsg(), data);
    }

    public static Object write(ErrorCode respCode, String message) {
        return write(respCode.getErrcode(), StringUtils.isEmpty(message) ? respCode.getErrmsg() : message);
    }

    public static Object write(Object data) {
        return write(ErrorCode.EM_SUCCESS, data);
    }

    public static Object write(String errcode) {
        return write(ErrorCode.of(errcode), null);
    }

    public static Object write(String errcode, String errmsg) {
        return write(errcode, errmsg, null);
    }

    public static Object write(String errcode, String errmsg, Object data) {
        ErrorCode resultCode = ErrorCode.of(errcode);
        if (ObjectUtils.isNotEmpty(resultCode)) {
            errmsg = StringUtils.isEmpty(errmsg) ? resultCode.getErrmsg() : errmsg;
            return new Message(resultCode.getErrcode(), errmsg, data);
        }
        return new Message(ErrorCode.EM_FAILURE.getErrcode(), ErrorCode.EM_FAILURE.errmsg);
    }

}
