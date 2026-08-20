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
package org.miaixz.bus.auth.protocol.oauth1;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Preserves an exact OAuth 1.0 signature method registration or extension while leaving execution to an allow-list.
 *
 * @param value case-sensitive signature method value
 * @author Kimi Liu
 */
public record SignatureMethod(String value) {

    /**
     * HMAC-SHA1 signature method registered by RFC 5849.
     */
    public static final SignatureMethod HMAC_SHA1 = new SignatureMethod("HMAC-SHA1");
    /**
     * RSA-SHA1 signature method registered by RFC 5849.
     */
    public static final SignatureMethod RSA_SHA1 = new SignatureMethod("RSA-SHA1");

    /**
     * Validates an open registration value without enabling unsupported or unsafe execution.
     *
     * @throws IllegalArgumentException if value is {@code null} or blank
     * @throws ValidateException        if value contains ASCII control or whitespace characters
     */
    public SignatureMethod {
        Assert.notBlank(value, "OAuth 1.0 signature method must not be blank");
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) <= 0x20 || value.charAt(index) == 0x7f) {
                throw new ValidateException("OAuth 1.0 signature method contains control or whitespace characters");
            }
        }
    }

}
