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
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.claim.ClaimSet;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.core.lang.Assert;

/**
 * Loads project-disclosed claim records for one verified identity and stable subject.
 */
@FunctionalInterface
public interface ClaimLoader {

    /**
     * Loads project-disclosed claims for a stable subject and verified external identity.
     *
     * @param request validated claim-loading coordinates
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return asynchronous project loading outcome
     */
    CompletionStage<Outcome<Record>> load(Request request, Context context, Timeout.Budget timeout);

    /**
     * Binds the stable subject to the verified external identity used for claim loading.
     *
     * @param subject  stable framework subject
     * @param identity verified completed external identity
     */
    record Request(Subject subject, ExternalIdentity identity) {

        /**
         * Validates one complete claim-loading request.
         */
        public Request {
            Assert.notNull(subject, "Claim loading Subject must not be null");
            Assert.notNull(identity, "Claim loading external identity must not be null");
        }

    }

    /**
     * Project-loaded claim entries awaiting framework parsing.
     *
     * @param entries ordered claim entries disclosed by the project
     */
    record Record(List<ClaimSet.Entry> entries) {

    }

}
