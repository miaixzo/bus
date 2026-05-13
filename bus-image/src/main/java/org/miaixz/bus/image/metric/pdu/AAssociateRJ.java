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
 * Represents the AAssociateRJ type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AAssociateRJ extends IOException {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852275733131L;

    /**
     * The result rejected permanent value.
     */
    public static final int RESULT_REJECTED_PERMANENT = 1;

    /**
     * The result rejected transient value.
     */
    public static final int RESULT_REJECTED_TRANSIENT = 2;

    /**
     * The source service user value.
     */
    public static final int SOURCE_SERVICE_USER = 1;

    /**
     * The source service provider acse value.
     */
    public static final int SOURCE_SERVICE_PROVIDER_ACSE = 2;

    /**
     * The source service provider pres value.
     */
    public static final int SOURCE_SERVICE_PROVIDER_PRES = 3;

    /**
     * The reason no reason given value.
     */
    public static final int REASON_NO_REASON_GIVEN = 1;

    /**
     * The reason app ctx name not supported value.
     */
    public static final int REASON_APP_CTX_NAME_NOT_SUPPORTED = 2;

    /**
     * The reason calling aet not recognized value.
     */
    public static final int REASON_CALLING_AET_NOT_RECOGNIZED = 3;

    /**
     * The reason called aet not recognized value.
     */
    public static final int REASON_CALLED_AET_NOT_RECOGNIZED = 7;

    /**
     * The reason protocol version not supported value.
     */
    public static final int REASON_PROTOCOL_VERSION_NOT_SUPPORTED = 2;

    /**
     * The reason temporary congestion value.
     */
    public static final int REASON_TEMPORARY_CONGESTION = 1;

    /**
     * The reason local limit exceeded value.
     */
    public static final int REASON_LOCAL_LIMIT_EXCEEDED = 2;

    /**
     * The results value.
     */
    private static final String[] RESULTS = { "0", "1 - rejected-permanent", "2 - rejected-transient" };

    /**
     * The sources value.
     */
    private static final String[] SOURCES = { "0", "1 - service-user", "2 - service-provider (ACSE related function)",
            "3 - service-provider (Presentation related function)" };

    /**
     * The service user reasons value.
     */
    private static final String[] SERVICE_USER_REASONS = { "0", "1 - no-reason-given",
            "2 - application-context-name-not-supported", "3 - calling-AE-title-not-recognized", "4", "5", "6",
            "7 - called-AE-title-not-recognized", };

    /**
     * The service provider acse reasons value.
     */
    private static final String[] SERVICE_PROVIDER_ACSE_REASONS = { "0", "1 - no-reason-given",
            "2 - protocol-version-not-supported", };

    /**
     * The service provider pres reasons value.
     */
    private static final String[] SERVICE_PROVIDER_PRES_REASONS = { "0", "1 - temporary-congestion",
            "2 - local-limit-exceeded", };

    /**
     * The reasons value.
     */
    private static final String[][] REASONS = { Normal.EMPTY_STRING_ARRAY, SERVICE_USER_REASONS,
            SERVICE_PROVIDER_ACSE_REASONS, SERVICE_PROVIDER_PRES_REASONS };

    /**
     * The result value.
     */
    private final int result;

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
     * @param result the result.
     * @param source the source.
     * @param reason the reason.
     */
    public AAssociateRJ(int result, int source, int reason) {
        super("A-ASSOCIATE-RJ[result: " + toString(RESULTS, result) + ", source: " + toString(SOURCES, source)
                + ", reason: " + toReason(source, reason) + ']');
        this.result = result;
        this.source = source;
        this.reason = reason;
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
     * Gets the result.
     *
     * @return the result.
     */
    public final int getResult() {
        return result;
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
     * Gets the reason.
     *
     * @return the reason.
     */
    public final int getReason() {
        return reason;
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
