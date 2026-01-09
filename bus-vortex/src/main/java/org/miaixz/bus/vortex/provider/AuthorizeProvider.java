/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.magic.Delegate;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.magic.Principal;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;
import reactor.core.publisher.Mono;

/**
 * A Service Provider Interface (SPI) for performing authentication and authorization.
 * <p>
 * This interface defines the contract for validating credentials. Implementations of this interface should contain the
 * actual business logic for checking tokens or API keys against a database, an authentication server, or any other
 * identity provider. An instance of this provider is injected into the {@link QualifierStrategy}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface AuthorizeProvider {

    /**
     * Asynchronously validates the provided principal and performs the authorization process. This default method acts
     * as a template, dispatching to the appropriate specific validation method (e.g., {@link #token(Principal)} or
     * {@link #apiKey(Principal)}) based on the principal's type.
     * <p>
     * This method can be overridden to handle custom credential types or more complex dispatching logic.
     *
     * @param principal The {@link Principal} object containing the credential to be validated.
     * @return A {@code Mono<Delegate>} emitting the authorization result.
     */
    default Mono<Delegate> authorize(Principal principal) {
        if (ObjectKit.isEmpty(principal)) {
            Logger.warn(false, "Authorize", "Authorization failed: The principal entity is null or empty.");
            // Return a Mono that signals an error immediately.
            return Mono.error(new ValidateException(ErrorCode._100806));
        }

        // Determine acceptable credential types based on policy
        // Policy 1, 2: Token ONLY (reject API Key even if provided)
        // Policy 3, 4: API Key ONLY (reject Token even if provided)
        // Policy 5: Token or API Key (both accepted)
        final Integer type = principal.getType();
        final boolean acceptToken = Consts.ONE.equals(type) || Consts.TWO.equals(type) || Consts.FIVE.equals(type);
        final boolean acceptApiKey = Consts.THREE.equals(type) || Consts.FOUR.equals(type) || Consts.FIVE.equals(type);

        // Dispatch based on the credential type using an if-else if chain,
        // as case labels in a switch must be compile-time constants.
        if (acceptToken) {
            return this.token(principal); // Now returns Mono<Delegate>
        } else if (acceptApiKey) {
            return this.apiKey(principal); // Now returns Mono<Delegate>
        }
        Logger.warn(
                false,
                "Authorize",
                "Unsupported principal type: {}. Override the 'authorize' method to handle it.",
                principal.getType());
        // Return a Mono emitting a Delegate with the error information.
        return Mono.just(
                Delegate.builder()
                        .message(
                                Message.builder().errcode(ErrorCode._116002.getKey())
                                        .errmsg("Unsupported credential type: " + principal.getType()).build())
                        .build());
    }

    /**
     * Asynchronously validates a token-based principal (e.g., JWT, Opaque Token).
     * <p>
     * <strong>Warning:</strong> The default implementation of this method provides no security and always returns a
     * successful result. It is a placeholder and **must be overridden** with actual validation logic, such as JWT
     * signature verification, introspection against an OAuth2 server, or a database/cache lookup.
     *
     * @param principal The {@link Principal} object containing the token.
     * @return A {@code Mono<Delegate>} emitting the authorization result.
     */
    default Mono<Delegate> token(Principal principal) {
        Logger.debug(
                true,
                "Authorize",
                "Executing default `token` method. This provides no security and should be overridden.");
        // Wrap the synchronous default result in Mono.just()
        return Mono.just(
                Delegate.builder()
                        .message(
                                Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                                        .errmsg(ErrorCode._SUCCESS.getValue()).build())
                        .authorize(Authorize.builder().build()).build());
    }

    /**
     * Asynchronously validates an API key-based principal.
     * <p>
     * <strong>Warning:</strong> The default implementation of this method provides no security and always returns a
     * successful result. It is a placeholder and **must be overridden** with actual validation logic, such as looking
     * up the API key in a database and checking its permissions.
     *
     * @param principal The {@link Principal} object containing the API key.
     * @return A {@code Mono<Delegate>} emitting the authorization result.
     */
    default Mono<Delegate> apiKey(Principal principal) {
        Logger.debug(
                true,
                "Authorize",
                "Executing default `apiKey` method. This provides no security and should be overridden.");
        // Wrap the synchronous default result in Mono.just()
        return Mono.just(
                Delegate.builder()
                        .message(
                                Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                                        .errmsg(ErrorCode._SUCCESS.getValue()).build())
                        .authorize(Authorize.builder().build()).build());
    }

}
