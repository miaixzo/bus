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
