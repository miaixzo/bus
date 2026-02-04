/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.captcha.strategy;

import java.io.Serial;

import org.miaixz.bus.core.lang.Normal;

/**
 * Abstract CAPTCHA code generation strategy. Generates a random CAPTCHA string from a given base character set and
 * length.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractStrategy implements CodeStrategy {

    @Serial
    private static final long serialVersionUID = 2852292099713L;

    /**
     * The base character set from which to randomly select characters for the CAPTCHA string.
     */
    protected final String baseStr;
    /**
     * The length of the CAPTCHA code.
     */
    protected final int length;

    /**
     * Constructor, using letters and numbers as the base set.
     *
     * @param count The length of the CAPTCHA code to generate.
     */
    public AbstractStrategy(final int count) {
        this(Normal.LOWER_ALPHABET_NUMBER, count);
    }

    /**
     * Constructor.
     *
     * @param baseStr The base character set from which to randomly select characters.
     * @param length  The length of the CAPTCHA code to generate.
     */
    public AbstractStrategy(final String baseStr, final int length) {
        this.baseStr = baseStr;
        this.length = length;
    }

    /**
     * Gets the length of the CAPTCHA code.
     *
     * @return The length of the CAPTCHA code.
     */
    public int getLength() {
        return this.length;
    }

}
