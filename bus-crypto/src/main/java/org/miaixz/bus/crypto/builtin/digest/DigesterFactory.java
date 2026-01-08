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
package org.miaixz.bus.crypto.builtin.digest;

import java.security.MessageDigest;
import java.security.Provider;

import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Holder;

/**
 * A simple factory for creating {@link Digester} objects. Inspired by Guava, this factory holds a prototype
 * {@link MessageDigest} object and prioritizes creating new {@link Digester} instances by cloning the prototype to
 * improve initialization performance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DigesterFactory {

    /**
     * The prototype {@link MessageDigest} instance used for cloning.
     */
    private final MessageDigest prototype;
    /**
     * Indicates whether the prototype {@link MessageDigest} supports cloning.
     */
    private final boolean cloneSupport;

    /**
     * Constructs a {@code DigesterFactory} with the given prototype {@link MessageDigest}.
     *
     * @param messageDigest The prototype {@link MessageDigest} instance.
     */
    private DigesterFactory(final MessageDigest messageDigest) {
        this.prototype = messageDigest;
        this.cloneSupport = checkCloneSupport(messageDigest);
    }

    /**
     * Creates a {@code DigesterFactory} that uses only JDK-provided algorithms.
     *
     * @param algorithm The algorithm name (e.g., "MD5", "SHA-256").
     * @return A new {@code DigesterFactory} instance.
     */
    public static DigesterFactory ofJdk(final String algorithm) {
        return of(Builder.createJdkMessageDigest(algorithm));
    }

    /**
     * Creates a {@code DigesterFactory} that uses a provider found by {@link Holder}.
     *
     * @param algorithm The algorithm name (e.g., "MD5", "SHA-256").
     * @return A new {@code DigesterFactory} instance.
     */
    public static DigesterFactory of(final String algorithm) {
        return of(Builder.createMessageDigest(algorithm, null));
    }

    /**
     * Creates a {@code DigesterFactory} with the given {@link MessageDigest} instance.
     *
     * @param messageDigest The {@link MessageDigest} instance, which can be created using
     *                      {@link Builder#createMessageDigest(String, Provider)}.
     * @return A new {@code DigesterFactory} instance.
     */
    public static DigesterFactory of(final MessageDigest messageDigest) {
        return new DigesterFactory(messageDigest);
    }

    /**
     * Checks if the given {@link MessageDigest} object supports cloning.
     *
     * @param messageDigest The {@link MessageDigest} to check.
     * @return {@code true} if cloning is supported, {@code false} otherwise.
     */
    private static boolean checkCloneSupport(final MessageDigest messageDigest) {
        try {
            messageDigest.clone();
            return true;
        } catch (final CloneNotSupportedException e) {
            return false;
        }
    }

    /**
     * Creates a new {@link Digester} instance. This method attempts to clone the prototype {@link MessageDigest} for
     * better performance.
     *
     * @return A new {@link Digester} instance.
     */
    public Digester createDigester() {
        return new Digester(createMessageDigester());
    }

    /**
     * Creates a new {@link MessageDigest} instance. If cloning is supported, a clone of the prototype is returned.
     * Otherwise, a new instance is created using {@link Builder#createJdkMessageDigest(String)}.
     *
     * @return A new {@link MessageDigest} instance.
     */
    public MessageDigest createMessageDigester() {
        if (cloneSupport) {
            try {
                return (MessageDigest) prototype.clone();
            } catch (final CloneNotSupportedException ignore) {
                // ignore
            }
        }
        return Builder.createJdkMessageDigest(prototype.getAlgorithm());
    }

}
