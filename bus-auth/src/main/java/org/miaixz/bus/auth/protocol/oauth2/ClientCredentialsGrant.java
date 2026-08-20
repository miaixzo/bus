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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Represents the parameters of an RFC 6749 client-credentials grant.
 * <p>
 * Client credentials are supplied by endpoint authentication and never become grant fields. This value contains only
 * the optional scope parameter defined for the grant.
 * </p>
 *
 * @param scope optional scope requested for the client's own authorization
 * @author Kimi Liu
 */
public record ClientCredentialsGrant(Optional<Scope> scope) implements TokenRequest.Grant {

    /**
     * Creates an immutable client-credentials grant.
     *
     * @throws IllegalArgumentException if the scope container is {@code null}
     */
    public ClientCredentialsGrant {
        Assert.notNull(scope, "OAuth 2.x client-credentials scope container must not be null");
        scope = Optional.ofNullable(scope.getOrNull());
    }

}
