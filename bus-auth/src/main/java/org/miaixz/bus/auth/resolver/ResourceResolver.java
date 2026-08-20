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
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Resolves externally managed protected-resource descriptions from namespace, audience, and resource indicators.
 * <p>
 * JWT audience values and RFC 8707 resource indicators remain distinct ordered inputs. This resolver performs lookup
 * only; authorization evaluation, permission checks, and recursive Registry capability invocation are excluded.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ResourceResolver {

    /**
     * Copies ordered lexical values while preserving duplicates required by the owning protocol representation.
     *
     * @param values source lexical values
     * @param label  semantic entry label
     * @return immutable ordered values
     * @throws IllegalArgumentException if the list or an entry is {@code null} or blank
     */
    private static List<String> immutableValues(final List<String> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        final List<String> copy = new ArrayList<>(values.size());
        for (String value : values) {
            Assert.notBlank(value, label + " must not be blank");
            copy.add(value);
        }
        return List.copyOf(copy);
    }

    /**
     * Resolves one protected resource description.
     *
     * @param request immutable namespace and protocol-indicator query
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a successful resource, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Resource>> resolve(Query request, Context context, Timeout.Budget timeout);

    /**
     * Carries protected-resource lookup coordinates without conflating audience and resource indicators.
     *
     * @param namespaceId external persistence namespace identifier
     * @param audience    ordered JWT audience lexical values
     * @param resource    ordered RFC 8707 resource indicator lexical values
     * @author Kimi Liu
     */
    record Query(String namespaceId, List<String> audience, List<String> resource) {

        /**
         * Creates an immutable protected-resource query.
         *
         * @param namespaceId external persistence namespace identifier
         * @param audience    ordered JWT audience lexical values
         * @param resource    ordered RFC 8707 resource indicators
         * @throws IllegalArgumentException if the namespace is blank or a list or entry is {@code null} or blank
         */
        public Query {
            Assert.notBlank(namespaceId, "Resource query namespace id must not be blank");
            audience = immutableValues(audience, "Resource query audience");
            resource = immutableValues(resource, "Resource query indicator");
        }

    }

    /**
     * Carries one immutable protected-resource description returned by external management.
     *
     * @param id       stable protected-resource identifier
     * @param audience audience values identifying this resource
     * @param scopes   scope-token values supported by this resource
     * @param metadata immutable non-sensitive external metadata
     * @author Kimi Liu
     */
    record Resource(String id, List<String> audience, List<String> scopes, JsonValue.ObjectValue metadata) {

        /**
         * Creates a detached immutable protected-resource description.
         *
         * @param id       stable protected-resource identifier
         * @param audience audience values identifying this resource
         * @param scopes   supported scope-token values
         * @param metadata non-sensitive external metadata
         * @throws IllegalArgumentException if text is blank or another component or entry is {@code null}
         */
        public Resource {
            Assert.notBlank(id, "Resolved resource identifier must not be blank");
            audience = immutableValues(audience, "Resolved resource audience");
            scopes = immutableValues(scopes, "Resolved resource scope");
            Assert.notNull(metadata, "Resolved resource metadata must not be null");
            metadata = new JsonValue.ObjectValue(metadata.values());
        }

    }

}
