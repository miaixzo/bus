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
package org.miaixz.bus.auth.worker.identity;

import org.miaixz.bus.auth.Principal;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.shared.claim.ClaimSet;
import org.miaixz.bus.core.lang.Assert;

/**
 * Constructs the framework Principal without applying project authorization or session policy.
 *
 * @author Kimi Liu
 */
final class PrincipalFactory {

    /**
     * Creates the stateless factory.
     */
    PrincipalFactory() {
        // No initialization required.
    }

    /**
     * Constructs one Principal using the stable Subject key as its unambiguous framework name.
     *
     * @param subject stable resolved Subject
     * @param claims  verified provider-neutral claims
     * @return immutable authenticated Principal
     */
    Principal create(final Subject subject, final ClaimSet claims) {
        final Subject checked = Assert.notNull(subject, "Principal Subject must not be null");
        final ClaimSet verified = Assert.notNull(claims, "Principal ClaimSet must not be null");
        return new Principal(checked.key().value(), verified.values());
    }

}
