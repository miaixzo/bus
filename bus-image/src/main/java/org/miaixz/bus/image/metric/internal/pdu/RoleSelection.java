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
package org.miaixz.bus.image.metric.internal.pdu;

import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.Property;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RoleSelection {

    private final String cuid;
    private final boolean scu;
    private final boolean scp;

    public RoleSelection(String cuid, boolean scu, boolean scp) {
        if (null == cuid) {
            throw new NullPointerException("cuid cannot be null");
        }

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
        sb.append("  RoleSelection[")
                .append(Property.LINE_SEPARATOR)
                .append("    sopClass: ");
        return UID.promptTo(cuid, sb)
                .append(Property.LINE_SEPARATOR)
                .append("    scu: ").append(scu)
                .append(Property.LINE_SEPARATOR)
                .append("    scp: ").append(scp)
                .append(Property.LINE_SEPARATOR)
                .append("  ]");
    }

}
