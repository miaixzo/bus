/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.safety.algorithm;

import org.miaixz.bus.core.lang.Normal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 安全随机数
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SecureRandom extends java.security.SecureRandom {

    private byte[] _data;
    private int _index;
    private int _intPad;

    public SecureRandom(byte[] value) {
        this(false, new byte[][]{value});
    }

    public SecureRandom(byte[][] values) {
        this(false, values);
    }

    public SecureRandom(boolean intPad, byte[] value) {
        this(intPad, new byte[][]{value});
    }

    public SecureRandom(boolean intPad, byte[][] values) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        for (int i = 0; i != values.length; i++) {
            try {
                bOut.write(values[i]);
            } catch (IOException e) {
                throw new IllegalArgumentException("can't save value array.");
            }
        }

        _data = bOut.toByteArray();

        if (intPad) {
            _intPad = _data.length % 4;
        }
    }

    public void nextBytes(byte[] bytes) {
        System.arraycopy(_data, _index, bytes, 0, bytes.length);

        _index += bytes.length;
    }

    public byte[] generateSeed(int numBytes) {
        byte[] bytes = new byte[numBytes];

        this.nextBytes(bytes);

        return bytes;
    }

    public int nextInt() {
        int val = 0;

        val |= nextValue() << 24;
        val |= nextValue() << Normal._16;

        if (_intPad == 2) {
            _intPad--;
        } else {
            val |= nextValue() << 8;
        }

        if (_intPad == 1) {
            _intPad--;
        } else {
            val |= nextValue();
        }

        return val;
    }


    public long nextLong() {
        long val = 0;

        val |= (long) nextValue() << 56;
        val |= (long) nextValue() << 48;
        val |= (long) nextValue() << 40;
        val |= (long) nextValue() << Normal._32;
        val |= (long) nextValue() << 24;
        val |= (long) nextValue() << Normal._16;
        val |= (long) nextValue() << 8;
        val |= nextValue();

        return val;
    }

    public boolean isExhausted() {
        return _index == _data.length;
    }

    private int nextValue() {
        return _data[_index++] & 0xff;
    }

}
