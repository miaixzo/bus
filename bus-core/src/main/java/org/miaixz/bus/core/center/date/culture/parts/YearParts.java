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
package org.miaixz.bus.core.center.date.culture.parts;

import org.miaixz.bus.core.center.date.culture.Loops;

/**
 * Abstract base class for date components containing year information.
 *
 * <p>
 * This class extends {@link org.miaixz.bus.core.center.date.culture.Loops} and adds the year field, serving as the
 * fundamental building block for cultural calendar implementations. All other part classes extend this class directly
 * or indirectly, creating a hierarchy of date components.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class YearParts extends Loops {

    /**
     * The year value in the calendar system.
     */
    protected int year;

    /**
     * Gets the year.
     *
     * @return the year value in the calendar system
     */
    public int getYear() {
        return year;
    }

}
