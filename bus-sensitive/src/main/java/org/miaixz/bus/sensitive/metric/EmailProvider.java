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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.sensitive.Context;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

/**
 * A desensitization provider for email addresses. The strategy masks a portion of the username (the part before the
 * '@') while keeping the domain visible. It aims to keep the first 3 characters of the username visible. For example:
 * {@code "johndoe@example.com"} becomes {@code "joh***@example.com"}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EmailProvider extends AbstractProvider {

    /**
     * Applies email-specific desensitization logic to the provided value.
     *
     * @param object  The object containing the email string to be desensitized.
     * @param context The current desensitization context.
     * @return The desensitized email string, or null if the input is empty.
     */
    @Override
    public Object build(Object object, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return null;
        }
        final Shield shield = context.getShield();
        return email(ObjectKit.isNull(object) ? Normal.EMPTY : object.toString(), shield.shadow());
    }

    /**
     * Masks the given email address.
     *
     * @param email  The email address to mask.
     * @param shadow The character to use for masking.
     * @return The masked email address.
     */
    private static String email(final String email, final String shadow) {
        if (StringKit.isEmpty(email)) {
            return null;
        }

        final int prefixLength = 3;

        final int atIndex = email.indexOf(Symbol.AT);
        String middle = StringKit.fill(4, shadow);

        if (atIndex > 0) {
            int middleLength = atIndex - prefixLength;
            middle = StringKit.repeat(shadow, middleLength);
        }
        return StringKit.build(email, middle, prefixLength);
    }

}
