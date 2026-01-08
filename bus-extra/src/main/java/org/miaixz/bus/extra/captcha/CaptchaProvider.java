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
package org.miaixz.bus.extra.captcha;

import java.io.OutputStream;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumValue;

/**
 * CAPTCHA interface, defining the contract for CAPTCHA objects. Implementations of this interface are responsible for
 * generating CAPTCHA images and their corresponding text.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CaptchaProvider extends Provider {

    /**
     * Creates a CAPTCHA. Implementations should generate both a random CAPTCHA string and a CAPTCHA image.
     */
    void of();

    /**
     * Retrieves the text content of the generated CAPTCHA.
     *
     * @return The text content of the CAPTCHA.
     */
    String get();

    /**
     * Verifies if the user-provided CAPTCHA input matches the generated CAPTCHA text. It is recommended to perform a
     * case-insensitive comparison.
     *
     * @param userInputCode The CAPTCHA code entered by the user.
     * @return {@code true} if the user input matches the generated CAPTCHA, {@code false} otherwise.
     */
    boolean verify(String userInputCode);

    /**
     * Writes the CAPTCHA image to the target output stream.
     *
     * @param out The target output stream to which the CAPTCHA image will be written.
     */
    void write(OutputStream out);

    @Override
    default Object type() {
        return EnumValue.Povider.CAPTCHA;
    }

}
