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
package org.miaixz.bus.core.basic.entity;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Represents a standard response message.
 *
 * @param <T> the response payload type
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Message<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852291039238L;

    /**
     * The response code, indicating the status of the request.
     */
    public String errcode;

    /**
     * A descriptive message providing details about the response.
     */
    public String errmsg;

    /**
     * The data payload of the response.
     */
    public T data;

    /**
     * Creates a successful response while preserving the framework's existing response contract.
     *
     * @param data the response payload
     * @param <T>  the payload type
     * @return a successful response
     */
    public static <T> Message<T> success(T data) {
        return success(ErrorCode._SUCCESS, data);
    }

    /**
     * Creates a successful response from the supplied registered success descriptor.
     *
     * @param errors the registered success descriptor
     * @param data   the response payload
     * @param <T>    the payload type
     * @return a successful response
     */
    public static <T> Message<T> success(Errors errors, T data) {
        return Message.<T>builder().errcode(errors.getKey()).errmsg(errors.getValue()).data(data).build();
    }

    /**
     * Creates an error response from a registered error descriptor.
     *
     * @param errors the registered error descriptor
     * @param <T>    the expected response payload type
     * @return an error response
     */
    public static <T> Message<T> failure(Errors errors) {
        return Message.<T>builder().errcode(errors.getKey()).errmsg(errors.getValue()).build();
    }

    /**
     * Creates an error response with an explicit code and message.
     *
     * @param errcode the response code
     * @param errmsg  the response message
     * @param <T>     the expected response payload type
     * @return an error response
     */
    public static <T> Message<T> failure(String errcode, String errmsg) {
        return Message.<T>builder().errcode(errcode).errmsg(errmsg).build();
    }

}
