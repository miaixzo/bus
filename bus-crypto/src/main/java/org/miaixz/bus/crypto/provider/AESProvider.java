/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.crypto.provider;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.crypto.Provider;
import org.miaixz.bus.crypto.symmetric.AES;

/**
 * 高级加密标准,是下一代的加密算法标准,速度快,安全级别高；
 * AES是一个使用128为分组块的分组加密算法,分组块和128、192或256位的密钥一起作为输入,
 * 对4×4的字节数组上进行操作 众所周之AES是种十分高效的算法,尤其在8位架构中,这源于它面向字节的设计
 * AES 适用于8位的小型单片机或者普通的32位微处理器,并且适合用专门的硬件实现,硬件实现能够使其吞吐量(每秒可以到达的加密/解密bit数)
 * 达到十亿量级
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AESProvider implements Provider {

    /**
     * 加密
     *
     * @param key     密钥
     * @param content 需要加密的内容
     */
    @Override
    public byte[] encrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        AES aes = new AES(key.getBytes());
        return aes.encrypt(content);
    }


    /**
     * 解密
     *
     * @param key     密钥
     * @param content 需要解密的内容
     */
    @Override
    public byte[] decrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        AES aes = new AES(key.getBytes());
        return aes.decrypt(content);
    }

}
