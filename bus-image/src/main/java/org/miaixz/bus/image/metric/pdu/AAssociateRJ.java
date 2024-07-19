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

import org.miaixz.bus.core.lang.Normal;

import java.io.IOException;
/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AAssociateRJ extends IOException {

    private static final long serialVersionUID = -1L;

    public static final int RESULT_REJECTED_PERMANENT = 1;
    public static final int RESULT_REJECTED_TRANSIENT = 2;
    public static final int SOURCE_SERVICE_USER = 1;
    public static final int SOURCE_SERVICE_PROVIDER_ACSE = 2;
    public static final int SOURCE_SERVICE_PROVIDER_PRES = 3;
    public static final int REASON_NO_REASON_GIVEN = 1;
    public static final int REASON_APP_CTX_NAME_NOT_SUPPORTED = 2;
    public static final int REASON_CALLING_AET_NOT_RECOGNIZED = 3;
    public static final int REASON_CALLED_AET_NOT_RECOGNIZED = 7;
    public static final int REASON_PROTOCOL_VERSION_NOT_SUPPORTED = 2;
    public static final int REASON_TEMPORARY_CONGESTION = 1;
    public static final int REASON_LOCAL_LIMIT_EXCEEDED = 2;
    private static final String[] RESULTS = {
            "0",
            "1 - rejected-permanent",
            "2 - rejected-transient"
    };

    private static final String[] SOURCES = {
            "0",
            "1 - service-user",
            "2 - service-provider (ACSE related function)",
            "3 - service-provider (Presentation related function)"
    };

    private static final String[] SERVICE_USER_REASONS = {
            "0",
            "1 - no-reason-given",
            "2 - application-context-name-not-supported",
            "3 - calling-AE-title-not-recognized",
            "4",
            "5",
            "6",
            "7 - called-AE-title-not-recognized",
    };

    private static final String[] SERVICE_PROVIDER_ACSE_REASONS = {
            "0",
            "1 - no-reason-given",
            "2 - protocol-version-not-supported",
    };

    private static final String[] SERVICE_PROVIDER_PRES_REASONS = {
            "0",
            "1 - temporary-congestion",
            "2 - local-limit-exceeded",
    };

    private static final String[][] REASONS = {
            Normal.EMPTY_STRING_ARRAY,
            SERVICE_USER_REASONS,
            SERVICE_PROVIDER_ACSE_REASONS,
            SERVICE_PROVIDER_PRES_REASONS
    };

    private final int result;
    private final int source;
    private final int reason;

    public AAssociateRJ(int result, int source, int reason) {
        super("A-ASSOCIATE-RJ[result: " + toString(RESULTS, result)
                + ", source: " + toString(SOURCES, source)
                + ", reason: " + toReason(source, reason)
                + ']');
        this.result = result;
        this.source = source;
        this.reason = reason;
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

    public final int getResult() {
        return result;
    }

    public final int getSource() {
        return source;
    }

    public final int getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return getMessage();
    }

}
