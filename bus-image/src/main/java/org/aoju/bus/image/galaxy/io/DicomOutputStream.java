/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.galaxy.io;

import org.aoju.bus.core.utils.ByteUtils;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.data.*;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class DicomOutputStream extends FilterOutputStream {

    private static final byte[] DICM = {'D', 'I', 'C', 'M'};
    private final byte[] buf = new byte[12];
    private byte[] preamble = new byte[128];
    private boolean explicitVR;
    private boolean bigEndian;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;

    public DicomOutputStream(OutputStream out, String tsuid)
            throws IOException {
        super(out);
        switchTransferSyntax(tsuid);
    }

    public DicomOutputStream(File file) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(file)),
                UID.ExplicitVRLittleEndian);
    }

    public final void setPreamble(byte[] preamble) {
        if (preamble.length != 128)
            throw new IllegalArgumentException(
                    "preamble.length=" + preamble.length);
        this.preamble = preamble.clone();
    }

    public final boolean isExplicitVR() {
        return explicitVR;
    }

    public final boolean isBigEndian() {
        return bigEndian;
    }

    public final DicomEncodingOptions getEncodingOptions() {
        return encOpts;
    }

    public final void setEncodingOptions(DicomEncodingOptions encOpts) {
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
            throw new IllegalStateException("explicitVR=" + explicitVR
                    + ", bigEndian=" + bigEndian);
        cmd.writeGroupTo(this, Tag.CommandGroupLength);
    }

    public void writeFileMetaInformation(Attributes fmi) throws IOException {
        if (!explicitVR || bigEndian)
            throw new IllegalStateException("explicitVR=" + explicitVR
                    + ", bigEndian=" + bigEndian);
        write(preamble);
        write(DICM);
        fmi.writeGroupTo(this, Tag.FileMetaInformationGroupLength);
    }

    public void writeDataset(Attributes fmi, Attributes dataset)
            throws IOException {
        if (fmi != null) {
            writeFileMetaInformation(fmi);
            switchTransferSyntax(fmi.getString(Tag.TransferSyntaxUID, null));
        }
        if (dataset.bigEndian() != bigEndian
                || encOpts.groupLength
                || !encOpts.undefSequenceLength
                || !encOpts.undefItemLength)
            dataset = new Attributes(dataset, bigEndian);
        if (encOpts.groupLength)
            dataset.calcLength(encOpts, explicitVR);
        dataset.writeTo(this);
    }

    private void switchTransferSyntax(String tsuid) {
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndianRetired);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian)
                || tsuid.equals(UID.JPIPReferencedDeflate)) {
            super.out = new DeflaterOutputStream(super.out,
                    new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        }
    }

    public void writeHeader(int tag, VR vr, int len) throws IOException {
        byte[] b = buf;
        ByteUtils.tagToBytes(tag, b, 0, bigEndian);
        int headerLen;
        if (!Tag.isItem(tag) && explicitVR) {
            if ((len & 0xffff0000) != 0 && vr.headerLength() == 8)
                vr = VR.UN;
            ByteUtils.shortToBytesBE(vr.code(), b, 4);
            if ((headerLen = vr.headerLength()) == 8) {
                ByteUtils.shortToBytes(len, b, 6, bigEndian);
            } else {
                b[6] = b[7] = 0;
                ByteUtils.intToBytes(len, b, 8, bigEndian);
            }
        } else {
            ByteUtils.intToBytes(len, b, 4, bigEndian);
            headerLen = 8;
        }
        out.write(b, 0, headerLen);
    }


    public void writeAttribute(int tag, VR vr, Object value,
                               SpecificCharacterSet cs) throws IOException {
        if (value instanceof Value)
            writeAttribute(tag, vr, (Value) value);
        else
            writeAttribute(tag, vr,
                    (value instanceof byte[])
                            ? (byte[]) value
                            : vr.toBytes(value, cs));
    }

    public void writeAttribute(int tag, VR vr, byte[] val) throws IOException {
        int padlen = val.length & 1;
        writeHeader(tag, vr, val.length + padlen);
        out.write(val);
        if (padlen > 0)
            out.write(vr.paddingByte());
    }

    public void writeAttribute(int tag, VR vr, Value val) throws IOException {
        if (val instanceof BulkData
                && super.out instanceof ObjectOutputStream) {
            writeHeader(tag, vr, BulkData.MAGIC_LEN);
            ((BulkData) val).serializeTo((ObjectOutputStream) super.out);
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
        ByteUtils.tagToBytes(tag, b, 0, bigEndian);
        if (explicitVR) {
            ByteUtils.shortToBytesBE(VR.UL.code(), b, 4);
            ByteUtils.shortToBytes(4, b, 6, bigEndian);
        } else {
            ByteUtils.intToBytes(4, b, 4, bigEndian);
        }
        ByteUtils.intToBytes(len, b, 8, bigEndian);
        out.write(b, 0, 12);
    }

    public void finish() throws IOException {
        if (out instanceof DeflaterOutputStream) {
            ((DeflaterOutputStream) out).finish();
        }
    }

    public void close() throws IOException {
        try {
            finish();
        } catch (IOException ignored) {
        }
        super.close();
    }

}
