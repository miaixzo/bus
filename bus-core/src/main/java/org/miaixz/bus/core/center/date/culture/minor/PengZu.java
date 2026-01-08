/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.minor;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;

/**
 * Represents the Peng Zu Bai Ji (彭祖百忌), a traditional Chinese calendar concept of taboos for certain days. This class
 * extends {@link Tradition} to provide information about daily taboos.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PengZu extends Tradition {

    /**
     * Peng Zu Bai Ji associated with Heavenly Stems.
     */
    protected PengZuHeavenStem pengZuHeavenStem;

    /**
     * Peng Zu Bai Ji associated with Earthly Branches.
     */
    protected PengZuEarthBranch pengZuEarthBranch;

    /**
     * Constructs a {@code PengZu} instance based on a {@link SixtyCycle}.
     *
     * @param sixtyCycle The SixtyCycle (GanZhi) of the day.
     */
    public PengZu(SixtyCycle sixtyCycle) {
        pengZuHeavenStem = PengZuHeavenStem.fromIndex(sixtyCycle.getHeavenStem().getIndex());
        pengZuEarthBranch = PengZuEarthBranch.fromIndex(sixtyCycle.getEarthBranch().getIndex());
    }

    /**
     * Creates a {@code PengZu} instance from a {@link SixtyCycle}.
     *
     * @param sixtyCycle The SixtyCycle (GanZhi).
     * @return A new {@code PengZu} instance.
     */
    public static PengZu fromSixtyCycle(SixtyCycle sixtyCycle) {
        return new PengZu(sixtyCycle);
    }

    /**
     * Gets the combined name of the Peng Zu Bai Ji from Heavenly Stem and Earthly Branch.
     *
     * @return The combined name as a formatted string.
     */
    public String getName() {
        return String.format("%s %s", pengZuHeavenStem, pengZuEarthBranch);
    }

    /**
     * Gets the Peng Zu Bai Ji associated with Heavenly Stems.
     *
     * @return The {@link PengZuHeavenStem} instance.
     */
    public PengZuHeavenStem getPengZuHeavenStem() {
        return pengZuHeavenStem;
    }

    /**
     * Gets the Peng Zu Bai Ji associated with Earthly Branches.
     *
     * @return The {@link PengZuEarthBranch} instance.
     */
    public PengZuEarthBranch getPengZuEarthBranch() {
        return pengZuEarthBranch;
    }

}
