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
package org.miaixz.bus.image.galaxy.io;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.CountingOutputStream;
import org.miaixz.bus.image.galaxy.data.*;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageOutputStream extends FilterOutputStream {

    private static final byte[] DICM = { 'D', 'I', 'C', 'M' };
    private final byte[] buf = new byte[12];
    private byte[] preamble = new byte[Normal._128];
    private boolean explicitVR;
    private boolean bigEndian;
    private CountingOutputStream countingOutputStream;
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;
    private Deflater deflater;

    public ImageOutputStream(OutputStream out, String tsuid) throws IOException {
        super(out);
        switchTransferSyntax(tsuid);
    }

    public ImageOutputStream(File file) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(file)), UID.ExplicitVRLittleEndian.uid);
    }

    public final void setPreamble(byte[] preamble) {
        if (preamble.length != Normal._128)
            throw new IllegalArgumentException("preamble.length=" + preamble.length);
        this.preamble = preamble.clone();
    }

    public final boolean isExplicitVR() {
        return explicitVR;
    }

    public final boolean isBigEndian() {
        return bigEndian;
    }

    public final ImageEncodingOptions getEncodingOptions() {
        return encOpts;
    }

    public final void setEncodingOptions(ImageEncodingOptions encOpts) {
        if (encOpts == null)
            throw new NullPointerException();
        this.encOpts = encOpts;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void writeCommand(Attributes cmd) throws IOException {
        if (explicitVR || bigEndian)
            throw new IllegalStateException("explicitVR=" + explicitVR + ", bigEndian=" + bigEndian);
        cmd.writeGroupTo(this, Tag.CommandGroupLength);
    }

    public void writeFileMetaInformation(Attributes fmi) throws IOException {
        if (!explicitVR || bigEndian || countingOutputStream != null)
            throw new IllegalStateException("explicitVR=" + explicitVR + ", bigEndian=" + bigEndian + ", deflated="
                    + (countingOutputStream != null));
        write(preamble);
        write(DICM);
        fmi.writeGroupTo(this, Tag.FileMetaInformationGroupLength);
    }

    public void writeDataset(Attributes fmi, Attributes dataset) throws IOException {
        if (fmi != null) {
            writeFileMetaInformation(fmi);
            switchTransferSyntax(fmi.getString(Tag.TransferSyntaxUID, null));
        }
        if (dataset.bigEndian() != bigEndian || encOpts.groupLength || !encOpts.undefSequenceLength
                || !encOpts.undefItemLength)
            dataset = new Attributes(dataset, bigEndian);
        if (encOpts.groupLength)
            dataset.calcLength(encOpts, explicitVR);
        dataset.writeTo(this);
    }

    public void switchTransferSyntax(String tsuid) {
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndian.uid);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian.uid);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian.uid) || tsuid.equals(UID.JPIPReferencedDeflate.uid)
                || tsuid.equals(UID.JPIPHTJ2KReferencedDeflate.uid)) {
            this.countingOutputStream = new CountingOutputStream(super.out);
            super.out = new DeflaterOutputStream(countingOutputStream,
                    deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        }
    }

    public void writeHeader(int tag, VR vr, int len) throws IOException {
        byte[] b = buf;
        ByteKit.tagToBytes(tag, b, 0, bigEndian);
        int headerLen;
        if (!Tag.isItem(tag) && explicitVR) {
            if ((len & 0xffff0000) != 0 && vr.headerLength() == 8)
                vr = VR.UN;
            ByteKit.shortToBytesBE(vr.code(), b, 4);
            if ((headerLen = vr.headerLength()) == 8) {
                ByteKit.shortToBytes(len, b, 6, bigEndian);
            } else {
                b[6] = b[7] = 0;
                ByteKit.intToBytes(len, b, 8, bigEndian);
            }
        } else {
            ByteKit.intToBytes(len, b, 4, bigEndian);
            headerLen = 8;
        }
        out.write(b, 0, headerLen);
    }

    public void writeAttribute(int tag, VR vr, Object value, SpecificCharacterSet cs) throws IOException {
        if (value instanceof Value)
            writeAttribute(tag, vr, (Value) value);
        else
            writeAttribute(tag, vr, (value instanceof byte[]) ? (byte[]) value : vr.toBytes(value, cs));
    }

    public void writeAttribute(int tag, VR vr, byte[] val) throws IOException {
        int padlen = val.length & 1;
        writeHeader(tag, vr, val.length + padlen);
        out.write(val);
        if (padlen > 0)
            out.write(vr.paddingByte());
    }

    public void writeAttribute(int tag, VR vr, Value val) throws IOException {
        if (val instanceof BulkData && super.out instanceof ObjectOutputStream) {
            writeHeader(tag, vr, BulkData.MAGIC_LEN);
            ((ObjectOutputStream) super.out).writeObject(val);
        } else {
            int length = val.getEncodedLength(encOpts, explicitVR, vr);
            writeHeader(tag, vr, length);
            val.writeTo(this, vr);
            if (length == -1)
                writeHeader(Tag.SequenceDelimitationItem, null, 0);
        }
    }

    public void writeGroupLength(int tag, int len) throws IOException {
        byte[] b = buf;
        ByteKit.tagToBytes(tag, b, 0, bigEndian);
        if (explicitVR) {
            ByteKit.shortToBytesBE(VR.UL.code(), b, 4);
            ByteKit.shortToBytes(4, b, 6, bigEndian);
        } else {
            ByteKit.intToBytes(4, b, 4, bigEndian);
        }
        ByteKit.intToBytes(len, b, 8, bigEndian);
        out.write(b, 0, 12);
    }

    public void finish() throws IOException {
        if (countingOutputStream != null) {
            ((DeflaterOutputStream) out).finish();
            if ((countingOutputStream.getCount() & 1) != 0)
                countingOutputStream.write(0);
        }
    }

    public void close() throws IOException {
        try {
            finish();
        } catch (IOException ignored) {
        }
        if (deflater != null) {
            deflater.end();
        }
        super.close();
    }

}
