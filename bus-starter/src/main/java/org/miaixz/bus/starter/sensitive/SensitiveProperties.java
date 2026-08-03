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
package org.miaixz.bus.starter.sensitive;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable sensitive-data crypto properties using an explicit algorithm allowlist.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.SENSITIVE)
public class SensitiveProperties {

    /**
     * Whether the sensitive integration is enabled.
     */
    private final boolean enabled;
    /**
     * Encryption rules keyed by the protected data category.
     */
    private final Encrypt encrypt;
    /**
     * Decryption rules keyed by the protected data category.
     */
    private final Decrypt decrypt;
    /**
     * Whether sensitive processing emits diagnostic events without exposing protected values.
     */
    private final boolean debug;

    /**
     * Creates sensitive-data properties.
     *
     * @param enabled whether the feature is enabled
     * @param encrypt encryption settings
     * @param decrypt decryption settings
     * @param debug   whether sensitive-data debugging is enabled
     */
    public SensitiveProperties(@DefaultValue("false") boolean enabled, Encrypt encrypt, Decrypt decrypt,
            @DefaultValue("false") boolean debug) {
        this.enabled = enabled;
        this.encrypt = encrypt;
        this.decrypt = decrypt;
        this.debug = debug;
    }

    /**
     * Supported symmetric algorithms.
     */
    public enum Type {
        /**
         * AES.
         */
        AES,
        /**
         * DES.
         */
        DES,
        /**
         * Chinese SM4.
         */
        SM4
    }

    /**
     * Encryption configuration; key contains only an external secret reference.
     *
     * @param key  lookup key
     * @param type allowlisted encryption algorithm
     */
    public record Encrypt(String key, Type type) {

        /**
         * Exposes the external secret reference used for encryption.
         *
         * @return secret reference
         */
        public String getKey() {
            return key;
        }

        /**
         * Exposes the allowlisted encryption algorithm name.
         *
         * @return allowlisted algorithm name
         */
        public String getType() {
            return type == null ? null : type.name();
        }

        /**
         * @return masked representation
         */
        @Override
        public String toString() {
            return "Encrypt[key=***, type=" + type + "]";
        }
    }

    /**
     * Decryption configuration; key contains only an external secret reference.
     *
     * @param key  lookup key
     * @param type allowlisted decryption algorithm
     */
    public record Decrypt(String key, Type type) {

        /**
         * Exposes the external secret reference used for decryption.
         *
         * @return secret reference
         */
        public String getKey() {
            return key;
        }

        /**
         * Exposes the allowlisted decryption algorithm name.
         *
         * @return allowlisted algorithm name
         */
        public String getType() {
            return type == null ? null : type.name();
        }

        /**
         * @return masked representation
         */
        @Override
        public String toString() {
            return "Decrypt[key=***, type=" + type + "]";
        }
    }

    /**
     * @return masked diagnostic representation
     */
    @Override
    public String toString() {
        return "SensitiveProperties[enabled=" + enabled + ", debug=" + debug + ", encrypt=***, decrypt=***]";
    }

}
