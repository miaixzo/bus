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

import reactor.core.publisher.Mono;

/**
 * A Service Provider Interface (SPI) for performing authentication and authorization.
 * <p>
 * This interface defines the contract for validating credentials. Implementations of this interface should contain the
 * actual business logic for checking tokens or API keys against a database, an authentication server, or any other
 * identity provider. An instance of this provider is injected into protocol-specific qualifier strategies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface AuthorizeProvider {

    /**
     * Asynchronously validates the provided principal and performs the authorization process. This default method acts
     * as a template, dispatching to the appropriate specific validation method (e.g., {@link #token(Principal)},
     * {@link #apiKey(Principal)}, or {@link #license(Principal)}) based on the principal's credential type.
     * <p>
     * Policy dispatch logic:
     * <ul>
     * <li>Type 1: Token-based authentication</li>
     * <li>Type 2: API key-based authentication</li>
     * <li>Route policy 3: Includes license verification before the selected credential check</li>
     * </ul>
     * <p>
     * This method can be overridden to handle custom credential types or more complex dispatching logic.
     *
     * @param principal The {@link Principal} object containing the credential to be validated.
     * @return A {@code Mono<Delegate>} emitting the authorization result.
     */
    default Mono<Delegate> authorize(Principal principal) {
        if (ObjectKit.isEmpty(principal)) {
            Logger.warn(false, "Vortex", "Access validation failed: principal entity is null or empty.");
            return Mono.error(new ValidateException(ErrorCode._100806));
        }

        final Integer type = principal.getType();
        final Integer policy = principal.getContext() == null || principal.getContext().getAssets() == null ? null
                : principal.getContext().getAssets().getPolicy();
        if (policy != null && (policy < Consts.ONE || policy > Consts.THREE)) {
            Logger.warn(false, "Vortex", "Unsupported assets policy: {}. Route policy must be in range 1..3.", policy);
            return Mono.just(
                    Delegate.builder()
                            .message(
                                    Message.builder().errcode(ErrorCode._116002.getKey())
                                            .errmsg("Unsupported policy: " + policy).build())
                            .build());
        }

        final boolean isTokenBased = Consts.ONE.equals(type);
        final boolean isApiKeyBased = Consts.TWO.equals(type);

        final boolean requiresLicense = Consts.THREE.equals(policy);

        if (isTokenBased) {
            Mono<Delegate> chain = requiresLicense ? this.license(principal)
                    .flatMap(delegate -> delegate.isOk() ? this.token(principal) : Mono.just(delegate)) : Mono.empty();

            return chain.switchIfEmpty(this.token(principal));
        } else if (isApiKeyBased) {
            Mono<Delegate> chain = requiresLicense ? this.license(principal)
                    .flatMap(delegate -> delegate.isOk() ? this.apiKey(principal) : Mono.just(delegate)) : Mono.empty();

            return chain.switchIfEmpty(this.apiKey(principal));
        }
        Logger.warn(
                false,
                "Vortex",
                "Unsupported principal type: {}. Override the 'authorize' method to handle it.",
                principal.getType());
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
     * Used when principal type is {@link Consts#ONE}.
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
                "Vortex",
                "Executing default credential validation method. This provides no security and should be overridden.");
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
     * Used when principal type is {@link Consts#TWO}.
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
                "Vortex",
                "Executing default `apiKey` method. This provides no security and should be overridden.");
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
     * Used before selected credential validation when route policy is {@link Consts#THREE}.
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
                "Vortex",
                "Executing default `license` method. This provides no security and should be overridden.");
        return Mono.just(
                Delegate.builder()
                        .message(
                                Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                                        .errmsg(ErrorCode._SUCCESS.getValue()).build())
                        .authorize(Authorize.builder().build()).build());
    }

}
