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
package org.miaixz.bus.image.metric;

import java.util.EnumSet;

import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum QueryOption {
    RELATIONAL, DATETIME, FUZZY, TIMEZONE;

    public static byte[] toExtendedNegotiationInformation(EnumSet<QueryOption> opts) {
        byte[] info = new byte[opts.contains(TIMEZONE) ? 4 : opts.contains(FUZZY) || opts.contains(DATETIME) ? 3 : 1];
        for (QueryOption query : opts)
            info[query.ordinal()] = 1;
        return info;
    }

    public static EnumSet<QueryOption> toOptions(ExtendedNegotiation extNeg) {
        EnumSet<QueryOption> opts = EnumSet.noneOf(QueryOption.class);
        if (extNeg != null) {
            toOption(extNeg, QueryOption.RELATIONAL, opts);
            toOption(extNeg, QueryOption.DATETIME, opts);
            toOption(extNeg, QueryOption.FUZZY, opts);
            toOption(extNeg, QueryOption.TIMEZONE, opts);
        }
        return opts;
    }

    private static void toOption(ExtendedNegotiation extNeg, QueryOption opt, EnumSet<QueryOption> opts) {
        if (extNeg.getField(opt.ordinal(), (byte) 0) == 1)
            opts.add(opt);
    }

}
