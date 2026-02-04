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
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.time.Period;
import java.time.temporal.TemporalAmount;

/**
 * Converter for {@link Period} objects.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PeriodConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852270870935L;

    /**
     * Converts the given value to a Period.
     * <p>
     * Supports conversion from TemporalAmount, Integer (days), and string representations.
     * </p>
     *
     * @param targetClass the target class (should be Period.class)
     * @param value       the value to convert
     * @return the converted Period object
     */
    @Override
    protected Period convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof TemporalAmount) {
            return Period.from((TemporalAmount) value);
        } else if (value instanceof Integer) {
            return Period.ofDays((Integer) value);
        } else {
            return Period.parse(convertToString(value));
        }
    }

}
