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
package org.miaixz.bus.core.center.date.culture.festival;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.parts.DayParts;

/**
 * Abstract base class for festival instances.
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractFestival extends Loops {

    /**
     * Festival index within the registry.
     */
    protected int index;

    /**
     * Day associated with this festival instance.
     */
    protected DayParts day;

    /**
     * Festival definition.
     */
    protected Festival event;

    /**
     * Constructs a festival instance bound to a specific day.
     *
     * @param index festival index within the registry
     * @param event festival definition
     * @param day   matched day
     */
    public AbstractFestival(int index, Festival event, DayParts day) {
        this.index = index;
        this.event = event;
        this.day = day;
    }

    /**
     * Gets the festival index.
     *
     * @return festival index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the associated day.
     *
     * @return associated day
     */
    public DayParts getDay() {
        return day;
    }

    /**
     * Gets the display name from the underlying festival definition.
     *
     * @return festival name
     */
    public String getName() {
        return event.getName();
    }

    /**
     * Returns a combined textual representation of the festival day and its name.
     *
     * @return display string
     */
    @Override
    public String toString() {
        return String.format("%s %s", day, getName());
    }

}
