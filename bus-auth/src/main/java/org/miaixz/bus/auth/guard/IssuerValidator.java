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
package org.miaixz.bus.auth.guard;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Enforces exact authorization-server issuer comparison for mix-up attack prevention.
 * <p>
 * OAuth 2.0 Authorization Server Issuer Identification and OpenID Connect require the received issuer to identify the
 * expected authorization server. This guard compares the two lexical values exactly and deliberately performs no URI
 * normalization, discovery, defaulting, case folding, or trailing-slash repair.
 * </p>
 *
 * @author Kimi Liu
 */
public final class IssuerValidator {

    /**
     * Creates a stateless issuer validator.
     */
    public IssuerValidator() {
        // No initialization required.
    }

    /**
     * Validates that a received issuer exactly identifies the expected authorization server.
     *
     * @param expected issuer lexical value bound to the selected provider
     * @param actual   issuer lexical value received from the protocol message
     * @throws IllegalArgumentException if either value is {@code null}
     * @throws ValidateException        if either value is blank or the values do not match exactly
     */
    public void validate(final String expected, final String actual) {
        Assert.notNull(expected, "Expected issuer must not be null");
        Assert.notNull(actual, "Actual issuer must not be null");
        if (expected.isBlank() || actual.isBlank()) {
            throw new ValidateException("Issuer must not be blank");
        }
        if (!expected.equals(actual)) {
            throw new ValidateException("Issuer does not identify the expected authorization server");
        }
    }

}
