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
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.center.date.Almanac;

/**
 * An abstract class representing a traditional cultural element (like a festival) with an associated index, often used
 * for multi-day events.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Replenish extends Tradition {

    /**
     * The underlying traditional element (e.g., the festival itself).
     */
    protected Tradition tradition;

    /**
     * The index of the day within the event (0-based).
     */
    protected int dayIndex;

    /**
     * Constructs a new {@code Replenish} instance.
     *
     * @param tradition The underlying traditional element.
     * @param dayIndex  The 0-based index of the day within the event.
     */
    public Replenish(Tradition tradition, int dayIndex) {
        this.tradition = tradition;
        this.dayIndex = dayIndex;
    }

    /**
     * Gets the day index within the event.
     *
     * @return The 0-based day index.
     */
    public int getDayIndex() {
        return dayIndex;
    }

    /**
     * Gets the underlying traditional element.
     *
     * @return The {@link Almanac} instance.
     */
    protected Almanac getTradition() {
        return tradition;
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return String.format("%s第%d天", tradition, dayIndex + 1);
    }

    /**
     * Gets the name of the underlying traditional element.
     *
     * @return The name of the tradition.
     */
    @Override
    public String getName() {
        return tradition.getName();
    }

}
