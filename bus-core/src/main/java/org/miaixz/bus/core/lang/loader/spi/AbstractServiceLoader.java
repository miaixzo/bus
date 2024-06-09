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
package org.miaixz.bus.core.lang.loader.spi;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

import java.nio.charset.Charset;

/**
 * 抽象服务加载器，提供包括路径前缀、服务类、类加载器、编码、安全相关持有
 *
 * @param <S> 服务类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractServiceLoader<S> implements ServiceLoader<S> {

    /**
     * 路径前缀
     */
    protected final String pathPrefix;
    /**
     * 服务类
     */
    protected final Class<S> serviceClass;
    /**
     * 类加载器
     */
    protected final ClassLoader classLoader;
    /**
     * 字符集
     */
    protected final Charset charset;

    /**
     * 构造
     *
     * @param pathPrefix   路径前缀
     * @param serviceClass 服务名称
     * @param classLoader  自定义类加载器, {@code null}表示使用默认当前的类加载器
     * @param charset      编码，默认UTF-8
     */
    public AbstractServiceLoader(final String pathPrefix, final Class<S> serviceClass,
                                 final ClassLoader classLoader, final Charset charset) {
        this.pathPrefix = StringKit.addSuffixIfNot(pathPrefix, Symbol.SLASH);
        this.serviceClass = serviceClass;
        this.classLoader = classLoader;
        this.charset = charset;
    }

}
