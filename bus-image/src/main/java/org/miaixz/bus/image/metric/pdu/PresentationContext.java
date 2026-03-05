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
 * @since Java 17+
 */
public class PresentationContext {

    public static final int ACCEPTANCE = 0;
    public static final int USER_REJECTION = 1;
    public static final int PROVIDER_REJECTION = 2;
    public static final int ABSTRACT_SYNTAX_NOT_SUPPORTED = 3;
    public static final int TRANSFER_SYNTAX_NOT_SUPPORTED = 4;

    private static final String[] RESULTS = { "0 - acceptance", "1 - user-rejection",
            "2 - no-reason (provider rejection)", "3 - abstract-syntax-not-supported (provider rejection)",
            "4 - transfer-syntaxes-not-supported (provider rejection)" };

    private final int pcid;
    private final int result;
    private final String as;
    private final String[] tss;

    public PresentationContext(int pcid, int result, String as, String... tss) {
        this.pcid = pcid;
        this.result = result;
        this.as = as;
        this.tss = tss;
    }

    public PresentationContext(int pcid, String as, String... tss) {
        this(pcid, 0, as, tss);
    }

    public PresentationContext(int pcid, int result, String ts) {
        this(pcid, result, null, ts);
    }

    private static String resultAsString(int result) {
        try {
            return RESULTS[result];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(result);
        }
    }

    public final int getPCID() {
        return pcid;
    }

    public final int getResult() {
        return result;
    }

    public boolean isAccepted() {
        return result == ACCEPTANCE;
    }

    public final String getAbstractSyntax() {
        return as;
    }

    public final String[] getTransferSyntaxes() {
        return tss;
    }

    public boolean containsTransferSyntax(String ts) {
        for (String ts0 : tss)
            if (ts.equals(ts0))
                return true;
        return false;
    }

    public String getTransferSyntax() {
        return tss[0];
    }

    public int length() {
        int len = 4;
        if (as != null)
            len += 4 + as.length();
        for (String ts : tss)
            len += 4 + ts.length();
        return len;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

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
