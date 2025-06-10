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
package org.miaixz.bus.core.io.resource;

import java.io.Serial;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;

/**
 * {@link CharSequence}资源，字符串做为资源
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharSequenceResource extends BytesResource {

    @Serial
    private static final long serialVersionUID = 2852230576091L;

    /**
     * 由于{@link java.nio.charset.Charset} 无法序列化，此处使用编码名称
     */
    private final String charsetName;

    /**
     * 构造，使用UTF8编码
     *
     * @param data 资源数据
     */
    public CharSequenceResource(final CharSequence data) {
        this(data, null);
    }

    /**
     * 构造，使用UTF8编码
     *
     * @param data 资源数据
     * @param name 资源名称
     */
    public CharSequenceResource(final CharSequence data, final String name) {
        this(data, name, Charset.UTF_8);
    }

    /**
     * 构造
     *
     * @param data    资源数据
     * @param name    资源名称
     * @param charset 编码
     */
    public CharSequenceResource(final CharSequence data, final String name, final java.nio.charset.Charset charset) {
        super(ByteKit.toBytes(data, charset), name);
        this.charsetName = charset.name();
    }

    /**
     * 读取为字符串
     *
     * @return 字符串
     */
    public String readString() {
        return readString(getCharset());
    }

    /**
     * 获取编码名
     *
     * @return 编码名
     */
    public String getCharsetName() {
        return this.charsetName;
    }

    /**
     * 获取编码
     *
     * @return 编码
     */
    public java.nio.charset.Charset getCharset() {
        return Charset.charset(this.charsetName);
    }

}
