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

import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.True;

/**
 * Validator for the {@link True} annotation. Checks if a Boolean value is {@code true}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TrueMatcher implements Matcher<Boolean, True> {

    /**
     * Checks if the given Boolean object is {@code true}.
     *
     * @param object     The Boolean object to validate.
     * @param annotation The {@link True} annotation instance, which provides the `nullable` property.
     * @param context    The validation context (ignored).
     * @return {@code true} if the object is `null` and `nullable` is true, or if the object is `Boolean.TRUE`;
     *         {@code false} otherwise.
     */
    @Override
    public boolean on(Boolean object, True annotation, Context context) {
        if (BooleanKit.isFalse(object)) {
            return annotation.nullable();
        }
        return object;
    }

}
