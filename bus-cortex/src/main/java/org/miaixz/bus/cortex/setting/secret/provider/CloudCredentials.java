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
package org.miaixz.bus.cortex.setting.secret.provider;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.ForbiddenException;
import org.miaixz.bus.cortex.setting.secret.SecretContext;

/**
 * Owns one request attempt's signing identity or standalone access token. Public factories copy character input; close
 * and credential access synchronize on this instance. Sources must return fresh instances, never shared credentials.
 * Temporary header strings and JCA copies cannot be reliably erased.
 */
public final class CloudCredentials implements AutoCloseable {

    /**
     * Signing key identifier; null distinguishes standalone-token credentials.
     */
    private final String accessKeyId;
    /**
     * Known absolute identity expiration, or null when the source cannot determine it.
     */
    private final Instant expiresAt;
    /**
     * Owned signing-secret or standalone-token characters; null after close.
     */
    private transient char[] secret;
    /**
     * Optional immutable temporary signing-session token; its reference is released on close.
     */
    private transient String token;

    /**
     * Copies AK/SK signing identity and its optional session token into request-owned storage. The caller must erase
     * the input array and close this object after use.
     *
     * @param accessKeyId non-blank access-key identifier; null is reserved internally for token-only credentials
     * @param secret      non-empty signing secret copied into owned storage
     * @param token       optional non-blank temporary session token for signing credentials
     * @param expiresAt   absolute backend or identity expiration; null means unknown, not unlimited validity
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public CloudCredentials(String accessKeyId, char[] secret, String token, Instant expiresAt) {
        if (accessKeyId == null || accessKeyId.isBlank() || secret == null || secret.length == 0
                || token != null && token.isBlank()) {
            throw new IllegalArgumentException("Cloud signing identity is required");
        }
        this.accessKeyId = accessKeyId;
        this.secret = secret.clone();
        this.token = token;
        this.expiresAt = expiresAt;
    }

    /**
     * Copies standalone access-token characters after header-safety validation. The caller must erase the input array
     * and close this object after use.
     *
     * @param material  non-null character input copied into privately owned storage; the caller retains responsibility
     *                  for wiping its input
     * @param expiresAt absolute backend or identity expiration; null means unknown, not unlimited validity
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    private CloudCredentials(char[] material, Instant expiresAt) {
        if (material == null || material.length == 0) {
            throw new IllegalArgumentException("A secret access token is required");
        }
        for (char value : material) {
            if (value <= 0x20 || value >= 0x7f) {
                throw new IllegalArgumentException("Invalid token header characters");
            }
        }
        this.accessKeyId = null;
        this.secret = material.clone();
        this.expiresAt = expiresAt;
    }

    /**
     * Creates standalone Azure/Vault token credentials, copying the input and rejecting whitespace, controls and
     * non-ASCII characters before header construction.
     *
     * @param material  non-null character input copied into privately owned storage; the caller retains responsibility
     *                  for wiping its input
     * @param expiresAt absolute backend or identity expiration; null means unknown, not unlimited validity
     * @return caller-owned token credentials
     */
    public static CloudCredentials forToken(char[] material, Instant expiresAt) {
        return new CloudCredentials(material, expiresAt);
    }

    /**
     * Checks that the credential is live and matches the requested signing or standalone-token representation.
     *
     * @param signing true requires AK/SK credentials; false requires a standalone access token
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    private void requireKind(boolean signing) {
        /**
         * Rejects a closed credential or one whose known absolute expiration has been reached.
         *
         * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
         *                                                               representation is not permitted
         * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
         *                                                               checks fail; inspect the exception type;
         *                                                               diagnostics are deliberately sanitized
         */
        check();
        if ((accessKeyId != null) != signing) {
            throw new ForbiddenException("Secret access denied");
        }
    }

    /**
     * Reads a live standalone access token for a request header. The returned JVM string cannot be erased; it must not
     * be logged or persisted.
     *
     * @return standalone token header value
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    synchronized String header() {
        requireKind(false);
        return new String(secret);
    }

    /**
     * Rejects a closed credential or one whose known absolute expiration has been reached.
     *
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    synchronized void check() {
        if (secret == null) {
            throw new CryptoException("Secret resource is closed");
        }
        if (expiresAt != null && !Instant.now().isBefore(expiresAt)) {
            throw new ForbiddenException("Secret access denied");
        }
    }

    /**
     * Reads the access-key identifier after checking signing representation, lifetime and open state.
     *
     * @return signing identity access-key identifier
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    synchronized String accessKeyId() {
        requireKind(true);
        return accessKeyId;
    }

    /**
     * Reads the optional signing session token after checking representation and lifetime. This is not the standalone
     * Azure/Vault token.
     *
     * @return session token, or null when not supplied
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    synchronized String token() {
        requireKind(true);
        return token;
    }

    /**
     * Creates protocol-specific UTF-8 key bytes around the live signing secret. The caller must erase the returned
     * array after HMAC derivation.
     *
     * @param prefix protocol-specific prefix added before the signing secret
     * @param suffix protocol-specific suffix added after the signing secret
     * @return new caller-owned signing-key bytes
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    synchronized byte[] signingKey(String prefix, String suffix) {
        requireKind(true);
        return (prefix + new String(secret) + suffix).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Releases owned sensitive state. Repeated calls are safe; input arrays and temporary JVM or HTTP string copies are
     * not owned by this object.
     */
    @Override
    public synchronized void close() {
        if (secret != null) {
            Arrays.fill(secret, '\0');
            secret = null;
        }
        token = null;
    }

    /**
     * Returns a fixed redacted representation without including secret contents or identity details.
     *
     * @return fixed redacted diagnostic string
     */
    @Override
    public String toString() {
        return "CloudCredentials[REDACTED]";
    }

    /**
     * Supplies externally bootstrapped credentials without implicit discovery or fallback accounts. Each acquire must
     * honor the absolute deadline and transfer a fresh instance to the provider. Implementations shared by providers
     * must support concurrent calls.
     */
    @FunctionalInterface
    public interface Source {

        /**
         * Obtains a fresh credential instance within the request deadline. The adapter owns and closes each returned
         * instance; implementations must not return a shared cached object.
         *
         * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
         *                reference
         * @return new owned signing credentials or standalone token credentials
         * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
         *                                                               representation is not permitted
         * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
         *                                                               checks fail; inspect the exception type;
         *                                                               diagnostics are deliberately sanitized
         */
        CloudCredentials acquire(SecretContext context);

    }

}
