/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
