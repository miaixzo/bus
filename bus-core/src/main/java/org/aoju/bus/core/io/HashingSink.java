/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.aoju.bus.core.io;

import org.aoju.bus.core.utils.IoUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A sink that computes a hash of the full stream of bytes it has accepted. To use, create an
 * instance with your preferred hash algorithm. Write all of the data to the sink and then call
 * {@link #hash()} to compute the final hash value.
 *
 * <p>In this example we use {@code HashingSink} with a {@link BufferedSink} to make writing to the
 * sink easier. <pre>   {@code
 *
 *   HashingSink hashingSink = HashingSink.sha256(s);
 *   BufferedSink bufferedSink = IoUtils.buffer(hashingSink);
 *
 *   ... // Write to bufferedSink and either flush or close it.
 *
 *   ByteString hash = hashingSink.hash();
 * }</pre>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class HashingSink extends ForwardingSink {

    private final MessageDigest messageDigest;
    private final Mac mac;

    private HashingSink(Sink sink, String algorithm) {
        super(sink);
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
            this.mac = null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    private HashingSink(Sink sink, ByteString key, String algorithm) {
        super(sink);
        try {
            this.mac = Mac.getInstance(algorithm);
            this.mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
            this.messageDigest = null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns a sink that uses the obsolete MD5 hash algorithm to produce 128-bit hashes.
     */
    public static HashingSink md5(Sink sink) {
        return new HashingSink(sink, "MD5");
    }

    /**
     * Returns a sink that uses the obsolete SHA-1 hash algorithm to produce 160-bit hashes.
     */
    public static HashingSink sha1(Sink sink) {
        return new HashingSink(sink, "SHA-1");
    }

    /**
     * Returns a sink that uses the SHA-256 hash algorithm to produce 256-bit hashes.
     */
    public static HashingSink sha256(Sink sink) {
        return new HashingSink(sink, "SHA-256");
    }

    /**
     * Returns a sink that uses the SHA-512 hash algorithm to produce 512-bit hashes.
     */
    public static HashingSink sha512(Sink sink) {
        return new HashingSink(sink, "SHA-512");
    }

    /**
     * Returns a sink that uses the obsolete SHA-1 HMAC algorithm to produce 160-bit hashes.
     */
    public static HashingSink hmacSha1(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA1");
    }

    /**
     * Returns a sink that uses the SHA-256 HMAC algorithm to produce 256-bit hashes.
     */
    public static HashingSink hmacSha256(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA256");
    }

    /**
     * Returns a sink that uses the SHA-512 HMAC algorithm to produce 512-bit hashes.
     */
    public static HashingSink hmacSha512(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA512");
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        IoUtils.checkOffsetAndCount(source.size, 0, byteCount);

        // Hash byteCount bytes from the prefix of source.
        long hashedCount = 0;
        for (Segment s = source.head; hashedCount < byteCount; s = s.next) {
            int toHash = (int) Math.min(byteCount - hashedCount, s.limit - s.pos);
            if (messageDigest != null) {
                messageDigest.update(s.data, s.pos, toHash);
            } else {
                mac.update(s.data, s.pos, toHash);
            }
            hashedCount += toHash;
        }

        // Write those bytes to the sink.
        super.write(source, byteCount);
    }

    /**
     * Returns the hash of the bytes accepted thus far and resets the internal state of this sink.
     *
     * <p><strong>Warning:</strong> This method is not idempotent. Each time this method is called its
     * internal state is cleared. This starts a new hash with zero bytes accepted.
     */
    public final ByteString hash() {
        byte[] result = messageDigest != null ? messageDigest.digest() : mac.doFinal();
        return ByteString.of(result);
    }

}
