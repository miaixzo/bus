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
import org.miaixz.bus.validate.magic.annotation.Null;

/**
 * Validator for the {@link Null} annotation. Checks if an object is null.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NullMatcher implements Validator<Object>, Matcher<Object, Null> {

    /**
     * Checks if the given object is null.
     *
     * @param object  The object to validate.
     * @param context The validation context (ignored).
     * @return {@code true} if the object is null, {@code false} otherwise.
     */
    @Override
    public boolean on(Object object, Context context) {
        return ObjectKit.isEmpty(object);
    }

    /**
     * Checks if the given object is null, based on the {@link Null} annotation.
     *
     * @param object     The object to validate.
     * @param annotation The {@link Null} annotation instance (ignored).
     * @param context    The validation context.
     * @return {@code true} if the object is null, {@code false} otherwise.
     */
    @Override
    public boolean on(Object object, Null annotation, Context context) {
        return on(object, context);
    }

}
