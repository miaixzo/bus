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
package org.miaixz.bus.crypto.center;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.builtin.digest.Digester;
import org.miaixz.bus.crypto.builtin.digest.DigesterFactory;

/**
 * MD5算法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MD5 extends Digester {

    private static final long serialVersionUID = -1L;

    /**
     * MD5算法不使用BC库，使用JDK默认以提高初始性能
     */
    private static final DigesterFactory FACTORY = DigesterFactory.ofJdk(Algorithm.MD5.getValue());

    /**
     * 构造
     */
    public MD5() {
        super(FACTORY.createMessageDigester());
    }

    /**
     * 构造
     *
     * @param salt 盐值
     */
    public MD5(final byte[] salt) {
        this(salt, 0, 1);
    }

    /**
     * 构造
     *
     * @param salt        盐值
     * @param digestCount 摘要次数，当此值小于等于1,默认为1。
     */
    public MD5(final byte[] salt, final int digestCount) {
        this(salt, 0, digestCount);
    }

    /**
     * 构造
     *
     * @param salt         盐值
     * @param saltPosition 加盐位置，即将盐值字符串放置在数据的index数，默认0
     * @param digestCount  摘要次数，当此值小于等于1,默认为1。
     */
    public MD5(final byte[] salt, final int saltPosition, final int digestCount) {
        this();
        this.salt = salt;
        this.saltPosition = saltPosition;
        this.digestCount = digestCount;
    }

    /**
     * 创建MD5实例
     *
     * @return MD5
     */
    public static MD5 of() {
        return new MD5();
    }

    /**
     * 生成16位MD5摘要
     *
     * @param data    数据
     * @param charset 编码
     * @return 16位MD5摘要
     */
    public String digestHex16(final String data, final Charset charset) {
        return Builder.md5HexTo16(digestHex(data, charset));
    }

    /**
     * 生成16位MD5摘要
     *
     * @param data 数据
     * @return 16位MD5摘要
     */
    public String digestHex16(final String data) {
        return Builder.md5HexTo16(digestHex(data));
    }

    /**
     * 生成16位MD5摘要
     *
     * @param data 数据
     * @return 16位MD5摘要
     */
    public String digestHex16(final InputStream data) {
        return Builder.md5HexTo16(digestHex(data));
    }

    /**
     * 生成16位MD5摘要
     *
     * @param data 数据
     * @return 16位MD5摘要
     */
    public String digestHex16(final File data) {
        return Builder.md5HexTo16(digestHex(data));
    }

    /**
     * 生成16位MD5摘要
     *
     * @param data 数据
     * @return 16位MD5摘要
     */
    public String digestHex16(final byte[] data) {
        return Builder.md5HexTo16(digestHex(data));
    }

}
