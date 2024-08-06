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
package org.miaixz.bus.crypto.builtin.digest;

import java.security.MessageDigest;
import java.security.Provider;

import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Holder;

/**
 * {@link Digester}创建简单工厂，用于生产{@link Digester}对象
 * 参考Guava方式，工厂负责持有一个原始的{@link MessageDigest}对象，使用时优先通过clone方式创建对象，提高初始化性能。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DigesterFactory {

    private final MessageDigest prototype;
    private final boolean cloneSupport;

    /**
     * 构造
     *
     * @param messageDigest {@link MessageDigest}模板
     */
    private DigesterFactory(final MessageDigest messageDigest) {
        this.prototype = messageDigest;
        this.cloneSupport = checkCloneSupport(messageDigest);
    }

    /**
     * 创建工厂，只使用JDK提供的算法
     *
     * @param algorithm 算法
     * @return DigesterFactory
     */
    public static DigesterFactory ofJdk(final String algorithm) {
        return of(Builder.createJdkMessageDigest(algorithm));
    }

    /**
     * 创建工厂，使用{@link Holder}找到的提供方。
     *
     * @param algorithm 算法
     * @return DigesterFactory
     */
    public static DigesterFactory of(final String algorithm) {
        return of(Builder.createMessageDigest(algorithm, null));
    }

    /**
     * 创建工厂
     *
     * @param messageDigest {@link MessageDigest}，可以通过{@link Builder#createMessageDigest(String, Provider)} 创建
     * @return DigesterFactory
     */
    public static DigesterFactory of(final MessageDigest messageDigest) {
        return new DigesterFactory(messageDigest);
    }

    /**
     * 检查{@link MessageDigest}对象是否支持clone方法
     *
     * @param messageDigest {@link MessageDigest}
     * @return 是否支持clone方法
     */
    private static boolean checkCloneSupport(final MessageDigest messageDigest) {
        try {
            messageDigest.clone();
            return true;
        } catch (final CloneNotSupportedException e) {
            return false;
        }
    }

    /**
     * 创建{@link Digester}
     *
     * @return {@link Digester}
     */
    public Digester createDigester() {
        return new Digester(createMessageDigester());
    }

    /**
     * 创建{@link MessageDigest}
     *
     * @return {@link MessageDigest}
     */
    public MessageDigest createMessageDigester() {
        if (cloneSupport) {
            try {
                return (MessageDigest) prototype.clone();
            } catch (final CloneNotSupportedException ignore) {
                // ignore
            }
        }
        return Builder.createJdkMessageDigest(prototype.getAlgorithm());
    }

}
