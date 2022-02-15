/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.io;

import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.toolkit.IoKit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 一个接收器,计算它接受的全部字节流的哈希值 若要使用,请创建
 * 使用您首选的哈希算法实例 将所有数据写入接收器,然后调用
 * {@link #hash()}来计算最终的哈希值
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public final class HashSink extends DelegateSink {

    private final MessageDigest messageDigest;
    private final Mac mac;

    private HashSink(Sink sink, String algorithm) {
        super(sink);
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
            this.mac = null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    private HashSink(Sink sink, ByteString key, String algorithm) {
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

    public static HashSink md5(Sink sink) {
        return new HashSink(sink, Algorithm.MD5.getValue());
    }

    public static HashSink sha1(Sink sink) {
        return new HashSink(sink, Algorithm.SHA1.getValue());
    }

    public static HashSink sha256(Sink sink) {
        return new HashSink(sink, Algorithm.SHA256.getValue());
    }

    public static HashSink sha512(Sink sink) {
        return new HashSink(sink, Algorithm.SHA512.getValue());
    }

    public static HashSink hmacSha1(Sink sink, ByteString key) {
        return new HashSink(sink, key, Algorithm.HMACSHA1.getValue());
    }

    public static HashSink hmacSha256(Sink sink, ByteString key) {
        return new HashSink(sink, key, Algorithm.HMACSHA256.getValue());
    }

    public static HashSink hmacSha512(Sink sink, ByteString key) {
        return new HashSink(sink, key, Algorithm.HMACSHA512.getValue());
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        IoKit.checkOffsetAndCount(source.size, 0, byteCount);

        long hashedCount = 0;
        for (Segment s = source.head; hashedCount < byteCount; s = s.next) {
            int toHash = (int) Math.min(byteCount - hashedCount, s.limit - s.pos);
            if (null != messageDigest) {
                messageDigest.update(s.data, s.pos, toHash);
            } else {
                mac.update(s.data, s.pos, toHash);
            }
            hashedCount += toHash;
        }

        super.write(source, byteCount);
    }

    public final ByteString hash() {
        byte[] result = null != messageDigest ? messageDigest.digest() : mac.doFinal();
        return ByteString.of(result);
    }

}
