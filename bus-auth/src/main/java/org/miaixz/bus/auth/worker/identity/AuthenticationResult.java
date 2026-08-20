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

import java.util.List;

import org.miaixz.bus.auth.Evidence;
import org.miaixz.bus.auth.Principal;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.shared.claim.ClaimSet;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.core.lang.Assert;

/**
 * Carries the immutable protocol-neutral result of identity completion.
 * <p>
 * The result deliberately contains no business Session, cookie, application token, redirect, role decision, or raw
 * protocol credential. The external project decides how to establish its login state after receiving this value.
 * </p>
 *
 * @param subject   stable project Subject loaded by the project and accepted by the framework parser
 * @param principal framework authenticated-principal view
 * @param identity  verified Source-scoped external identity
 * @param claims    provider-neutral claims and their provenance
 * @author Kimi Liu
 */
public record AuthenticationResult(Subject subject, Principal principal, ExternalIdentity identity, ClaimSet claims) {

    /**
     * Creates an immutable completed authentication result.
     *
     * @throws IllegalArgumentException if any component is {@code null}
     */
    public AuthenticationResult {
        Assert.notNull(subject, "Authentication result Subject must not be null");
        Assert.notNull(principal, "Authentication result Principal must not be null");
        Assert.notNull(identity, "Authentication result external identity must not be null");
        Assert.notNull(claims, "Authentication result ClaimSet must not be null");
    }

    /**
     * Returns the immutable evidence retained by the verified external identity.
     *
     * @return verified authentication evidence
     */
    public List<Evidence> evidence() {
        return identity.evidence();
    }

}
