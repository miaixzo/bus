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
package org.miaixz.bus.auth.resolver;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.SecretLease;

/**
 * Acquires a new short-lived lease for externally stored character-based secret material.
 * <p>
 * Implementations support password, client-secret, and shared-secret references. Private keys and certificates are
 * resolved through their dedicated typed ports. Every successful call returns a new lease for immediate use inside one
 * bounded operation; consumers close it before crossing another asynchronous boundary.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface SecretResolver {

    /**
     * Acquires a new lease for one typed external credential reference.
     *
     * @param request password, client-secret, or shared-secret reference
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a successful new lease, expected rejection, or operational failure
     */
    CompletionStage<Outcome<SecretLease>> resolve(
            Credential.Reference request,
            Context context,
            Timeout.Budget timeout);

}
