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
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Loads project-maintained protected-resource records.
 */
@FunctionalInterface
public interface ResourceLoader {

    CompletionStage<Outcome<Record>> load(Request request, Context context, Timeout.Budget timeout);

    record Request(String namespaceId, List<String> audience, List<String> resource) {

        public Request {
            Assert.notBlank(namespaceId, "Resource request namespace id must not be blank");
            audience = immutable(audience, "Resource request audience");
            resource = immutable(resource, "Resource request indicator");
        }

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
     */
    record Record(String namespaceId, String id, List<String> audience, List<String> scopes,
            JsonValue.ObjectValue attributes) {
    }
}
