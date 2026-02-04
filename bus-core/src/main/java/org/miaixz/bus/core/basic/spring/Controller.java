/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
    public Object write(Object data) {
        return write(data, false);
    }

    /**
     * Creates a success response, optionally extracting the "id" field from the data.
     *
     * @param data The data object.
     * @param id   If true, extracts the "id" field from the data as the response data.
     * @return A response object.
     */
    public Object write(Object data, boolean id) {
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
    public Object write(String errcode) {
        return write(errcode, Errors.require(errcode));
    }

    /**
     * Creates an error response using an {@link Errors} object.
     *
     * @param errors The error object.
     * @return A response object.
     */
    public Object write(Errors errors) {
        return write(errors.getKey(), errors.getValue());
    }

    /**
     * Creates a response with a specific error code and data.
     *
     * @param errcode The error code.
     * @param data    The data to be included in the response.
     * @return A response object.
     */
    public Object write(String errcode, Object data) {
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
    public Object write(Errors errors, Object data) {
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
    public Object write(String errcode, String errmsg) {
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
    public Object write(String errcode, String errmsg, String format) {
        if (StringKit.isNotEmpty(errcode) && StringKit.isNotEmpty(format)) {
            return Message.builder().errcode(errcode).errmsg(StringKit.format(format, errmsg)).build();
        }
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

}
