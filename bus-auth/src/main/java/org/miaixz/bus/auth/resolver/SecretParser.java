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

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.worker.SecretLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Pure parser for externally leased character-based secrets.
 */
public final class SecretParser {

    /** Creates a stateless secret-material parser. */
    public SecretParser() {
    }

    /**
     * Validates Source and credential-reference ownership of a loaded secret lease.
     *
     * @param registration exact Source registration that requested the secret
     * @param expected     exact requested credential reference
     * @param record       project-loaded secret record
     * @return validated fresh secret lease
     */
    public SecretLease parse(
            final Registration.SourceEntry registration,
            final Credential.Reference expected,
            final SecretLoader.Record record) {
        final String sourceId = Assert.notNull(registration, "Secret Source registration must not be null").resource()
                .getId();
        final Credential.Reference reference = Assert.notNull(expected, "Expected secret reference must not be null");
        final SecretLoader.Record loaded = Assert.notNull(record, "Loaded secret record must not be null");
        if (!sourceId.equals(loaded.sourceId())) {
            throw new ValidateException("Loaded secret does not belong to the requested Source");
        }
        if (!reference.equals(loaded.reference())) {
            throw new ValidateException("Loaded secret reference does not match the requested reference");
        }
        return Assert.notNull(loaded.lease(), "Loaded secret lease must not be null");
    }

}
