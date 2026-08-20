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
package org.miaixz.bus.auth.identity;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.shared.claim.ClaimSet;
import org.miaixz.bus.core.lang.Assert;

/**
 * Constructs an authenticated provider-neutral Principal from a stable Subject and verified claims.
 * <p>
 * The external project implements presentation and persistence integration. This port does not parse protocol messages,
 * create sessions, define roles or authorities, or evaluate application permissions.
 * </p>
 *
 * @author Kimi Liu
 */
public interface IdentityService {

    /**
     * Constructs the authenticated Principal for one stable Subject and ClaimSet.
     *
     * @param request immutable Principal construction input
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a successful Principal, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Principal>> principal(Request request, Context context, Timeout.Budget timeout);

    /**
     * Carries the stable Subject and verified claims used to construct a Principal.
     *
     * @param subject stable resolved Subject
     * @param claims  verified provider-neutral ClaimSet
     * @author Kimi Liu
     */
    record Request(Subject subject, ClaimSet claims) {

        /**
         * Creates immutable Principal construction input.
         *
         * @param subject stable Subject
         * @param claims  verified ClaimSet
         * @throws IllegalArgumentException if either component is {@code null}
         */
        public Request {
            Assert.notNull(subject, "Identity request Subject must not be null");
            Assert.notNull(claims, "Identity request ClaimSet must not be null");
        }

    }

}
