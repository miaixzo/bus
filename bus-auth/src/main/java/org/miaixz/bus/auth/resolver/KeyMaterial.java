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

import java.security.Key;
import java.time.Instant;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Parsed exact cryptographic key material and validity interval.
 *
 * @param keyId     stable key identifier
 * @param algorithm declared cryptographic algorithm
 * @param key       parsed JCA key material
 * @param notBefore first instant at which the key is valid
 * @param notAfter  exclusive upper validity boundary
 */
public record KeyMaterial(String keyId, String algorithm, Key key, Instant notBefore, Instant notAfter) {

    /**
     * Validates one exact key and its positive validity interval.
     */
    public KeyMaterial {
        Assert.notBlank(keyId, "Key identifier must not be blank");
        Assert.notBlank(algorithm, "Key algorithm must not be blank");
        Assert.notNull(key, "JCA key must not be null");
        Assert.notNull(notBefore, "Key not-before instant must not be null");
        Assert.notNull(notAfter, "Key not-after instant must not be null");
        if (!notBefore.isBefore(notAfter)) {
            throw new ValidateException("Key validity interval must be positive");
        }
    }

}
