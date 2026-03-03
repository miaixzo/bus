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
package org.miaixz.bus.extra.captcha.strategy;

import java.io.Serial;

import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Random character CAPTCHA generation strategy. Generates a random CAPTCHA string from a given base character set and
 * length.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RandomStrategy extends AbstractStrategy {

    @Serial
    private static final long serialVersionUID = 2852292312392L;

    /**
     * Constructs a new {@code RandomStrategy} using letters and numbers as the base character set.
     *
     * @param count The length of the CAPTCHA code to generate.
     */
    public RandomStrategy(final int count) {
        super(count);
    }

    /**
     * Constructs a new {@code RandomStrategy} with a custom base character set and length.
     *
     * @param baseStr The base character set from which to randomly select characters.
     * @param length  The length of the CAPTCHA code to generate.
     */
    public RandomStrategy(final String baseStr, final int length) {
        super(baseStr, length);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Generates a random CAPTCHA string by selecting characters randomly from the configured base character set.
     * </p>
     *
     * @return a randomly generated CAPTCHA string of the configured length
     */
    @Override
    public String generate() {
        return RandomKit.randomString(this.baseStr, this.length);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Verifies the user's input by performing a case-insensitive comparison with the generated CAPTCHA code.
     * </p>
     *
     * @param code          the generated CAPTCHA code
     * @param userInputCode the user's input to verify
     * @return {@code true} if the user input matches the code (case-insensitive), {@code false} otherwise
     */
    @Override
    public boolean verify(final String code, final String userInputCode) {
        if (StringKit.isNotBlank(userInputCode)) {
            return StringKit.equalsIgnoreCase(code, userInputCode);
        }
        return false;
    }

}
