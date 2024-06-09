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
package org.miaixz.bus.spring.banner;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.InputStream;

/**
 * 旗标生成器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractBanner {

    /**
     * Resource类
     */
    protected Class<?> resourceClass;
    /**
     * Resource位置
     */
    protected String resourceLocation;
    /**
     * 默认旗标文本
     */
    protected String defaultBanner;
    /**
     * 最终旗标文本
     */
    protected String banner;

    public AbstractBanner(Class<?> resourceClass, String resourceLocation, String defaultBanner) {
        this.resourceClass = resourceClass;
        this.resourceLocation = resourceLocation;
        this.defaultBanner = defaultBanner;
    }

    protected void initialize() {
        InputStream inputStream = null;
        String bannerText = null;
        try {
            if (null != resourceLocation) {
                inputStream = resourceClass.getResourceAsStream(resourceLocation);
                bannerText = IoKit.readUtf8(IoKit.toBuffered(inputStream));
            }
        } catch (Exception e) {

        } finally {
            banner = generateBanner(bannerText);

            if (null != inputStream) {
                IoKit.close(inputStream);
            }
        }
    }

    public String getBanner() {
        return banner;
    }

    /**
     * 显示成非ansi模式
     *
     * @return the strings
     */
    public String getPlainBanner() {
        if (null != banner) {
            banner = banner.replaceAll("\u001b\\[[;\\d]*m", Normal.EMPTY);
        }

        return banner;
    }

    protected abstract String generateBanner(String bannerText);

}
