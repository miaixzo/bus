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
package org.miaixz.bus.auth.guard.token;

import java.time.Instant;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;

/**
 * Supplies the currently trusted HS256 keys to one service identity evaluator. Implementations publish consistent,
 * thread-safe snapshots. Remote providers refresh outside the request path.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ServiceKeyProvider {

    /**
     * Returns the currently published signing and verification material.
     *
     * @param now time supplied by the framework
     * @return a snapshot for the configured trust domain
     * @throws IllegalStateException if key material cannot be read
     */
    Snapshot keys(Instant now);

    /**
     * A key version with a bounded period of use.
     *
     * @param version immutable identifier for the same key material
     * @param secret  HS256 key material, copied on input and access
     * @param from    first instant at which the key is eligible
     * @param until   exclusive end of the key's trusted window
     */
    record Key(String version, byte[] secret, Instant from, Instant until) {

        /**
         * Validates the key metadata and isolates the secret from caller-owned arrays.
         *
         * @param version immutable identifier for the key material
         * @param secret  HS256 key material to copy
         * @param from    first instant at which the key is eligible
         * @param until   exclusive end of the key's trusted window
         * @throws IllegalArgumentException if the version, secret, or validity window is invalid
         */
        public Key {
            if (version == null || version.isBlank() || secret == null || secret.length < Normal._32 || from == null
                    || until == null || !until.isAfter(from))
                throw new IllegalArgumentException("Invalid service key material or validity window");
            secret = secret.clone();
        }

        /**
         * Returns a defensive copy for the cryptographic provider.
         *
         * @return isolated key material
         */
        @Override
        public byte[] secret() {
            return secret.clone();
        }

        /**
         * Tests whether the key is eligible at the current time.
         *
         * @param now time supplied by the framework
         * @return whether the key is within its trusted window
         */
        public boolean active(Instant now) {
            return !now.isBefore(from) && now.isBefore(until);
        }

        /**
         * Returns a safe description without exposing key material or provider identifiers.
         *
         * @return redacted key description
         */
        @Override
        public String toString() {
            return "ServiceKey[redacted]";
        }

    }

    /**
     * Keys captured together for a single request. A null signing key or an empty verification list disables that
     * capability until a new valid snapshot is published.
     *
     * @param signing      current key selected for signing
     * @param verification ordered candidates for verification
     */
    record Snapshot(Key signing, List<Key> verification) {

        /**
         * Creates a snapshot with an immutable verification list bounded to three distinct key versions.
         *
         * @param signing      current signing key, or {@code null} when signing is disabled
         * @param verification ordered candidates for verification
         * @throws IllegalArgumentException if the verification list exceeds three keys or repeats a version
         */
        public Snapshot {
            verification = List.copyOf(verification);
            if (verification.size() > Normal._3
                    || verification.stream().map(Key::version).distinct().count() != verification.size())
                throw new IllegalArgumentException("Invalid service verification key set");
        }

    }

}
