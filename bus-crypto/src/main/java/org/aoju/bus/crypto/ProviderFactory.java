/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.crypto;

import java.security.Provider;

/**
 * Provider对象生产法工厂类
 *
 * <pre>
 * 调用{@link #createBouncyCastleProvider()} 用于新建一个BouncyCastleProvider对象
 * </pre>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class ProviderFactory implements CryptoFactory {

    /**
     * 创建Bouncy Castle 提供者
     *
     * @return {@link Provider}
     */
    public static Provider createBouncyCastleProvider() {
        return new org.bouncycastle.jce.provider.BouncyCastleProvider();
    }

}
