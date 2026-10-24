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

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CryptoException;

/**
 * Owns one private character array and immutable backend metadata. Copy and close are synchronized; each returned copy
 * belongs to the caller and must be erased separately. Closing is idempotent and does not wipe input arrays or
 * immutable strings held elsewhere.
 *
 * @author Kimi Liu
 */
public final class SecretMaterial implements AutoCloseable {

    /**
     * Actual non-blank backend version; retained after close.
     */
    private final String resolvedVersion;
    /**
     * Time the backend value was fetched; retained after close.
     */
    private final Instant fetchedAt;
    /**
     * Known backend expiration, or null when unknown; retained after close.
     */
    private final Instant expiresAt;
    /**
     * Non-negative connection generation used to reject obsolete results.
     */
    private final long connectionGeneration;
    /**
     * Privately owned sensitive characters, null after close and excluded from ordinary field serialization.
     */
    private transient char[] material;

    /**
     * Copies a backend value and records its immutable version and lifetime in this same owned object.
     *
     * @param material             character input copied into owned storage; caller must wipe its input separately
     * @param resolvedVersion      non-blank immutable backend version
     * @param fetchedAt            non-null backend fetch time
     * @param expiresAt            absolute backend expiration, or null when unknown
     * @param connectionGeneration non-negative connection configuration generation
     * @throws NullPointerException     if material or fetchedAt is null
     * @throws IllegalArgumentException if resolvedVersion is null or blank, or generation is negative
     */
    public SecretMaterial(char[] material, String resolvedVersion, Instant fetchedAt, Instant expiresAt,
            long connectionGeneration) {
        if (resolvedVersion == null || resolvedVersion.isBlank() || connectionGeneration < 0) {
            throw new IllegalArgumentException("Invalid secret metadata");
        }
        this.fetchedAt = Objects.requireNonNull(fetchedAt, "fetchedAt");
        this.resolvedVersion = resolvedVersion;
        this.expiresAt = expiresAt;
        this.connectionGeneration = connectionGeneration;
        this.material = Objects.requireNonNull(material, "material").clone();
    }

    /**
     * Returns the actual immutable backend version without exposing secret characters.
     *
     * @return resolved version, also available after close
     */
    public String resolvedVersion() {
        return resolvedVersion;
    }

    /**
     * Returns the original backend fetch time.
     *
     * @return fetch instant, also available after close
     */
    public Instant fetchedAt() {
        return fetchedAt;
    }

    /**
     * Returns the business Secret expiration independently of its cache lifetime.
     *
     * @return expiration instant, or null when unknown; available after close
     */
    public Instant expiresAt() {
        return expiresAt;
    }

    /**
     * Returns the configuration generation with which this value was read.
     *
     * @return non-negative connection generation, also available after close
     */
    public long connectionGeneration() {
        return connectionGeneration;
    }

    /**
     * Copies live owned characters. The caller must erase the returned array; access after close or backend expiration
     * is rejected.
     *
     * @return independent character array owned by the caller
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the material is closed or its backend expiration
     *                                                            has been reached
     */
    public synchronized char[] copy() {
        if (material == null) {
            throw new CryptoException("Secret resource is closed");
        }
        if (expiresAt != null && !Instant.now().isBefore(expiresAt)) {
            throw new CryptoException("Secret value has expired");
        }
        return material.clone();
    }

    /**
     * Releases owned sensitive state. Repeated calls are safe; input arrays and temporary JVM or HTTP string copies are
     * not owned by this object.
     */
    @Override
    public synchronized void close() {
        if (material != null) {
            Arrays.fill(material, Symbol.C_NUL);
            material = null;
        }
    }

    /**
     * Returns a fixed redacted representation without including secret contents or identity details.
     *
     * @return fixed redacted diagnostic string
     */
    @Override
    public String toString() {
        return "SecretMaterial[REDACTED]";
    }

}
