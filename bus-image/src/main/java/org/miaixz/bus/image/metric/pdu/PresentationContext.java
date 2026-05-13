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
 * Represents the PresentationContext type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PresentationContext {

    /**
     * The acceptance value.
     */
    public static final int ACCEPTANCE = 0;

    /**
     * The user rejection value.
     */
    public static final int USER_REJECTION = 1;

    /**
     * The provider rejection value.
     */
    public static final int PROVIDER_REJECTION = 2;

    /**
     * The abstract syntax not supported value.
     */
    public static final int ABSTRACT_SYNTAX_NOT_SUPPORTED = 3;

    /**
     * The transfer syntax not supported value.
     */
    public static final int TRANSFER_SYNTAX_NOT_SUPPORTED = 4;

    /**
     * The results value.
     */
    private static final String[] RESULTS = { "0 - acceptance", "1 - user-rejection",
            "2 - no-reason (provider rejection)", "3 - abstract-syntax-not-supported (provider rejection)",
            "4 - transfer-syntaxes-not-supported (provider rejection)" };

    /**
     * The pcid value.
     */
    private final int pcid;

    /**
     * The result value.
     */
    private final int result;

    /**
     * The as value.
     */
    private final String as;

    /**
     * The tss value.
     */
    private final String[] tss;

    /**
     * Creates a new instance.
     *
     * @param pcid   the pcid.
     * @param result the result.
     * @param as     the as.
     * @param tss    the tss.
     */
    public PresentationContext(int pcid, int result, String as, String... tss) {
        this.pcid = pcid;
        this.result = result;
        this.as = as;
        this.tss = tss;
    }

    /**
     * Creates a new instance.
     *
     * @param pcid the pcid.
     * @param as   the as.
     * @param tss  the tss.
     */
    public PresentationContext(int pcid, String as, String... tss) {
        this(pcid, 0, as, tss);
    }

    /**
     * Creates a new instance.
     *
     * @param pcid   the pcid.
     * @param result the result.
     * @param ts     the ts.
     */
    public PresentationContext(int pcid, int result, String ts) {
        this(pcid, result, null, ts);
    }

    /**
     * Executes the result as string operation.
     *
     * @param result the result.
     * @return the operation result.
     */
    private static String resultAsString(int result) {
        try {
            return RESULTS[result];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(result);
        }
    }

    /**
     * Gets the pcid.
     *
     * @return the pcid.
     */
    public final int getPCID() {
        return pcid;
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
     * Determines whether accepted.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isAccepted() {
        return result == ACCEPTANCE;
    }

    /**
     * Gets the abstract syntax.
     *
     * @return the abstract syntax.
     */
    public final String getAbstractSyntax() {
        return as;
    }

    /**
     * Gets the transfer syntaxes.
     *
     * @return the transfer syntaxes.
     */
    public final String[] getTransferSyntaxes() {
        return tss;
    }

    /**
     * Determines whether transfer syntax.
     *
     * @param ts the ts.
     * @return true if the condition is met; otherwise false.
     */
    public boolean containsTransferSyntax(String ts) {
        for (String ts0 : tss)
            if (ts.equals(ts0))
                return true;
        return false;
    }

    /**
     * Gets the transfer syntax.
     *
     * @return the transfer syntax.
     */
    public String getTransferSyntax() {
        return tss[0];
    }

    /**
     * Executes the length operation.
     *
     * @return the operation result.
     */
    public int length() {
        int len = 4;
        if (as != null)
            len += 4 + as.length();
        for (String ts : tss)
            len += 4 + ts.length();
        return len;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

    /**
     * Executes the prompt to operation.
     *
     * @param sb the sb.
     * @return the operation result.
     */
    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  PresentationContext[id: ").append(pcid).append(Builder.LINE_SEPARATOR);
        if (as != null)
            UID.promptTo(as, sb.append("    as: "));
        else
            sb.append("    result: ").append(resultAsString(result));
        sb.append(Builder.LINE_SEPARATOR);
        for (String ts : tss)
            UID.promptTo(ts, sb.append("    ts: ")).append(Builder.LINE_SEPARATOR);
        return sb.append("  ]");
    }

}
