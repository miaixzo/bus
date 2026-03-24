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

import java.io.IOException;
import java.io.Serial;

import org.miaixz.bus.core.lang.Normal;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class AAbort extends IOException {

    @Serial
    private static final long serialVersionUID = 2852275517117L;

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
