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
package org.aoju.bus.core.io.segment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 计算其提供的全部字节流的散列的源 若要使用,请创建
 * 使用您首选的哈希算法实例 通过读取源文件的所有字节来耗尽源文件
 * 然后调用{@link #hash()}来计算最终的哈希值
 *
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
public final class HashSource extends ForwardSource {

    private final MessageDigest messageDigest;
    private final Mac mac;

    private HashSource(Source source, String algorithm) {
        super(source);
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
            this.mac = null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    private HashSource(Source source, ByteString key, String algorithm) {
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

    public static HashSource md5(Source source) {
        return new HashSource(source, "MD5");
    }

    public static HashSource sha1(Source source) {
        return new HashSource(source, "SHA-1");
    }

    public static HashSource sha256(Source source) {
        return new HashSource(source, "SHA-256");
    }

    public static HashSource hmacSha1(Source source, ByteString key) {
        return new HashSource(source, key, "HmacSHA1");
    }

    public static HashSource hmacSha256(Source source, ByteString key) {
        return new HashSource(source, key, "HmacSHA256");
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

    public final ByteString hash() {
        byte[] result = messageDigest != null ? messageDigest.digest() : mac.doFinal();
        return ByteString.of(result);
    }

}
