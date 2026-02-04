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

import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.Date;

/**
 * Validator for checking if a string represents a valid date according to a specified format.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DateMatcher implements Matcher<Object, Date> {

    /**
     * Checks if the given object is a valid date according to the format specified in the annotation.
     *
     * @param object     The object to validate. It can be a {@code java.util.Date} or a {@code String} representing a
     *                   date.
     * @param annotation The {@link Date} annotation instance, which provides the date format.
     * @param context    The validation context (ignored).
     * @return {@code true} if the object is a valid date, {@code false} otherwise. Returns {@code true} if the object
     *         is null or a blank string, allowing other annotations like {@code @NotNull} or {@code @NotBlank} to
     *         handle such cases.
     */
    @Override
    public boolean on(Object object, Date annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }

        String value = StringKit.toString(object);
        if (StringKit.isBlank(value)) {
            return true;
        }
        DateKit.parse(value);
        return true;
    }

}
