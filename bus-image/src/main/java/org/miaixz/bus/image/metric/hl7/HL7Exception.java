/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
