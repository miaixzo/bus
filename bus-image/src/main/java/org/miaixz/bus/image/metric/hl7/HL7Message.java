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

import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.util.ArrayList;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Message extends ArrayList<HL7Segment> {

    private static final long serialVersionUID = -1L;

    public HL7Message() {

    }

    public HL7Message(int initialCapacity) {
        super(initialCapacity);
    }

    public static HL7Message parse(byte[] b, String defCharset) {
        return parse(b, b.length, defCharset);
    }

    public static HL7Message parse(byte[] b, int size, String defCharset) {
        ParsePosition pos = new ParsePosition(0);
        HL7Message msg = new HL7Message();
        HL7Segment seg = HL7Segment.parseMSH(b, size, pos);
        char fieldSeparator = seg.getFieldSeparator();
        String encodingCharacters = seg.getEncodingCharacters();
        String charsetName = HL7Charset.toCharsetName(seg.getField(17, defCharset));
        msg.add(seg);
        while ((seg = HL7Segment.parse(b, size, pos, fieldSeparator, encodingCharacters, charsetName)) != null)
            msg.add(seg);
        msg.trimToSize();
        return msg;
    }

    public static HL7Message makeACK(HL7Segment msh, String ackCode, String text) {
        int size = msh.size();
        HL7Segment ackmsh = HL7Segment.makeMSH(size, msh.getFieldSeparator(), msh.getEncodingCharacters());
        ackmsh.setField(2, msh.getField(4, null));
        ackmsh.setField(3, msh.getField(5, null));
        ackmsh.setField(4, msh.getField(2, null));
        ackmsh.setField(5, msh.getField(3, null));
        ackmsh.setField(8, "ACK^" + eventType(msh) + "^ACK");
        for (int i = 10; i < size; i++)
            ackmsh.setField(i, msh.getField(i, null));
        HL7Segment msa = new HL7Segment(4, msh.getFieldSeparator(), msh.getEncodingCharacters());
        msa.setField(0, "MSA");
        msa.setField(1, ackCode);
        msa.setField(2, msh.getMessageControlID());
        msa.setField(3, text != null && text.length() > 80 ? text.substring(0, 80) : text);
        HL7Message ack = new HL7Message(2);
        ack.add(ackmsh);
        ack.add(msa);
        return ack;
    }

    public static String eventType(HL7Segment msh) {
        String messageType = msh.getMessageType();
        return messageType == null || messageType.equals("") ? messageType : messageType.substring(4, 7);
    }

    public static HL7Message makePixQuery(String pid, String... domains) {
        HL7Segment msh = HL7Segment.makeMSH();
        msh.setField(8, "QBP^Q23^QBP_Q21");
        HL7Segment qpd = new HL7Segment(5);
        qpd.setField(0, "QPD");
        qpd.setField(1, "IHE PIX Query");
        qpd.setField(2, "QRY" + msh.getField(9, Normal.EMPTY));
        qpd.setField(3, pid);
        qpd.setField(4, HL7Segment.concat(domains, Symbol.C_TILDE));
        HL7Segment rcp = new HL7Segment(8);
        rcp.setField(0, "RCP");
        rcp.setField(1, "I");
        HL7Message qbp = new HL7Message(3);
        qbp.add(msh);
        qbp.add(qpd);
        qbp.add(rcp);
        return qbp;
    }

    public static HL7Message makePdqQuery(String[] queryParams, String... domains) {
        HL7Segment msh = HL7Segment.makeMSH();
        msh.setField(8, "QBP^Q22^QBP_Q21");
        HL7Segment qpd = new HL7Segment(5);
        qpd.setField(0, "QPD");
        qpd.setField(1, "IHE PDQ Query");
        qpd.setField(2, "QRY" + msh.getField(9, ""));
        qpd.setField(3, HL7Segment.concat(queryParams, Symbol.C_TILDE));
        qpd.setField(4, HL7Segment.concat(domains, Symbol.C_TILDE));
        HL7Segment rcp = new HL7Segment(8);
        rcp.setField(0, "RCP");
        rcp.setField(1, "I");
        HL7Message qbp = new HL7Message(3);
        qbp.add(msh);
        qbp.add(qpd);
        qbp.add(rcp);
        return qbp;
    }

    public static HL7Message makeACK(HL7Segment msh, HL7Exception e) {
        HL7Message ack = makeACK(msh, e.getAcknowledgmentCode(), e.getErrorMessage());
        HL7Segment err = e.getErrorSegment();
        if (err != null)
            ack.add(err);
        return ack;
    }

    public HL7Segment getSegment(String name) {
        for (HL7Segment seg : this)
            if (name.equals(seg.getField(0, null)))
                return seg;
        return null;
    }

    @Override
    public String toString() {
        return toString('\r');
    }

    public String toString(char segdelim) {
        int len = size();
        for (HL7Segment seg : this) {
            int segSize = seg.size();
            len += segSize - 1;
            for (int i = 0; i < segSize; i++) {
                String s = seg.getField(i, null);
                if (s != null)
                    len += s.length();
            }
        }
        char[] cs = new char[len];
        int off = 0;
        for (HL7Segment seg : this) {
            char delim = seg.getFieldSeparator();
            int segSize = seg.size();
            for (int i = 0; i < segSize; i++) {
                String s = seg.getField(i, null);
                if (s != null) {
                    int l = s.length();
                    s.getChars(0, l, cs, off);
                    off += l;
                }
                cs[off++] = delim;
            }
            cs[off - 1] = segdelim;
        }
        return new String(cs);
    }

    public byte[] getBytes(String defCharset) {
        try {
            return toString().getBytes(HL7Charset.toCharsetName(get(0).getField(17, defCharset)));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
