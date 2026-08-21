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
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Loads project-maintained subject attributes.
 */
@FunctionalInterface
public interface AttributeLoader {

    /**
     * Loads subject attributes within the exact Source registration scope.
     *
     * @param registration exact Source registration requesting the data
     * @param subject      exact external subject key
     * @param context      immutable non-secret invocation context
     * @param timeout      shared end-to-end operation budget
     * @return asynchronous project loading outcome
     */
    CompletionStage<Outcome<Record>> load(
            Registration.SourceEntry registration,
            Subject.Key subject,
            Context context,
            Timeout.Budget timeout);

    /**
     * Loaded subject attributes awaiting framework parsing.
     *
     * @param sourceId exact Source identifier that owns the returned data
     * @param subject  exact subject key resolved by the project
     * @param values   project-provided attribute object
     */
    record Record(String sourceId, Subject.Key subject, JsonValue.ObjectValue values) {

    }

}
