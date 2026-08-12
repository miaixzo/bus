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
package org.miaixz.bus.auth.metric.jwt.signature;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.JWTException;

/**
 * Compatibility sentinel for the permanently disabled unsigned JWT algorithm.
 * <p>
 * The type and its existing fields remain available for binary compatibility. It can neither create nor validate an
 * unsigned token.
 * </p>
 *
 * @author Kimi Liu
 * @see JWTSigner
 */
public class NoneJWTSigner implements JWTSigner {

    /**
     * Registered JOSE identifier retained for rejection and compatibility checks.
     */
    public static final String ID_NONE = Normal.NONE;
    /**
     * Compatibility singleton for the disabled algorithm.
     */
    public static NoneJWTSigner NONE = new NoneJWTSigner();

    /**
     * Constructs a new {@code NoneJWTSigner} instance.
     */
    public NoneJWTSigner() {
        // No initialization required.
    }

    /**
     * Rejects every unsigned JWT signing attempt with a stable shared error.
     *
     * @param headerBase64  ignored encoded header
     * @param payloadBase64 ignored encoded payload
     * @return no value because unsigned signing is disabled
     */
    @Override
    public String sign(final String headerBase64, final String payloadBase64) {
        throw new JWTException(ErrorCode._100532, "Unsigned JWT signing is disabled");
    }

    /**
     * Rejects every unsigned JWT verification attempt.
     *
     * @param headerBase64  ignored encoded header
     * @param payloadBase64 ignored encoded payload
     * @param signBase64    ignored signature input
     * @return always {@code false}
     */
    @Override
    public boolean verify(final String headerBase64, final String payloadBase64, final String signBase64) {
        return false;
    }

    /**
     * Returns the disabled JOSE algorithm identifier for compatibility detection.
     *
     * @return the algorithm identifier "none"
     */
    @Override
    public String getAlgorithm() {
        return ID_NONE;
    }

}
