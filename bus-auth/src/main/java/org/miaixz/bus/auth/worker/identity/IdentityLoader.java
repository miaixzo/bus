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

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Project identity input port that loads or explicitly establishes one stable subject record.
 */
@FunctionalInterface
public interface IdentityLoader {

    /**
     * Loads or establishes one stable subject for a verified external identity.
     *
     * @param identity verified completed external identity
     * @param context  immutable non-secret invocation context
     * @param timeout  shared end-to-end operation budget
     * @return asynchronous project identity-loading outcome
     */
    CompletionStage<Outcome<Record>> load(ExternalIdentity identity, Context context, Timeout.Budget timeout);

    /**
     * Project-adapted subject data awaiting framework parsing.
     *
     * @param key        stable subject key established by the project
     * @param reference  protocol-neutral subject reference
     * @param attributes detached project-owned subject attributes
     */
    record Record(Subject.Key key, Subject.Reference reference, JsonValue.ObjectValue attributes) {

    }

}
