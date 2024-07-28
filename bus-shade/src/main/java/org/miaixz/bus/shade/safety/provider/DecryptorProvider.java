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
package org.miaixz.bus.shade.safety.provider;

import org.miaixz.bus.shade.safety.algorithm.Key;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 解密器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface DecryptorProvider {

    /**
     * 解密,将目标文件解密输出至目标文件
     *
     * @param key  密钥
     * @param src  源文件
     * @param dest 目标文件
     * @throws IOException I/O 异常
     */
    void decrypt(Key key, File src, File dest) throws IOException;

    /**
     * 解密,将输入流解密输出至输出流
     *
     * @param key 密钥
     * @param in  输入流
     * @param out 输出流
     * @throws IOException I/O 异常
     */
    void decrypt(Key key, InputStream in, OutputStream out) throws IOException;

    /**
     * 解密,将输入流包装成解密的输入流
     *
     * @param key 密钥
     * @param in  输入流
     * @return 解密后的输入流
     * @throws IOException I/O 异常
     */
    InputStream decrypt(Key key, InputStream in) throws IOException;

    /**
     * 解密,将输入流包装成解密的输出流
     *
     * @param key 密钥
     * @param out 输出流
     * @return 解密后的输出流
     * @throws IOException I/O 异常
     */
    OutputStream decrypt(Key key, OutputStream out) throws IOException;

}
