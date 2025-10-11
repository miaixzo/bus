/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

/**
 * Argon2 hashing implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Argon2 {

    /**
     * Default hash length.
     */
    public static final int DEFAULT_HASH_LENGTH = 32;

    private int hashLength = DEFAULT_HASH_LENGTH;
    private final Argon2Parameters.Builder paramsBuilder;

    /**
     * Constructor, uses {@link Argon2Parameters#ARGON2_id} type by default.
     */
    public Argon2() {
        this(Argon2Parameters.ARGON2_id);
    }

    /**
     * Constructor.
     *
     * @param type The Argon2 type, e.g., {@link Argon2Parameters#ARGON2_d}, {@link Argon2Parameters#ARGON2_i},
     *             {@link Argon2Parameters#ARGON2_id}.
     */
    public Argon2(final int type) {
        this(new Argon2Parameters.Builder(type));
    }

    /**
     * Constructor.
     *
     * @param paramsBuilder The parameter builder.
     */
    public Argon2(final Argon2Parameters.Builder paramsBuilder) {
        this.paramsBuilder = paramsBuilder;
    }

    /**
     * Sets the hash length.
     *
     * @param hashLength The hash length.
     * @return this
     */
    public Argon2 setHashLength(final int hashLength) {
        this.hashLength = hashLength;
        return this;
    }

    /**
     * Sets the version.
     *
     * @param version The version.
     * @return this
     * @see Argon2Parameters#ARGON2_VERSION_10
     * @see Argon2Parameters#ARGON2_VERSION_13
     */
    public Argon2 setVersion(final int version) {
        this.paramsBuilder.withVersion(version);
        return this;
    }

    /**
     * Sets the salt.
     *
     * @param salt The salt.
     * @return this
     */
    public Argon2 setSalt(final byte[] salt) {
        this.paramsBuilder.withSalt(salt);
        return this;
    }

    /**
     * Sets optional secret data to increase hash complexity.
     *
     * @param secret The secret key.
     * @return this
     */
    public Argon2 setSecret(final byte[] secret) {
        this.paramsBuilder.withSecret(secret);
        return this;
    }

    /**
     * Sets additional data.
     *
     * @param additional The additional data.
     * @return this
     */
    public Argon2 setAdditional(final byte[] additional) {
        this.paramsBuilder.withAdditional(additional);
        return this;
    }

    /**
     * Sets the number of iterations. The more iterations, the longer it takes to generate the hash, and the harder it
     * is to crack.
     *
     * @param iterations The number of iterations.
     * @return this
     */
    public Argon2 setIterations(final int iterations) {
        this.paramsBuilder.withIterations(iterations);
        return this;
    }

    /**
     * Sets the memory cost in KB. The more memory, the longer it takes to generate the hash, and the harder it is to
     * crack.
     *
     * @param memoryAsKB The memory cost in kilobytes.
     * @return this
     */
    public Argon2 setMemoryAsKB(final int memoryAsKB) {
        this.paramsBuilder.withMemoryAsKB(memoryAsKB);
        return this;
    }

    /**
     * Sets the degree of parallelism (number of cores to use). The higher the value, the longer it takes to generate
     * the hash, and the harder it is to crack.
     *
     * @param parallelism The degree of parallelism.
     * @return this
     */
    public Argon2 setParallelism(final int parallelism) {
        this.paramsBuilder.withParallelism(parallelism);
        return this;
    }

    /**
     * Generates the hash value.
     *
     * @param password The password.
     * @return The hash value.
     */
    public byte[] digest(final char[] password) {
        final Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(paramsBuilder.build());
        final byte[] result = new byte[hashLength];
        generator.generateBytes(password, result);
        return result;
    }

}
