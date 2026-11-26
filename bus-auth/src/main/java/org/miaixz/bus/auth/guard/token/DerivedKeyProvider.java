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
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;

/**
 * Default {@link ServiceKeyProvider} that derives short-lived keys from the configured Base64 root and stable
 * deployment label.
 *
 * @author Kimi Liu
 */
public final class DerivedKeyProvider implements ServiceKeyProvider {

    /**
     * Deployment root used only for epoch derivation.
     */
    private final byte[] root;

    /**
     * Rotation period in seconds.
     */
    private final long rotate;

    /**
     * Domain separation label of the deployment.
     */
    private final String label;

    /**
     * Epoch currently held in the default provider's cache.
     */
    private long epoch = Long.MIN_VALUE;

    /**
     * Immutable keys cached for the current epoch.
     */
    private Snapshot current;

    /**
     * Creates a provider interoperable with the original epoch derivation.
     *
     * @param secret Base64 root with at least 32 decoded bytes
     * @param rotate epoch length in seconds
     * @param label  domain separation label
     */
    public DerivedKeyProvider(String secret, long rotate, String label) {
        try {
            this.root = Base64.getDecoder().decode(Objects.requireNonNullElse(secret, Normal.EMPTY));
        } catch (IllegalArgumentException malformed) {
            throw new IllegalArgumentException("The service identity root must be valid Base64");
        }
        if (root.length < Normal._32)
            throw new IllegalArgumentException("The service identity root must contain at least 32 bytes");
        this.rotate = Math.max(Normal._1, rotate);
        this.label = Objects.requireNonNull(label);
    }

    /**
     * Returns the current signing key and the previous, current, and next verification keys.
     *
     * @param now time supplied by the framework
     * @return trusted keys for the current epoch
     */
    @Override
    public synchronized Snapshot keys(Instant now) {
        long next = Math.floorDiv(now.getEpochSecond(), rotate);
        if (current == null || next != epoch) {
            Key signing = key(next);
            current = new Snapshot(signing, List.of(key(next - 1), signing, key(next + 1)));
            epoch = next;
        }
        return current;
    }

    /**
     * Derives one epoch key with the existing one-epoch verification allowance on either side.
     *
     * @param index epoch of the key
     * @return derived key and its verification window
     */
    private Key key(long index) {
        try {
            Mac mac = Mac.getInstance(Algorithm.HMACSHA256.getValue());
            mac.init(new SecretKeySpec(root, Algorithm.HMACSHA256.getValue()));
            return new Key(Long.toString(index), mac.doFinal((label + index).getBytes(Charset.US_ASCII)),
                    Instant.ofEpochSecond(Math.multiplyExact(index - 1, rotate)),
                    Instant.ofEpochSecond(Math.multiplyExact(index + 2, rotate)));
        } catch (java.security.GeneralSecurityException failure) {
            throw new IllegalStateException("Service key derivation is unavailable", failure);
        }
    }

}
