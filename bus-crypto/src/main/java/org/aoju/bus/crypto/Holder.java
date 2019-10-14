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
package org.aoju.bus.crypto;

import java.security.Provider;

/**
 * Provider对象生产法工厂类
 *
 * <pre>
 * 1. 调用{@link #createBouncyCastleProvider()}
 * 用于新建一个org.bouncycastle.jce.provider.BouncyCastleProvider对象
 * </pre>
 *
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
 */
public class Holder {

    private static boolean useBouncyCastle = true;

    /**
     * 创建Bouncy Castle 提供者
     * 如果用户未引入bouncycastle库，则此方法抛出{@link NoClassDefFoundError} 异常
     *
     * @return {@link Provider}
     */
    public static Provider createBouncyCastleProvider() {
        return new org.bouncycastle.jce.provider.BouncyCastleProvider();
    }

    /**
     * 设置是否使用Bouncy Castle库
     * 如果设置为false，表示强制关闭Bouncy Castle而使用JDK
     *
     * @param isUseBouncyCastle 是否自定义
     */
    public static void setUseBouncyCastle(boolean isUseBouncyCastle) {
        useBouncyCastle = isUseBouncyCastle;
    }

    /**
     * 获取{@link Provider}
     *
     * @return {@link Provider}
     */
    public Provider getProvider() {
        return useBouncyCastle ? createBouncyCastleProvider() : null;
    }

}
