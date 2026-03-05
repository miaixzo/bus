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
package org.miaixz.bus.image.galaxy.data;

import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.miaixz.bus.image.Format;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
enum TemporalType {

    DA {

        @Override
        public Temporal parseTemporal(String s, DatePrecision precision) {
            precision.lastField = Calendar.DAY_OF_MONTH;
            return Format.parseLocalDA(s);
        }

        @Override
        public Date parse(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
            precision.lastField = Calendar.DAY_OF_MONTH;
            return Format.parseDA(null, s, ceil);
        }

        @Override
        public String format(TimeZone tz, Date date, DatePrecision precision) {
            return Format.formatDA(tz, date);
        }
    },
    DT {

        public Temporal parseTemporal(String s, DatePrecision precision) {
            return Format.parseTemporalDT(s, precision);
        }

        @Override
        public Date parse(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
            return Format.parseDT(tz, s, ceil, precision);
        }

        @Override
        public String format(TimeZone tz, Date date, DatePrecision precision) {
            return Format.formatDT(tz, date, precision);
        }
    },
    TM {

        @Override
        public Temporal parseTemporal(String s, DatePrecision precision) {
            return Format.parseLocalTM(s, precision);
        }

        @Override
        public Date parse(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
            return Format.parseTM(null, s, ceil, precision);
        }

        @Override
        public String format(TimeZone tz, Date date, DatePrecision precision) {
            return Format.formatTM(tz, date, precision);
        }
    };

    public abstract Temporal parseTemporal(String s, DatePrecision precision);

    public abstract Date parse(TimeZone tz, String val, boolean ceil, DatePrecision precision);

    public abstract String format(TimeZone tz, Date date, DatePrecision precision);

}
