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
package org.miaixz.bus.crypto;

import org.miaixz.bus.core.xyz.SPIKit;
import org.miaixz.bus.crypto.metric.BouncyCastleProvider;

/**
 * 全局单例的{@link java.security.Provider}对象
 * 在此类加载时，通过SPI方式查找用户引入的加密库，查找对应的{@link java.security.Provider}实现，然后全局创建唯一的{@link BouncyCastleProvider}对象
 * 用户依旧可以通过{@link #setUseCustomProvider(boolean)} 方法选择是否使用自定义的Provider。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Holder {

    private static final java.security.Provider provider = _createProvider();
    private static boolean useCustomProvider = true;

    /**
     * 获取{@link java.security.Provider}，无提供方，返回{@code null}表示使用JDK默认
     *
     * @return {@link java.security.Provider} or {@code null}
     */
    public static java.security.Provider getProvider() {
        return useCustomProvider ? provider : null;
    }

    /**
     * 设置是否使用自定义的{@link java.security.Provider}
     * 如果设置为false，表示使用JDK默认的Provider
     *
     * @param isUseCustomProvider 是否使用自定义{@link java.security.Provider}
     */
    public static void setUseCustomProvider(final boolean isUseCustomProvider) {
        useCustomProvider = isUseCustomProvider;
    }

    /**
     * 通过SPI方式，创建{@link java.security.Provider}，无提供的返回{@code null}
     *
     * @return {@link java.security.Provider} or {@code null}
     */
    private static java.security.Provider _createProvider() {
        final BouncyCastleProvider factory = SPIKit.loadFirstAvailable(BouncyCastleProvider.class);
        if (null == factory) {
            // 默认JCE
            return null;
        }

        final java.security.Provider provider = factory.create();
        Builder.addProvider(provider);
        return provider;
    }

}
