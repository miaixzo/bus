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
package org.miaixz.bus.vortex;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.core.basic.entity.Tracer;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * Context parameter class, used to store and pass request-related context information.
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Context extends Tracer {

    /**
     * The key name for the context object in ServerWebExchange or ServerRequest attributes.
     */
    private static final String $ = "_context";

    /**
     * Request parameters, stored as key-value pairs.
     */
    private Map<String, String> requestMap;

    /**
     * Request headers, stored as key-value pairs.
     */
    private Map<String, String> headerMap;

    /**
     * File upload parameters, storing a map of file parts.
     */
    private Map<String, Part> filePartMap;

    /**
     * Data format, defaults to JSON format.
     */
    @Builder.Default
    private Formats formats = Formats.JSON;

    /**
     * Request channel, defaults to the web channel.
     */
    @Builder.Default
    private Channel channel = Channel.WEB;

    /**
     * The HTTP method of the request.
     */
    private HttpMethod httpMethod;

    /**
     * Asset information, specifically defined by the {@link Assets} class.
     */
    private Assets assets;

    /**
     * Token, used for authentication or session management.
     */
    private String token;

    /**
     * Indicates whether the data is encrypted and signed.
     */
    private Integer sign;

    /**
     * Request start time, used for performance monitoring or logging.
     */
    private long timestamp;

    /**
     * Retrieves or initializes the context object from {@link ServerWebExchange}. It automatically extracts header
     * information from the request and sets it into the headerMap.
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @return The context object; if it does not exist, a new empty context is created and header information is set.
     */
    public static Context get(ServerWebExchange exchange) {
        Context context = exchange.getAttribute(Context.$);
        if (context == null) {
            context = new Context();
            // Extracts header information from the request
            Map<String, String> headers = exchange.getRequest().getHeaders().toSingleValueMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            context.setHeaderMap(headers);
            exchange.getAttributes().put(Context.$, context);
        } else if (context.getHeaderMap() == null) {
            // If context exists but headerMap is null, also set header information
            Map<String, String> headers = exchange.getRequest().getHeaders().toSingleValueMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            context.setHeaderMap(headers);
        }
        return context;
    }

    /**
     * Retrieves or initializes the context object from {@link ServerRequest}. It automatically extracts header
     * information from the request and sets it into the headerMap.
     *
     * @param request The current {@link ServerRequest} object.
     * @return The context object; if it does not exist, a new empty context is created and header information is set.
     */
    public static Context get(ServerRequest request) {
        Context context = (Context) request.attribute(Context.$).orElse(null);
        if (context == null) {
            context = new Context();
            // Extracts header information from the request
            Map<String, String> headers = request.headers().asHttpHeaders().toSingleValueMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            context.setHeaderMap(headers);
            request.attributes().put(Context.$, context);
        } else if (context.getHeaderMap() == null) {
            // If context exists but headerMap is null, also set header information
            Map<String, String> headers = request.headers().asHttpHeaders().toSingleValueMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            context.setHeaderMap(headers);
        }
        return context;
    }

}
