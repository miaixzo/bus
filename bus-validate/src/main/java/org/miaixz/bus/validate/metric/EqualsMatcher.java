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
import org.miaixz.bus.validate.magic.annotation.Equals;

import java.util.Objects;

/**
 * Validator for checking if an object is equal to a specified value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EqualsMatcher implements Matcher<Object, Equals> {

    /**
     * Checks if the given object is equal to the value specified in the {@link Equals} annotation.
     *
     * @param object     The object to validate.
     * @param annotation The {@link Equals} annotation instance, providing the value to compare against.
     * @param context    The validation context (ignored).
     * @return {@code true} if the object is empty (null) or if it is equal to the annotation's value, {@code false}
     *         otherwise.
     */
    @Override
    public boolean on(Object object, Equals annotation, Context context) {
        return ObjectKit.isEmpty(object) || Objects.equals(object.toString(), annotation.value());
    }

}
