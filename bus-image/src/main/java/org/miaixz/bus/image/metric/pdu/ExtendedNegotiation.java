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
public class ExtendedNegotiation {

    private final String cuid;
    private final byte[] info;

    public ExtendedNegotiation(String cuid, byte[] info) {
        if (cuid == null)
            throw new NullPointerException();

        this.cuid = cuid;
        this.info = info.clone();
    }

    public final String getSOPClassUID() {
        return cuid;
    }

    public final byte[] getInformation() {
        return info.clone();
    }

    public final byte getField(int index, byte def) {
        return index < info.length ? info[index] : def;
    }

    public int length() {
        return cuid.length() + info.length + 2;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  ExtendedNegotiation[").append(Builder.LINE_SEPARATOR).append("    sopClass: ");
        UID.promptTo(cuid, sb).append(Builder.LINE_SEPARATOR).append("    info: [");
        for (byte b : info)
            sb.append(b).append(", ");
        return sb.append(']').append(Builder.LINE_SEPARATOR).append("  ]");
    }

}
