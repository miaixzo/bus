/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * Provides basic response wrapping for controllers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Controller {

    /**
     * Constructs a new Controller. Utility class constructor for static access.
     */
    public Controller() {
    }

    /**
     * Creates a success response with the given data.
     *
     * @param data The data to be included in the response.
     * @return A response object.
     */
    public static Object write(Object data) {
        return write(data, false);
    }

    /**
     * Creates a success response, optionally extracting the "id" field from the data.
     *
     * @param data The data object.
     * @param id   If true, extracts the "id" field from the data as the response data.
     * @return A response object.
     */
    public static Object write(Object data, boolean id) {
        if (id) {
            return write(ErrorCode._SUCCESS, FieldKit.getFieldValue(data, "id"));
        }
        return write(ErrorCode._SUCCESS, data);
    }

    /**
     * Creates an error response using an error code string.
     *
     * @param errcode The error code.
     * @return A response object.
     */
    public static Object write(String errcode) {
        return write(errcode, Errors.require(errcode));
    }

    /**
     * Creates an error response using an {@link Errors} object.
     *
     * @param errors The error object.
     * @return A response object.
     */
    public static Object write(Errors errors) {
        return write(errors.getKey(), errors.getValue());
    }

    /**
     * Creates a response with a specific error code and data.
     *
     * @param errcode The error code.
     * @param data    The data to be included in the response.
     * @return A response object.
     */
    public static Object write(String errcode, Object data) {
        if (Errors.contains(errcode)) {
            return Message.builder().errcode(errcode).errmsg(Errors.require(errcode).getValue()).data(data).build();
        }
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

    /**
     * Creates a response with a specific {@link Errors} object and data.
     *
     * @param errors The error object.
     * @param data   The data to be included in the response.
     * @return A response object.
     */
    public static Object write(Errors errors, Object data) {
        if (Errors.contains(errors.getKey())) {
            return Message.builder().errcode(errors.getKey()).errmsg(errors.getValue()).data(data).build();
        }
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

    /**
     * Creates a response with a specific error code and message.
     *
     * @param errcode The error code.
     * @param errmsg  The error message.
     * @return A response object.
     */
    public static Object write(String errcode, String errmsg) {
        if (Errors.contains(errcode) && StringKit.isNotEmpty(errmsg)) {
            return Message.builder().errcode(errcode).errmsg(errmsg).build();
        }
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

    /**
     * Creates a response with a formatted error message.
     *
     * @param errcode The error code.
     * @param errmsg  The error message content to be formatted.
     * @param format  The format string.
     * @return A response object.
     */
    public static Object write(String errcode, String errmsg, String format) {
        if (StringKit.isNotEmpty(errcode) && StringKit.isNotEmpty(format)) {
            return Message.builder().errcode(errcode).errmsg(StringKit.format(format, errmsg)).build();
        }
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

}
