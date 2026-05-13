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
 * Represents the CommonExtended type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CommonExtended {

    /**
     * The sop cuid value.
     */
    private final String sopCUID;

    /**
     * The service cuid value.
     */
    private final String serviceCUID;

    /**
     * The rel sop cui ds value.
     */
    private final String[] relSopCUIDs;

    /**
     * Creates a new instance.
     *
     * @param sopCUID     the sop cuid.
     * @param serviceCUID the service cuid.
     * @param relSopCUIDs the rel sop cui ds.
     */
    public CommonExtended(String sopCUID, String serviceCUID, String... relSopCUIDs) {
        if (sopCUID == null)
            throw new NullPointerException("sopCUID");

        if (serviceCUID == null)
            throw new NullPointerException("serviceCUID");

        this.sopCUID = sopCUID;
        this.serviceCUID = serviceCUID;
        this.relSopCUIDs = relSopCUIDs;
    }

    /**
     * Gets the sop class uid.
     *
     * @return the sop class uid.
     */
    public final String getSOPClassUID() {
        return sopCUID;
    }

    /**
     * Gets the service class uid.
     *
     * @return the service class uid.
     */
    public final String getServiceClassUID() {
        return serviceCUID;
    }

    /**
     * Gets the related general sop class ui ds.
     *
     * @return the related general sop class ui ds.
     */
    public String[] getRelatedGeneralSOPClassUIDs() {
        return relSopCUIDs;
    }

    /**
     * Executes the length operation.
     *
     * @return the operation result.
     */
    public int length() {
        return 6 + sopCUID.length() + serviceCUID.length() + getRelatedGeneralSOPClassUIDsLength();
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
        sb.append("  CommonExtendedNegotiation[").append(Builder.LINE_SEPARATOR).append("    sopClass: ");
        UID.promptTo(sopCUID, sb).append(Builder.LINE_SEPARATOR).append("    serviceClass: ");
        UID.promptTo(serviceCUID, sb).append(Builder.LINE_SEPARATOR);
        if (relSopCUIDs.length != 0) {
            sb.append("    relatedSOPClasses:").append(Builder.LINE_SEPARATOR);
            for (String uid : relSopCUIDs)
                UID.promptTo(uid, sb.append("      ")).append(Builder.LINE_SEPARATOR);
        }
        return sb.append("  ]");
    }

    /**
     * Gets the related general sop class ui ds length.
     *
     * @return the related general sop class ui ds length.
     */
    public int getRelatedGeneralSOPClassUIDsLength() {
        int len = 0;
        for (String cuid : relSopCUIDs)
            len += 2 + cuid.length();
        return len;
    }

}
