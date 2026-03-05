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
package org.miaixz.bus.core.center.date.culture.dog;

import org.miaixz.bus.core.center.date.culture.Replenish;

/**
 * Represents a specific day within the "Sanfu" (Dog Days) period. This class extends {@link Replenish} to associate a
 * specific day index with a {@link Dog} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DogDay extends Replenish {

    /**
     * Constructs a {@code DogDay} instance with the specified {@link Dog} and day index.
     *
     * @param dog      The {@link Dog} instance representing the Sanfu period.
     * @param dayIndex The index of the day within the Sanfu period.
     */
    public DogDay(Dog dog, int dayIndex) {
        super(dog, dayIndex);
    }

    /**
     * Gets the {@link Dog} instance associated with this Dog Day.
     *
     * @return The {@link Dog} instance.
     */
    public Dog getDog() {
        return (Dog) tradition;
    }

}
