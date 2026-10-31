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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Represents a standard response message.
 *
 * Uses {@code code}, {@code desc}, and {@code data} for both success and failure responses. Serialized objects using
 * the former response fields are rejected by the changed serialization version. Rebuild callers against the code/desc
 * API and migrate persisted objects before upgrading.
 *
 * @param <T> the response payload type
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Message<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2535303508002L;

    /**
     * The response code, indicating the status of the request.
     */
    public String code;

    /**
     * A descriptive message providing details about the response.
     */
    public String desc;

    /**
     * The data payload of the response.
     */
    public T data;

    /**
     * Constructs a new {@code Message} instance.
     */
    public Message() {
        // No initialization required.
    }

    /**
     * Creates a successful response using the standard code, description, and data fields.
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
        return Message.<T>builder().code(errors.getKey()).desc(errors.getValue()).data(data).build();
    }

    /**
     * Creates an error response from a registered error descriptor.
     *
     * @param errors the registered error descriptor
     * @param <T>    the expected response payload type
     * @return an error response
     */
    public static <T> Message<T> failure(Errors errors) {
        return Message.<T>builder().code(errors.getKey()).desc(errors.getValue()).build();
    }

    /**
     * Creates an error response with an explicit code and message.
     *
     * @param code the response code
     * @param desc the response message
     * @param <T>  the expected response payload type
     * @return an error response
     */
    public static <T> Message<T> failure(String code, String desc) {
        return Message.<T>builder().code(code).desc(desc).build();
    }

}
