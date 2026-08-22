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
package org.miaixz.bus.auth.protocol.oauth2;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Defines the closed RFC 6749 authorization-endpoint response union.
 * <p>
 * A response is either a successful authorization code result or an authorization error. The union cannot represent a
 * mixed success/error object and does not include token-endpoint response models.
 * </p>
 *
 * @author Kimi Liu
 */
public interface AuthorizationResponse {

    /**
     * Returns the exact request state carried by either response variant.
     *
     * @return optional state parameter
     */
    Optional<String> state();

    /**
     * Returns registered extension parameters not represented by the concrete response components.
     *
     * @return immutable extension parameters
     */
    JsonValue.ObjectValue extensions();

    /**
     * Returns the optional RFC 9207 authorization-response issuer extension.
     *
     * @return optional issuer parameter
     */
    default Optional<String> issuer() {
        final JsonValue value = extensions().values().get(OAuth2.Parameters.ISS);
        return value instanceof JsonValue.StringValue text ? Optional.of(text.value()) : Optional.empty();
    }

    /**
     * Returns the optional authorization-response scope extension when supplied by the authorization server.
     *
     * @return optional validated scope
     */
    default Optional<Scope> scope() {
        final JsonValue value = extensions().values().get(OAuth2.Parameters.SCOPE);
        return value instanceof JsonValue.StringValue text ? Optional.of(Scope.parse(text.value())) : Optional.empty();
    }

}
