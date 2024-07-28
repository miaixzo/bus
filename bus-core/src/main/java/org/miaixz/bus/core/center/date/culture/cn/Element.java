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
package org.miaixz.bus.core.center.date.culture.cn;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * 五行
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Element extends Samsara {

    public static final String[] NAMES = { "木", "火", "土", "金", "水" };

    public Element(int index) {
        super(NAMES, index);
    }

    public Element(String name) {
        super(NAMES, name);
    }

    public static Element fromIndex(int index) {
        return new Element(index);
    }

    public static Element fromName(String name) {
        return new Element(name);
    }

    public Element next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 我生者（生）
     *
     * @return 五行
     */
    public Element getReinforce() {
        return next(1);
    }

    /**
     * 我克者（克）
     *
     * @return 五行
     */
    public Element getRestrain() {
        return next(2);
    }

    /**
     * 生我者（泄）
     *
     * @return 五行
     */
    public Element getReinforced() {
        return next(-1);
    }

    /**
     * 克我者（耗）
     *
     * @return 五行
     */
    public Element getRestrained() {
        return next(-2);
    }

}
