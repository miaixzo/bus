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
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Resolves an externally managed immutable client registration view.
 * <p>
 * External projects implement persistence and lookup. The returned record contains registered protocol values and an
 * optional typed credential reference, never plaintext material. Expected lookup rejection and operational failure are
 * represented by {@link Outcome} so resolver stages do not encode protocol wire errors.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ClientResolver {

    /**
     * Resolves one client registration from an externally scoped lookup value.
     *
     * @param request non-blank client lookup value
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a successful client, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Client>> resolve(String request, Context context, Timeout.Budget timeout);

    /**
     * Carries one immutable registered client view without credential material.
     *
     * @param id            registered client identifier
     * @param credential    optional typed external credential reference
     * @param redirectUris  registered redirect URI lexical values
     * @param grantTypes    registered grant type lexical values
     * @param responseTypes registered response type lexical values
     * @param scopes        registered scope-token lexical values
     * @param metadata      immutable non-sensitive externally managed metadata
     * @author Kimi Liu
     */
    record Client(String id, Optional<Credential.Reference> credential, List<String> redirectUris,
            Set<String> grantTypes, Set<String> responseTypes, Set<String> scopes, JsonValue.ObjectValue metadata) {

        /**
         * Creates a detached immutable client registration view.
         *
         * @param id            registered client identifier
         * @param credential    optional typed credential reference
         * @param redirectUris  registered redirect URI lexical values
         * @param grantTypes    registered grant type lexical values
         * @param responseTypes registered response type lexical values
         * @param scopes        registered scope-token lexical values
         * @param metadata      immutable non-sensitive metadata
         * @throws IllegalArgumentException if a required component or collection entry is {@code null}
         * @throws ValidateException        if text is blank or a collection contains duplicate values
         */
        public Client {
            Assert.notBlank(id, "Client identifier must not be blank");
            Assert.notNull(credential, "Client credential container must not be null");
            credential = Optional.ofNullable(credential.getOrNull());
            redirectUris = immutableList(redirectUris, "Client redirect URI");
            grantTypes = immutableSet(grantTypes, "Client grant type");
            responseTypes = immutableSet(responseTypes, "Client response type");
            scopes = immutableSet(scopes, "Client scope");
            Assert.notNull(metadata, "Client metadata must not be null");
            metadata = new JsonValue.ObjectValue(metadata.values());
        }

        /**
         * Copies and validates an ordered lexical value list.
         *
         * @param values source values
         * @param label  semantic entry label
         * @return immutable ordered values
         * @throws IllegalArgumentException if the list or an entry is {@code null}
         * @throws ValidateException        if an entry is blank or duplicated
         */
        private static List<String> immutableList(final List<String> values, final String label) {
            Assert.notNull(values, label + " list must not be null");
            final List<String> copy = new ArrayList<>(values.size());
            final Set<String> unique = new HashSet<>(values.size());
            for (String value : values) {
                Assert.notBlank(value, label + " must not be blank");
                if (!unique.add(value)) {
                    throw new ValidateException(label + " list must not contain duplicates");
                }
                copy.add(value);
            }
            return List.copyOf(copy);
        }

        /**
         * Copies and validates an unordered lexical value set.
         *
         * @param values source values
         * @param label  semantic entry label
         * @return immutable values
         * @throws IllegalArgumentException if the set or an entry is {@code null} or blank
         */
        private static Set<String> immutableSet(final Set<String> values, final String label) {
            Assert.notNull(values, label + " set must not be null");
            for (String value : values) {
                Assert.notBlank(value, label + " must not be blank");
            }
            return Set.copyOf(values);
        }

    }

}
