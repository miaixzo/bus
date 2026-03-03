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

import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.emoji.EmojiKit;
import org.miaixz.bus.sensitive.Context;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

/**
 * A desensitization provider for addresses. This strategy masks the detailed part of an address to protect privacy. For
 * example: "Beijing Haidian District Xizhimen Street No. 1" might become "Beijing Haidian District**********".
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AddressProvider extends AbstractProvider {

    /**
     * Applies address-specific desensitization logic to the provided value.
     *
     * @param object  The object containing the address string to be desensitized.
     * @param context The current desensitization context, providing access to field annotations and other details.
     * @return The desensitized address string, or null if the input is empty.
     */
    @Override
    public String build(Object object, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return null;
        }
        String value = object.toString();
        // Do not process strings containing emoji.
        if (CollKit.isNotEmpty(EmojiKit.extractEmojis(value))) {
            return value;
        }
        final int RIGHT = 10;
        final int LEFT = 6;

        final Shield shield = context.getShield();
        int length = StringKit.length(value);
        if (length > RIGHT + LEFT) {
            return StringKit.padPre(StringKit.left(value, length - RIGHT), length, shield.shadow());
        }
        if (length <= LEFT) {
            return value;
        } else {
            return value.substring(0, LEFT + 1).concat(StringKit.fill(5, shield.shadow()));
        }
    }

}
