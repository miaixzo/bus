/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.metric.internal.pdu;

import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.Property;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExtendedNegotiate {

    private final String cuid;
    private final byte[] info;

    public ExtendedNegotiate(String cuid, byte[] info) {
        if (null == cuid)
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
        sb.append("  ExtendedNegotiation[")
                .append(Property.LINE_SEPARATOR)
                .append("    sopClass: ");
        UID.promptTo(cuid, sb)
                .append(Property.LINE_SEPARATOR)
                .append("    info: [");
        for (byte b : info)
            sb.append(b).append(", ");
        return sb.append(']')
                .append(Property.LINE_SEPARATOR)
                .append("  ]");
    }

}
