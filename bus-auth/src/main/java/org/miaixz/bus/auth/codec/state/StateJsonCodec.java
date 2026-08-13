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
package org.miaixz.bus.auth.codec.state;

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.codec.WireCodec;
import org.miaixz.bus.auth.codec.json.StrictJsonReader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Encodes and decodes bounded string-keyed JSON objects inside the authentication state envelope. The codec owns no
 * protocol fields and deliberately preserves failures raised by the strict JSON reader and envelope codec.
 *
 * @author Kimi Liu
 */
public final class StateJsonCodec implements WireCodec<Map<String, ?>, byte[]> {

    /**
     * Product-owned JSON provider.
     */
    private final JsonProvider provider;

    /**
     * Strict JSON reader bound to the same provider and limits.
     */
    private final StrictJsonReader reader;

    /**
     * Maximum accepted serialized JSON bytes.
     */
    private final int maximumBytes;

    /**
     * Creates a state JSON codec from an immutable provider and explicit closed bounds.
     *
     * @param provider     product-owned JSON provider
     * @param maximumBytes maximum accepted serialized JSON length in bytes
     * @param maximumDepth maximum accepted object or array nesting depth
     * @throws ValidateException if the provider is null or either bound is not positive
     */
    public StateJsonCodec(final JsonProvider provider, final int maximumBytes, final int maximumDepth) {
        this.provider = Assert.notNull(provider, () -> new ValidateException("JSON provider must not be null"));
        if (maximumBytes <= Normal._0) {
            throw new ValidateException("Maximum JSON bytes must be positive");
        }
        if (maximumDepth <= Normal._0) {
            throw new ValidateException("Maximum JSON depth must be positive");
        }
        this.reader = new StrictJsonReader(provider, maximumBytes, maximumDepth);
        this.maximumBytes = maximumBytes;
    }

    /**
     * Serializes one string-keyed object and wraps it in the authenticated state envelope.
     *
     * @param values state values
     * @return complete state envelope
     */
    @Override
    public byte[] encode(final Map<String, ?> values) {
        final Map<String, ?> source = Assert
                .notNull(values, () -> new ValidateException("State JSON object must not be null"));
        for (final Object key : source.keySet()) {
            if (!(key instanceof String)) {
                throw new ValidateException("State JSON object keys must be strings");
            }
        }
        final byte[] json = provider.write(source);
        requireDocument(json);
        return StateEnvelopeCodec.INSTANCE.encode(json);
    }

    /**
     * Validates one complete state envelope and decodes its string-keyed JSON object.
     *
     * @param envelope complete state envelope
     * @return independent insertion-ordered object
     */
    @Override
    public Map<String, Object> decode(final byte[] envelope) {
        final Object decoded = reader.read(StateEnvelopeCodec.INSTANCE.decode(envelope), Map.class);
        if (!(decoded instanceof Map<?, ?> values)) {
            throw new ProtocolException(ErrorCode._100302);
        }
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (final Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new ProtocolException(ErrorCode._100302);
            }
            result.put(key, entry.getValue());
        }
        return result;
    }

    /**
     * Enforces the closed serialized JSON byte boundary.
     *
     * @param json serialized JSON
     */
    private void requireDocument(final byte[] json) {
        if (json == null || json.length == Normal._0) {
            throw new ProtocolException(ErrorCode._100302);
        }
        if (json.length > maximumBytes) {
            throw new ProtocolException(ErrorCode._100530);
        }
    }

}
