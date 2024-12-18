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
package org.miaixz.bus.core.center.date.culture.cn.sixty;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.cn.HiddenStems;

/**
 * 藏干（即人元，司令取天干，分野取天干的五行）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HiddenStem extends Tradition {

    /**
     * 天干
     */
    protected HeavenStem heavenStem;

    /**
     * 藏干类型
     */
    protected HiddenStems type;

    public HiddenStem(HeavenStem heavenStem, HiddenStems type) {
        this.heavenStem = heavenStem;
        this.type = type;
    }

    public HiddenStem(String heavenStemName, HiddenStems type) {
        this(HeavenStem.fromName(heavenStemName), type);
    }

    public HiddenStem(int heavenStemIndex, HiddenStems type) {
        this(HeavenStem.fromIndex(heavenStemIndex), type);
    }

    /**
     * 天干
     *
     * @return 天干
     */
    public HeavenStem getHeavenStem() {
        return heavenStem;
    }

    /**
     * 藏干类型
     *
     * @return 藏干类型
     */
    public HiddenStems getType() {
        return type;
    }

    @Override
    public String getName() {
        return heavenStem.getName();
    }

}
