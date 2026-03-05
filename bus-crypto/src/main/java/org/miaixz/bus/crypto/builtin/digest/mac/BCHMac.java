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
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * BouncyCastle HMAC algorithm implementation engine. This class extends {@link BCMac} and specifically uses
 * {@link HMac} from BouncyCastle to provide HMAC (Hash-based Message Authentication Code) functionality. When the
 * BouncyCastle library is included, it is automatically used as the provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BCHMac extends BCMac {

    /**
     * Constructs a {@code BCHMac} instance with the specified digest algorithm, key, and initialization vector (IV).
     *
     * @param digest The digest algorithm, an implementation of {@link Digest}.
     * @param key    The key as a byte array.
     * @param iv     The initialization vector (IV) as a byte array.
     */
    public BCHMac(final Digest digest, final byte[] key, final byte[] iv) {
        this(digest, new ParametersWithIV(new KeyParameter(key), iv));
    }

    /**
     * Constructs a {@code BCHMac} instance with the specified digest algorithm and key.
     *
     * @param digest The digest algorithm, an implementation of {@link Digest}.
     * @param key    The key as a byte array.
     */
    public BCHMac(final Digest digest, final byte[] key) {
        this(digest, new KeyParameter(key));
    }

    /**
     * Constructs a {@code BCHMac} instance with the specified digest algorithm and cipher parameters.
     *
     * @param digest The digest algorithm, an implementation of {@link Digest}.
     * @param params The {@link CipherParameters} for initializing the HMAC, e.g., a {@link KeyParameter} for the key.
     */
    public BCHMac(final Digest digest, final CipherParameters params) {
        this(new HMac(digest), params);
    }

    /**
     * Constructs a {@code BCHMac} instance with an existing BouncyCastle {@link HMac} and cipher parameters.
     *
     * @param mac    The BouncyCastle {@link HMac} instance.
     * @param params The {@link CipherParameters} for initializing the HMAC, e.g., a {@link KeyParameter} for the key.
     */
    public BCHMac(final HMac mac, final CipherParameters params) {
        super(mac, params);
    }

}
