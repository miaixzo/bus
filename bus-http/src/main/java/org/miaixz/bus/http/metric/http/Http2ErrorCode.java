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
package org.miaixz.bus.http.metric.http;

/**
 * Represents an HTTP/2 error code, as defined in RFC 7540, Section 7.
 *
 * @author Kimi Liu
 * @since Java 17+
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
