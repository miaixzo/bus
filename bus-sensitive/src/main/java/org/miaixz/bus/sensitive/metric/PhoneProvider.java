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
 * A desensitization provider for phone numbers (landlines or mobile). The strategy keeps the first 3 and last 4 digits
 * visible and masks the middle digits. For example: {@code "18012341120"} becomes {@code "180****1120"}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PhoneProvider extends AbstractProvider {

    /**
     * Masks the given phone number, keeping the first 3 and last 4 digits visible.
     *
     * @param phone  The phone number to mask.
     * @param shadow The character to use for masking.
     * @return The masked phone number.
     */
    private static String phone(final String phone, final String shadow) {
        final int prefixLength = 3;
        final String middle = StringKit.fill(4, shadow);
        return StringKit.build(phone, middle, prefixLength);
    }

    /**
     * Applies phone number-specific desensitization logic to the provided value.
     *
     * @param object  The object containing the phone number string to be desensitized.
     * @param context The current desensitization context.
     * @return The desensitized phone number, or null if the input is empty.
     */
    @Override
    public Object build(Object object, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return null;
        }
        final Shield shield = context.getShield();
        return phone(ObjectKit.isNull(object) ? Normal.EMPTY : object.toString(), shield.shadow());
    }

}
