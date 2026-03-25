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
package org.miaixz.bus.http.metric.http;

/**
 * Represents an HTTP/2 error code, as defined in RFC 7540, Section 7.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Http2ErrorCode {

    /**
     * The associated condition is not a result of an error.
     */
    NO_ERROR(0),

    /**
     * The endpoint detected an unspecific protocol error.
     */
    PROTOCOL_ERROR(1),

    /**
     * The endpoint encountered an unexpected internal error.
     */
    INTERNAL_ERROR(2),

    /**
     * The endpoint detected that its peer violated the flow-control protocol.
     */
    FLOW_CONTROL_ERROR(3),

    /**
     * The endpoint refuses the stream prior to processing any application logic.
     */
    REFUSED_STREAM(7),

    /**
     * The stream is no longer needed.
     */
    CANCEL(8),

    /**
     * The endpoint is unable to maintain the header compression context for the connection.
     */
    COMPRESSION_ERROR(9),

    /**
     * The connection established in response to a CONNECT request was reset or abnormally closed.
     */
    CONNECT_ERROR(0xa),

    /**
     * The endpoint detected that its peer is exhibiting a behavior that might be generating excessive load.
     */
    ENHANCE_YOUR_CALM(0xb),

    /**
     * The underlying transport has properties that do not meet the minimum security requirements.
     */
    INADEQUATE_SECURITY(0xc),

    /**
     * The endpoint requires that HTTP/1.1 be used instead of HTTP/2.
     */
    HTTP_1_1_REQUIRED(0xd);

    /**
     * The HTTP/2 error code as an integer.
     */
    public final int httpCode;

    Http2ErrorCode(int httpCode) {
        this.httpCode = httpCode;
    }

    /**
     * Returns the {@link Http2ErrorCode} corresponding to the given HTTP/2 code.
     *
     * @param code The HTTP/2 error code.
     * @return The corresponding {@link Http2ErrorCode}, or null if not found.
     */
    public static Http2ErrorCode fromHttp2(int code) {
        for (Http2ErrorCode errorCode : Http2ErrorCode.values()) {
            if (errorCode.httpCode == code) {
                return errorCode;
            }
        }
        return null;
    }

}
