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
package org.miaixz.bus.core.center.date.culture.cn.minor;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;

/**
 * 彭祖百忌
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PengZu extends Tradition {

    /**
     * 天干彭祖百忌
     */
    protected PengZuHeavenStem pengZuHeavenStem;

    /**
     * 地支彭祖百忌
     */
    protected PengZuEarthBranch pengZuEarthBranch;

    public PengZu(SixtyCycle sixtyCycle) {
        pengZuHeavenStem = PengZuHeavenStem.fromIndex(sixtyCycle.getHeavenStem().getIndex());
        pengZuEarthBranch = PengZuEarthBranch.fromIndex(sixtyCycle.getEarthBranch().getIndex());
    }

    /**
     * 从干支初始化
     *
     * @param sixtyCycle 干支
     * @return 彭祖百忌
     */
    public static PengZu fromSixtyCycle(SixtyCycle sixtyCycle) {
        return new PengZu(sixtyCycle);
    }

    public String getName() {
        return String.format("%s %s", pengZuHeavenStem, pengZuEarthBranch);
    }

    /**
     * 天干彭祖百忌
     *
     * @return 天干彭祖百忌
     */
    public PengZuHeavenStem getPengZuHeavenStem() {
        return pengZuHeavenStem;
    }

    /**
     * 地支彭祖百忌
     *
     * @return 地支彭祖百忌
     */
    public PengZuEarthBranch getPengZuEarthBranch() {
        return pengZuEarthBranch;
    }

}
