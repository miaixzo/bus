/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.rabjung;

import org.miaixz.bus.core.center.date.culture.cn.Element;

/**
 * 藏历五行
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabjungElement extends Element {

    public RabjungElement(int index) {
        super(index);
    }

    public RabjungElement(String name) {
        super(name.replace("铁", "金"));
    }

    public static RabjungElement fromIndex(int index) {
        return new RabjungElement(index);
    }

    public static RabjungElement fromName(String name) {
        return new RabjungElement(name);
    }

    public RabjungElement next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 我生者
     *
     * @return 五行
     */
    public RabjungElement getReinforce() {
        return next(1);
    }

    /**
     * 我克者
     *
     * @return 五行
     */
    public RabjungElement getRestrain() {
        return next(2);
    }

    /**
     * 生我者
     *
     * @return 五行
     */
    public RabjungElement getReinforced() {
        return next(-1);
    }

    /**
     * 克我者
     *
     * @return 五行
     */
    public RabjungElement getRestrained() {
        return next(-2);
    }

    @Override
    public String getName() {
        return super.getName().replace("金", "铁");
    }

}
