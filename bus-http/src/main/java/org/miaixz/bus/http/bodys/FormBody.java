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
package org.miaixz.bus.http.bodys;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.UnoUrl;

/**
 * Form请求体内容
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormBody extends RequestBody {

    private final List<String> encodedNames;
    private final List<String> encodedValues;

    FormBody(List<String> encodedNames, List<String> encodedValues) {
        this.encodedNames = org.miaixz.bus.http.Builder.immutableList(encodedNames);
        this.encodedValues = org.miaixz.bus.http.Builder.immutableList(encodedValues);
    }

    public int size() {
        return encodedNames.size();
    }

    public String encodedName(int index) {
        return encodedNames.get(index);
    }

    public String name(int index) {
        return UnoUrl.percentDecode(encodedName(index), true);
    }

    public String encodedValue(int index) {
        return encodedValues.get(index);
    }

    public String value(int index) {
        return UnoUrl.percentDecode(encodedValue(index), true);
    }

    @Override
    public MediaType mediaType() {
        return MediaType.APPLICATION_FORM_URLENCODED_TYPE;
    }

    @Override
    public long length() {
        return writeOrCountBytes(null, true);
    }

    @Override
    public void writeTo(BufferSink sink) {
        writeOrCountBytes(sink, false);
    }

    /**
     * 将此请求写入{@code sink}或测量其内容长度。我们有一种方法可以 同时确保计数和内容是一致的，特别是当涉及到一些棘手的操作时，比如 测量报头字符串的编码长度，或者编码整数的位数长度
     *
     * @param sink       保存缓冲区
     * @param countBytes 是否统计数量
     * @return 当前缓冲区总大小
     */
    private long writeOrCountBytes(BufferSink sink, boolean countBytes) {
        long byteCount = 0L;

        Buffer buffer;
        if (countBytes) {
            buffer = new Buffer();
        } else {
            buffer = sink.buffer();
        }

        for (int i = 0, size = encodedNames.size(); i < size; i++) {
            if (i > 0)
                buffer.writeByte(Symbol.C_AND);
            buffer.writeUtf8(encodedNames.get(i));
            buffer.writeByte(Symbol.C_EQUAL);
            buffer.writeUtf8(encodedValues.get(i));
        }

        if (countBytes) {
            byteCount = buffer.size();
            buffer.clear();
        }

        return byteCount;
    }

    public static class Builder {

        private final List<String> names = new ArrayList<>();
        private final List<String> values = new ArrayList<>();
        private final Charset charset;

        public Builder() {
            this(null);
        }

        public Builder(Charset charset) {
            this.charset = charset;
        }

        public Builder add(String name, String value) {
            if (null == name) {
                throw new NullPointerException("name == null");
            }
            if (null == value) {
                throw new NullPointerException("value == null");
            }

            names.add(UnoUrl.canonicalize(name, UnoUrl.FORM_ENCODE_SET, false, false, true, true, charset));
            values.add(UnoUrl.canonicalize(value, UnoUrl.FORM_ENCODE_SET, false, false, true, true, charset));
            return this;
        }

        public Builder addEncoded(String name, String value) {
            if (null == name) {
                throw new NullPointerException("name == null");
            }
            if (null == value) {
                throw new NullPointerException("value == null");
            }

            names.add(UnoUrl.canonicalize(name, UnoUrl.FORM_ENCODE_SET, true, false, true, true, charset));
            values.add(UnoUrl.canonicalize(value, UnoUrl.FORM_ENCODE_SET, true, false, true, true, charset));
            return this;
        }

        public FormBody build() {
            return new FormBody(names, values);
        }
    }

}
