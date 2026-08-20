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
package org.miaixz.bus.auth.resolver;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Resolves an immutable subject attribute snapshot from an external project.
 * <p>
 * Attribute keys retain their complete schema or claim names, including unknown extensions. This lookup port does not
 * mutate the Subject, translate attributes into protocol claims, or perform a Registry invocation.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface AttributeResolver {

    /**
     * Resolves the current attribute snapshot for one Subject reference.
     *
     * @param request stable internal Subject key
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing successful attributes, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Attributes>> resolve(Subject.Key request, Context context, Timeout.Budget timeout);

    /**
     * Carries an immutable provider-neutral JSON attribute object.
     *
     * @param values complete attribute values keyed by their original schema or claim names
     * @author Kimi Liu
     */
    record Attributes(JsonValue.ObjectValue values) {

        /**
         * Creates a detached immutable attribute snapshot.
         *
         * @param values complete provider-neutral attribute object
         * @throws IllegalArgumentException if {@code values} is {@code null}
         */
        public Attributes {
            Assert.notNull(values, "Resolved subject attributes must not be null");
            values = new JsonValue.ObjectValue(values.values());
        }

    }

}
