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
package org.miaixz.bus.auth.protocol.jwt.signature;

import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.protocol.jwt.JWT.VerificationPolicy;
import org.miaixz.bus.core.lang.Normal;

/**
 * Policy-gated implementation of the unsecured JWS algorithm.
 * <p>
 * Instances can only be obtained from {@link #authorized(VerificationPolicy)} when the trusted product policy selects
 * {@code none}; ordinary signer factories never expose this algorithm.
 * </p>
 *
 * @author Kimi Liu
 * @see JWTSigner
 */
public final class NoneJWTSigner implements JWTSigner {

    /**
     * Registered JOSE identifier.
     */
    public static final String ID_NONE = Normal.NONE;

    /**
     * Constructs a policy-authorized signer.
     */
    private NoneJWTSigner() {
        // No initialization required.
    }

    /**
     * Creates an unsecured signer only for a policy that explicitly selects {@code none}.
     *
     * @param policy trusted product policy
     * @return policy-authorized unsecured signer
     * @throws IllegalArgumentException when the policy does not select {@code none}
     */
    public static NoneJWTSigner authorized(final VerificationPolicy policy) {
        if (policy == null || policy.algorithm() != TrustedAlgorithm.NONE) {
            throw new IllegalArgumentException("Unsigned JWT requires an explicit none policy");
        }
        return new NoneJWTSigner();
    }

    /**
     * Produces the required empty signature for a validated signing input.
     *
     * @param signingInput complete ASCII JWS signing input; never retained
     * @return a newly allocated empty signature
     */
    @Override
    public byte[] sign(final byte[] signingInput) {
        JwsSupport.signingInput(signingInput);
        return new byte[0];
    }

    /**
     * Accepts only the required empty signature for a validated signing input.
     *
     * @param signingInput complete ASCII JWS signing input; never retained
     * @param signature    untrusted signature bytes
     * @return {@code true} only when the signature is empty
     */
    @Override
    public boolean verify(final byte[] signingInput, final byte[] signature) {
        JwsSupport.signingInput(signingInput);
        return signature != null && signature.length == 0;
    }

    /**
     * Returns the policy-gated JOSE algorithm.
     *
     * @return {@link TrustedAlgorithm#NONE}
     */
    @Override
    public TrustedAlgorithm algorithm() {
        return TrustedAlgorithm.NONE;
    }

}
