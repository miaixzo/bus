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
package org.miaixz.bus.image.metric.pdu;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.UID;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RoleSelection {

    private final String cuid;
    private final boolean scu;
    private final boolean scp;

    public RoleSelection(String cuid, boolean scu, boolean scp) {
        if (cuid == null)
            throw new NullPointerException();

        this.cuid = cuid;
        this.scu = scu;
        this.scp = scp;
    }

    public final String getSOPClassUID() {
        return cuid;
    }

    public final boolean isSCU() {
        return scu;
    }

    public final boolean isSCP() {
        return scp;
    }

    public int length() {
        return cuid.length() + 4;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  RoleSelection[").append(Builder.LINE_SEPARATOR).append("    sopClass: ");
        return UID.promptTo(cuid, sb).append(Builder.LINE_SEPARATOR).append("    scu: ").append(scu)
                .append(Builder.LINE_SEPARATOR).append("    scp: ").append(scp).append(Builder.LINE_SEPARATOR)
                .append("  ]");
    }

}
