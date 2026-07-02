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
package org.miaixz.bus.health.builtin.hardware.common;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.Display;
import org.miaixz.bus.health.builtin.hardware.DisplayInfo;
import org.miaixz.bus.health.builtin.hardware.DisplayInfoImpl;

/**
 * A Display
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public abstract class AbstractDisplay implements Display {

    /**
     * The displayInfo value.
     */
    private final DisplayInfo displayInfo;

    /**
     * Constructor for AbstractDisplay from a raw EDID byte array.
     *
     * @param edid a byte array representing a display EDID
     */
    protected AbstractDisplay(byte[] edid) {
        this.displayInfo = new DisplayInfoImpl(edid);
    }

    /**
     * Constructor for AbstractDisplay from decoded display information.
     *
     * @param displayInfo the decoded display information
     */
    protected AbstractDisplay(DisplayInfo displayInfo) {
        this.displayInfo = displayInfo;
    }

    /**
     * Returns the edid.
     *
     * @return the get edid result
     */
    @Override
    public byte[] getEdid() {
        return this.displayInfo.getEdid();
    }

    /**
     * Returns the display information.
     *
     * @return the display information
     */
    @Override
    public DisplayInfo getDisplayInfo() {
        return this.displayInfo;
    }

    /**
     * Returns whether the EDID is synthetic.
     *
     * @return {@code true} if the EDID is synthetic, otherwise {@code false}
     */
    @Override
    public boolean isEdidSynthetic() {
        return this.displayInfo.isEdidSynthetic();
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        return this.displayInfo.toString();
    }

}
