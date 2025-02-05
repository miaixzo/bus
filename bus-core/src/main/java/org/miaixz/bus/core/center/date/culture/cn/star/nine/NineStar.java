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
package org.miaixz.bus.core.center.date.culture.cn.star.nine;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.Direction;
import org.miaixz.bus.core.center.date.culture.cn.Element;
import org.miaixz.bus.core.lang.Normal;

/**
 * 九星
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NineStar extends Samsara {

    public static final String[] NAMES = { "一", "二", "三", "四", "五", "六", "七", "八", "九" };

    public NineStar(int index) {
        super(NAMES, index);
    }

    public NineStar(String name) {
        super(NAMES, name);
    }

    public static NineStar fromIndex(int index) {
        return new NineStar(index);
    }

    public static NineStar fromName(String name) {
        return new NineStar(name);
    }

    public NineStar next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 颜色
     *
     * @return 颜色
     */
    public String getColor() {
        return Normal.COLOR[index];
    }

    /**
     * 五行
     *
     * @return 五行
     */
    public Element getElement() {
        return Element.fromIndex(new int[] { 4, 2, 0, 0, 2, 3, 3, 2, 1 }[index]);
    }

    /**
     * 北斗九星
     *
     * @return 北斗九星
     */
    public Dipper getDipper() {
        return Dipper.fromIndex(index);
    }

    /**
     * 方位
     *
     * @return 方位
     */
    public Direction getDirection() {
        return Direction.fromIndex(index);
    }

    @Override
    public String toString() {
        return getName() + getColor() + getElement();
    }

}
