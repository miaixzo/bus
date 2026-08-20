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

/**
 * Identifies an immutable identity security domain and its management presentation metadata.
 * <p>
 * A realm scopes identity interpretation within an authentication deployment. It is not a persistence namespace,
 * tenant, role, permission set, or authorization evaluator and therefore does not replace {@link Library} namespace
 * isolation.
 * </p>
 *
 * @param key      stable identity-domain key
 * @param metadata human-readable identity-domain metadata
 * @author Kimi Liu
 */
public record Realm(Key key, Metadata metadata) {

    /**
     * Creates an immutable identity realm.
     *
     * @param key      stable identity-domain key
     * @param metadata human-readable identity-domain metadata
     * @throws IllegalArgumentException if either component is {@code null}
     */
    public Realm {
        Assert.notNull(key, "Realm key must not be null");
        Assert.notNull(metadata, "Realm metadata must not be null");
    }

    /**
     * Wraps the stable value used to identify an identity security domain.
     *
     * @param value stable realm identifier
     * @author Kimi Liu
     */
    public record Key(String value) {

        /**
         * Creates a realm key.
         *
         * @param value non-blank stable realm identifier
         * @throws IllegalArgumentException if the value is blank
         */
        public Key {
            Assert.notBlank(value, "Realm key value must not be blank");
        }

    }

    /**
     * Carries human-readable realm presentation data without security policy or permission semantics.
     *
     * @param name        non-blank realm display name
     * @param description realm description, which may be empty
     * @author Kimi Liu
     */
    public record Metadata(String name, String description) {

        /**
         * Creates realm presentation metadata.
         *
         * @param name        non-blank display name
         * @param description non-null description, which may be empty
         * @throws IllegalArgumentException if the name is blank or the description is {@code null}
         */
        public Metadata {
            Assert.notBlank(name, "Realm metadata name must not be blank");
            Assert.notNull(description, "Realm metadata description must not be null");
        }

    }

}
