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
import java.util.Objects;

import org.miaixz.bus.core.lang.exception.CryptoException;

/**
 * Caller-supplied invocation identity and absolute deadline. This context does not authenticate callers. Construct only
 * from trusted authentication data; persisted references must not supply identity. The context is immutable, but
 * deadline validity must be checked throughout an operation.
 * @param tenantId authenticated tenant identifier
 * @param appId authenticated application identifier
 * @param principal authenticated principal identifier used for authorization and cache isolation
 * @param requestId non-blank tracing identifier, never used as an authorization decision
 * @param purpose non-blank trusted purpose of the operation
 * @param deadline non-null absolute deadline; past instants are accepted here and rejected by checkDeadline
 */
public record SecretContext(String tenantId, String appId, String principal, String requestId, String purpose,
        Instant deadline) {

    /**
     * Requires nonblank identity components and a non-null deadline. Deadline expiration is checked separately by checkDeadline.
     * @param tenantId authenticated tenant identifier
     * @param appId authenticated application identifier
     * @param principal authenticated principal identifier used for authorization and cache isolation
     * @param requestId non-blank tracing identifier, never used as an authorization decision
     * @param purpose non-blank trusted purpose of the operation
     * @param deadline non-null absolute deadline; past instants are accepted here and rejected by checkDeadline
     * @throws IllegalArgumentException if component values violate this record's constraints
     * @throws NullPointerException if the deadline is null
     */
    public SecretContext {
        if (tenantId == null || tenantId.isBlank() || appId == null || appId.isBlank() || principal == null
                || principal.isBlank() || requestId == null || requestId.isBlank() || purpose == null
                || purpose.isBlank()) {
            throw new IllegalArgumentException("Secret identity context is required");
        }
        Objects.requireNonNull(deadline, "deadline");
    }

    /**
     * Rejects operations at or after the absolute invocation deadline, using the system UTC clock.
     *
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the invocation deadline has been reached
     */
    public void checkDeadline() {
        if (!Instant.now().isBefore(deadline)) {
            throw new CryptoException("Secret operation timed out");
        }
    }

    /**
     * Returns a fixed redacted representation without including secret contents or identity details.
     *
     * @return fixed redacted diagnostic string
     */
    @Override
    public String toString() {
        return "SecretContext[REDACTED]";
    }

}
