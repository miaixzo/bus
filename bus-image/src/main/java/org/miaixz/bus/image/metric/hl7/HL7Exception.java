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
package org.miaixz.bus.image.metric.hl7;

import java.io.Serial;

import org.miaixz.bus.core.lang.Normal;

/**
 * Represents the HL7Exception type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7Exception extends Exception {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852263939653L;

    /**
     * The aa value.
     */
    public static final String AA = "AA";

    /**
     * The ar value.
     */
    public static final String AR = "AR";

    /**
     * The ae value.
     */
    public static final String AE = "AE";

    /**
     * The ack value.
     */
    private final String ack;

    /**
     * The err value.
     */
    private final HL7Segment err;

    /**
     * Creates a new instance.
     *
     * @param ack the ack.
     */
    public HL7Exception(String ack) {
        this.ack = ack;
        this.err = null;
    }

    /**
     * Creates a new instance.
     *
     * @param ack     the ack.
     * @param message the message.
     */
    public HL7Exception(String ack, String message) {
        super(message);
        this.ack = ack;
        this.err = null;
    }

    /**
     * Creates a new instance.
     *
     * @param ack   the ack.
     * @param cause the cause.
     */
    public HL7Exception(String ack, Throwable cause) {
        super(cause);
        this.ack = ack;
        this.err = null;
    }

    /**
     * Creates a new instance.
     *
     * @param err the err.
     */
    public HL7Exception(HL7Segment err) {
        super(err.getField(8, null));
        this.ack = toAck(err);
        this.err = err;
    }

    /**
     * Creates a new instance.
     *
     * @param err   the err.
     * @param cause the cause.
     */
    public HL7Exception(HL7Segment err, Throwable cause) {
        super(err.getField(8, null), cause);
        this.ack = toAck(err);
        this.err = err;
    }

    /**
     * Converts this value to ack.
     *
     * @param err the err.
     * @return the operation result.
     */
    private static String toAck(HL7Segment err) {
        return err.getField(3, Normal.EMPTY).startsWith("1") ? AE : AR;
    }

    /**
     * Gets the acknowledgment code.
     *
     * @return the acknowledgment code.
     */
    public final String getAcknowledgmentCode() {
        return ack;
    }

    /**
     * Gets the error message.
     *
     * @return the error message.
     */
    public String getErrorMessage() {
        return getMessage();
    }

    /**
     * Gets the error segment.
     *
     * @return the error segment.
     */
    public HL7Segment getErrorSegment() {
        return err;
    }

}
