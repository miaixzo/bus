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
package org.miaixz.bus.auth.source.protocol.oauth2.grant;

import java.util.Arrays;

import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Builder;

/**
 * Generates opaque OAuth token material and derives irreversible Source-isolated cache keys.
 * <p>
 * This class owns only token entropy and key derivation. It does not validate grants, persist state, construct protocol
 * responses, or decide authorization policy.
 * </p>
 *
 * @author Kimi Liu
 */
public class TokenMaterial {

    /**
     * Exact Source identifier included in derived storage keys.
     */
    private final String sourceId;

    /**
     * Policy-derived number of random bytes per token.
     */
    private final int tokenBytes;

    /**
     * Creates a Source-isolated token generator using the runtime security rules.
     *
     * @param sourceId exact Source identifier
     * @param services Source-scoped runtime services
     */
    public TokenMaterial(final String sourceId, final DriverServices services) {
        this.sourceId = Assert.notBlank(sourceId, "OAuth 2.x Source id must not be blank");
        final int entropyBits = Math.max(
                Normal._256,
                Assert.notNull(services, "OAuth 2.x execution services must not be null").policies()
                        .require(Protocol.OAUTH2).minimumEntropyBits());
        this.tokenBytes = (entropyBits + Byte.SIZE - 1) / Byte.SIZE;
    }

    /**
     * Creates a cryptographically random base64url token without padding.
     *
     * @return opaque token material
     */
    public String create() {
        final byte[] bytes = RandomKit.randomBytes(tokenBytes, RandomKit.getSecureRandom());
        try {
            return Base64.encodeUrlSafe(bytes);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Derives the irreversible lookup key for one opaque credential.
     *
     * @param token opaque credential material
     * @return hexadecimal SHA-256 cache key
     */
    public String key(final String token) {
        return Builder.sha256Hex(sourceId + Symbol.C_NUL + Assert.notBlank(token, "OAuth 2.x token must not be blank"));
    }

}
