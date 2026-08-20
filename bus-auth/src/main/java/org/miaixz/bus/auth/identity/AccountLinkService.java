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

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.ExternalIdentity;

/**
 * Resolves a verified Source-scoped external identity to an opaque internal Subject reference.
 * <p>
 * The external project implements explicit account-link policy and persistence. Stable Source identifier plus
 * Source-local subject is the primary external identity key; unverified email or display-name similarity must never
 * produce silent linking. This port does not create Subjects, Principals, Sessions, roles, or permissions.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface AccountLinkService {

    /**
     * Resolves one verified external identity to its opaque Subject reference.
     *
     * @param request verified Source-scoped external identity
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a Subject reference, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Subject.Reference>> resolve(
            ExternalIdentity request,
            Context context,
            Timeout.Budget timeout);

}
