/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.IOException;
import java.io.Serial;

import org.miaixz.bus.core.lang.Normal;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AAbort extends IOException {

    @Serial
    private static final long serialVersionUID = 2852270155656L;

    public static final int UL_SERIVE_USER = 0;
    public static final int UL_SERIVE_PROVIDER = 2;
    public static final int REASON_NOT_SPECIFIED = 0;
    public static final int UNRECOGNIZED_PDU = 1;
    public static final int UNEXPECTED_PDU = 2;
    public static final int UNRECOGNIZED_PDU_PARAMETER = 4;
    public static final int UNEXPECTED_PDU_PARAMETER = 5;
    public static final int INVALID_PDU_PARAMETER_VALUE = 6;
    private static final String[] SOURCES = { "0 - service-user", "1", "2 - service-provider", };

    private static final String[] SERVICE_USER_REASONS = { "0", };

    private static final String[] SERVICE_PROVIDER_REASONS = { "0 - reason-not-specified", "1 - unrecognized-PDU",
            "2 - unexpected-PDU", "3", "4 - unrecognized-PDU-parameter", "5 - unexpected-PDU-parameter",
            "6 - invalid-PDU-parameter-value" };

    private static final String[][] REASONS = { SERVICE_USER_REASONS, Normal.EMPTY_STRING_ARRAY,
            SERVICE_PROVIDER_REASONS };

    private final int source;
    private final int reason;

    public AAbort(int source, int reason) {
        super("A-ABORT[source: " + toString(SOURCES, source) + ", reason: " + toReason(source, reason) + ']');
        this.source = source;
        this.reason = reason;
    }

    public AAbort() {
        this(UL_SERIVE_USER, 0);
    }

    private static String toString(String[] ss, int i) {
        try {
            return ss[i];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(i);
        }
    }

    private static String toReason(int source, int reason) {
        try {
            return toString(REASONS[source], reason);
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(reason);
        }
    }

    public final int getReason() {
        return reason;
    }

    public final int getSource() {
        return source;
    }

    @Override
    public String toString() {
        return getMessage();
    }

}
