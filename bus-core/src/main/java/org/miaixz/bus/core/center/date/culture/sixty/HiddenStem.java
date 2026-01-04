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
package org.miaixz.bus.core.center.date.culture.sixty;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.HiddenStems;

/**
 * Represents a Hidden Stem (藏干), also known as Ren Yuan (人元), which are Heavenly Stems hidden within Earthly Branches.
 * This class associates a {@link HeavenStem} with a {@link HiddenStems} type.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HiddenStem extends Tradition {

    /**
     * The Heavenly Stem itself.
     */
    protected HeavenStem heavenStem;

    /**
     * The type of Hidden Stem (e.g., Residual Qi, Middle Qi, Principal Qi).
     */
    protected HiddenStems type;

    /**
     * Constructs a {@code HiddenStem} instance with the specified Heavenly Stem and Hidden Stem type.
     *
     * @param heavenStem The {@link HeavenStem} instance.
     * @param type       The {@link HiddenStems} type.
     */
    public HiddenStem(HeavenStem heavenStem, HiddenStems type) {
        this.heavenStem = heavenStem;
        this.type = type;
    }

    /**
     * Constructs a {@code HiddenStem} instance with the specified Heavenly Stem name and Hidden Stem type.
     *
     * @param heavenStemName The name of the Heavenly Stem.
     * @param type           The {@link HiddenStems} type.
     */
    public HiddenStem(String heavenStemName, HiddenStems type) {
        this(HeavenStem.fromName(heavenStemName), type);
    }

    /**
     * Constructs a {@code HiddenStem} instance with the specified Heavenly Stem index and Hidden Stem type.
     *
     * @param heavenStemIndex The index of the Heavenly Stem.
     * @param type            The {@link HiddenStems} type.
     */
    public HiddenStem(int heavenStemIndex, HiddenStems type) {
        this(HeavenStem.fromIndex(heavenStemIndex), type);
    }

    /**
     * Gets the Heavenly Stem associated with this Hidden Stem.
     *
     * @return The {@link HeavenStem} instance.
     */
    public HeavenStem getHeavenStem() {
        return heavenStem;
    }

    /**
     * Gets the type of this Hidden Stem.
     *
     * @return The {@link HiddenStems} type.
     */
    public HiddenStems getType() {
        return type;
    }

    /**
     * Gets the name of the Heavenly Stem.
     *
     * @return The name of the Heavenly Stem.
     */
    @Override
    public String getName() {
        return heavenStem.getName();
    }

}
