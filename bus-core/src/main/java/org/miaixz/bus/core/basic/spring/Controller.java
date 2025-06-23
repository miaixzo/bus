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
package org.miaixz.bus.core.basic.spring;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 基础输出封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Controller {

    /**
     * 返回值:数据处理
     *
     * @param data 数据信息
     * @return body 返回值
     */
    public static Object write(Object data) {
        return write(data, false);
    }

    /**
     * 返回值:数据处理
     *
     * @param data 数据信息
     * @return body 返回值
     */
    public static Object write(Object data, boolean id) {
        if (id) {
            return write(ErrorCode._SUCCESS, FieldKit.getFieldValue(data, "id"));
        }
        return write(ErrorCode._SUCCESS, data);
    }

    /**
     * 返回值:数据处理
     *
     * @param errcode 错误编码
     * @return body 返回值
     */
    public static Object write(String errcode) {
        return write(errcode, Errors.require(errcode));
    }

    /**
     * 返回值:数据处理
     *
     * @param errors 错误信息
     * @return body 返回值
     */
    public static Object write(Errors errors) {
        return write(errors.getKey(), errors.getValue());
    }

    /**
     * 返回值:数据处理
     *
     * @param errcode 错误编码
     * @param data    数据信息
     * @return body 返回值
     */
    public static Object write(String errcode, Object data) {
        if (Errors.contains(errcode)) {
            return Message.builder().errcode(errcode).errmsg(Errors.require(errcode).getValue()).data(data).build();
        }
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

    /**
     * 返回值:数据处理
     *
     * @param errors 错误编码
     * @param data   数据信息
     * @return body 返回值
     */
    public static Object write(Errors errors, Object data) {
        if (Errors.contains(errors.getKey())) {
            return Message.builder().errcode(errors.getKey()).errmsg(errors.getValue()).data(data).build();
        }
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

    /**
     * 返回值:数据处理
     *
     * @param errcode 错误编码
     * @param errmsg  错误信息
     * @return body 返回值
     */
    public static Object write(String errcode, String errmsg) {
        if (Errors.contains(errcode) && StringKit.isNotEmpty(errmsg)) {
            return Message.builder().errcode(errcode).errmsg(errmsg).build();
        }
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

    /**
     * 返回值:自定义处理
     *
     * @param errcode 错误编码
     * @param errmsg  错误信息
     * @param format  输出格式
     * @return body 返回值
     */
    public static Object write(String errcode, String errmsg, String format) {
        if (StringKit.isNotEmpty(errcode) && StringKit.isNotEmpty(format)) {
            return Message.builder().errcode(errcode).errmsg(StringKit.format(format, errmsg)).build();
        }
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

}
