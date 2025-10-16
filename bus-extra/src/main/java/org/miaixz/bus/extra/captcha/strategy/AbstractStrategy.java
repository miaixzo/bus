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
