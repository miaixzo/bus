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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
