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
package org.miaixz.bus.cortex.guard.token;

import java.time.Duration;

import org.miaixz.bus.auth.shared.jwt.JWT;

/**
 * Cryptographically issues and verifies service tokens using framework-selected claims and trusted keys.
 * Implementations are trusted application components. The framework applies transport rules and verifies claims.
 *
 * @author Kimi Liu
 */
public interface ServiceTokenProvider {

    /**
     * Produces a compact HS256 JWT with the supplied service claims.
     *
     * @param request claims and lifetime assigned by the service identity evaluator
     * @param key     current trusted signing material
     * @return the signed compact token
     * @throws IllegalStateException if the provider cannot sign
     */
    String issue(Issue request, ServiceKeyProvider.Key key);

    /**
     * Verifies a compact token cryptographically using the indicated key.
     *
     * @param compact compact service token
     * @param key     trusted verification candidate
     * @return the verified JWT or null if verification fails for this key
     * @throws IllegalStateException if the provider is unavailable
     */
    JWT verify(String compact, ServiceKeyProvider.Key key);

    /**
     * Claims supplied by the framework to a token signer.
     *
     * @param subject  required service issuer and subject
     * @param audience next-hop service audience
     * @param lifetime maximum token lifetime
     * @param origin   previous-hop service authenticated by this hop, when forwarding
     * @param asset    exact route selected by the forwarding hop, when bound to an asset
     */
    record Issue(String subject, String audience, Duration lifetime, String origin, String asset) {

    }

}
