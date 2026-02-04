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
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.Regex;

import java.util.regex.Pattern;

/**
 * Validator for the {@link Regex} annotation, checking if a string matches a given regular expression.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexMatcher implements Matcher<Object, Regex> {

    /**
     * Checks if the given object, when converted to a string, matches the regular expression specified in the
     * {@link Regex} annotation.
     *
     * @param object        The object to validate.
     * @param regexValidate The {@link Regex} annotation instance, providing the regular expression pattern and zeroAble
     *                      property.
     * @param context       The validation context (ignored).
     * @return {@code true} if the object matches the regex or if it's a zero-length string and zeroAble is true,
     *         {@code false} otherwise.
     */
    @Override
    public boolean on(Object object, Regex regexValidate, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }
        String value = StringKit.toString(object);
        if (regexValidate.zeroAble() && value.length() == 0) {
            return false;
        }
        Pattern pattern = Pattern.compile(regexValidate.pattern());
        return pattern.matcher(value).matches();
    }

}
