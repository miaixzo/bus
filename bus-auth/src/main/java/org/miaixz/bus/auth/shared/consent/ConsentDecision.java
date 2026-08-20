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
package org.miaixz.bus.auth.shared.consent;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Captures an external consent decision and the exact subset of requested OAuth scopes approved by the subject.
 *
 * @param request       immutable authorization context presented for decision
 * @param status        explicit approval or denial
 * @param grantedScopes exact approved scope subset, empty for denial
 * @author Kimi Liu
 */
public record ConsentDecision(ConsentRequest request, Status status, Set<String> grantedScopes) {

    /**
     * Validates that approval narrows the request and denial grants no scope.
     *
     * @throws IllegalArgumentException if a component is {@code null}
     * @throws ValidateException        if approved scopes are empty/expanded or denied scopes are non-empty
     */
    public ConsentDecision {
        Assert.notNull(request, "Consent decision request must not be null");
        Assert.notNull(status, "Consent decision status must not be null");
        Assert.notNull(grantedScopes, "Consent decision granted scopes must not be null");
        grantedScopes = Set.copyOf(grantedScopes);
        if (status == Status.APPROVED) {
            if (grantedScopes.isEmpty()) {
                throw new ValidateException("Approved consent must grant at least one requested scope");
            }
            new ScopeValidator().validateGranted(List.copyOf(grantedScopes), List.copyOf(request.scopes()));
        } else if (!grantedScopes.isEmpty()) {
            throw new ValidateException("Denied consent must not grant scopes");
        }
    }

    /**
     * Creates an approved decision for an explicit requested-scope subset.
     *
     * @param request       decided authorization context
     * @param grantedScopes non-empty requested-scope subset
     * @return approved consent decision
     */
    public static ConsentDecision approve(final ConsentRequest request, final Set<String> grantedScopes) {
        return new ConsentDecision(request, Status.APPROVED, grantedScopes);
    }

    /**
     * Creates a denial that grants no requested scope.
     *
     * @param request decided authorization context
     * @return denied consent decision
     */
    public static ConsentDecision deny(final ConsentRequest request) {
        return new ConsentDecision(request, Status.DENIED, Set.of());
    }

    /**
     * Enumerates the only application-level consent outcomes.
     *
     * @author Kimi Liu
     */
    public enum Status {
        /**
         * The subject approved an explicit scope subset.
         */
        APPROVED,
        /**
         * The subject denied the authorization request.
         */
        DENIED

    }

}
