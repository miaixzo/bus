/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
     * as a template, dispatching to the appropriate specific validation method (e.g., {@link #token(Principal)},
     * {@link #apiKey(Principal)}, or {@link #license(Principal)}) based on the principal's type (policy).
     * <p>
     * Policy dispatch logic:
     * <ul>
     * <li>Policy 1-3: Token-based authentication (policy 3 includes license verification)</li>
     * <li>Policy 4-6: ApiKey-based authentication (policy 6 includes license verification)</li>
     * </ul>
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

        final Integer type = principal.getType();

        // Dispatch based on the policy type:
        // Policy 1-3: Token-based (1: basic, 2: with permissions, 3: with permissions and license)
        // Policy 4-6: ApiKey-based (4: basic, 5: with permissions, 6: with permissions and license)
        final boolean isTokenBased = Consts.ONE.equals(type) || Consts.TWO.equals(type) || Consts.THREE.equals(type);
        final boolean isApiKeyBased = Consts.FOUR.equals(type) || Consts.FIVE.equals(type) || Consts.SIX.equals(type);

        // For policy 3 or 6: license verification required
        final boolean requiresLicense = Consts.THREE.equals(type) || Consts.SIX.equals(type);

        // Dispatch based on the credential type using an if-else if chain,
        // as case labels in a switch must be compile-time constants.
        if (isTokenBased) {
            // If license verification is required (policy 3), validate license first, then token
            Mono<Delegate> chain = requiresLicense ? this.license(principal)
                    .flatMap(delegate -> delegate.isOk() ? this.token(principal) : Mono.just(delegate)) : Mono.empty();

            return chain.switchIfEmpty(this.token(principal));
        } else if (isApiKeyBased) {
            // If license verification is required (policy 6), validate license first, then apiKey
            Mono<Delegate> chain = requiresLicense ? this.license(principal)
                    .flatMap(delegate -> delegate.isOk() ? this.apiKey(principal) : Mono.just(delegate)) : Mono.empty();

            return chain.switchIfEmpty(this.apiKey(principal));
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
     * Used for policy 1-3 (Token-based authentication).
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
     * Used for policy 4-6 (ApiKey-based authentication).
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

    /**
     * Asynchronously validates a license-based principal for enhanced security.
     * <p>
     * Used for policy 3 (Token with license) and policy 6 (ApiKey with license).
     * <p>
     * <strong>Warning:</strong> The default implementation of this method provides no security and always returns a
     * successful result. It is a placeholder and **must be overridden** with actual validation logic, such as license
     * key verification against a database or license server.
     *
     * @param principal The {@link Principal} object containing the license credential.
     * @return A {@code Mono<Delegate>} emitting the authorization result.
     */
    default Mono<Delegate> license(Principal principal) {
        Logger.debug(
                true,
                "Authorize",
                "Executing default `license` method. This provides no security and should be overridden.");
        // Wrap the synchronous default result in Mono.just()
        return Mono.just(
                Delegate.builder()
                        .message(
                                Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                                        .errmsg(ErrorCode._SUCCESS.getValue()).build())
                        .authorize(Authorize.builder().build()).build());
    }

}
