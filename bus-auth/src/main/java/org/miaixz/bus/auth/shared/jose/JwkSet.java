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
package org.miaixz.bus.auth.shared.jose;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an RFC 7517 JWK Set with ordered keys and provider-neutral top-level extension members.
 *
 * @param keys       JWK values in wire order, which does not imply selection priority
 * @param extensions top-level members other than the reserved {@code keys} member
 * @author Kimi Liu
 */
public record JwkSet(List<Jwk> keys, JsonValue.ObjectValue extensions) {

    /**
     * Exact case-sensitive JWK Set array member name.
     */
    private static final String KEYS = "keys";

    /**
     * Validates and freezes the complete set without imposing meaning on key order.
     *
     * @throws IllegalArgumentException if a component or key is {@code null}
     * @throws ValidateException        if extensions contain the reserved {@code keys} member
     */
    public JwkSet {
        Assert.notNull(keys, "JWK Set keys must not be null");
        Assert.notNull(extensions, "JWK Set extensions must not be null");
        keys = List.copyOf(keys);
        extensions = new JsonValue.ObjectValue(extensions.values());
        if (extensions.values().containsKey(KEYS)) {
            throw new ValidateException("JWK Set extensions must not redefine keys");
        }
    }

    /**
     * Parses a provider-neutral RFC 7517 JWK Set object.
     *
     * @param value complete JSON object containing the required {@code keys} array
     * @return immutable JWK Set retaining all non-keys members
     */
    public static JwkSet fromJson(final JsonValue.ObjectValue value) {
        Assert.notNull(value, "JWK Set JSON object must not be null");
        final JsonValue keysValue = value.values().get(KEYS);
        if (!(keysValue instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("JWK Set keys member must be a JSON array");
        }
        final List<Jwk> keys = new ArrayList<>(array.values().size());
        for (JsonValue element : array.values()) {
            if (!(element instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("JWK Set keys entries must be JSON objects");
            }
            keys.add(new Jwk(object));
        }
        final Map<String, JsonValue> extensions = new LinkedHashMap<>(value.values());
        extensions.remove(KEYS);
        return new JwkSet(keys, new JsonValue.ObjectValue(extensions));
    }

    /**
     * Encodes the set using the exact RFC 7517 {@code keys} member and retained extensions.
     *
     * @return provider-neutral JWK Set JSON object
     */
    public JsonValue.ObjectValue toJson() {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        final List<JsonValue> keyValues = new ArrayList<>(keys.size());
        for (Jwk key : keys) {
            keyValues.add(key.parameters());
        }
        values.put(KEYS, new JsonValue.ArrayValue(keyValues));
        values.putAll(extensions.values());
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Returns every key whose optional identifier exactly matches the requested value.
     *
     * @param keyId case-sensitive key identifier
     * @return immutable matches in original wire order
     */
    public List<Jwk> byKeyId(final String keyId) {
        Assert.notBlank(keyId, "JWK key identifier must not be blank");
        return keys.stream().filter(key -> key.keyId().filter(keyId::equals).isPresent()).toList();
    }

    /**
     * Converts every asymmetric member to its public publication view.
     *
     * @return new public-only JWK Set retaining top-level extensions
     * @throws ValidateException if any key is symmetric or has an unknown key type
     */
    public JwkSet publicOnly() {
        return new JwkSet(keys.stream().map(Jwk::publicOnly).toList(), extensions);
    }

    /**
     * Returns the IANA media type already defined by bus-core for JWK Set representations.
     *
     * @return {@code application/jwk-set+json} media type
     */
    public MediaType mediaType() {
        return MediaType.APPLICATION_JWK_SET_JSON_TYPE;
    }

}
