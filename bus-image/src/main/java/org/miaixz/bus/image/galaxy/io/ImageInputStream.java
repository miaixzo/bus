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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.logger.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageInputStream extends FilterInputStream
        implements ImageInputHandler, BulkDataCreator {

    private static final String UNEXPECTED_NON_ZERO_ITEM_LENGTH =
            "Unexpected item value of {} #{} @ {}";
    private static final String UNEXPECTED_ATTRIBUTE =
            "Unexpected attribute {} #{} @ {}";
    private static final String MISSING_TRANSFER_SYNTAX =
            "Missing Transfer Syntax (0002,0010) - assume Explicit VR Little Endian";
    private static final String MISSING_FMI_LENGTH =
            "Missing or wrong File Meta Information Group Length (0002,0000)";
    private static final String NOT_A_DICOM_STREAM =
            "Not a DICOM Stream";
    private static final String IMPLICIT_VR_BIG_ENDIAN =
            "Implicit VR Big Endian encoded DICOM Stream";
    private static final String DEFLATED_WITH_ZLIB_HEADER =
            "Deflated DICOM Stream with ZLIB Header";
    private static final int ZLIB_HEADER = 0x789c;
    private static final int DEF_ALLOCATE_LIMIT = 0x4000000; // 64MiB
    // Length of the buffer used for readFully(short[], int, int)
    private static final int BYTE_BUF_LENGTH = 8192;
    private final byte[] buffer = new byte[12];
    private final List<ItemPointer> itemPointers = new ArrayList<>(4);
    private byte[] byteBuf;
    private int allocateLimit = DEF_ALLOCATE_LIMIT;
    private String uri;
    private String tsuid;
    private byte[] preamble;
    private Attributes fileMetaInformation;
    private boolean hasfmi;
    private boolean bigEndian;
    private boolean explicitVR;
    private IncludeBulkData includeBulkData = IncludeBulkData.YES;
    private long pos;
    private long fmiEndPos = -1L;
    private long tagPos;
    private long markPos;
    private int tag;
    private VR vr;
    private int encodedVR;
    private int length;
    private ImageInputHandler handler = this;
    private BulkDataCreator bulkDataCreator = this;
    private BulkDataDescriptor bulkDataDescriptor = BulkDataDescriptor.DEFAULT;
    private boolean excludeBulkData;
    private boolean includeBulkDataURI;
    private boolean catBlkFiles = true;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private List<File> blkFiles;
    private String blkURI;
    private FileOutputStream blkOut;
    private long blkOutPos;

    public ImageInputStream(InputStream in, String tsuid) throws IOException {
        super(in);
        switchTransferSyntax(tsuid);
    }

    public ImageInputStream(InputStream in) throws IOException {
        super(in.markSupported() ? in : new BufferedInputStream(in));
        guessTransferSyntax();
    }

    public ImageInputStream(File file) throws IOException {
        super(new BufferedInputStream(new FileInputStream(file)));
        try {
            guessTransferSyntax();
        } catch (IOException e) {
            IoKit.close(in);
            throw e;
        }
        uri = file.toURI().toString();
    }

    public static String toAttributePath(List<ItemPointer> itemPointers, int tag) {
        StringBuilder sb = new StringBuilder();
        for (ItemPointer itemPointer : itemPointers) {
            sb.append(Symbol.C_SLASH).append(Tag.toHexString(itemPointer.sequenceTag))
                    .append(Symbol.C_SLASH).append(itemPointer.itemIndex);
        }
        sb.append(Symbol.C_SLASH).append(Tag.toHexString(tag));
        return sb.toString();
    }

    public final String getTransferSyntax() {
        return tsuid;
    }

    /**
     * Returns the limit of initial allocated memory for element values.
     * <p>
     * By default, the limit is set to 67108864 (64 MiB).
     *
     * @return Limit of initial allocated memory for value or -1 for no limit
     * @see #setAllocateLimit(int)
     */
    public final int getAllocateLimit() {
        return allocateLimit;
    }

    /**
     * Sets the limit of initial allocated memory for element values. If the
     * value length exceeds the limit, a byte array with the specified size is
     * allocated. If the array can filled with bytes read from this
     * DicomInputStream, the byte array is reallocated with
     * twice the previous length and filled again. That continues until
     * the twice of the previous length exceeds the actual value length. Then
     * the byte array is reallocated with actual value length and filled with
     * the remaining bytes for the value from this DicomInputStream.
     * <p>
     * The rational of the incrementing allocation of byte arrays is to avoid
     * OutOfMemoryErrors on parsing corrupted DICOM streams.
     * <p>
     * By default, the limit is set to 67108864 (64 MiB).
     *
     * @param allocateLimit limit of initial allocated memory or -1 for no limit
     */
    public final void setAllocateLimit(int allocateLimit) {
        this.allocateLimit = allocateLimit;
    }

    public final String getURI() {
        return uri;
    }

    public final void setURI(String uri) {
        this.uri = uri;
    }

    public final IncludeBulkData getIncludeBulkData() {
        return includeBulkData;
    }

    public final void setIncludeBulkData(IncludeBulkData includeBulkData) {
        if (null == includeBulkData)
            throw new NullPointerException();
        this.includeBulkData = includeBulkData;
    }

    public final BulkDataDescriptor getBulkDataDescriptor() {
        return bulkDataDescriptor;
    }

    public final void setBulkDataDescriptor(BulkDataDescriptor bulkDataDescriptor) {
        this.bulkDataDescriptor = bulkDataDescriptor;
    }

    public final String getBulkDataFilePrefix() {
        return blkFilePrefix;
    }

    public final void setBulkDataFilePrefix(String blkFilePrefix) {
        this.blkFilePrefix = blkFilePrefix;
    }

    public final String getBulkDataFileSuffix() {
        return blkFileSuffix;
    }

    public final void setBulkDataFileSuffix(String blkFileSuffix) {
        this.blkFileSuffix = blkFileSuffix;
    }

    public final File getBulkDataDirectory() {
        return blkDirectory;
    }

    public final void setBulkDataDirectory(File blkDirectory) {
        this.blkDirectory = blkDirectory;
    }

    public final boolean isConcatenateBulkDataFiles() {
        return catBlkFiles;
    }

    public final void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        this.catBlkFiles = catBlkFiles;
    }

    public final List<File> getBulkDataFiles() {
        if (null != blkFiles)
            return blkFiles;
        else
            return Collections.emptyList();
    }

    public final void setImageInputHandler(ImageInputHandler handler) {
        if (null == handler)
            throw new NullPointerException("handler");
        this.handler = handler;
    }

    public void setBulkDataCreator(BulkDataCreator bulkDataCreator) {
        if (null == bulkDataCreator)
            throw new NullPointerException("bulkDataCreator");
        this.bulkDataCreator = bulkDataCreator;
    }

    public final void setFileMetaInformationGroupLength(byte[] val) {
        fmiEndPos = pos + ByteKit.bytesToInt(val, 0, bigEndian);
    }

    public final byte[] getPreamble() {
        return preamble;
    }

    public Attributes getFileMetaInformation() throws IOException {
        readFileMetaInformation();
        return fileMetaInformation;
    }

    public final int level() {
        return itemPointers.size();
    }

    public final int tag() {
        return tag;
    }

    public final VR vr() {
        return vr;
    }

    public final int length() {
        return length;
    }

    public final long getPosition() {
        return pos;
    }

    public void setPosition(long pos) {
        this.pos = pos;
    }

    public long getTagPosition() {
        return tagPos;
    }

    public final boolean bigEndian() {
        return bigEndian;
    }

    public final boolean explicitVR() {
        return explicitVR;
    }

    public boolean isExcludeBulkData() {
        return excludeBulkData;
    }

    public boolean isIncludeBulkDataURI() {
        return includeBulkDataURI;
    }

    public String getAttributePath() {
        return toAttributePath(itemPointers, tag);
    }

    @Override
    public void close() throws IOException {
        IoKit.close(blkOut);
        super.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        markPos = pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        pos = markPos;
    }

    @Override
    public final int read() throws IOException {
        int read = super.read();
        if (read >= 0)
            pos++;
        return read;
    }

    @Override
    public final int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read > 0)
            pos += read;
        return read;
    }

    @Override
    public final int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public final long skip(long n) throws IOException {
        long skip = super.skip(n);
        pos += skip;
        return skip;
    }

    public void skipFully(long n) throws IOException {
        StreamKit.skipFully(this, n);
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        StreamKit.readFully(this, b, off, len);
    }

    public void readFully(short[] s, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                    ("off < 0 || len < 0 || off + len > s.length!");
        }

        if (null == byteBuf)
            byteBuf = new byte[BYTE_BUF_LENGTH];

        while (len > 0) {
            int nelts = Math.min(len, byteBuf.length / 2);
            readFully(byteBuf, 0, nelts * 2);
            ByteKit.bytesToShort(byteBuf, s, off, nelts, bigEndian);
            off += nelts;
            len -= nelts;
        }
    }

    public void readHeader() throws IOException {
        byte[] buf = buffer;
        tagPos = pos;
        readFully(buf, 0, 8);
        encodedVR = 0;
        switch (tag = ByteKit.bytesToTag(buf, 0, bigEndian)) {
            case Tag.Item:
            case Tag.ItemDelimitationItem:
            case Tag.SequenceDelimitationItem:
                vr = null;
                break;
            default:
                if (explicitVR) {
                    vr = VR.valueOf(encodedVR = ByteKit.bytesToVR(buf, 4));
                    if (vr.headerLength() == 8) {
                        length = ByteKit.bytesToUShort(buf, 6, bigEndian);
                        return;
                    }
                    readFully(buf, 4, 4);
                } else {
                    vr = VR.UN;
                }
        }
        length = ByteKit.bytesToInt(buf, 4, bigEndian);
    }

    public boolean readItemHeader() throws IOException {
        for (; ; ) {
            readHeader();
            if (tag == Tag.Item)
                return true;
            if (tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                return false;
            }
            skipAttribute(UNEXPECTED_ATTRIBUTE);
        }
    }

    public Attributes readCommand() throws IOException {
        if (bigEndian || explicitVR)
            throw new IllegalStateException(
                    "bigEndian=" + bigEndian + ", explicitVR=" + explicitVR);
        Attributes attrs = new Attributes(9);
        readAttributes(attrs, -1, -1);
        return attrs;
    }

    public Attributes readDataset(int len, int stopTag) throws IOException {
        handler.startDataset(this);
        readFileMetaInformation();
        Attributes attrs = new Attributes(bigEndian, Normal._64);
        readAttributes(attrs, len, stopTag);
        attrs.trimToSize();
        handler.endDataset(this);
        return attrs;
    }

    public Attributes readFileMetaInformation() throws IOException {
        if (!hasfmi)
            return null;  // No File Meta Information
        if (null != fileMetaInformation)
            return fileMetaInformation;  // already read

        Attributes attrs = new Attributes(bigEndian, 9);
        while (pos != fmiEndPos) {
            mark(12);
            readHeader();
            if (Tag.groupNumber(tag) != 2) {
                Logger.warn(MISSING_FMI_LENGTH);
                reset();
                break;
            }
            if (null != vr) {
                if (vr == VR.UN)
                    vr = ElementDictionary.getStandardElementDictionary()
                            .vrOf(tag);
                handler.readValue(this, attrs);
            } else
                skipAttribute(UNEXPECTED_ATTRIBUTE);
        }
        fileMetaInformation = attrs;

        String tsuid = attrs.getString(Tag.TransferSyntaxUID, null);
        if (null == tsuid) {
            Logger.warn(MISSING_TRANSFER_SYNTAX);
            tsuid = UID.ExplicitVRLittleEndian;
        }
        switchTransferSyntax(tsuid);
        return attrs;
    }

    public void readAttributes(Attributes attrs, int len, int stopTag)
            throws IOException {
        boolean undeflen = len == -1;
        boolean hasStopTag = stopTag != -1;
        long endPos = pos + (len & 0xffffffffL);
        while (undeflen || this.pos < endPos) {
            try {
                readHeader();
            } catch (EOFException e) {
                if (undeflen && pos == tagPos)
                    break;
                throw e;
            }
            if (hasStopTag && tag == stopTag)
                break;
            if (null != vr) {
                if (vr == VR.UN) {
                    vr = ElementDictionary.vrOf(tag,
                            attrs.getPrivateCreator(tag));
                    if (vr == VR.UN && length == -1)
                        vr = VR.SQ; // assumes UN with undefined length are SQ,
                    // will fail on UN fragments!
                }
                excludeBulkData = includeBulkData == IncludeBulkData.NO && isBulkData(attrs);
                includeBulkDataURI = len != 0 && vr != VR.SQ
                        && includeBulkData == IncludeBulkData.URI && isBulkData(attrs);
                handler.readValue(this, attrs);
            } else
                skipAttribute(UNEXPECTED_ATTRIBUTE);
        }
    }

    @Override
    public void readValue(ImageInputStream dis, Attributes attrs)
            throws IOException {
        checkIsThis(dis);
        if (excludeBulkData) {
            skipFully(length);
        } else if (length == 0) {
            attrs.setNull(tag, vr);
        } else if (vr == VR.SQ) {
            readSequence(length, attrs, tag);
        } else if (length == -1) {
            readFragments(attrs, tag, vr);
        } else if (length == Builder.MAGIC_LEN
                && super.in instanceof ObjectInputStream) {
            attrs.setValue(tag, vr, BulkData.deserializeFrom(
                    (ObjectInputStream) super.in));
        } else if (includeBulkDataURI) {
            attrs.setValue(tag, vr, bulkDataCreator.createBulkData(this));
        } else {
            byte[] b = readValue();
            if (!Tag.isGroupLength(tag)) {
                if (bigEndian != attrs.bigEndian())
                    vr.toggleEndian(b, false);
                attrs.setBytes(tag, vr, b);
            } else if (tag == Tag.FileMetaInformationGroupLength)
                setFileMetaInformationGroupLength(b);
        }
    }

    @Override
    public BulkData createBulkData(ImageInputStream dis) throws IOException {
        BulkData bulkData;
        if (null != uri && !(super.in instanceof InflaterInputStream)) {
            bulkData = new BulkData(uri, pos, length, bigEndian);
            skipFully(length);
        } else {
            if (null == blkOut) {
                File blkfile = File.createTempFile(blkFilePrefix,
                        blkFileSuffix, blkDirectory);
                if (null == blkFiles) {
                    blkFiles = new ArrayList<>();
                }
                blkFiles.add(blkfile);
                blkURI = blkfile.toURI().toString();
                blkOut = new FileOutputStream(blkfile);
                blkOutPos = 0L;
            }
            try {
                IoKit.copy(this, blkOut, length);
            } finally {
                if (!catBlkFiles) {
                    IoKit.close(blkOut);
                    blkOut = null;
                }
            }
            bulkData = new BulkData(blkURI, blkOutPos, length, bigEndian);
            blkOutPos += length;
        }
        return bulkData;
    }

    private boolean isBulkData(Attributes attrs) {
        return bulkDataDescriptor.isBulkData(itemPointers,
                attrs.getPrivateCreator(tag), tag, vr, length);
    }

    @Override
    public void readValue(ImageInputStream dis, Sequence seq)
            throws IOException {
        checkIsThis(dis);
        if (length == 0) {
            seq.add(new Attributes(seq.getParent().bigEndian(), 0));
            return;
        }
        Attributes attrs = new Attributes(seq.getParent().bigEndian());
        seq.add(attrs);
        readAttributes(attrs, length, Tag.ItemDelimitationItem);
        attrs.trimToSize();
    }

    @Override
    public void readValue(ImageInputStream dis, Fragments frags)
            throws IOException {
        checkIsThis(dis);
        if (excludeBulkData) {
            skipFully(length);
        } else if (length == 0) {
            frags.add(Normal.EMPTY);
        } else if (length == Builder.MAGIC_LEN
                && super.in instanceof ObjectInputStream) {
            frags.add(BulkData.deserializeFrom((ObjectInputStream) super.in));
        } else if (includeBulkDataURI) {
            frags.add(bulkDataCreator.createBulkData(this));
        } else {
            byte[] b = readValue();
            if (bigEndian != frags.bigEndian())
                frags.vr().toggleEndian(b, false);
            frags.add(b);
        }
    }

    @Override
    public void startDataset(ImageInputStream dis) {
    }

    @Override
    public void endDataset(ImageInputStream dis) {
    }

    private void checkIsThis(ImageInputStream dis) {
        if (dis != this)
            throw new IllegalArgumentException("dis != this");
    }

    private void skipAttribute(String message) throws IOException {
        Logger.warn(message,
                Tag.toString(tag), length, tagPos);
        skipFully(length);
    }

    private void readSequence(int len, Attributes attrs, int sqtag)
            throws IOException {
        if (len == 0) {
            attrs.setNull(sqtag, VR.SQ);
            return;
        }
        Sequence seq = attrs.newSequence(sqtag, 10);
        String privateCreator = attrs.getPrivateCreator(sqtag);
        boolean undefLen = len == -1;
        long endPos = pos + (len & 0xffffffffL);
        boolean explicitVR0 = explicitVR;
        boolean bigEndian0 = bigEndian;
        if (encodedVR == 0x554e // UN
                && !probeExplicitVR()) {
            explicitVR = false;
            bigEndian = false;
        }
        for (int i = 0; (undefLen || pos < endPos) && readItemHeader(); ++i) {
            addItemPointer(sqtag, privateCreator, i);
            handler.readValue(this, seq);
            removeItemPointer();
        }
        explicitVR = explicitVR0;
        bigEndian = bigEndian0;
        if (seq.isEmpty())
            attrs.setNull(sqtag, VR.SQ);
        else
            seq.trimToSize();
    }

    private boolean probeExplicitVR() throws IOException {
        byte[] buf = new byte[14];
        if (in.markSupported()) {
            in.mark(14);
            in.read(buf);
            in.reset();
        } else {
            if (!(in instanceof PushbackInputStream))
                in = new PushbackInputStream(in, 14);
            int len = in.read(buf);
            ((PushbackInputStream) in).unread(buf, 0, len);
        }
        switch (ByteKit.bytesToVR(buf, 12)) {
            case 0x4145: // AE
            case 0x4153: // AS
            case 0x4154: // AT
            case 0x4353: // CS
            case 0x4441: // DA
            case 0x4453: // DS
            case 0x4454: // DT
            case 0x4644: // FD
            case 0x464c: // FL
            case 0x4953: // IS
            case 0x4c4f: // LO
            case 0x4c54: // LT
            case 0x4f42: // OB
            case 0x4f44: // OD
            case 0x4f46: // OF
            case 0x4f4c: // OL
            case 0x4f57: // OW
            case 0x504e: // PN
            case 0x5348: // SH
            case 0x534c: // SL
            case 0x5351: // SQ
            case 0x5353: // SS
            case 0x5354: // ST
            case 0x544d: // TM
            case 0x5543: // UC
            case 0x5549: // UI
            case 0x554c: // UL
            case 0x554e: // UN
            case 0x5552: // UR
            case 0x5553: // US
            case 0x5554: // UT
                return true;
        }
        return false;
    }

    private void addItemPointer(int sqtag, String privateCreator, int itemIndex) {
        itemPointers.add(new ItemPointer(sqtag, privateCreator, itemIndex));
    }

    private void removeItemPointer() {
        itemPointers.remove(itemPointers.size() - 1);
    }

    public Attributes readItem() throws IOException {
        readHeader();
        if (tag != Tag.Item)
            throw new IOException("Unexpected attribute "
                    + Tag.toString(tag) + " #" + length + " @ " + pos);
        Attributes attrs = new Attributes(bigEndian);
        attrs.setItemPosition(tagPos);
        readAttributes(attrs, length, Tag.ItemDelimitationItem);
        attrs.trimToSize();
        return attrs;
    }

    private void readFragments(Attributes attrs, int fragsTag, VR vr)
            throws IOException {
        Fragments frags = new Fragments(vr, attrs.bigEndian(), 10);
        String privateCreator = attrs.getPrivateCreator(fragsTag);
        for (int i = 0; readItemHeader(); ++i) {
            addItemPointer(fragsTag, privateCreator, i);
            handler.readValue(this, frags);
            removeItemPointer();
        }
        if (frags.isEmpty())
            attrs.setNull(fragsTag, vr);
        else {
            frags.trimToSize();
            attrs.setValue(fragsTag, vr, frags);
        }
    }

    public byte[] readValue() throws IOException {
        int valLen = length;
        try {
            if (valLen < 0)
                throw new EOFException(); // assume InputStream length < 2 GiB
            int allocLen = allocateLimit >= 0
                    ? Math.min(valLen, allocateLimit)
                    : valLen;
            byte[] value = new byte[allocLen];
            readFully(value, 0, allocLen);
            while (allocLen < valLen) {
                int newLength = Math.min(valLen, allocLen << 1);
                value = Arrays.copyOf(value, newLength);
                readFully(value, allocLen, newLength - allocLen);
                allocLen = newLength;
            }
            return value;
        } catch (IOException e) {
            Logger.warn("IOException during read of {} #{} @ {}",
                    Tag.toString(tag), length, tagPos, e);
            throw e;
        }
    }

    private void switchTransferSyntax(String tsuid) throws IOException {
        this.tsuid = tsuid;
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndianRetired);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian)
                || tsuid.equals(UID.JPIPReferencedDeflate)) {
            if (hasZLIBHeader()) {
                Logger.warn(DEFLATED_WITH_ZLIB_HEADER);
                super.in = new InflaterInputStream(super.in);
            } else
                super.in = new InflaterInputStream(super.in,
                        new Inflater(true));
        }
    }

    private boolean hasZLIBHeader() throws IOException {
        if (!markSupported())
            return false;
        byte[] buf = buffer;
        mark(2);
        read(buf, 0, 2);
        reset();
        return ByteKit.bytesToUShortBE(buf, 0) == ZLIB_HEADER;
    }

    private void guessTransferSyntax() throws IOException {
        byte[] b132 = new byte[132];
        mark(132);
        int rlen = StreamKit.readAvailable(this, b132, 0, 132);
        if (rlen == 132) {
            if (b132[Normal._128] == 'D' && b132[129] == 'I' && b132[130] == 'C' && b132[131] == 'M') {
                preamble = new byte[Normal._128];
                System.arraycopy(b132, 0, preamble, 0, Normal._128);
                if (!markSupported()) {
                    hasfmi = true;
                    tsuid = UID.ExplicitVRLittleEndian;
                    bigEndian = false;
                    explicitVR = true;
                    return;
                }
                mark(132);
                rlen = StreamKit.readAvailable(this, b132, 0, 132);
            }
        }
        if (rlen < 8
                || !guessTransferSyntax(b132, rlen, false)
                && !guessTransferSyntax(b132, rlen, true))
            throw new InternalException(NOT_A_DICOM_STREAM);
        reset();
        hasfmi = Tag.isFileMetaInformation(
                ByteKit.bytesToTag(b132, 0, bigEndian));
    }

    private boolean guessTransferSyntax(byte[] b128, int rlen, boolean bigEndian)
            throws InternalException {
        int tag1 = ByteKit.bytesToTag(b128, 0, bigEndian);
        VR vr = ElementDictionary.vrOf(tag1, null);
        if (vr == VR.UN)
            return false;
        if (ByteKit.bytesToVR(b128, 4) == vr.code()) {
            this.tsuid = bigEndian ? UID.ExplicitVRBigEndianRetired
                    : UID.ExplicitVRLittleEndian;
            this.bigEndian = bigEndian;
            this.explicitVR = true;
            return true;
        }
        int len = ByteKit.bytesToInt(b128, 4, bigEndian);
        if (len < 0 || 8 + len > rlen)
            return false;

        if (bigEndian)
            throw new InternalException(IMPLICIT_VR_BIG_ENDIAN);

        this.tsuid = UID.ImplicitVRLittleEndian;
        this.bigEndian = false;
        this.explicitVR = false;
        return true;
    }

    public enum IncludeBulkData {NO, YES, URI}

}
