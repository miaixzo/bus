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
package org.miaixz.bus.auth.protocol.radius;

import org.miaixz.bus.core.lang.Assert;

/**
 * Represents the unsigned one-octet RADIUS Code registry value.
 * <p>
 * The value object accepts only the six packet Codes represented by the built-in packet model. Numeric registration
 * remains exclusively in {@link Radius.Codes}; this type adds validation and value semantics without duplicating the
 * registry table.
 * </p>
 *
 * @param value unsigned RADIUS Code value
 * @author Kimi Liu
 */
public record RadiusCode(int value) {

    /**
     * Validates one packet Code supported by the built-in Access and Accounting model.
     *
     * @param value unsigned one-octet Code value
     * @throws IllegalArgumentException if the value is not implemented by the built-in packet model
     */
    public RadiusCode {
        Assert.isTrue(implemented(value), "RADIUS Code is not implemented by the packet model");
    }

    /**
     * Tests one value against the authoritative protocol registry subset.
     *
     * @param value unsigned Code value
     * @return {@code true} for one of the six implemented packet Codes
     */
    private static boolean implemented(final int value) {
        return switch (value) {
            case Radius.Codes.ACCESS_REQUEST, Radius.Codes.ACCESS_ACCEPT, Radius.Codes.ACCESS_REJECT, Radius.Codes.ACCOUNTING_REQUEST, Radius.Codes.ACCOUNTING_RESPONSE, Radius.Codes.ACCESS_CHALLENGE -> true;
            default -> false;
        };
    }

}
