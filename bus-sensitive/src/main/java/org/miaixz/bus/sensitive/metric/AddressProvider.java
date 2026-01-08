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
