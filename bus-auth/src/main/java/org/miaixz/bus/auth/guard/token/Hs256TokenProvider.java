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
package org.miaixz.bus.auth.guard.token;

import org.miaixz.bus.auth.shared.jwt.JWT;

/**
 * Default {@link ServiceTokenProvider} that uses {@link ServiceTokenIssuer} for HS256 signing and
 * {@link ServiceTokenVerifier} for signature verification.
 *
 * @author Kimi Liu
 */
public final class Hs256TokenProvider implements ServiceTokenProvider {

    /**
     * Creates the default HS256 token provider.
     */
    public Hs256TokenProvider() {
        // No initialization required.
    }

    /**
     * Signs the claims selected by the service identity evaluator.
     *
     * @param issue trusted claims and lifetime
     * @param key   current signing material
     * @return compact signed service token
     */
    @Override
    public String issue(Issue issue, ServiceKeyProvider.Key key) {
        return new ServiceTokenIssuer(issue.subject(), issue.audience(), key.secret(), issue.lifetime(), issue.origin(),
                issue.asset()).sign();
    }

    /**
     * Verifies the signature against the selected key. The evaluator enforces the route and claim rules.
     *
     * @param compact compact service token
     * @param key     trusted verification material
     * @return authenticated JWT or null when the signature or compact format is invalid
     */
    @Override
    public JWT verify(String compact, ServiceKeyProvider.Key key) {
        return ServiceTokenVerifier.verify(compact, key.secret());
    }

}
