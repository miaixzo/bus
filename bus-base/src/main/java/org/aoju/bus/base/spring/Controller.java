/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
import org.aoju.bus.core.utils.StringUtils;

/**
 * 基础输出封装
 *
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8+
 */
public class Controller {

    /**
     * 返回值:数据处理
     *
     * @param data 数据信息
     * @return body 返回值
     */
    public static Object write(Object data) {
        return write(ErrorCode.EM_SUCCESS, data);
    }

    /**
     * 返回值:数据处理
     *
     * @param errcode 错误编码
     * @return body 返回值
     */
    public static Object write(String errcode) {
        return write(errcode, ErrorCode.require(errcode));
    }

    /**
     * 返回值:数据处理
     *
     * @param errcode 错误编码
     * @param data    数据信息
     * @return body 返回值
     */
    public static Object write(String errcode, Object data) {
        String errmsg = ErrorCode.require(errcode);
        if (StringUtils.isNotEmpty(errmsg)) {
            return new Message(errcode, errmsg, data);
        }
        return new Message(ErrorCode.EM_FAILURE, ErrorCode.require(ErrorCode.EM_FAILURE));
    }

    /**
     * 返回值:数据处理
     *
     * @param errcode 错误编码
     * @param errmsg  错误信息
     * @return body 返回值
     */
    public static Object write(String errcode, String errmsg) {
        String error = ErrorCode.require(errcode);
        if (StringUtils.isNotEmpty(error)) {
            return new Message(errcode, errmsg);
        }
        return new Message(ErrorCode.EM_FAILURE, ErrorCode.require(ErrorCode.EM_FAILURE));
    }

}
