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
