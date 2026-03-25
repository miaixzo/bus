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
package org.miaixz.bus.validate.metric;

import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.NotEmpty;

/**
 * Validator for the {@link NotEmpty} annotation. Checks if an object is not empty. An object is considered empty if it
 * is null, an empty string, an empty collection, an empty map, or an empty array.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NotEmptyMatcher implements Matcher<Object, NotEmpty> {

    /**
     * Checks if the given object is not empty.
     *
     * @param object     The object to validate.
     * @param annotation The {@link NotEmpty} annotation instance (ignored).
     * @param context    The validation context (ignored).
     * @return {@code true} if the object is not empty, {@code false} otherwise.
     */
    @Override
    public boolean on(Object object, NotEmpty annotation, Context context) {
        return ObjectKit.isNotEmpty(object);
    }

}
