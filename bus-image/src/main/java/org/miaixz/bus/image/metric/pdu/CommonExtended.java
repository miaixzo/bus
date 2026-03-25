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
 * @author Kimi Liu
 * @since Java 21+
 */
public class CommonExtended {

    private final String sopCUID;
    private final String serviceCUID;
    private final String[] relSopCUIDs;

    public CommonExtended(String sopCUID, String serviceCUID, String... relSopCUIDs) {
        if (sopCUID == null)
            throw new NullPointerException("sopCUID");

        if (serviceCUID == null)
            throw new NullPointerException("serviceCUID");

        this.sopCUID = sopCUID;
        this.serviceCUID = serviceCUID;
        this.relSopCUIDs = relSopCUIDs;
    }

    public final String getSOPClassUID() {
        return sopCUID;
    }

    public final String getServiceClassUID() {
        return serviceCUID;
    }

    public String[] getRelatedGeneralSOPClassUIDs() {
        return relSopCUIDs;
    }

    public int length() {
        return 6 + sopCUID.length() + serviceCUID.length() + getRelatedGeneralSOPClassUIDsLength();
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

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

    public int getRelatedGeneralSOPClassUIDsLength() {
        int len = 0;
        for (String cuid : relSopCUIDs)
            len += 2 + cuid.length();
        return len;
    }

}
