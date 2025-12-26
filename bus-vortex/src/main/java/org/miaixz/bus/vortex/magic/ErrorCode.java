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
package org.miaixz.bus.vortex.magic;

import org.miaixz.bus.core.basic.normal.ErrorRegistry;
import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Defines the set of specific error codes for the Vortex gateway module.
 * <p>
 * This class extends a base error code class and registers all gateway-specific errors. The error codes in this module
 * are typically in the 116xxx range.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ErrorCode extends org.miaixz.bus.core.basic.normal.ErrorCode {

    /**
     * Error code: 116000 - Service not responding.
     * <p>
     * Indicates a general failure when communicating with a downstream service, often due to timeouts or network issues
     * after multiple retries.
     */
    public static final Errors _116000 = ErrorRegistry.builder().key("116000").value("Service not responding").build();

    /**
     * Error code: 116001 - Role exception.
     * <p>
     * Indicates that the authenticated user does not have the required role or permission to access a resource.
     */
    public static final Errors _116001 = ErrorRegistry.builder().key("116001").value("Role exception").build();

    /**
     * Error code: 116002 - Invalid ApiKey.
     * <p>
     * Indicates that the provided API Key is not valid, not found, or has been disabled.
     */
    public static final Errors _116002 = ErrorRegistry.builder().key("116002").value("Invalid ApiKey").build();

    /**
     * Error code: 116003 - ApiKey expired.
     * <p>
     * Indicates that the provided API Key is valid but has passed its expiration date.
     */
    public static final Errors _116003 = ErrorRegistry.builder().key("116003").value("ApiKey expired").build();

    /**
     * Error code: 116004 - Request body is too large.
     * <p>
     * Indicates that the size of the incoming request body exceeds the configured limit for that request type.
     * 
     * @see org.miaixz.bus.vortex.strategy.RequestStrategy
     */
    public static final Errors _116004 = ErrorRegistry.builder().key("116004").value("Request body is too large")
            .build();

    /**
     * Error code: 116005 - Unsupported interaction mode.
     * <p>
     * Indicates that the configured {@code mode} in the {@link org.miaixz.bus.vortex.Assets} is not supported or
     * recognized by the current Vortex gateway instance.
     * </p>
     */
    public static final Errors _116005 = ErrorRegistry.builder().key("116005").value("Unsupported interaction mode")
            .build();
}
