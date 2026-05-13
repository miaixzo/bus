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
package org.miaixz.bus.image.metric.pdu;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.UID;

/**
 * Represents the ExtendedNegotiation type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ExtendedNegotiation {

    /**
     * The cuid value.
     */
    private final String cuid;

    /**
     * The info value.
     */
    private final byte[] info;

    /**
     * Creates a new instance.
     *
     * @param cuid the cuid.
     * @param info the info.
     */
    public ExtendedNegotiation(String cuid, byte[] info) {
        if (cuid == null)
            throw new NullPointerException();

        this.cuid = cuid;
        this.info = info.clone();
    }

    /**
     * Gets the sop class uid.
     *
     * @return the sop class uid.
     */
    public final String getSOPClassUID() {
        return cuid;
    }

    /**
     * Gets the information.
     *
     * @return the information.
     */
    public final byte[] getInformation() {
        return info.clone();
    }

    /**
     * Gets the field.
     *
     * @param index the index.
     * @param def   the def.
     * @return the field.
     */
    public final byte getField(int index, byte def) {
        return index < info.length ? info[index] : def;
    }

    /**
     * Executes the length operation.
     *
     * @return the operation result.
     */
    public int length() {
        return cuid.length() + info.length + 2;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

    /**
     * Executes the prompt to operation.
     *
     * @param sb the sb.
     * @return the operation result.
     */
    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  ExtendedNegotiation[").append(Builder.LINE_SEPARATOR).append("    sopClass: ");
        UID.promptTo(cuid, sb).append(Builder.LINE_SEPARATOR).append("    info: [");
        for (byte b : info)
            sb.append(b).append(", ");
        return sb.append(']').append(Builder.LINE_SEPARATOR).append("  ]");
    }

}
