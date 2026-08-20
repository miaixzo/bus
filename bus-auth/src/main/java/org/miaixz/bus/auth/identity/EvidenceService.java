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

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.core.lang.Assert;

/**
 * Evaluates verified authentication evidence for a resolved external identity and stable Subject.
 * <p>
 * Input arrives only after ExternalIdentityService validation and account-link resolution. Raw protocol responses,
 * Vendor payloads, passwords, and tokens are prohibited from this port, which neither maps claims nor creates sessions.
 * </p>
 *
 * @author Kimi Liu
 */
public interface EvidenceService {

    /**
     * Evaluates immutable verified evidence for one resolved identity.
     *
     * @param request verified external identity and stable Subject
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing immutable evidence, expected rejection, or operational failure
     */
    CompletionStage<Outcome<List<Evidence>>> evaluate(Request request, Context context, Timeout.Budget timeout);

    /**
     * Carries verified evidence-evaluation input.
     *
     * @param identity verified Source-scoped external identity
     * @param subject  stable resolved Subject
     * @author Kimi Liu
     */
    record Request(ExternalIdentity identity, Subject subject) {

        /**
         * Creates immutable evidence-evaluation input.
         *
         * @param identity verified external identity
         * @param subject  stable Subject
         * @throws IllegalArgumentException if either component is {@code null}
         */
        public Request {
            Assert.notNull(identity, "Evidence request external identity must not be null");
            Assert.notNull(subject, "Evidence request Subject must not be null");
        }

    }

}
