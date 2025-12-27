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
package org.miaixz.bus.vortex.support.grpc;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.springframework.beans.factory.DisposableBean;

/**
 * A service for invoking gRPC methods on remote services via HTTP gateway.
 * <p>
 * This service uses HTTP as transport protocol instead of direct gRPC, avoiding third-party gRPC library dependencies.
 * gRPC-Web or gRPC-HTTP proxy is required on the server side to translate HTTP requests to gRPC calls.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GrpcService implements DisposableBean {

    /**
     * Invokes a gRPC method via HTTP gateway.
     * <p>
     * This method sends HTTP POST requests to a gRPC-Web/gRPC-HTTP gateway, which translates the request to actual gRPC
     * calls. The request and response payloads are in JSON format for compatibility.
     *
     * @param assets  The configuration containing the gRPC service details (host, port, method).
     * @param payload The JSON string content of the request message.
     * @return The JSON response from the gRPC service.
     */
    public String invoke(Assets assets, String payload) {
        try {
            // Parse the method name (format: "package.Service/Method")
            String fullMethodName = assets.getMethod();

            Logger.info(
                    true,
                    "gRPC",
                    "Invoking gRPC method via HTTP: {} on {}:{}",
                    fullMethodName,
                    assets.getHost(),
                    assets.getPort());

            // Build HTTP URL for gRPC gateway
            String url = buildGrpcUrl(assets, fullMethodName);

            Logger.info(true, "gRPC", "gRPC method {} invoked successfully", fullMethodName);
            // Send HTTP POST request to gRPC gateway
            return Httpx.post(url, payload, MediaType.APPLICATION_JSON);

        } catch (Exception e) {
            Logger.error("Failed to invoke gRPC method '{}'", assets.getMethod(), e);
            throw new RuntimeException("Failed to invoke gRPC method: " + assets.getMethod(), e);
        }
    }

    /**
     * Builds the URL for gRPC gateway call.
     * <p>
     * Standard gRPC-Web URLs follow the pattern: http://host:port/package.Service/Method
     *
     * @param assets         The configuration for the target gRPC service.
     * @param fullMethodName The full gRPC method name (package.Service/Method).
     * @return The HTTP URL for the gRPC gateway.
     */
    private String buildGrpcUrl(Assets assets, String fullMethodName) {
        StringBuilder url = new StringBuilder();
        url.append("http://").append(assets.getHost()).append(Symbol.COLON).append(assets.getPort());

        // Replace dots with slashes for URL path
        // e.g., "package.Service/Method" -> "/package.Service/Method"
        if (StringKit.isNotEmpty(fullMethodName)) {
            url.append(Symbol.SLASH).append(fullMethodName);
        }

        return url.toString();
    }

    /**
     * Destroys this service when the Spring container shuts down.
     * <p>
     * This method cleans up resources.
     */
    @Override
    public void destroy() {
        Logger.info("GrpcService shut down successfully.");
    }

}
