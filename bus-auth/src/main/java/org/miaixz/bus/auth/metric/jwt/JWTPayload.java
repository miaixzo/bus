/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.metric.jwt;

import java.io.Serial;
import java.util.Map;

/**
 * JWT Payload information. The payload is the part where effective information is stored. This name is like referring
 * to the cargo carried on an airplane. This effective information includes three parts:
 *
 * <ul>
 * <li>Standard registered claims</li>
 * <li>Public claims</li>
 * <li>Private claims</li>
 * </ul>
 * <p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWTPayload extends Claims implements JWTRegister<JWTPayload> {

    @Serial
    private static final long serialVersionUID = 2852289330860L;

    /**
     * Adds custom JWT authentication payload information.
     *
     * @param payloadClaims a map containing multiple payload claims to add
     * @return this {@link JWTPayload} instance
     */
    public JWTPayload addPayloads(final Map<String, ?> payloadClaims) {
        putAll(payloadClaims);
        return this;
    }

    /**
     *
     * 
     * Sets a specific payload claim with the given name and value.
     *
     * @param name  the name of the claim
     * @param value the value of the claim
     * @return this {@link JWTPayload} instance
     */
    @Override
    public JWTPayload setPayload(final String name, final Object value) {
        setClaim(name, value);
        return this;
    }

}
