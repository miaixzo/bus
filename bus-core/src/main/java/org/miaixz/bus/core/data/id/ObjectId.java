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
package org.miaixz.bus.core.data.id;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A MongoDB ObjectId generator. An ObjectId is composed of:
 *
 * <pre>
 * 1. Time: A timestamp.
 * 2. Machine: A unique identifier for the host, typically a hash of the machine's hostname.
 * 3. Random number.
 * 4. INC: An auto-incrementing counter to ensure uniqueness within the same second.
 * </pre>
 *
 * <pre>
 *     | Timestamp | Random Number | Auto-incrementing Counter |
 *     |   4 bytes |   4 bytes     |    4 bytes                |
 * </pre>
 * 
 * Reference: <a href=
 * "https://github.com/mongodb/mongo-java-driver/blob/master/bson/src/main/org/bson/types/ObjectId.java">ObjectId</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ObjectId {

    /**
     * A thread-safe counter for generating unique IDs.
     */
    private static final AtomicInteger NEXT_INC = new AtomicInteger(RandomKit.randomInt());
    /**
     * The machine identifier.
     */
    private static final char[] MACHINE_CODE = initMachineCode();

    /**
     * Checks if the given string is a valid ObjectId.
     *
     * @param s The string to check.
     * @return {@code true} if the string is a valid ObjectId, {@code false} otherwise.
     */
    public static boolean isValid(String s) {
        if (s == null) {
            return false;
        }
        s = StringKit.removeAll(s, Symbol.MINUS);
        final int len = s.length();
        if (len != 24) {
            return false;
        }

        char c;
        for (int i = 0; i < len; i++) {
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c >= 'a' && c <= 'f') {
                continue;
            }
            if (c >= 'A' && c <= 'F') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the byte representation of a new ObjectId.
     *
     * @return The byte array of the ObjectId.
     */
    public static byte[] nextBytes() {
        return next().getBytes();
    }

    /**
     * Gets a new ObjectId string (without hyphens).
     *
     * @return The ObjectId string.
     */
    public static String id() {
        return next();
    }

    /**
     * Gets a new ObjectId string (without hyphens).
     *
     * @return The ObjectId string.
     */
    public static String next() {
        final char[] ids = new char[24];
        int epoch = (int) ((System.currentTimeMillis() / 1000));
        // 4 bytes: timestamp
        for (int i = 7; i >= 0; i--) {
            ids[i] = Normal.DIGITS_16_LOWER[(epoch & 15)];
            epoch >>>= 4;
        }
        // 4 bytes: random number
        System.arraycopy(MACHINE_CODE, 0, ids, 8, 8);
        // 4 bytes: auto-incrementing sequence. Wraps around on overflow.
        int seq = NEXT_INC.incrementAndGet();
        for (int i = 23; i >= 16; i--) {
            ids[i] = Normal.DIGITS_16_LOWER[(seq & 15)];
            seq >>>= 4;
        }
        return new String(ids);
    }

    /**
     * Gets a new ObjectId string.
     *
     * @param withHyphen Whether to include hyphens as separators.
     * @return The ObjectId string.
     */
    public static String next(final boolean withHyphen) {
        if (!withHyphen) {
            return next();
        }
        final char[] ids = new char[26];
        ids[8] = Symbol.C_MINUS;
        ids[17] = Symbol.C_MINUS;
        int epoch = (int) ((System.currentTimeMillis() / 1000));
        // 4 bytes: timestamp
        for (int i = 7; i >= 0; i--) {
            ids[i] = Normal.DIGITS_16_LOWER[(epoch & 15)];
            epoch >>>= 4;
        }
        // 4 bytes: random number
        System.arraycopy(MACHINE_CODE, 0, ids, 9, 8);
        // 4 bytes: auto-incrementing sequence. Wraps around on overflow.
        int seq = NEXT_INC.incrementAndGet();
        for (int i = 25; i >= 18; i--) {
            ids[i] = Normal.DIGITS_16_LOWER[(seq & 15)];
            seq >>>= 4;
        }
        return new String(ids);
    }

    /**
     * Initializes the machine code.
     *
     * @return The machine code.
     */
    private static char[] initMachineCode() {
        // Machine code: 4-byte random number, 8 hex characters. Avoids bugs with identical machine codes in Docker
        // containers.
        final char[] macAndPid = new char[8];
        final Random random = new Random();
        for (int i = 7; i >= 0; i--) {
            macAndPid[i] = Normal.DIGITS_16_LOWER[random.nextInt() & 15];
        }
        return macAndPid;
    }

}
