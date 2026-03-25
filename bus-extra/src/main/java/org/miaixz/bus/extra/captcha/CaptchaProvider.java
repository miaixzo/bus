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
package org.miaixz.bus.extra.captcha;

import java.io.OutputStream;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumValue;

/**
 * CAPTCHA interface, defining the contract for CAPTCHA objects. Implementations of this interface are responsible for
 * generating CAPTCHA images and their corresponding text.
 *
 * @author Kimi Liu
 * @since Java 21+
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
