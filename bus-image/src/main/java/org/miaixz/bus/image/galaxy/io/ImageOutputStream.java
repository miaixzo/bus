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
package org.miaixz.bus.image.galaxy.io;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.CountingOutputStream;
import org.miaixz.bus.image.galaxy.data.*;

/**
 * Represents the ImageOutputStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageOutputStream extends FilterOutputStream {

    /**
     * The dicm value.
     */
    private static final byte[] DICM = { 'D', 'I', 'C', 'M' };

    /**
     * The buf value.
     */
    private final byte[] buf = new byte[12];

    /**
     * The preamble value.
     */
    private byte[] preamble = new byte[Normal._128];

    /**
     * The explicit vr value.
     */
    private boolean explicitVR;

    /**
     * The big endian value.
     */
    private boolean bigEndian;

    /**
     * The counting output stream value.
     */
    private CountingOutputStream countingOutputStream;

    /**
     * The enc opts value.
     */
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;

    /**
     * The deflater value.
     */
    private Deflater deflater;

    /**
     * Creates a new instance.
     *
     * @param out   the out.
     * @param tsuid the tsuid.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageOutputStream(OutputStream out, String tsuid) throws IOException {
        super(out);
        switchTransferSyntax(tsuid);
    }

    /**
     * Creates a new instance.
     *
     * @param file the file.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageOutputStream(File file) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(file)), UID.ExplicitVRLittleEndian.uid);
    }

    /**
     * Sets the preamble.
     *
     * @param preamble the preamble.
     */
    public final void setPreamble(byte[] preamble) {
        if (preamble.length != Normal._128)
            throw new IllegalArgumentException("preamble.length=" + preamble.length);
        this.preamble = preamble.clone();
    }

    /**
     * Determines whether explicit vr.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isExplicitVR() {
        return explicitVR;
    }

    /**
     * Determines whether big endian.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isBigEndian() {
        return bigEndian;
    }

    /**
     * Gets the encoding options.
     *
     * @return the encoding options.
     */
    public final ImageEncodingOptions getEncodingOptions() {
        return encOpts;
    }

    /**
     * Sets the encoding options.
     *
     * @param encOpts the enc opts.
     */
    public final void setEncodingOptions(ImageEncodingOptions encOpts) {
        if (encOpts == null)
            throw new NullPointerException();
        this.encOpts = encOpts;
    }

    /**
     * Executes the write operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    /**
     * Writes the command.
     *
     * @param cmd the cmd.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeCommand(Attributes cmd) throws IOException {
        if (explicitVR || bigEndian)
            throw new IllegalStateException("explicitVR=" + explicitVR + ", bigEndian=" + bigEndian);
        cmd.writeGroupTo(this, Tag.CommandGroupLength);
    }

    /**
     * Writes the file meta information.
     *
     * @param fmi the fmi.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeFileMetaInformation(Attributes fmi) throws IOException {
        if (!explicitVR || bigEndian || countingOutputStream != null)
            throw new IllegalStateException("explicitVR=" + explicitVR + ", bigEndian=" + bigEndian + ", deflated="
                    + (countingOutputStream != null));
        write(preamble);
        write(DICM);
        fmi.writeGroupTo(this, Tag.FileMetaInformationGroupLength);
    }

    /**
     * Writes the dataset.
     *
     * @param fmi     the fmi.
     * @param dataset the dataset.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Executes the switch transfer syntax operation.
     *
     * @param tsuid the tsuid.
     */
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

    /**
     * Writes the header.
     *
     * @param tag the tag.
     * @param vr  the vr.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Writes the attribute.
     *
     * @param tag   the tag.
     * @param vr    the vr.
     * @param value the value.
     * @param cs    the cs.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeAttribute(int tag, VR vr, Object value, SpecificCharacterSet cs) throws IOException {
        if (value instanceof Value)
            writeAttribute(tag, vr, (Value) value);
        else
            writeAttribute(tag, vr, (value instanceof byte[]) ? (byte[]) value : vr.toBytes(value, cs));
    }

    /**
     * Writes the attribute.
     *
     * @param tag the tag.
     * @param vr  the vr.
     * @param val the val.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeAttribute(int tag, VR vr, byte[] val) throws IOException {
        int padlen = val.length & 1;
        writeHeader(tag, vr, val.length + padlen);
        out.write(val);
        if (padlen > 0)
            out.write(vr.paddingByte());
    }

    /**
     * Writes the attribute.
     *
     * @param tag the tag.
     * @param vr  the vr.
     * @param val the val.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Writes the group length.
     *
     * @param tag the tag.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Executes the finish operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void finish() throws IOException {
        if (countingOutputStream != null) {
            ((DeflaterOutputStream) out).finish();
            if ((countingOutputStream.getCount() & 1) != 0)
                countingOutputStream.write(0);
        }
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
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
