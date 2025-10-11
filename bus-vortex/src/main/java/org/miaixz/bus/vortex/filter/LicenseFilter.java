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
package org.miaixz.bus.vortex.filter;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.LicenseProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * License validation filter.
 * <p>
 * As one of the highest priority filters in the system, it enforces license validity checks at the very beginning of
 * all requests. If license validation fails, the request will be immediately interrupted and will not proceed to
 * subsequent business logic.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 4)
public class LicenseFilter extends AbstractFilter {

    /**
     * The license provider, used for handling the actual logic of license validation.
     */
    private final LicenseProvider provider;

    /**
     * Constructs a {@code LicenseFilter} with the specified license validation provider.
     *
     * @param provider The license provider responsible for handling license validation.
     */
    public LicenseFilter(LicenseProvider provider) {
        this.provider = provider;
    }

    /**
     * The core execution method of the filter.
     * <p>
     * This method extracts the hostname and port (Authority) from the current request and passes it as the validation
     * subject to the {@link LicenseProvider}. If the {@code provider.validate} method fails, it should throw an
     * exception, which will be caught by the framework's global exception handler, thereby interrupting the request and
     * returning a unified error response.
     * </p>
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} representing the completion of the asynchronous filtering operation.
     * @throws ValidateException If license validation fails.
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        Assets assets = getAssets(context);

        // Get the domain name and port
        Optional<String> authorityOptional = getOriginalAuthority(exchange.getRequest());

        // Use Optional's API to handle the result
        String authority = authorityOptional.orElseThrow(() -> {
            // If Optional is empty, it means no valid host information was found, construct and throw an exception
            String errorMessage = "Unable to determine the request authority (host:port) for license validation. The request will be ignored.";
            Logger.error("==>     Filter: {}", errorMessage);
            throw new ValidateException(ErrorCode._100527);
        });

        Logger.info("==>     Filter: Validating license for authority: {}", authority);
        if (Consts.TWO == assets.getFirewall()) {
            // Call the provider to perform validation.
            // If validation fails, provider.validate(authority) should throw an exception.
            this.provider.validate(authority);
        }

        // Validation passed, request continues.
        return chain.filter(exchange);
    }

}
