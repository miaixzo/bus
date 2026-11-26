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
package org.miaixz.bus.crypto.builtin.digest.mac;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;

/**
 * BouncyCastle MAC algorithm implementation engine. This class wraps a BouncyCastle {@link org.bouncycastle.crypto.Mac}
 * instance to provide MAC functionality. When the BouncyCastle library is included, it is automatically used as the
 * provider.
 *
 * @author Kimi Liu
 */
public class BCMac extends SimpleWrapper<org.bouncycastle.crypto.Mac> implements Mac {

    /**
     * Constructs a {@code BCMac} instance with the specified BouncyCastle MAC and cipher parameters.
     *
     * @param mac    The BouncyCastle {@link org.bouncycastle.crypto.Mac} instance.
     * @param params The {@link CipherParameters} for initializing the MAC, e.g., a {@link KeyParameter} for the key.
     */
    public BCMac(final org.bouncycastle.crypto.Mac mac, final CipherParameters params) {
        super(initMac(mac, params));
    }

    /**
     * Initializes the BouncyCastle MAC instance with the given parameters.
     *
     * @param mac    The BouncyCastle {@link org.bouncycastle.crypto.Mac} instance.
     * @param params The {@link CipherParameters} for initialization.
     * @return The initialized BouncyCastle {@link org.bouncycastle.crypto.Mac} instance.
     */
    private static org.bouncycastle.crypto.Mac initMac(
            final org.bouncycastle.crypto.Mac mac,
            final CipherParameters params) {
        mac.init(params);
        return mac;
    }

    /**
     * Updates the MAC with the specified portion of the given byte array.
     *
     * @param in    input bytes used to update the MAC
     * @param inOff inclusive starting offset in the input array
     * @param len   number of bytes to process
     */
    @Override
    public void update(final byte[] in, final int inOff, final int len) {
        this.raw.update(in, inOff, len);
    }

    /**
     * Completes the MAC calculation and returns the result. The MAC is reset after this operation.
     *
     * @return The calculated MAC value as a byte array.
     */
    @Override
    public byte[] doFinal() {
        final byte[] result = new byte[getMacLength()];
        this.raw.doFinal(result, 0);
        return result;
    }

    /**
     * Resets the MAC to its initial state, discarding all processed data.
     */
    @Override
    public void reset() {
        this.raw.reset();
    }

    /**
     * Retrieves the length of the MAC in bytes.
     *
     * @return The MAC length in bytes.
     */
    @Override
    public int getMacLength() {
        return this.raw.getMacSize();
    }

    /**
     * Retrieves the name of the MAC algorithm.
     *
     * @return The algorithm name.
     */
    @Override
    public String getAlgorithm() {
        return this.raw.getAlgorithmName();
    }

}
