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
package org.miaixz.bus.auth.source.vendor;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.SecretLease;

/**
 * Stores recoverable client-side Vendor credential material in project-owned secure storage.
 * <p>
 * Implementations must synchronously capture the minimum material needed by their backend before this method returns.
 * The caller closes and erases the supplied lease immediately after the returned stage is obtained, so an
 * implementation must never read the lease later from an asynchronous callback. Encryption, KMS, Vault, database,
 * replacement, and lifecycle policy remain entirely project-owned.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface VendorCredentialWriter {

    /**
     * Stores one plaintext client-side credential and returns only its non-secret external reference.
     *
     * @param vendor     exact external platform identifier
     * @param variant    exact external platform variant
     * @param type       credential type fixed by the selected manifest variant
     * @param clientId   public external client identifier
     * @param credential short-lived caller-owned plaintext lease
     * @param context    immutable non-secret invocation context
     * @param timeout    shared end-to-end operation timeout
     * @return asynchronous project storage outcome containing only a credential reference
     */
    CompletionStage<Outcome<Credential.Reference>> write(
            Vendor.Id vendor,
            Vendor.Variant variant,
            Credential.Type type,
            String clientId,
            SecretLease credential,
            Context context,
            Timeout timeout);

}
