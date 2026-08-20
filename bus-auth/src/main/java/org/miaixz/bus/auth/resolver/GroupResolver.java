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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Resolves immutable group membership for a stable Subject from an external project.
 * <p>
 * Group identifiers are data consumed by claim, SCIM, and LDAP services. They do not create role, authority,
 * permission, or operator-access semantics inside bus-auth, and this port performs no group mutation.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface GroupResolver {

    /**
     * Resolves current group identifiers for one Subject reference.
     *
     * @param request opaque external Subject reference
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing successful groups, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Groups>> resolve(Subject.Reference request, Context context, Timeout.Budget timeout);

    /**
     * Carries immutable group identifiers in externally stable order.
     *
     * @param values stable group identifiers
     * @author Kimi Liu
     */
    record Groups(List<String> values) {

        /**
         * Creates a detached immutable group-membership snapshot.
         *
         * @param values stable group identifiers
         * @throws IllegalArgumentException if the list or an entry is {@code null}
         * @throws ValidateException        if an identifier is blank or duplicated
         */
        public Groups {
            Assert.notNull(values, "Resolved group list must not be null");
            final List<String> copy = new ArrayList<>(values.size());
            final Set<String> unique = new HashSet<>(values.size());
            for (String value : values) {
                Assert.notBlank(value, "Resolved group identifier must not be blank");
                if (!unique.add(value)) {
                    throw new ValidateException("Resolved group identifiers must not contain duplicates");
                }
                copy.add(value);
            }
            values = List.copyOf(copy);
        }

    }

}
