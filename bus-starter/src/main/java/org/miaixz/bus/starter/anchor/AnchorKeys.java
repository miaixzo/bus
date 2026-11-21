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
package org.miaixz.bus.starter.anchor;

import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.cortex.guard.token.ServiceToken;
import org.miaixz.bus.cortex.guard.token.ServiceTokenVerifier;

/**
 * Derives the effective signing key from {@code SECRET_KEY} and the current epoch.
 * <p>
 * The deployment secret is read once at startup and never refreshed from the environment. The effective key changes
 * every {@code rotate} seconds: {@code key = HMAC-SHA256(SECRET_KEY, "miaixz-bus-anchor-v1:" + epoch)}. Signing uses
 * the current epoch; verification accepts the previous, current and next epoch so clock skew and the rotation boundary
 * do not reject valid tokens.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AnchorKeys {

    /**
     * Domain separation label for the derived key.
     */
    private static final String LABEL = "miaixz-bus-anchor-v1:";

    /**
     * Deployment settings.
     */
    private final AnchorProperties properties;

    /**
     * Root secret read once at startup.
     */
    private final byte[] root;

    /**
     * Current epoch snapshot.
     */
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>();

    /**
     * One immutable epoch snapshot.
     *
     * @param epoch      rotation period number
     * @param material   derived key material for the current epoch
     * @param subject    outbound subject
     * @param ttlSeconds token lifetime in seconds
     * @param tokens     one signing token per target audience
     * @param previous   verifier for the previous epoch
     * @param current    verifier for the current epoch
     * @param next       verifier for the next epoch
     */
    private record Snapshot(long epoch, byte[] material, String subject, long ttlSeconds,
            Map<String, ServiceToken> tokens, ServiceTokenVerifier previous, ServiceTokenVerifier current,
            ServiceTokenVerifier next) {
    }

    /**
     * Creates the holder.
     *
     * @param properties deployment settings
     * @param root       root secret
     */
    private AnchorKeys(final AnchorProperties properties, final byte[] root) {
        this.properties = properties;
        this.root = root.clone();
    }

    /**
     * Creates the holder from deployment settings.
     *
     * @param properties deployment settings
     * @return key holder deriving one effective key per rotation period
     * @throws IllegalStateException when the secret is missing
     * @throws ValidateException     when the secret is not Base64 or shorter than 32 bytes
     */
    public static AnchorKeys of(final AnchorProperties properties) {
        final String text = properties.secret() == null ? "" : properties.secret().strip();
        if (text.isEmpty()) {
            throw new IllegalStateException("SECRET_KEY is required when internal service identity is enabled");
        }
        final byte[] root;
        try {
            root = Base64.getDecoder().decode(text);
        } catch (IllegalArgumentException invalid) {
            throw new ValidateException("SECRET_KEY must be Base64 text", invalid);
        }
        if (root.length < 32) {
            throw new ValidateException("SECRET_KEY must contain at least 32 bytes");
        }
        return new AnchorKeys(properties, root);
    }

    /**
     * Returns a token signed with the current epoch key for one target service.
     *
     * @param audience target service code
     * @return compact service token
     */
    public String token(final String audience) {
        if (audience == null || audience.isBlank()) {
            throw new IllegalStateException("Service token audience is required");
        }
        final Snapshot current = snapshot();
        if (current.subject() == null || current.subject().isBlank()) {
            throw new IllegalStateException("bus.anchor.outbound.subject is required to sign service tokens");
        }
        return current.tokens()
                .computeIfAbsent(
                        audience,
                        target -> new ServiceToken(current.subject(), target, current.material(),
                                Duration.ofSeconds(current.ttlSeconds())))
                .value();
    }

    /**
     * Verifies a token against the previous, current and next epoch key.
     *
     * @param compact compact token
     * @return whether any accepted epoch key validates the token
     */
    public boolean accepts(final String compact) {
        final Snapshot current = snapshot();
        return (current.current() != null && current.current().accepts(compact))
                || (current.previous() != null && current.previous().accepts(compact))
                || (current.next() != null && current.next().accepts(compact));
    }

    /**
     * Returns the snapshot for the current epoch, rebuilding it when the period changes.
     *
     * @return current epoch snapshot
     */
    private Snapshot snapshot() {
        final long epoch = System.currentTimeMillis() / 1000L / rotationSeconds();
        Snapshot current = snapshot.get();
        if (current != null && current.epoch() == epoch) {
            return current;
        }
        synchronized (snapshot) {
            current = snapshot.get();
            if (current != null && current.epoch() == epoch) {
                return current;
            }
            final Snapshot rebuilt = build(epoch);
            snapshot.set(rebuilt);
            return rebuilt;
        }
    }

    /**
     * Builds one epoch snapshot.
     *
     * @param epoch current rotation period number
     * @return immutable epoch snapshot
     */
    private Snapshot build(final long epoch) {
        final AnchorProperties.Inbound inbound = properties.inbound();
        final AnchorProperties.Outbound outbound = properties.outbound();
        final byte[] material = derive(epoch);
        final Set<String> subjects = subjects(properties);
        final boolean verify = !subjects.isEmpty() && inbound.audience() != null && !inbound.audience().isBlank();
        return new Snapshot(epoch, material, outbound.subject(), outbound.ttlSeconds(), new ConcurrentHashMap<>(),
                verify ? verifier(inbound, subjects, derive(epoch - 1)) : null,
                verify ? verifier(inbound, subjects, material) : null,
                verify ? verifier(inbound, subjects, derive(epoch + 1)) : null);
    }

    /**
     * Creates one verifier for a derived key.
     *
     * @param inbound  inbound settings
     * @param material derived key bytes
     * @return verifier for the supplied key
     */
    private static ServiceTokenVerifier verifier(
            final AnchorProperties.Inbound inbound,
            final Set<String> subjects,
            final byte[] material) {
        return new ServiceTokenVerifier(subjects, inbound.audience(), Duration.ofSeconds(inbound.skewSeconds()),
                material);
    }

    /**
     * Parses the accepted subjects.
     *
     * @param properties deployment settings
     * @return immutable accepted subject set
     */
    private static Set<String> subjects(final AnchorProperties properties) {
        return Arrays.stream(properties.inbound().subjects().split(Symbol.COMMA)).map(String::strip)
                .filter(value -> !value.isEmpty()).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Derives the key for one epoch.
     *
     * @param epoch rotation period number
     * @return 32-byte derived key
     */
    private byte[] derive(final long epoch) {
        try {
            final Mac mac = Mac.getInstance(Algorithm.HMACSHA256.getValue());
            mac.init(new SecretKeySpec(root, Algorithm.HMACSHA256.getValue()));
            return mac.doFinal((LABEL + epoch).getBytes(Charset.US_ASCII));
        } catch (Exception impossible) {
            throw new IllegalStateException("HMAC-SHA256 is required to derive the service key", impossible);
        }
    }

    /**
     * Returns the configured rotation period.
     *
     * @return rotation period in seconds, at least one
     */
    private long rotationSeconds() {
        return Math.max(1L, properties.rotate());
    }

}
