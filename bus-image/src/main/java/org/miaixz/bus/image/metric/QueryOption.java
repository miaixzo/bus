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
package org.miaixz.bus.image.metric;

import java.util.EnumSet;

import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;

/**
 * @author Kimi Liu
 * @since Java 21+
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
