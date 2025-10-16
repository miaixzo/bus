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
package org.miaixz.bus.sensitive.metric;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.sensitive.Context;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

/**
 * A desensitization provider for Chinese citizen ID numbers. The strategy keeps the first 6 and last 2 digits visible
 * and masks the characters in between. For example: {@code "110101199001011234"} becomes {@code "110101**********34"}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CitizenIdProvider extends AbstractProvider {

    /**
     * Masks the given ID card number.
     *
     * @param cardId The ID card number to mask.
     * @param shadow The character to use for masking.
     * @return The masked ID card number.
     */
    private static String cardId(final String cardId, final String shadow) {
        final int prefixLength = 6;
        // The middle part of a Chinese ID card (18 digits) is 10 digits long (from index 6 to 15).
        final String middle = StringKit.fill(10, shadow);
        return StringKit.build(cardId, middle, prefixLength);
    }

    /**
     * Applies citizen ID-specific desensitization logic to the provided value.
     *
     * @param object  The object containing the ID number string to be desensitized.
     * @param context The current desensitization context, providing access to field annotations and other details.
     * @return The desensitized ID number, or null if the input is empty.
     */
    @Override
    public Object build(Object object, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return null;
        }
        final Shield shield = context.getShield();
        return cardId(ObjectKit.isNull(object) ? Normal.EMPTY : object.toString(), shield.shadow());
    }

}
