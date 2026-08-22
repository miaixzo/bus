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
package org.miaixz.bus.auth.worker;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Regex;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Stores character-based credentials generated dynamically during protocol execution.
 * <p>
 * This external port is distinct from {@link SecretLoader}: a loader reads material referenced by persistent options,
 * while this store owns short-lived credential material generated dynamically during protocol execution.
 * Implementations use a secure vault or equivalent protected persistence and never fall back to an ordinary
 * process-local cache.
 * </p>
 *
 * @author Kimi Liu
 */
public interface CredentialStore {

    /**
     * Securely persists one dynamic credential before completing the returned stage.
     * <p>
     * The caller retains ownership of {@code secret} and closes it after this stage completes. The implementation must
     * finish its protected copy or persistence before completion and must neither close nor retain the supplied lease.
     * </p>
     *
     * @param key       isolated dynamic credential key
     * @param secret    caller-owned live secret lease
     * @param expiresAt optional absolute material expiration
     * @param context   immutable non-secret invocation context
     * @param timeout   shared end-to-end operation timeout
     * @return stage containing success, an expected refusal, or an operational failure
     */
    CompletionStage<Outcome<Void>> store(
            Key key,
            SecretLease secret,
            Optional<Instant> expiresAt,
            Context context,
            Timeout timeout);

    /**
     * Resolves a new lease without consuming the stored dynamic credential.
     *
     * @param key     isolated dynamic credential key
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation timeout
     * @return stage containing a newly owned lease, an expected refusal, or an operational failure
     */
    CompletionStage<Outcome<SecretLease>> resolve(Key key, Context context, Timeout timeout);

    /**
     * Atomically resolves a new lease and removes the stored dynamic credential.
     *
     * @param key     isolated one-time dynamic credential key
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation timeout
     * @return stage containing a newly owned lease, an expected refusal, or an operational failure
     */
    CompletionStage<Outcome<SecretLease>> take(Key key, Context context, Timeout timeout);

    /**
     * Deletes one dynamic credential without returning its material.
     *
     * @param key     isolated dynamic credential key
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation timeout
     * @return stage containing success, an expected refusal, or an operational failure
     */
    CompletionStage<Outcome<Void>> delete(Key key, Context context, Timeout timeout);

    /**
     * Identifies dynamic credential material without containing its raw protocol binding.
     *
     * @param spaceId       space isolation identifier
     * @param ownerId       Provider or Source registration identifier
     * @param purpose       stable protocol-specific use label
     * @param bindingDigest lowercase SHA-256 hexadecimal digest of the opaque protocol binding
     * @param type          exact stored credential material type
     * @author Kimi Liu
     */
    record Key(String spaceId, String ownerId, String purpose, String bindingDigest, Credential.Type type) {

        /**
         * Validates isolation fields and the irreversible binding digest.
         */
        public Key {
            Assert.notBlank(spaceId, "Dynamic credential space id must not be blank");
            Assert.notBlank(ownerId, "Dynamic credential owner id must not be blank");
            Assert.notBlank(purpose, "Dynamic credential purpose must not be blank");
            Assert.notBlank(bindingDigest, "Dynamic credential binding digest must not be blank");
            if (bindingDigest.length() != 64 || !Pattern.matches(Regex.HEX, bindingDigest)
                    || !bindingDigest.equals(bindingDigest.toLowerCase(Locale.ROOT))) {
                throw new ValidateException(
                        "Dynamic credential binding must be a lowercase SHA-256 hexadecimal digest");
            }
            Assert.notNull(type, "Dynamic credential type must not be null");
            if (type != Credential.Type.PASSWORD && type != Credential.Type.CLIENT_SECRET
                    && type != Credential.Type.SHARED_SECRET) {
                throw new ValidateException("Dynamic credential store accepts only character-based credential types");
            }
        }

        /**
         * Returns non-sensitive isolation metadata while redacting the binding digest.
         *
         * @return redacted dynamic credential key
         */
        @Override
        public String toString() {
            return "CredentialStore.Key[spaceId=" + spaceId + ", ownerId=" + ownerId + ", purpose=" + purpose
                    + ", bindingDigest=[REDACTED], type=" + type.name() + Symbol.C_BRACKET_RIGHT;
        }

    }

}
