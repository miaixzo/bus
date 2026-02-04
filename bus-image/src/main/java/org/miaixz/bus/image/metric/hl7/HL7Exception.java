/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric.hl7;

import java.io.Serial;

import org.miaixz.bus.core.lang.Normal;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Exception extends Exception {

    @Serial
    private static final long serialVersionUID = 2852263939653L;

    public static final String AA = "AA";
    public static final String AR = "AR";
    public static final String AE = "AE";
    private final String ack;
    private final HL7Segment err;

    public HL7Exception(String ack) {
        this.ack = ack;
        this.err = null;
    }

    public HL7Exception(String ack, String message) {
        super(message);
        this.ack = ack;
        this.err = null;
    }

    public HL7Exception(String ack, Throwable cause) {
        super(cause);
        this.ack = ack;
        this.err = null;
    }

    public HL7Exception(HL7Segment err) {
        super(err.getField(8, null));
        this.ack = toAck(err);
        this.err = err;
    }

    public HL7Exception(HL7Segment err, Throwable cause) {
        super(err.getField(8, null), cause);
        this.ack = toAck(err);
        this.err = err;
    }

    private static String toAck(HL7Segment err) {
        return err.getField(3, Normal.EMPTY).startsWith("1") ? AE : AR;
    }

    public final String getAcknowledgmentCode() {
        return ack;
    }

    public String getErrorMessage() {
        return getMessage();
    }

    public HL7Segment getErrorSegment() {
        return err;
    }

}
