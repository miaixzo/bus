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
package org.miaixz.bus.auth.worker;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Session;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;

/**
 * Integrates the framework's protocol session lifecycle with project-owned login state.
 * <p>
 * Implementations are project code and must be idempotent. They may map the minimal framework {@link Session} to a
 * database, web session, cookie, or SSO record, but no such business model is defined or implemented by bus-auth.
 * </p>
 */
public interface SessionWorker {

    /**
     * Records or confirms one active authentication session.
     * <p>
     * Implementations must be idempotent for the same binding because framework retries and concurrent protocol
     * requests may repeat the operation.
     * </p>
     *
     * @param binding exact Source and framework Session binding
     * @param context current authentication invocation context
     * @param timeout shared end-to-end operation budget
     * @return asynchronous project integration outcome
     */
    CompletionStage<Outcome<Void>> establish(Binding binding, Context context, Timeout.Budget timeout);

    /**
     * Ends the project login state mapped to one framework session.
     * <p>
     * Implementations must be idempotent. The framework deliberately repeats this operation while the corresponding
     * Session is {@link Session.State#ENDING}; success means the project state is absent or conclusively ended.
     * </p>
     *
     * @param binding exact Source and framework Session binding
     * @param context current authentication invocation context
     * @param timeout shared end-to-end operation budget
     * @return asynchronous project integration outcome
     */
    CompletionStage<Outcome<Void>> end(Binding binding, Context context, Timeout.Budget timeout);

    /**
     * Carries the exact compiled Source identity with its framework Session.
     * <p>
     * The Source identifier prevents equal Session keys issued by different Sources from colliding in project-owned
     * login state. This value is an integration coordinate only; it does not define a project business Session model.
     * </p>
     *
     * @param sourceId exact registered Source identifier
     * @param session  immutable framework Session state
     */
    record Binding(String sourceId, Session session) {

        /**
         * Creates a validated Source-to-Session binding.
         */
        public Binding {
            Assert.notBlank(sourceId, "Session worker Source id must not be blank");
            Assert.notNull(session, "Session worker Session must not be null");
        }
    }
}
