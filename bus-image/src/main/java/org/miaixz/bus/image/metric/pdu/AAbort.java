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
 * Represents the AAbort type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AAbort extends IOException {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852275517117L;

    /**
     * The ul serive user value.
     */
    public static final int UL_SERIVE_USER = 0;

    /**
     * The ul serive provider value.
     */
    public static final int UL_SERIVE_PROVIDER = 2;

    /**
     * The reason not specified value.
     */
    public static final int REASON_NOT_SPECIFIED = 0;

    /**
     * The unrecognized pdu value.
     */
    public static final int UNRECOGNIZED_PDU = 1;

    /**
     * The unexpected pdu value.
     */
    public static final int UNEXPECTED_PDU = 2;

    /**
     * The unrecognized pdu parameter value.
     */
    public static final int UNRECOGNIZED_PDU_PARAMETER = 4;

    /**
     * The unexpected pdu parameter value.
     */
    public static final int UNEXPECTED_PDU_PARAMETER = 5;

    /**
     * The invalid pdu parameter value value.
     */
    public static final int INVALID_PDU_PARAMETER_VALUE = 6;

    /**
     * The sources value.
     */
    private static final String[] SOURCES = { "0 - service-user", "1", "2 - service-provider", };

    /**
     * The service user reasons value.
     */
    private static final String[] SERVICE_USER_REASONS = { "0", };

    /**
     * The service provider reasons value.
     */
    private static final String[] SERVICE_PROVIDER_REASONS = { "0 - reason-not-specified", "1 - unrecognized-PDU",
            "2 - unexpected-PDU", "3", "4 - unrecognized-PDU-parameter", "5 - unexpected-PDU-parameter",
            "6 - invalid-PDU-parameter-value" };

    /**
     * The reasons value.
     */
    private static final String[][] REASONS = { SERVICE_USER_REASONS, Normal.EMPTY_STRING_ARRAY,
            SERVICE_PROVIDER_REASONS };

    /**
     * The source value.
     */
    private final int source;

    /**
     * The reason value.
     */
    private final int reason;

    /**
     * Creates a new instance.
     *
     * @param source the source.
     * @param reason the reason.
     */
    public AAbort(int source, int reason) {
        super("A-ABORT[source: " + toString(SOURCES, source) + ", reason: " + toReason(source, reason) + ']');
        this.source = source;
        this.reason = reason;
    }

    /**
     * Creates a new instance.
     */
    public AAbort() {
        this(UL_SERIVE_USER, 0);
    }

    /**
     * Returns the string representation.
     *
     * @param ss the ss.
     * @param i  the i.
     * @return the string representation.
     */
    private static String toString(String[] ss, int i) {
        try {
            return ss[i];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(i);
        }
    }

    /**
     * Converts this value to reason.
     *
     * @param source the source.
     * @param reason the reason.
     * @return the operation result.
     */
    private static String toReason(int source, int reason) {
        try {
            return toString(REASONS[source], reason);
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(reason);
        }
    }

    /**
     * Gets the reason.
     *
     * @return the reason.
     */
    public final int getReason() {
        return reason;
    }

    /**
     * Gets the source.
     *
     * @return the source.
     */
    public final int getSource() {
        return source;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return getMessage();
    }

}
