/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
 * 一个接收器，计算它接受的全部字节流的哈希值。若要使用，请创建
 * 使用您首选的哈希算法实例。将所有数据写入接收器，然后调用
 * {@link #hash()}来计算最终的哈希值。
 *
 * @author Kimi Liu
 * @version 3.5.2
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

    public static HashingSink md5(Sink sink) {
        return new HashingSink(sink, "MD5");
    }

    public static HashingSink sha1(Sink sink) {
        return new HashingSink(sink, "SHA-1");
    }

    public static HashingSink sha256(Sink sink) {
        return new HashingSink(sink, "SHA-256");
    }

    public static HashingSink sha512(Sink sink) {
        return new HashingSink(sink, "SHA-512");
    }

    public static HashingSink hmacSha1(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA1");
    }

    public static HashingSink hmacSha256(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA256");
    }

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

    public final ByteString hash() {
        byte[] result = messageDigest != null ? messageDigest.digest() : mac.doFinal();
        return ByteString.of(result);
    }

}
