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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A source that computes a hash of the full stream of bytes it has supplied. To use, create an
 * instance with your preferred hash algorithm. Exhaust the source by reading all of its bytes and
 * then call {@link #hash()} to compute the final hash value.
 *
 * <p>In this example we use {@code HashingSource} with a {@link BufferedSource} to make reading
 * from the source easier. <pre>   {@code
 *
 *   HashingSource hashingSource = HashingSource.sha256(rawSource);
 *   BufferedSource bufferedSource = IoUtils.buffer(hashingSource);
 *
 *   ... // Read all of bufferedSource.
 *
 *   ByteString hash = hashingSource.hash();
 * }</pre>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class HashingSource extends ForwardingSource {

    private final MessageDigest messageDigest;
    private final Mac mac;

    private HashingSource(Source source, String algorithm) {
        super(source);
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
            this.mac = null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    private HashingSource(Source source, ByteString key, String algorithm) {
        super(source);
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
    public static HashingSource md5(Source source) {
        return new HashingSource(source, "MD5");
    }

    /**
     * Returns a sink that uses the obsolete SHA-1 hash algorithm to produce 160-bit hashes.
     */
    public static HashingSource sha1(Source source) {
        return new HashingSource(source, "SHA-1");
    }

    /**
     * Returns a sink that uses the SHA-256 hash algorithm to produce 256-bit hashes.
     */
    public static HashingSource sha256(Source source) {
        return new HashingSource(source, "SHA-256");
    }

    /**
     * Returns a sink that uses the obsolete SHA-1 HMAC algorithm to produce 160-bit hashes.
     */
    public static HashingSource hmacSha1(Source source, ByteString key) {
        return new HashingSource(source, key, "HmacSHA1");
    }

    /**
     * Returns a sink that uses the SHA-256 HMAC algorithm to produce 256-bit hashes.
     */
    public static HashingSource hmacSha256(Source source, ByteString key) {
        return new HashingSource(source, key, "HmacSHA256");
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        long result = super.read(sink, byteCount);

        if (result != -1L) {
            long start = sink.size - result;

            // Find the first segment that has new bytes.
            long offset = sink.size;
            Segment s = sink.head;
            while (offset > start) {
                s = s.prev;
                offset -= (s.limit - s.pos);
            }

            // Hash that segment and all the rest until the end.
            while (offset < sink.size) {
                int pos = (int) (s.pos + start - offset);
                if (messageDigest != null) {
                    messageDigest.update(s.data, pos, s.limit - pos);
                } else {
                    mac.update(s.data, pos, s.limit - pos);
                }
                offset += (s.limit - s.pos);
                start = offset;
                s = s.next;
            }
        }

        return result;
    }

    /**
     * Returns the hash of the bytes supplied thus far and resets the internal state of this source.
     *
     * <p><strong>Warning:</strong> This method is not idempotent. Each time this method is called its
     * internal state is cleared. This starts a new hash with zero bytes supplied.
     */
    public final ByteString hash() {
        byte[] result = messageDigest != null ? messageDigest.digest() : mac.doFinal();
        return ByteString.of(result);
    }

}
