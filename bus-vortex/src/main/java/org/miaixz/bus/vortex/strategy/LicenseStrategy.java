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
package org.miaixz.bus.vortex.strategy;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.LicenseProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * A strategy that enforces license validation based on the request's authority (domain and port).
 * <p>
 * This strategy acts as a gatekeeper for commercial deployments, ensuring that incoming requests are targeted at a host
 * that holds a valid license. It collaborates with a {@link LicenseProvider}, which encapsulates the actual logic for
 * checking the license status (e.g., by consulting a database, a file, or a remote service).
 * <p>
 * It is ordered to run after {@link AuthorizeStrategy}, as license validation is typically a broader,
 * infrastructure-level check performed after the caller's identity has been established.
 *
 * @author Kimi Liu
 * @see LicenseProvider
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 7)
public class LicenseStrategy extends AbstractStrategy {

    /**
     * The provider responsible for the actual logic of license validation.
     */
    private final LicenseProvider provider;

    /**
     * Constructs a new {@code LicenseStrategy}.
     *
     * @param provider The license provider, which must not be {@code null}.
     */
    public LicenseStrategy(LicenseProvider provider) {
        this.provider = provider;
    }

    /**
     * Applies the license validation logic.
     * <p>
     * This method orchestrates the validation by performing the following steps:
     * <ol>
     * <li>Retrieves the {@link Context} from the reactive stream.</li>
     * <li>Extracts the request's authority (domain and port) from the context, which was populated by
     * {@link RequestStrategy}.</li>
     * <li>Checks if the authority is present; if not, it throws an exception as validation is impossible.</li>
     * <li>Checks the {@code firewall} flag on the API {@link Assets}. If the flag indicates a license check is
     * required, it calls the {@link LicenseProvider#validate(String)} method.</li>
     * <li>If validation is successful, it proceeds to the next strategy in the chain. If validation fails, the provider
     * is expected to throw an exception, which will be caught by the global error handler.</li>
     * </ol>
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @return A {@code Mono<Void>} that signals the completion of this strategy.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, StrategyChain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            final Assets assets = context.getAssets();

            // The request authority (domain:port) is expected to be populated by a preceding strategy.
            String authority = context.getX_request_domain();

            if (StringKit.isEmpty(authority)) {
                String errorMessage = "Unable to determine the request authority (host:port) for license validation. The request will be ignored.";
                Logger.error("==>     Filter: {}", errorMessage);
                throw new ValidateException(ErrorCode._100527);
            }

            // The 'firewall' flag on the asset determines if a license check is mandatory.
            // A value of '2' might indicate a strict, license-required endpoint.
            if (Consts.TWO == assets.getFirewall()) {
                Logger.info("==>     Filter: Validating license for authority: {}", authority);
                // Delegate the actual validation logic to the provider.
                // The provider should throw an exception on validation failure.
                this.provider.validate(authority);
            }

            // If validation is not required or is successful, continue the chain.
            return chain.apply(exchange);
        });
    }

}
