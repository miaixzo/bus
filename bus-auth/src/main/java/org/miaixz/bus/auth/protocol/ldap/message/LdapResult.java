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
package org.miaixz.bus.auth.protocol.ldap.message;

import java.net.URI;
import java.util.List;

import org.miaixz.bus.auth.protocol.ldap.LDAP.ResultCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable bounded LDAP result carried by response protocol operations. Diagnostic text is printable, single-line, and
 * bounded; referral URIs are safe absolute values retained only for wire compatibility and are never followed.
 *
 * @param code       supported LDAP result code
 * @param matchedDn  exact matched distinguished name or empty string
 * @param diagnostic fixed safe diagnostic text
 * @param referrals  bounded referral URI list
 * @author Kimi Liu
 */
public record LdapResult(ResultCode code, String matchedDn, String diagnostic, List<URI> referrals) {

    /**
     * Maximum distinguished-name or diagnostic length.
     */
    private static final int MAXIMUM_TEXT_LENGTH = Normal._8192;

    /**
     * Maximum referral count.
     */
    private static final int MAXIMUM_REFERRALS = Normal._16;

    /**
     * Validates and snapshots one LDAP result.
     *
     * @param code       result code
     * @param matchedDn  matched distinguished name
     * @param diagnostic safe diagnostic text
     * @param referrals  referral URIs
     */
    public LdapResult {
        code = Assert.notNull(code, () -> new ValidateException("LDAP result code must not be null"));
        matchedDn = text(matchedDn, "LDAP matched distinguished name");
        diagnostic = text(diagnostic, "LDAP diagnostic");
        referrals = List
                .copyOf(Assert.notNull(referrals, () -> new ValidateException("LDAP referrals must not be null")));
        Assert.isTrue(
                referrals.size() <= MAXIMUM_REFERRALS,
                () -> new ValidateException("LDAP referral count exceeds the maximum"));
        Assert.isTrue(
                referrals.stream().allMatch(LdapResult::safe),
                () -> new ValidateException("LDAP referral URI is invalid"));
    }

    /**
     * Creates a result without referrals.
     *
     * @param code       result code
     * @param matchedDn  matched distinguished name
     * @param diagnostic safe diagnostic
     * @return immutable LDAP result
     */
    public static LdapResult of(final ResultCode code, final String matchedDn, final String diagnostic) {
        return new LdapResult(code, matchedDn, diagnostic, List.of());
    }

    /**
     * Validates one bounded single-line text value.
     *
     * @param value source value
     * @param name  value name
     * @return validated value or empty string
     */
    private static String text(final String value, final String name) {
        final String result = value == null ? "" : value;
        Assert.isTrue(
                result.length() <= MAXIMUM_TEXT_LENGTH && result.indexOf('\r') < 0 && result.indexOf('\n') < 0,
                () -> new ValidateException(name + " is invalid"));
        return result;
    }

    /**
     * Tests whether one referral is a safe absolute URI.
     *
     * @param value referral URI
     * @return whether the URI is safe
     */
    private static boolean safe(final URI value) {
        return value != null && value.isAbsolute() && !value.isOpaque() && value.getUserInfo() == null
                && value.getFragment() == null;
    }

}
