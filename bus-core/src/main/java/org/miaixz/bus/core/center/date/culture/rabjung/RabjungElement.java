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
package org.miaixz.bus.core.center.date.culture.rabjung;

import org.miaixz.bus.core.center.date.culture.Element;

/**
 * Represents the Five Elements in the Tibetan calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabjungElement extends Element {

    /**
     * Constructs a {@code RabjungElement} with the given index.
     *
     * @param index The index of the element.
     */
    public RabjungElement(int index) {
        super(index);
    }

    /**
     * Constructs a {@code RabjungElement} with the given name.
     *
     * @param name The name of the element. "铁" (Iron) will be replaced with "金" (Metal).
     */
    public RabjungElement(String name) {
        super(name.replace("铁", "金"));
    }

    /**
     * Creates a {@code RabjungElement} instance from the given index.
     *
     * @param index The index of the element.
     * @return A new {@link RabjungElement} instance.
     */
    public static RabjungElement fromIndex(int index) {
        return new RabjungElement(index);
    }

    /**
     * Creates a {@code RabjungElement} instance from the given name.
     *
     * @param name The name of the element.
     * @return A new {@link RabjungElement} instance.
     */
    public static RabjungElement fromName(String name) {
        return new RabjungElement(name);
    }

    /**
     * Gets the next Rabjung element after a specified number of steps.
     *
     * @param n The number of steps to advance.
     * @return The {@link RabjungElement} after {@code n} steps.
     */
    public RabjungElement next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the element that this element generates (我生者).
     *
     * @return The generated {@link RabjungElement}.
     */
    public RabjungElement getReinforce() {
        return next(1);
    }

    /**
     * Gets the element that this element overcomes (我克者).
     *
     * @return The overcome {@link RabjungElement}.
     */
    public RabjungElement getRestrain() {
        return next(2);
    }

    /**
     * Gets the element that generates this element (生我者).
     *
     * @return The generating {@link RabjungElement}.
     */
    public RabjungElement getReinforced() {
        return next(-1);
    }

    /**
     * Gets the element that overcomes this element (克我者).
     *
     * @return The overcoming {@link RabjungElement}.
     */
    public RabjungElement getRestrained() {
        return next(-2);
    }

    /**
     * Gets the name of this object.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return super.getName().replace("金", "铁");
    }

}
