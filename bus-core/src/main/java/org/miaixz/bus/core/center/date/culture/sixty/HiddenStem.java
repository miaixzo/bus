/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.sixty;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.HiddenStems;

/**
 * Represents a Hidden Stem (藏干), also known as Ren Yuan (人元), which are Heavenly Stems hidden within Earthly Branches.
 * This class associates a {@link HeavenStem} with a {@link HiddenStems} type.
 *
 * @author Kimi Liu
 * @since Java 21+
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
