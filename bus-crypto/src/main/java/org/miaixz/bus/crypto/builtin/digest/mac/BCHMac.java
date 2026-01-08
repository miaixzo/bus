/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
