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
package org.miaixz.bus.cortex.setting.secret;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Immutable reference-only backend address. Trusted tenant/application identity is supplied separately at invocation
 * time. Resource and optional field are not plaintext secret values and the diagnostic representation is redacted.
 *
 * @param connectionId non-null connectionId component
 * @param resource     logical backend resource name before URL encoding
 * @param version      immutable backend version; null selects the current version
 * @param field        optional top-level JSON string field; null requests the complete value
 * @param expectedType non-null logical payload classification, without automatic conversion
 */
public record SecretReference(String connectionId, String resource, String version, String field, Type expectedType) {

    /**
     * Validates the record components before publishing this immutable value.
     *
     * @param connectionId non-null connectionId component
     * @param resource     logical backend resource name before URL encoding
     * @param version      immutable backend version; null selects the current version
     * @param field        optional top-level JSON string field; null requests the complete value
     * @param expectedType non-null logical payload classification, without automatic conversion
     * @throws IllegalArgumentException if component values violate this record's constraints
     * @throws NullPointerException     if expectedType is null
     */
    public SecretReference {
        if (connectionId == null || connectionId.isBlank() || resource == null || resource.isBlank()
                || field != null && field.isBlank() || version != null && version.isBlank()) {
            throw new IllegalArgumentException("Invalid secret reference");
        }
        Objects.requireNonNull(expectedType, "expectedType");
    }

    /**
     * Parses the existing reference JSON format without changing persisted selector fields. CURRENT must omit its
     * version; VERSION must provide a non-blank immutable identifier. No trusted caller identity is read from JSON.
     *
     * @param json reference-only descriptor, including the existing selector and expectedType fields
     * @return validated reference with a nullable fixed version
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if JSON, fields, mode, or version are invalid
     */
    public static SecretReference parse(String json) {
        try {
            Object parsed = JsonKit.toJavaValue(JsonKit.readValue(json.getBytes(StandardCharsets.UTF_8), 16, true));
            if (!(parsed instanceof Map<?, ?> spec)
                    || !Set.of("connectionId", "resource", "selector", "field", "expectedType")
                            .containsAll(spec.keySet())
                    || !(spec.get("selector") instanceof Map<?, ?> selector)
                    || !Set.of("mode", "version").containsAll(selector.keySet())) {
                throw new CryptoException("Invalid secret reference");
            }
            String mode = text(selector, "mode", true);
            String version = text(selector, "version", false);
            if ("CURRENT".equals(mode) ? version != null : !"VERSION".equals(mode) || version == null) {
                throw new CryptoException("Invalid secret reference");
            }
            return new SecretReference(text(spec, "connectionId", true), text(spec, "resource", true), version,
                    text(spec, "field", false), Type.valueOf(text(spec, "expectedType", true)));
        } catch (RuntimeException failure) {
            throw new CryptoException("Invalid secret reference");
        }
    }

    /**
     * Reads a non-blank string from a decoded descriptor without exposing arbitrary parser diagnostics.
     *
     * @param map      decoded descriptor or selector object
     * @param key      required property name
     * @param required whether an absent or null property is rejected
     * @return validated string, or null for an absent optional property
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the property has an invalid type or value
     */
    private static String text(Map<?, ?> map, String key, boolean required) {
        Object value = map.get(key);
        if (value == null && !required) {
            return null;
        }
        if (!(value instanceof String text) || text.isBlank()) {
            throw new CryptoException("Invalid secret reference");
        }
        return text;
    }

    /**
     * Returns a fixed redacted representation without including secret contents or identity details.
     *
     * @return fixed redacted diagnostic string
     */
    @Override
    public String toString() {
        return "SecretReference[REDACTED]";
    }

    /**
     * Logical payload classification; this enum does not install a schema or parser.
     */
    public enum Type {
        /**
         * Treat the complete value as opaque text.
         */
        OPAQUE,

        /**
         * Classify the payload as JSON; optional field extraction still requires a string property.
         */
        JSON,

        /**
         * Classify text as application-defined database credentials, without implicit schema conversion.
         */
        DATABASE,

        /**
         * Classify text as application-defined Redis credentials, without implicit schema conversion.
         */
        REDIS,

        /**
         * Classify text as application-defined object-storage credentials, without implicit schema conversion.
         */
        OBJECT_STORAGE
    }

}
