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
package org.miaixz.bus.validate.metric;

import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.PhoneKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.Phone;

/**
 * Validator for the {@link Phone} annotation, checking if a string is a valid landline phone number.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PhoneMatcher implements Matcher<Object, Phone> {

    /**
     * Checks if the given object, when converted to a string, is a valid landline phone number.
     *
     * @param object     The object to validate.
     * @param annotation The {@link Phone} annotation instance (ignored).
     * @param context    The validation context (ignored).
     * @return {@code true} if the object is a valid landline phone number, {@code false} otherwise. Returns
     *         {@code true} if the object is null or empty.
     */
    @Override
    public boolean on(Object object, Phone annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }
        return PhoneKit.isTel(object.toString());
    }

}
