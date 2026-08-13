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
package org.miaixz.bus.auth.protocol.oauth2;

import java.security.SecureRandom;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.guard.ReplayKey;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.ProtocolError;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Clock;

/**
 * Supplies package-private OAuth 2.0 mechanics that carry no flow-specific state transition.
 *
 * @author Kimi Liu
 */
final class OAuth2Support {

    /**
     * Prevents construction of the stateless support class.
     */
    private OAuth2Support() {
        // No initialization required.
    }

    /**
     * Creates an already failed protocol stage.
     *
     * @param error non-null fixed OAuth protocol error
     * @param <T>   absent success value type
     * @return non-null stage failed with a {@link ProtocolException}
     */
    static <T> CompletionStage<T> failed(final ProtocolError error) {
        return CompletableFuture.failedFuture(new ProtocolException(Assert.notNull(error)));
    }

    /**
     * Reads the sole runtime security clock.
     *
     * @param clock non-null Fabric clock
     * @return non-null current instant supplied by the clock
     */
    static Instant now(final Clock clock) {
        return Assert.notNull(Assert.notNull(clock, "Clock must be not null!").now(), "Clock value must be not null!");
    }

    /**
     * Adds one trusted duration while mapping overflow to the caller-selected OAuth error.
     *
     * @param instant  non-null base instant
     * @param duration non-null trusted duration
     * @param error    non-null error used when the instant cannot represent the result
     * @return calculated instant
     * @throws ProtocolException when addition overflows the supported instant range
     */
    static Instant add(final Instant instant, final Duration duration, final ProtocolError error) {
        try {
            return Assert.notNull(instant).plus(Assert.notNull(duration));
        } catch (final DateTimeException | ArithmeticException failure) {
            final ProtocolError selected = Assert.notNull(error);
            throw new ProtocolException(selected.getKey(), selected.getValue(), failure);
        }
    }

    /**
     * Generates one exact-length unpadded Base64url credential and clears its random source bytes.
     *
     * @param random non-null secure random source
     * @param bytes  positive random byte count
     * @param error  non-null caller-selected error reserved for credential generation failures
     * @return unpadded URL-safe Base64 credential
     */
    static String credential(final SecureRandom random, final int bytes, final ProtocolError error) {
        Assert.isTrue(bytes > 0, "Credential byte count must be positive");
        final byte[] value = new byte[bytes];
        Assert.notNull(random, "Secure random must be not null!").nextBytes(value);
        try {
            return Base64.encodeUrlSafe(value);
        } finally {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Derives one tenant-isolated OAuth state key.
     *
     * @param invocation non-null tenant-scoped authentication context
     * @param kind       non-blank OAuth state class
     * @param value      non-blank opaque state value
     * @return deterministic tenant-isolated replay key
     */
    static String key(final Context invocation, final String kind, final String value) {
        final Context current = Assert.notNull(invocation, "Context must be not null!");
        return ReplayKey.derive(current.tenantId(), "oauth2", kind, value);
    }

    /**
     * Encodes scopes in their stable iteration order.
     *
     * @param scopes non-null ordered scope set
     * @return space-delimited scope text
     */
    static String encodeScopes(final Set<String> scopes) {
        return String.join(Symbol.SPACE, Assert.notNull(scopes, "Scopes must be not null!"));
    }

    /**
     * Decodes a stored ordered scope value and rejects duplicate entries.
     *
     * @param value non-null stored scope text
     * @param error non-null error used for malformed state
     * @return immutable insertion-ordered scope set
     * @throws ProtocolException when the stored text contains blank or duplicate scope entries
     */
    static Set<String> decodeScopes(final String value, final ProtocolError error) {
        final String source = Assert.notNull(value, "Scope state must be not null!");
        if (source.isEmpty()) {
            return Set.of();
        }
        final List<String> items = StringKit.split(source, Symbol.SPACE);
        final LinkedHashSet<String> result = new LinkedHashSet<>(items);
        if (items.isEmpty() || result.size() != items.size() || result.stream().anyMatch(String::isBlank)) {
            throw new ProtocolException(Assert.notNull(error));
        }
        return Collections.unmodifiableSet(result);
    }

}
