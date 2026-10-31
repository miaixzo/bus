/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 */
public class Controller {

    /**
     * Creates the base MVC controller support.
     */
    public Controller() {
        // No initialization required.
    }

    /**
     * Creates a success response with the given data.
     *
     * @param data The data to be included in the response.
     * @param <T>  the response payload type
     * @return A typed response message.
     */
    public <T> Message<T> write(T data) {
        return write(ErrorCode._SUCCESS, data);
    }

    /**
     * Creates a success response, optionally extracting the "id" field from the data.
     *
     * @param data The data object.
     * @param id   If true, extracts the "id" field from the data as the response data.
     * @return A response message whose payload is either the original data or its identifier.
     */
    public Message<Object> write(Object data, boolean id) {
        if (id) {
            return write(FieldKit.getFieldValue(data, "id"));
        }
        return write(data);
    }

    /**
     * Creates an error response using an error code string.
     *
     * @param code The error code.
     * @param <T>  the expected response payload type
     * @return A typed error response message.
     */
    public <T> Message<T> write(String code) {
        return write(Errors.require(code));
    }

    /**
     * Creates an error response using an {@link Errors} object.
     *
     * @param errors The error object.
     * @param <T>    the expected response payload type
     * @return A typed error response message.
     */
    public <T> Message<T> write(Errors errors) {
        return write(errors, null);
    }

    /**
     * Creates a response with a specific error code and data.
     *
     * @param code The error code.
     * @param data The data to be included in the response.
     * @param <T>  the response payload type
     * @return A typed response message.
     */
    public <T> Message<T> write(String code, T data) {
        return write(Errors.require(code), data);
    }

    /**
     * Creates a response with a specific {@link Errors} object and data.
     *
     * @param errors The error object.
     * @param data   The data to be included in the response.
     * @param <T>    the response payload type
     * @return A typed response message.
     */
    public <T> Message<T> write(Errors errors, T data) {
        if (null == errors || !Errors.contains(errors.getKey())) {
            return Message.failure(ErrorCode._FAILURE);
        }
        if (ErrorCode._SUCCESS.getKey().equals(errors.getKey())) {
            return Message.success(errors, data);
        }
        return write(errors.getKey(), errors.getValue());
    }

    /**
     * Creates a response with a specific error code and message.
     *
     * @param code The error code.
     * @param desc The error message.
     * @param <T>  the expected response payload type
     * @return A typed error response message.
     */
    public <T> Message<T> write(String code, String desc) {
        if (Errors.contains(code) && StringKit.isNotEmpty(desc)) {
            return Message.failure(code, desc);
        }
        return Message.failure(ErrorCode._FAILURE);
    }

    /**
     * Creates a response with a formatted error message.
     *
     * @param code   The error code.
     * @param desc   The error message content to be formatted.
     * @param format The format string.
     * @param <T>    the expected response payload type
     * @return A typed error response message.
     */
    public <T> Message<T> write(String code, String desc, String format) {
        if (StringKit.isNotEmpty(code) && StringKit.isNotEmpty(format)) {
            return Message.failure(code, StringKit.format(format, desc));
        }
        return Message.failure(ErrorCode._FAILURE);
    }

}
