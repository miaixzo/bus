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
package org.miaixz.bus.http.metric.anget;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ListKit;

import java.util.List;

/**
 * 浏览器解析引擎
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Engine extends UserAgent {

    /**
     * 未知
     */
    public static final Engine UNKNOWN = new Engine(Normal.UNKNOWN, null);

    /**
     * 支持的引擎类型
     */
    public static final List<Engine> ENGINES = ListKit.of(
            new Engine("Trident", "trident"),
            new Engine("Webkit", "webkit"),
            new Engine("Chrome", "chrome"),
            new Engine("Opera", "opera"),
            new Engine("Presto", "presto"),
            new Engine("Gecko", "gecko"),
            new Engine("KHTML", "khtml"),
            new Engine("Konqeror", "konqueror"),
            new Engine("MIDP", "MIDP")
    );

    /**
     * 构造
     *
     * @param name  引擎名称
     * @param regex 关键字或表达式
     */
    public Engine(String name, String regex) {
        super(name, regex);
    }

}
