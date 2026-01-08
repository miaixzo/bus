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
package org.miaixz.bus.crypto.center;

import java.io.Serial;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.crypto.builtin.digest.Digester;

/**
 * SM3 cryptographic hash (digest) algorithm implementation.
 * <p>
 * The suite of national cryptographic algorithms (Guomi algorithms) includes:
 * </p>
 * <ol>
 * <li>Asymmetric encryption and signature: SM2 (asymmetric)</li>
 * <li>Digest signature algorithm: SM3 (digest)</li>
 * <li>Symmetric encryption: SM4 (symmetric)</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SM3 extends Digester {

    @Serial
    private static final long serialVersionUID = 2852290571103L;

    /**
     * Constructs an SM3 digester.
     */
    public SM3() {
        super(Algorithm.SM3.getValue());
    }

    /**
     * Constructs an SM3 digester with the specified salt.
     *
     * @param salt The salt value as a byte array.
     */
    public SM3(final byte[] salt) {
        this(salt, 0, 1);
    }

    /**
     * Constructs an SM3 digester with the specified salt and digest count.
     *
     * @param salt        The salt value as a byte array.
     * @param digestCount The number of times to apply the digest algorithm. If less than or equal to 1, it defaults to
     *                    1.
     */
    public SM3(final byte[] salt, final int digestCount) {
        this(salt, 0, digestCount);
    }

    /**
     * Constructs an SM3 digester with the specified salt, salt position, and digest count.
     *
     * @param salt         The salt value as a byte array.
     * @param saltPosition The index at which the salt string is placed in the data. Defaults to 0.
     * @param digestCount  The number of times to apply the digest algorithm. If less than or equal to 1, it defaults to
     *                     1.
     */
    public SM3(final byte[] salt, final int saltPosition, final int digestCount) {
        this();
        this.salt = salt;
        this.saltPosition = saltPosition;
        this.digestCount = digestCount;
    }

    /**
     * Creates a new SM3 instance.
     *
     * @return A new {@link SM3} instance.
     */
    public static SM3 of() {
        return new SM3();
    }

}
