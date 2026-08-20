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

/**
 * Defines externally implemented lifecycle operations for the single framework Session model.
 * <p>
 * Implementations use their injected Clock, atomic SessionStore, and the existing operation budget. This service does
 * not define cookies, protocol tokens, renewal semantics, a second session entity, or a persistence implementation.
 * </p>
 *
 * @author Kimi Liu
 */
public interface SessionService {

    /**
     * Creates one framework Session for an authenticated Principal.
     *
     * @param request authenticated Principal
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing the created Session, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Session>> create(Principal request, Context context, Timeout.Budget timeout);

    /**
     * Retrieves one existing framework Session by its stable key.
     *
     * @param request framework Session key
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing the Session, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Session>> get(Session.Key request, Context context, Timeout.Budget timeout);

    /**
     * Ends one existing framework Session without producing a second session model.
     *
     * @param request framework Session key
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing success, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Void>> end(Session.Key request, Context context, Timeout.Budget timeout);

}
