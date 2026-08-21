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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Loads project-maintained protected-resource records.
 */
@FunctionalInterface
public interface ResourceLoader {

    /**
     * Loads a protected-resource record within the exact Source registration scope.
     *
     * @param registration exact Source registration requesting the resource
     * @param request      validated resource lookup coordinates
     * @param context      immutable non-secret invocation context
     * @param timeout      shared end-to-end operation budget
     * @return asynchronous project loading outcome
     */
    CompletionStage<Outcome<Record>> load(
            Registration.SourceEntry registration,
            Request request,
            Context context,
            Timeout.Budget timeout);

    /**
     * Identifies the protected resource required by one protocol operation.
     *
     * @param namespaceId project resource namespace, independent of the Source registration identifier
     * @param audience    requested token audiences
     * @param resource    requested resource indicators
     */
    record Request(String namespaceId, List<String> audience, List<String> resource) {

        /** Validates and freezes one protected-resource lookup request. */
        public Request {
            Assert.notBlank(namespaceId, "Resource request namespace id must not be blank");
            audience = immutable(audience, "Resource request audience");
            resource = immutable(resource, "Resource request indicator");
        }

        /**
         * Validates and freezes ordered resource coordinates.
         *
         * @param values values to validate
         * @param label  validation label
         * @return immutable list
         */
        private static List<String> immutable(final List<String> values, final String label) {
            Assert.notNull(values, label + " list must not be null");
            final List<String> copy = new ArrayList<>(values.size());
            for (String value : values) {
                copy.add(Assert.notBlank(value, label + " must not be blank"));
            }
            return List.copyOf(copy);
        }

    }

    /**
     * Loaded protected-resource data awaiting framework parsing.
     *
     * @param sourceId   exact Source identifier that owns the returned data
     * @param request    exact lookup coordinates resolved by the project
     * @param id         exact project resource identifier
     * @param audience   authorized token audiences
     * @param scopes     scopes allowed for the resource
     * @param attributes protocol-specific non-secret resource attributes
     */
    record Record(String sourceId, Request request, String id, List<String> audience, List<String> scopes,
            JsonValue.ObjectValue attributes) {

    }

}
