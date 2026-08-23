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
package org.miaixz.bus.auth;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents a stable framework identity subject independently of protocol wire subjects and active sessions.
 * <p>
 * The key identifies the stored subject, the reference is passed to project subject and attribute loaders, and
 * attributes are an immutable protocol- and Source-neutral snapshot. This type must not be used in place of the formal
 * SAML Subject model or as a container for session and token state.
 * </p>
 *
 * @param key        stable framework subject key
 * @param reference  external subject loader reference
 * @param attributes immutable subject attribute snapshot
 * @author Kimi Liu
 */
public record Subject(Key key, Reference reference, JsonValue.ObjectValue attributes) {

    /**
     * Creates an immutable stable identity subject.
     *
     * @param key        stable framework subject key
     * @param reference  external subject loader reference
     * @param attributes protocol- and Source-neutral subject attributes
     * @throws IllegalArgumentException if any component is {@code null}
     */
    public Subject {
        Assert.notNull(key, "Subject key must not be null");
        Assert.notNull(reference, "Subject reference must not be null");
        Assert.notNull(attributes, "Subject attributes must not be null");
        attributes = new JsonValue.ObjectValue(attributes.values());
    }

    /**
     * Wraps the stable internal identifier of a subject.
     *
     * @param value stable internal subject identifier
     * @author Kimi Liu
     */
    public record Key(String value) {

        /**
         * Creates a stable subject key.
         *
         * @param value non-blank stable internal identifier
         * @throws IllegalArgumentException if the value is blank
         */
        public Key {
            Assert.notBlank(value, "Subject key value must not be blank");
        }

    }

    /**
     * Wraps the opaque identifier used by external subject-related loader implementations.
     *
     * @param value opaque external subject reference
     * @author Kimi Liu
     */
    public record Reference(String value) {

        /**
         * Creates an external subject reference.
         *
         * @param value non-blank opaque reference value
         * @throws IllegalArgumentException if the value is blank
         */
        public Reference {
            Assert.notBlank(value, "Subject reference value must not be blank");
        }

    }

}
