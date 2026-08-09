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
package org.miaixz.bus.fabric.network.dns.message;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * EDNS Extended DNS Error metadata attached to an OPT response.
 *
 * @author Kimi Liu
 */
public final class DnsExtendedError {

    /**
     * EDE code for a response blocked by policy.
     */
    public static final int BLOCKED = 15;

    /**
     * EDE code for a filtered response.
     */
    public static final int FILTERED = 17;

    /**
     * EDE code for a response prohibited by policy.
     */
    public static final int PROHIBITED = 18;

    /**
     * EDE info code.
     */
    private final int code;

    /**
     * Optional UTF-8 diagnostic text.
     */
    private final String text;

    /**
     * Creates an EDNS Extended DNS Error value.
     *
     * @param code EDE info code
     * @param text optional diagnostic text, or {@code null}
     * @throws ValidateException if the code is outside the unsigned 16-bit range
     */
    public DnsExtendedError(final int code, final String text) {
        this.code = DnsCodec.validateUnsignedShort(code, "DNS EDE code");
        this.text = text == null || text.isBlank() ? null : text.trim();
    }

    /**
     * Creates a blocked-by-policy EDE value.
     *
     * @param text optional diagnostic text, or {@code null}
     * @return EDE value
     */
    public static DnsExtendedError blocked(final String text) {
        return new DnsExtendedError(BLOCKED, text);
    }

    /**
     * Returns the EDE info code.
     *
     * @return unsigned 16-bit EDE code
     */
    public int code() {
        return code;
    }

    /**
     * Returns optional EDE diagnostic text.
     *
     * @return diagnostic text, or {@code null}
     */
    public String text() {
        return text;
    }

}
