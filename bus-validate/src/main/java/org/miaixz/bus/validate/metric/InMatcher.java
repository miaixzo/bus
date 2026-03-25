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

import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.In;

/**
 * Validator for the {@link In} annotation, checking if a value is present in a specified array of strings.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class InMatcher implements Matcher<Object, In> {

    /**
     * Checks if the given object is present in the array of strings specified in the {@link In} annotation.
     *
     * @param object     The object to validate.
     * @param annotation The {@link In} annotation instance, which provides the array of allowed values.
     * @param context    The validation context (ignored).
     * @return {@code true} if the object is empty (null) or if its string representation is found in the annotation's
     *         value array, {@code false} otherwise.
     */
    @Override
    public boolean on(Object object, In annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }
        return ArrayKit.contains(annotation.value(), object.toString());
    }

}
