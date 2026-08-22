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

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.worker.loader.AttributeLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Pure parser for project-loaded subject attributes.
 *
 * @author Kimi Liu
 */
public class AttributeParser {

    /**
     * Creates a stateless subject-attribute parser.
     */
    public AttributeParser() {
        // No initialization required.
    }

    /**
     * Validates Source and subject ownership and detaches the returned attribute object.
     *
     * @param registration exact Source registration that requested the data
     * @param subject      exact requested subject key
     * @param record       project-loaded attribute record
     * @return detached validated attributes
     */
    public JsonValue.ObjectValue parse(
            final Blueprint.SourceEntry registration,
            final Subject.Key subject,
            final AttributeLoader.Record record) {
        final String sourceId = Assert.notNull(registration, "Attribute Source registration must not be null")
                .resource().getId();
        final Subject.Key expected = Assert.notNull(subject, "Subject key must not be null");
        final AttributeLoader.Record loaded = Assert.notNull(record, "Loaded attribute record must not be null");
        if (!sourceId.equals(loaded.sourceId())) {
            throw new ValidateException("Loaded attributes do not belong to the requested Source");
        }
        if (!expected.equals(loaded.subject())) {
            throw new ValidateException("Loaded attributes do not belong to the requested subject");
        }
        final JsonValue.ObjectValue values = Assert.notNull(loaded.values(), "Subject attributes must not be null");
        return new JsonValue.ObjectValue(values.values());
    }

}
