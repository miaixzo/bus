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
package org.miaixz.bus.sensitive.metric;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.emoji.EmojiKit;
import org.miaixz.bus.sensitive.Context;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

/**
 * A desensitization provider for Chinese names. The rules are as follows:
 * <p>
 * 1. If the name has 1 character, it is returned as is. 2. If the name has 2 characters, the first character (surname)
 * is masked. 3. If the name has 3 or more characters, only the first and last characters are kept visible.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NameProvider extends AbstractProvider {

    /**
     * Masks the given Chinese name according to the defined rules.
     *
     * @param value  The Chinese name to desensitize.
     * @param shadow The character to use for masking.
     * @return The desensitized result.
     */
    private static String name(final String value, final String shadow) {
        if (StringKit.isEmpty(value)) {
            return value;
        }
        // Do not process strings containing emoji.
        if (CollKit.isNotEmpty(EmojiKit.extractEmojis(value))) {
            return value;
        }
        final int nameLength = value.length();
        if (1 == nameLength) {
            return value;
        }

        if (2 == nameLength) {
            return shadow + value.charAt(1);
        }

        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(value.charAt(0));
        for (int i = 0; i < nameLength - 2; i++) {
            stringBuffer.append(shadow);
        }
        stringBuffer.append(value.charAt(nameLength - 1));
        return stringBuffer.toString();
    }

    /**
     * Applies Chinese name-specific desensitization logic to the provided value.
     *
     * @param object  The object containing the name string to be desensitized.
     * @param context The current desensitization context.
     * @return The desensitized name, or null if the input is empty.
     */
    @Override
    public Object build(Object object, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return null;
        }
        final Shield shield = context.getShield();
        return name(ObjectKit.isNull(object) ? Normal.EMPTY : object.toString(), shield.shadow());
    }

}
