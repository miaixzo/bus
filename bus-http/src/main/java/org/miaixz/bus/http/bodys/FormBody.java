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
 * HTTP 表单请求体
 * <p>
 * 表示以 <code>application/x-www-form-urlencoded</code> 格式编码的表单数据。 提供对表单字段名称和值的编码和解码支持，字段以键值对形式存储。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormBody extends RequestBody {

    /**
     * 编码后的字段名称列表
     */
    private final List<String> encodedNames;
    /**
     * 编码后的字段值列表
     */
    private final List<String> encodedValues;

    /**
     * 构造函数，初始化 FormBody 实例
     *
     * @param encodedNames  编码后的字段名称列表
     * @param encodedValues 编码后的字段值列表
     */
    FormBody(List<String> encodedNames, List<String> encodedValues) {
        this.encodedNames = org.miaixz.bus.http.Builder.immutableList(encodedNames);
        this.encodedValues = org.miaixz.bus.http.Builder.immutableList(encodedValues);
    }

    /**
     * 获取表单字段数量
     *
     * @return 字段数量
     */
    public int size() {
        return encodedNames.size();
    }

    /**
     * 获取指定索引的编码字段名称
     *
     * @param index 索引
     * @return 编码字段名称
     */
    public String encodedName(int index) {
        return encodedNames.get(index);
    }

    /**
     * 获取指定索引的解码字段名称
     *
     * @param index 索引
     * @return 解码字段名称
     */
    public String name(int index) {
        return UnoUrl.percentDecode(encodedName(index), true);
    }

    /**
     * 获取指定索引的编码字段值
     *
     * @param index 索引
     * @return 编码字段值
     */
    public String encodedValue(int index) {
        return encodedValues.get(index);
    }

    /**
     * 获取指定索引的解码字段值
     *
     * @param index 索引
     * @return 解码字段值
     */
    public String value(int index) {
        return UnoUrl.percentDecode(encodedValue(index), true);
    }

    /**
     * 获取媒体类型
     *
     * @return 媒体类型（application/x-www-form-urlencoded）
     */
    @Override
    public MediaType mediaType() {
        return MediaType.APPLICATION_FORM_URLENCODED_TYPE;
    }

    /**
     * 获取请求体长度
     *
     * @return 请求体字节长度
     */
    @Override
    public long length() {
        return writeOrCountBytes(null, true);
    }

    /**
     * 将请求体写入输出流
     *
     * @param sink 输出流
     */
    @Override
    public void writeTo(BufferSink sink) {
        writeOrCountBytes(sink, false);
    }

    /**
     * 写入或计算请求体字节长度
     * <p>
     * 用于写入请求体到输出流或计算其字节长度，确保内容一致性。
     * </p>
     *
     * @param sink       输出流（计算长度时为 null）
     * @param countBytes 是否仅计算字节数
     * @return 字节长度
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

    /**
     * FormBody 构建器
     */
    public static class Builder {

        /**
         * 字段名称列表
         */
        private final List<String> names = new ArrayList<>();
        /**
         * 字段值列表
         */
        private final List<String> values = new ArrayList<>();
        /**
         * 字符集
         */
        private final Charset charset;

        /**
         * 默认构造函数
         */
        public Builder() {
            this(null);
        }

        /**
         * 构造函数，指定字符集
         *
         * @param charset 字符集（null 表示默认 UTF-8）
         */
        public Builder(Charset charset) {
            this.charset = charset;
        }

        /**
         * 添加表单字段
         *
         * @param name  字段名称
         * @param value 字段值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 name 或 value 为 null
         */
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

        /**
         * 添加已编码的表单字段
         *
         * @param name  已编码的字段名称
         * @param value 已编码的字段值
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 name 或 value 为 null
         */
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

        /**
         * 构建 FormBody 实例
         *
         * @return FormBody 实例
         */
        public FormBody build() {
            return new FormBody(names, values);
        }
    }

}