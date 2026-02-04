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
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.Validator;
import org.miaixz.bus.validate.magic.annotation.Blank;

/**
 * Validator for checking if a value is blank. A value is considered blank if it is null, or if it is a string that is
 * empty or contains only whitespace.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BlankMatcher implements Validator<Object>, Matcher<Object, Blank> {

    /**
     * Checks if the given object is blank.
     *
     * @param object  The object to validate.
     * @param context The validation context (ignored).
     * @return {@code true} if the object is blank, {@code false} otherwise.
     */
    @Override
    public boolean on(Object object, Context context) {
        return ObjectKit.isBlankIfString(object);
    }

    /**
     * Checks if the given object is blank, based on the {@link Blank} annotation.
     *
     * @param object     The object to validate.
     * @param annotation The {@link Blank} annotation (ignored).
     * @param context    The validation context.
     * @return {@code true} if the object is blank, {@code false} otherwise.
     */
    @Override
    public boolean on(Object object, Blank annotation, Context context) {
        return on(object, context);
    }

}
