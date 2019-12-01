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
package org.aoju.bus.http.bodys;

import org.aoju.bus.core.consts.MediaType;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.io.segment.Buffer;
import org.aoju.bus.core.io.segment.BufferSink;
import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Url;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
public final class FormBody extends RequestBody {

    private final List<String> encodedNames;
    private final List<String> encodedValues;

    FormBody(List<String> encodedNames, List<String> encodedValues) {
        this.encodedNames = Internal.immutableList(encodedNames);
        this.encodedValues = Internal.immutableList(encodedValues);
    }

    public int size() {
        return encodedNames.size();
    }

    public String encodedName(int index) {
        return encodedNames.get(index);
    }

    public String name(int index) {
        return Url.percentDecode(encodedName(index), true);
    }

    public String encodedValue(int index) {
        return encodedValues.get(index);
    }

    public String value(int index) {
        return Url.percentDecode(encodedValue(index), true);
    }

    @Override
    public MediaType contentType() {
        return MediaType.APPLICATION_FORM_URLENCODED_TYPE;
    }

    @Override
    public long contentLength() {
        return writeOrCountBytes(null, true);
    }

    @Override
    public void writeTo(BufferSink sink) throws IOException {
        writeOrCountBytes(sink, false);
    }

    private long writeOrCountBytes(BufferSink sink, boolean countBytes) {
        long byteCount = 0L;

        Buffer buffer;
        if (countBytes) {
            buffer = new Buffer();
        } else {
            buffer = sink.buffer();
        }

        for (int i = 0, size = encodedNames.size(); i < size; i++) {
            if (i > 0) buffer.writeByte('&');
            buffer.writeUtf8(encodedNames.get(i));
            buffer.writeByte('=');
            buffer.writeUtf8(encodedValues.get(i));
        }

        if (countBytes) {
            byteCount = buffer.size();
            buffer.clear();
        }

        return byteCount;
    }

    public static final class Builder {

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
            if (name == null) throw new NullPointerException("name == null");
            if (value == null) throw new NullPointerException("value == null");

            names.add(Url.canonicalize(name, Symbol.FORM_ENCODE_SET, false, false, true, true, charset));
            values.add(Url.canonicalize(value, Symbol.FORM_ENCODE_SET, false, false, true, true, charset));
            return this;
        }

        public Builder addEncoded(String name, String value) {
            if (name == null) throw new NullPointerException("name == null");
            if (value == null) throw new NullPointerException("value == null");

            names.add(Url.canonicalize(name, Symbol.FORM_ENCODE_SET, true, false, true, true, charset));
            values.add(Url.canonicalize(value, Symbol.FORM_ENCODE_SET, true, false, true, true, charset));
            return this;
        }

        public FormBody build() {
            return new FormBody(names, values);
        }

    }

}
