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
import org.miaixz.bus.validate.magic.annotation.Size;

import java.util.Collection;
import java.util.Map;

/**
 * Validator for the {@link Size} annotation, which checks the size of various data types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SizeMatcher implements Matcher<Object, Size> {

    /**
     * Checks if the size of the given object is within the range specified by the {@link Size} annotation.
     *
     * @param object     The object to validate. Supported types are {@link String}, arrays ({@code Object[]}),
     *                   {@link Collection}, and {@link Map}.
     * @param annotation The {@link Size} annotation instance, providing the min, max, and zeroAble properties.
     * @param context    The validation context (ignored).
     * @return {@code true} if the object's size is within the specified range, or if its size is 0 and zeroAble is
     *         true; {@code false} otherwise. Returns {@code false} if the object is empty (null, empty string, empty
     *         collection, empty array, empty map) unless zeroAble is true and the size is 0.
     * @throws IllegalArgumentException if the object type is not supported for size checking.
     */
    @Override
    public boolean on(Object object, Size annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }

        int num;

        if (object instanceof String) {
            num = ((String) object).length();
        } else if (object.getClass().isArray()) {
            num = ((Object[]) object).length;
        } else if (object instanceof Collection) {
            num = ((Collection) object).size();
        } else if (object instanceof Map) {
            num = ((Map) object).keySet().size();
        } else {
            throw new IllegalArgumentException("Unsupported object type for size check: " + object.getClass());
        }
        return (annotation.zeroAble() && num == 0) || (num >= annotation.min() && num <= annotation.max());
    }

}
