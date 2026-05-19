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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.LimitedInputStream;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.logger.Logger;

/**
 * A specialized input stream for reading DICOM image data. This class extends {@link FilterInputStream} to provide
 * DICOM-specific reading capabilities, handling various DICOM encoding formats including Explicit VR Little Endian,
 * Implicit VR, and deflated streams. It implements {@link ImageInputHandler} and {@link BulkDataCreator} for
 * comprehensive DICOM data processing.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageInputStream extends FilterInputStream implements ImageInputHandler, BulkDataCreator {

    /**
     * The value too large value.
     */
    private static final String VALUE_TOO_LARGE = "tag value too large, must be less than 2Gib";

    /**
     * The unexpected non zero item length value.
     */
    private static final String UNEXPECTED_NON_ZERO_ITEM_LENGTH = "Unexpected item value of {} #{} @ {} during {}";

    /**
     * The unexpected attribute value.
     */
    private static final String UNEXPECTED_ATTRIBUTE = "Unexpected attribute {} #{} @ {} during {}";

    /**
     * The missing transfer syntax value.
     */
    private static final String MISSING_TRANSFER_SYNTAX = "Missing Transfer Syntax (0002,0010) - assume Explicit VR Little Endian";

    /**
     * The missing fmi length value.
     */
    private static final String MISSING_FMI_LENGTH = "Missing or wrong File Meta Information Group Length (0002,0000)";

    /**
     * The not a dicom stream value.
     */
    private static final String NOT_A_DICOM_STREAM = "Not a DICOM Stream";

    /**
     * The implicit vr big endian value.
     */
    private static final String IMPLICIT_VR_BIG_ENDIAN = "Implicit VR Big Endian encoded DICOM Stream";

    /**
     * The deflated with zlib header value.
     */
    private static final String DEFLATED_WITH_ZLIB_HEADER = "Deflated DICOM Stream with ZLIB Header";

    /**
     * The sequence exceed encoded length value.
     */
    private static final String SEQUENCE_EXCEED_ENCODED_LENGTH = "Actual length of Sequence %s exceeds encoded length: %d";

    /**
     * The treat sq as un value.
     */
    private static final String TREAT_SQ_AS_UN = "Actual length of Sequence {} exceeds encoded length: {} - treat as UN";

    /**
     * The treat sq as un max exceed length value.
     */
    private static final int TREAT_SQ_AS_UN_MAX_EXCEED_LENGTH = 1024;

    /**
     * The zlib header value.
     */
    private static final int ZLIB_HEADER = 0x789c;

    /**
     * The def allocate limit value.
     */
    private static final int DEF_ALLOCATE_LIMIT = 0x4000000; // 64MiB
    /**
     * The default preamble length value.
     */
    private static final int DEFAULT_PREAMBLE_LENGTH = 128;

    /**
     * The undefined length value.
     */
    private static final int UNDEFINED_LENGTH = -1;
    // Length of the buffer used for readFully(short[], int, int)
    /**
     * The byte buf length value.
     */
    private static final int BYTE_BUF_LENGTH = 8192;

    /**
     * The buffer value.
     */
    private final byte[] buffer = new byte[12];

    /**
     * The item pointers value.
     */
    private final List<ItemPointer> itemPointers = new ArrayList<>(4);

    /**
     * The byte buf value.
     */
    private byte[] byteBuf;

    /**
     * The allocate limit value.
     */
    private int allocateLimit = DEF_ALLOCATE_LIMIT;

    /**
     * The uri value.
     */
    private String uri;

    /**
     * The tsuid value.
     */
    private String tsuid;

    /**
     * The preamble value.
     */
    private byte[] preamble;

    /**
     * The file meta information value.
     */
    private Attributes fileMetaInformation;

    /**
     * The hasfmi value.
     */
    private boolean hasfmi;

    /**
     * The big endian value.
     */
    private boolean bigEndian;

    /**
     * The explicit vr value.
     */
    private boolean explicitVR;

    /**
     * The include bulk data value.
     */
    private IncludeBulkData includeBulkData = IncludeBulkData.YES;

    /**
     * The pos value.
     */
    private long pos;

    /**
     * The fmi end pos value.
     */
    private long fmiEndPos = -1L;

    /**
     * The tag pos value.
     */
    private long tagPos;

    /**
     * The mark pos value.
     */
    private long markPos;

    /**
     * The tag value.
     */
    private int tag;

    /**
     * The vr value.
     */
    private VR vr;

    /**
     * The encoded vr value.
     */
    private int encodedVR;

    /**
     * The length value.
     */
    private long length;

    /**
     * The handler value.
     */
    private ImageInputHandler handler = this;

    /**
     * The bulk data creator value.
     */
    private BulkDataCreator bulkDataCreator = this;

    /**
     * The bulk data descriptor value.
     */
    private BulkDataDescriptor bulkDataDescriptor = BulkDataDescriptor.DEFAULT;

    /**
     * The exclude bulk data value.
     */
    private boolean excludeBulkData;

    /**
     * The include bulk data uri value.
     */
    private boolean includeBulkDataURI;

    /**
     * The cat blk files value.
     */
    private boolean catBlkFiles = true;

    /**
     * The blk file prefix value.
     */
    private String blkFilePrefix = "blk";

    /**
     * The blk file suffix value.
     */
    private String blkFileSuffix;

    /**
     * The blk directory value.
     */
    private File blkDirectory;

    /**
     * The blk files value.
     */
    private List<File> blkFiles;

    /**
     * The blk uri value.
     */
    private String blkURI;

    /**
     * The blk out value.
     */
    private FileOutputStream blkOut;

    /**
     * The blk out pos value.
     */
    private long blkOutPos;

    /**
     * The inflater value.
     */
    private Inflater inflater;

    /**
     * Creates a new instance.
     *
     * @param in    the in.
     * @param tsuid the tsuid.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageInputStream(InputStream in, String tsuid) throws IOException {
        super(in);
        switchTransferSyntax(tsuid);
    }

    /**
     * Creates a new instance.
     *
     * @param in the in.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageInputStream(InputStream in) throws IOException {
        this(in, DEFAULT_PREAMBLE_LENGTH);
    }

    /**
     * Creates a new instance.
     *
     * @param in             the in.
     * @param preambleLength the preamble length.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageInputStream(InputStream in, int preambleLength) throws IOException {
        super(ensureMarkSupported(in));
        guessTransferSyntax(preambleLength);
    }

    /**
     * Creates a new instance.
     *
     * @param file the file.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageInputStream(File file) throws IOException {
        super(new BufferedInputStream(new FileInputStream(file)));
        try {
            guessTransferSyntax(DEFAULT_PREAMBLE_LENGTH);
        } catch (IOException e) {
            IoKit.close(in);
            throw e;
        }
        uri = file.toURI().toString();
    }

    /**
     * Parses the un sequence.
     *
     * @param buf   the buf.
     * @param attrs the attrs.
     * @param sqtag the sqtag.
     * @throws IOException if the operation cannot be completed.
     */
    public static void parseUNSequence(byte[] buf, Attributes attrs, int sqtag) throws IOException {
        ImageInputStream dis = new ImageInputStream(new ByteArrayInputStream(buf),
                attrs.bigEndian() ? UID.ExplicitVRBigEndian.uid : UID.ExplicitVRLittleEndian.uid);
        dis.encodedVR = 0x554e;
        dis.readSequence(buf.length, attrs, sqtag);
    }

    /**
     * Create a new DicomInputStream for the given input stream, Transfer Syntax UID and read limit. It ensures to never
     * read more than the limit from the stream by wrapping it with a {@link LimitedInputStream}. The limit also helps
     * to avoid OutOfMemory errors on parsing corrupt DICOM streams without the need to create temporary arrays when
     * allocating large tag values. (See also {@link #setAllocateLimit}.)
     *
     * @param in    input stream to read data from
     * @param tsuid Transfer Syntax UID
     * @param limit limit in bytes
     * @return new DicomInputStream
     * @throws IOException if there is a problem reading from the given stream
     */
    public static ImageInputStream createWithLimit(InputStream in, String tsuid, long limit) throws IOException {
        return new ImageInputStream(limited(ensureMarkSupported(in), limit), tsuid);
    }

    /**
     * Create a new DicomInputStream for the given input stream and read limit. It ensures to never read more than the
     * limit from the stream by wrapping it with a {@link LimitedInputStream}. The limit also helps to avoid OutOfMemory
     * errors on parsing corrupt DICOM streams without the need to create temporary arrays when allocating large tag
     * values. (See also {@link #setAllocateLimit}.)
     *
     * @param in    input stream to read data from
     * @param limit limit in bytes
     * @return new DicomInputStream
     * @throws IOException if there is a problem reading from the given stream
     */
    public static ImageInputStream createWithLimit(InputStream in, long limit) throws IOException {
        return new ImageInputStream(limited(ensureMarkSupported(in), limit));
    }

    /**
     * Create a new DicomInputStream for the given file. A limit will be set by reading the length of the file (see also
     * #createWithLimit).
     *
     * @param file file to read
     * @return new DicomInputStream
     * @throws IOException if there is a problem reading from the given file
     */
    public static ImageInputStream createWithLimitFromFileLength(File file) throws IOException {
        long fileLength = file.length();
        // Some operating systems may return 0 length for pathnames denoting system-dependent entities such as devices
        // or pipes
        if (fileLength > 0) {
            InputStream in = limited(new BufferedInputStream(new FileInputStream(file)), fileLength);
            ImageInputStream dicomInputStream;
            try {
                dicomInputStream = new ImageInputStream(in);
            } catch (IOException e) {
                IoKit.close(in);
                throw e;
            }
            dicomInputStream.setURI(file.toURI().toString());
            return dicomInputStream;
        } else {
            return new ImageInputStream(file);
        }
    }

    /**
     * Executes the ensure mark supported operation.
     *
     * @param in the in.
     * @return the operation result.
     */
    private static InputStream ensureMarkSupported(InputStream in) {
        return in.markSupported() ? in : new BufferedInputStream(in);
    }

    /**
     * Executes the limited operation.
     *
     * @param in    the in.
     * @param limit the limit.
     * @return the operation result.
     */
    private static LimitedInputStream limited(InputStream in, long limit) {
        return new LimitedInputStream(in, limit, true);
    }

    /**
     * Converts this value to attribute path.
     *
     * @param itemPointers the item pointers.
     * @param tag          the tag.
     * @return the operation result.
     */
    public static String toAttributePath(List<ItemPointer> itemPointers, int tag) {
        StringBuilder sb = new StringBuilder();
        for (ItemPointer itemPointer : itemPointers) {
            sb.append(Symbol.C_SLASH).append(Tag.toHexString(itemPointer.sequenceTag)).append(Symbol.C_SLASH)
                    .append(itemPointer.itemIndex);
        }
        sb.append(Symbol.C_SLASH).append(Tag.toHexString(tag));
        return sb.toString();
    }

    /**
     * Converts this value to long or undefined.
     *
     * @param length the length.
     * @return the operation result.
     */
    static long toLongOrUndefined(int length) {
        return length == UNDEFINED_LENGTH ? length : length & 0xffffffffL;
    }

    /**
     * Executes the tag equal or greater operation.
     *
     * @param stopTag the stop tag.
     * @return the operation result.
     */
    private static Predicate<ImageInputStream> tagEqualOrGreater(int stopTag) {
        return stopTag != -1 ? o -> Integer.compareUnsigned(o.tag, stopTag) >= 0 : o -> false;
    }

    /**
     * Gets the transfer syntax.
     *
     * @return the transfer syntax.
     */
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
     * Sets the limit of initial allocated memory for element values. If the value length exceeds the limit, a byte
     * array with the specified size is allocated. If the array can filled with bytes read from this
     * <code>DicomInputStream</code>, the byte array is reallocated with twice the previous length and filled again.
     * That continues until the twice of the previous length exceeds the actual value length. Then the byte array is
     * reallocated with actual value length and filled with the remaining bytes for the value from this
     * <code>DicomInputStream</code>.
     * <p>
     * The rational of the incrementing allocation of byte arrays is to avoid OutOfMemoryErrors on parsing corrupted
     * DICOM streams.
     * <p>
     * By default, the limit is set to 67108864 (64 MiB).
     * <p>
     * Note: If a limit is given using {@link #createWithLimit} or {@link #createWithLimitFromFileLength} or by
     * supplying a {@link LimitedInputStream}, then this allocateLimit will be ignored (except for deflated data) and no
     * temporary arrays need to be created.
     *
     * @param allocateLimit limit of initial allocated memory or -1 for no limit
     */
    public final void setAllocateLimit(int allocateLimit) {
        if (!(allocateLimit > 0 || allocateLimit == -1))
            throw new IllegalArgumentException("allocateLimit must be a positive number or -1");

        this.allocateLimit = allocateLimit;
    }

    /**
     * Gets the uri.
     *
     * @return the uri.
     */
    public final String getURI() {
        return uri;
    }

    /**
     * Sets the uri.
     *
     * @param uri the uri.
     */
    public final void setURI(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the include bulk data.
     *
     * @return the include bulk data.
     */
    public final IncludeBulkData getIncludeBulkData() {
        return includeBulkData;
    }

    /**
     * Sets the include bulk data.
     *
     * @param includeBulkData the include bulk data.
     */
    public final void setIncludeBulkData(IncludeBulkData includeBulkData) {
        if (includeBulkData == null)
            throw new NullPointerException();
        this.includeBulkData = includeBulkData;
    }

    /**
     * Gets the bulk data descriptor.
     *
     * @return the bulk data descriptor.
     */
    public final BulkDataDescriptor getBulkDataDescriptor() {
        return bulkDataDescriptor;
    }

    /**
     * Sets the bulk data descriptor.
     *
     * @param bulkDataDescriptor the bulk data descriptor.
     */
    public final void setBulkDataDescriptor(BulkDataDescriptor bulkDataDescriptor) {
        this.bulkDataDescriptor = bulkDataDescriptor;
    }

    /**
     * Gets the bulk data file prefix.
     *
     * @return the bulk data file prefix.
     */
    public final String getBulkDataFilePrefix() {
        return blkFilePrefix;
    }

    /**
     * Sets the bulk data file prefix.
     *
     * @param blkFilePrefix the blk file prefix.
     */
    public final void setBulkDataFilePrefix(String blkFilePrefix) {
        this.blkFilePrefix = blkFilePrefix;
    }

    /**
     * Gets the bulk data file suffix.
     *
     * @return the bulk data file suffix.
     */
    public final String getBulkDataFileSuffix() {
        return blkFileSuffix;
    }

    /**
     * Sets the bulk data file suffix.
     *
     * @param blkFileSuffix the blk file suffix.
     */
    public final void setBulkDataFileSuffix(String blkFileSuffix) {
        this.blkFileSuffix = blkFileSuffix;
    }

    /**
     * Gets the bulk data directory.
     *
     * @return the bulk data directory.
     */
    public final File getBulkDataDirectory() {
        return blkDirectory;
    }

    /**
     * Sets the bulk data directory.
     *
     * @param blkDirectory the blk directory.
     */
    public final void setBulkDataDirectory(File blkDirectory) {
        this.blkDirectory = blkDirectory;
    }

    /**
     * Determines whether concatenate bulk data files.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isConcatenateBulkDataFiles() {
        return catBlkFiles;
    }

    /**
     * Sets the concatenate bulk data files.
     *
     * @param catBlkFiles the cat blk files.
     */
    public final void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        this.catBlkFiles = catBlkFiles;
    }

    /**
     * Gets the bulk data files.
     *
     * @return the bulk data files.
     */
    public final List<File> getBulkDataFiles() {
        if (blkFiles != null)
            return blkFiles;
        else
            return Collections.emptyList();
    }

    /**
     * Sets the dicom input handler.
     *
     * @param handler the handler.
     */
    public final void setDicomInputHandler(ImageInputHandler handler) {
        if (handler == null)
            throw new NullPointerException("handler");
        this.handler = handler;
    }

    /**
     * Set {@code DicomInputHandler} to parse Datasets without accumulating read attributes in {@code Attributes}.
     */
    public final void setSkipAllDicomInputHandler() {
        this.handler = new ImageInputHandler() {

            @Override
            public void readValue(ImageInputStream dis, Attributes attrs) throws IOException {
                if (dis.length() == -1) {
                    dis.skipSequence();
                } else {
                    long n = dis.unsignedLength();
                    StreamKit.skipFully(dis, n);
                }
            }

            @Override
            public void readValue(ImageInputStream dis, Sequence seq) throws IOException {
                dis.readValue(dis, seq);
            }

            @Override
            public void readValue(ImageInputStream dis, Fragments frags) throws IOException {
                long n = dis.unsignedLength();
                StreamKit.skipFully(dis, n);
            }

            @Override
            public void startDataset(ImageInputStream dis) {
            }

            @Override
            public void endDataset(ImageInputStream dis) {
            }
        };
    }

    /**
     * Sets the bulk data creator.
     *
     * @param bulkDataCreator the bulk data creator.
     */
    public void setBulkDataCreator(BulkDataCreator bulkDataCreator) {
        if (bulkDataCreator == null)
            throw new NullPointerException("bulkDataCreator");
        this.bulkDataCreator = bulkDataCreator;
    }

    /**
     * Sets the file meta information group length.
     *
     * @param val the val.
     */
    public final void setFileMetaInformationGroupLength(byte[] val) {
        fmiEndPos = pos + ByteKit.bytesToInt(val, 0, bigEndian);
    }

    /**
     * Gets the preamble.
     *
     * @return the preamble.
     */
    public final byte[] getPreamble() {
        return preamble;
    }

    /**
     * Gets the file meta information.
     *
     * @return the file meta information.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes getFileMetaInformation() throws IOException {
        readFileMetaInformation();
        return fileMetaInformation;
    }

    /**
     * Executes the level operation.
     *
     * @return the operation result.
     */
    public final int level() {
        return itemPointers.size();
    }

    /**
     * Executes the tag operation.
     *
     * @return the operation result.
     */
    public final int tag() {
        return tag;
    }

    /**
     * Executes the vr operation.
     *
     * @return the operation result.
     */
    public final VR vr() {
        return vr;
    }

    /**
     * Returns value length of last parsed data element header. May be negative for value length >= 2^31. -1 indicates
     * an Undefined Length.
     *
     * @return value length of last parsed data element header.
     */
    public final int length() {
        return (int) length;
    }

    /**
     * Returns value length of last parsed data element header. -1 indicates an Undefined Length.
     *
     * @return value length of last parsed data element header.
     */
    public long unsignedLength() {
        return length;
    }

    /**
     * Gets the position.
     *
     * @return the position.
     */
    public final long getPosition() {
        return pos;
    }

    /**
     * Sets the position.
     *
     * @param pos the pos.
     */
    public void setPosition(long pos) {
        this.pos = pos;
    }

    /**
     * Gets the tag position.
     *
     * @return the tag position.
     */
    public long getTagPosition() {
        return tagPos;
    }

    /**
     * Executes the big endian operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean bigEndian() {
        return bigEndian;
    }

    /**
     * Executes the explicit vr operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean explicitVR() {
        return explicitVR;
    }

    /**
     * Determines whether exclude bulk data.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isExcludeBulkData() {
        return excludeBulkData;
    }

    /**
     * Determines whether include bulk data uri.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isIncludeBulkDataURI() {
        return includeBulkDataURI;
    }

    /**
     * Gets the attribute path.
     *
     * @return the attribute path.
     */
    public String getAttributePath() {
        return toAttributePath(itemPointers, tag);
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void close() throws IOException {
        IoKit.close(blkOut);
        if (inflater != null) {
            inflater.end();
        }
        super.close();
    }

    /**
     * Executes the mark operation.
     *
     * @param readlimit the readlimit.
     */
    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        markPos = pos;
    }

    /**
     * Executes the reset operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        pos = markPos;
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public final int read() throws IOException {
        int read = super.read();
        if (read >= 0)
            pos++;
        return read;
    }

    /**
     * Executes the read operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public final int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read > 0)
            pos += read;
        return read;
    }

    /**
     * Executes the read operation.
     *
     * @param b the b.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public final int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Executes the skip operation.
     *
     * @param n the n.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public final long skip(long n) throws IOException {
        long skip = super.skip(n);
        pos += skip;
        return skip;
    }

    /**
     * Executes the skip fully operation.
     *
     * @param n the n.
     * @throws IOException if the operation cannot be completed.
     */
    public void skipFully(long n) throws IOException {
        StreamKit.skipFully(this, n);
    }

    /**
     * Reads the fully.
     *
     * @param b the b.
     * @throws IOException if the operation cannot be completed.
     */
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    /**
     * Reads the fully.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    public void readFully(byte[] b, int off, int len) throws IOException {
        StreamKit.readFully(this, b, off, len);
    }

    /**
     * Reads the fully.
     *
     * @param s   the s.
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    public void readFully(short[] s, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > s.length!");
        }

        if (byteBuf == null)
            byteBuf = new byte[BYTE_BUF_LENGTH];

        while (len > 0) {
            int nelts = Math.min(len, byteBuf.length / 2);
            readFully(byteBuf, 0, nelts * 2);
            ByteKit.bytesToShort(byteBuf, s, off, nelts, bigEndian);
            off += nelts;
            len -= nelts;
        }
    }

    /**
     * Executes the tag value too large exception operation.
     *
     * @return the operation result.
     */
    private IOException tagValueTooLargeException() {
        return new IOException(String.format("0x%s %s", Tag.toHexString(tag), VALUE_TOO_LARGE));
    }

    /**
     * Reads the header.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void readHeader() throws IOException {
        readHeader(dis -> false);
    }

    /**
     * Reads the header.
     *
     * @param stopPredicate the stop predicate.
     * @throws IOException if the operation cannot be completed.
     */
    public void readHeader(Predicate<ImageInputStream> stopPredicate) throws IOException {
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
                    if (vr == null) {
                        vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                        if (!stopPredicate.test(this))
                            Logger.warn(
                                    false,
                                    "Image",
                                    "Unrecognized VR code: vrCode={}H for {} - treat as {}",
                                    Tag.shortToHexString(encodedVR),
                                    Tag.toString(tag),
                                    vr);
                    }
                    if (vr.headerLength() == 8) {
                        // This length can't overflow since length field is only 16 bits in this case.
                        length = ByteKit.bytesToUShort(buf, 6, bigEndian);
                        return;
                    }
                    readFully(buf, 4, 4);
                } else {
                    vr = VR.UN;
                }
        }
        length = toLongOrUndefined(ByteKit.bytesToInt(buf, 4, bigEndian));
    }

    /**
     * Reads the item header.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    public boolean readItemHeader() throws IOException {
        String methodName = "readItemHeader()";
        for (;;) {
            readHeader();
            if (tag == Tag.Item)
                return true;
            if (tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH, methodName);
                return false;
            }
            skipAttribute(UNEXPECTED_ATTRIBUTE, methodName);
        }
    }

    /**
     * Reads the command.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readCommand() throws IOException {
        if (bigEndian || explicitVR)
            throw new IllegalStateException("bigEndian=" + bigEndian + ", explicitVR=" + explicitVR);
        Attributes attrs = new Attributes(9);
        readAllAttributes(attrs);
        return attrs;
    }

    /**
     * Reads the all attributes.
     *
     * @param attrs the attrs.
     * @throws IOException if the operation cannot be completed.
     */
    public void readAllAttributes(Attributes attrs) throws IOException {
        readAttributes(attrs, UNDEFINED_LENGTH, o -> false);
    }

    /**
     * Reads the dataset.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readDataset() throws IOException {
        return readDataset(o -> false);
    }

    /**
     * Reads the dataset until pixel data.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readDatasetUntilPixelData() throws IOException {
        return readDataset(o -> o.tag == Tag.PixelData);
    }

    /**
     * Use one of the other {@link #readDataset()} methods instead. If you want to specify a length limit, you may
     * supply a {@link LimitedInputStream} or use {@link #createWithLimit} or {@link #createWithLimitFromFileLength}.
     *
     * @param len     the encoded length limit
     * @param stopTag the tag at which reading should stop
     * @return the read dataset attributes
     * @throws IOException if the operation cannot be completed
     */
    public Attributes readDataset(int len, int stopTag) throws IOException {
        return readDataset(len, tagEqualOrGreater(stopTag));
    }

    /**
     * Reads the dataset.
     *
     * @param stopTag the stop tag.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readDataset(int stopTag) throws IOException {
        return readDataset(tagEqualOrGreater(stopTag));
    }

    /**
     * Reads the dataset.
     *
     * @param stopPredicate the stop predicate.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readDataset(Predicate<ImageInputStream> stopPredicate) throws IOException {
        return readDataset(UNDEFINED_LENGTH, stopPredicate);
    }

    /**
     * Use one of the other {@link #readDataset()} methods instead. If you want to specify a length limit, you may
     * supply a {@link LimitedInputStream} or use {@link #createWithLimit} or {@link #createWithLimitFromFileLength}.
     *
     * @param len           the encoded length limit
     * @param stopPredicate the predicate that decides when reading should stop
     * @return the read dataset attributes
     * @throws IOException if the operation cannot be completed
     */
    public Attributes readDataset(long len, Predicate<ImageInputStream> stopPredicate) throws IOException {
        handler.startDataset(this);
        readFileMetaInformation();
        Attributes attrs = new Attributes(bigEndian, 64);
        readAttributes(attrs, len, stopPredicate);
        attrs.trimToSize();
        handler.endDataset(this);
        return attrs;
    }

    /**
     * Reads the file meta information.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readFileMetaInformation() throws IOException {
        if (!hasfmi)
            return null; // No File Meta Information
        if (fileMetaInformation != null)
            return fileMetaInformation; // already read

        Attributes attrs = new Attributes(bigEndian, 9);
        while (pos != fmiEndPos) {
            mark(12);
            readHeader();
            if (Tag.groupNumber(tag) != 2) {
                Logger.warn(false, "Image", MISSING_FMI_LENGTH);
                reset();
                break;
            }
            if (vr != null) {
                if (vr == VR.UN)
                    vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                handler.readValue(this, attrs);
            } else
                skipAttribute(UNEXPECTED_ATTRIBUTE, "readFileMetaInformation()");
        }
        fileMetaInformation = attrs;

        String tsuid = attrs.getString(Tag.TransferSyntaxUID, null);
        if (tsuid == null) {
            Logger.warn(false, "Image", MISSING_TRANSFER_SYNTAX);
            tsuid = UID.ExplicitVRLittleEndian.uid;
        }
        switchTransferSyntax(tsuid);
        return attrs;
    }

    /**
     * Reads the attributes.
     *
     * @param attrs   the attrs.
     * @param len     the len.
     * @param stopTag the stop tag.
     * @throws IOException if the operation cannot be completed.
     */
    public void readAttributes(Attributes attrs, long len, int stopTag) throws IOException {
        readAttributes(attrs, len, tagEqualOrGreater(stopTag));
    }

    /**
     * Reads the attributes.
     *
     * @param attrs         the attrs.
     * @param len           the len.
     * @param stopPredicate the stop predicate.
     * @throws IOException if the operation cannot be completed.
     */
    public void readAttributes(Attributes attrs, long len, Predicate<ImageInputStream> stopPredicate)
            throws IOException {
        boolean undeflen = len == UNDEFINED_LENGTH;
        long endPos = pos + (len & 0xffffffffL);
        while (undeflen || this.pos < endPos) {
            try {
                readHeader(stopPredicate);
            } catch (EOFException e) {
                if (undeflen && pos == tagPos)
                    break;
                throw e;
            }
            if (stopPredicate.test(this))
                break;
            if (vr != null) {
                if (vr == VR.UN) {
                    switch (tag) {
                        case Tag.SmallestValidPixelValue:
                        case Tag.LargestValidPixelValue:
                        case Tag.SmallestImagePixelValue:
                        case Tag.LargestImagePixelValue:
                        case Tag.SmallestPixelValueInSeries:
                        case Tag.LargestPixelValueInSeries:
                        case Tag.SmallestImagePixelValueInPlane:
                        case Tag.LargestImagePixelValueInPlane:
                        case Tag.PixelPaddingValue:
                        case Tag.PixelPaddingRangeLimit:
                        case Tag.GrayLookupTableDescriptor:
                        case Tag.RedPaletteColorLookupTableDescriptor:
                        case Tag.GreenPaletteColorLookupTableDescriptor:
                        case Tag.BluePaletteColorLookupTableDescriptor:
                        case Tag.LargeRedPaletteColorLookupTableDescriptor:
                        case Tag.LargeGreenPaletteColorLookupTableDescriptor:
                        case Tag.LargeBluePaletteColorLookupTableDescriptor:
                        case Tag.RealWorldValueLastValueMapped:
                        case Tag.RealWorldValueFirstValueMapped:
                        case Tag.HistogramFirstBinValue:
                        case Tag.HistogramLastBinValue:
                            vr = attrs.getRoot().getInt(Tag.PixelRepresentation, 0) == 0 ? VR.US : VR.SS;
                            break;

                        case Tag.PurposeOfReferenceCodeSequence:
                            vr = probeObservationClass() ? VR.CS : VR.SQ;
                            break;

                        default:
                            vr = ElementDictionary.vrOf(tag, attrs.getPrivateCreator(tag));
                            if (vr == VR.UN && length == UNDEFINED_LENGTH)
                                vr = VR.SQ; // assumes UN with undefined length are SQ,
                            // will fail on UN fragments!
                    }
                }
                excludeBulkData = includeBulkData == IncludeBulkData.NO && isBulkData(attrs);
                includeBulkDataURI = length != 0 && vr != VR.SQ && includeBulkData == IncludeBulkData.URI
                        && isBulkData(attrs);
                handler.readValue(this, attrs);
            } else
                skipAttribute(UNEXPECTED_ATTRIBUTE, "readAttributes()");
        }
    }

    /**
     * Executes the probe observation class operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    private boolean probeObservationClass() {
        return !itemPointers.isEmpty() && itemPointers.get(0).sequenceTag == Tag.FindingsSequenceTrial;
    }

    /**
     * Reads the value.
     *
     * @param dis   the dis.
     * @param attrs the attrs.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void readValue(ImageInputStream dis, Attributes attrs) throws IOException {
        checkIsThis(dis);
        if (excludeBulkData) {
            if (length == UNDEFINED_LENGTH) {
                skipSequence();
            } else {
                skipFully(length);
            }
        } else if (length == 0) {
            attrs.setNull(tag, vr);
        } else if (vr == VR.SQ) {
            readSequence(length, attrs, tag);
        } else if (length == UNDEFINED_LENGTH) {
            readFragments(attrs, tag, vr);
        } else if (length == BulkData.MAGIC_LEN && super.in instanceof ObjectInputStream) {
            attrs.setValue(tag, vr, deserializeBulkData((ObjectInputStream) super.in));
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

    /**
     * Executes the deserialize bulk data operation.
     *
     * @param ois the ois.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private Object deserializeBulkData(ObjectInputStream ois) throws IOException {
        try {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    /**
     * Executes the skip sequence operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void skipSequence() throws IOException {
        while (readItemHeader())
            skipItem();
    }

    /**
     * Executes the skip item operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void skipItem() throws IOException {
        if (length == UNDEFINED_LENGTH) {
            for (;;) {
                readHeader();
                if (length == UNDEFINED_LENGTH) {
                    skipSequence();
                } else {
                    skipFully(length);
                    if (tag == Tag.ItemDelimitationItem)
                        break;
                }
            }
        } else {
            skipFully(length);
        }
    }

    /**
     * Creates the bulk data.
     *
     * @param dis the dis.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public BulkData createBulkData(ImageInputStream dis) throws IOException {
        BulkData bulkData;
        if (uri != null && !(super.in instanceof InflaterInputStream)) {
            bulkData = new BulkData(uri, pos, length, bigEndian);
            skipFully(length);
        } else {
            if (blkOut == null) {
                File blkfile = File.createTempFile(blkFilePrefix, blkFileSuffix, blkDirectory);
                if (blkFiles == null)
                    blkFiles = new ArrayList<>();
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

    /**
     * Determines whether bulk data.
     *
     * @param attrs the attrs.
     * @return true if the condition is met; otherwise false.
     */
    private boolean isBulkData(Attributes attrs) {
        return bulkDataDescriptor.isBulkData(itemPointers, attrs.getPrivateCreator(tag), tag, vr, (int) length);
    }

    /**
     * Reads the value.
     *
     * @param dis the dis.
     * @param seq the seq.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void readValue(ImageInputStream dis, Sequence seq) throws IOException {
        checkIsThis(dis);
        if (length == 0) {
            seq.add(new Attributes(seq.getParent().bigEndian(), 0));
            return;
        }
        Attributes attrs = new Attributes(seq.getParent().bigEndian());
        seq.add(attrs);
        readItemValue(attrs, length);
        attrs.trimToSize();
    }

    /**
     * Reads the value.
     *
     * @param dis   the dis.
     * @param frags the frags.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void readValue(ImageInputStream dis, Fragments frags) throws IOException {
        checkIsThis(dis);
        if (excludeBulkData) {
            skipFully(length);
        } else if (length == 0) {
            frags.add(new byte[] {});
        } else if (length == BulkData.MAGIC_LEN && super.in instanceof ObjectInputStream) {
            frags.add(deserializeBulkData((ObjectInputStream) super.in));
        } else if (includeBulkDataURI) {
            frags.add(bulkDataCreator.createBulkData(this));
        } else {
            byte[] b = readValue();
            if (bigEndian != frags.bigEndian())
                frags.vr().toggleEndian(b, false);
            frags.add(b);
        }
    }

    /**
     * Executes the start dataset operation.
     *
     * @param dis the dis.
     */
    @Override
    public void startDataset(ImageInputStream dis) {
    }

    /**
     * Executes the end dataset operation.
     *
     * @param dis the dis.
     */
    @Override
    public void endDataset(ImageInputStream dis) {
    }

    /**
     * Executes the check is this operation.
     *
     * @param dis the dis.
     */
    private void checkIsThis(ImageInputStream dis) {
        if (dis != this)
            throw new IllegalArgumentException("dis != this");
    }

    /**
     * @param message    the message to use in the warning log message
     * @param methodName the name of the method that is skipping the attribute
     * @throws IOException potentially thrown when performing the 'skip' operation
     */
    private void skipAttribute(String message, String methodName) throws IOException {
        String tagAsString = Tag.toString(this.tag);
        Logger.warn(false, "Image", message, tagAsString, length, tagPos, methodName);
        skipFully(length);
    }

    /**
     * Reads the sequence.
     *
     * @param len   the len.
     * @param attrs the attrs.
     * @param sqtag the sqtag.
     * @throws IOException if the operation cannot be completed.
     */
    private void readSequence(long len, Attributes attrs, int sqtag) throws IOException {
        if (len == 0) {
            attrs.setNull(sqtag, VR.SQ);
            return;
        }
        Sequence seq = attrs.newSequence(sqtag, 10);
        String privateCreator = attrs.getPrivateCreator(sqtag);
        boolean undefLen = len == UNDEFINED_LENGTH;
        long endPos = pos + (len & 0xffffffffL);
        boolean explicitVR0 = explicitVR;
        boolean bigEndian0 = bigEndian;
        if (encodedVR == 0x554e // UN
                && !probeExplicitVR()) {
            explicitVR = false;
            bigEndian = false;
        }
        boolean recoverSequenceExceedsEncodedLength = !undefLen && markSupported() && len < allocateLimit;
        if (recoverSequenceExceedsEncodedLength)
            mark((int) len + TREAT_SQ_AS_UN_MAX_EXCEED_LENGTH);
        for (int i = 0; (undefLen || pos < endPos) && readItemHeader(); ++i) {
            addItemPointer(sqtag, privateCreator, i);
            handler.readValue(this, seq);
            removeItemPointer();
        }
        explicitVR = explicitVR0;
        bigEndian = bigEndian0;
        if (seq.isEmpty())
            attrs.setNull(sqtag, VR.SQ);
        else if (!undefLen && pos != endPos) {
            if (!recoverSequenceExceedsEncodedLength || (pos - endPos) > TREAT_SQ_AS_UN_MAX_EXCEED_LENGTH)
                throw new IOException(String.format(SEQUENCE_EXCEED_ENCODED_LENGTH, Tag.toString(sqtag), len));
            Logger.info(false, "Image", TREAT_SQ_AS_UN, Tag.toString(sqtag), len);
            reset();
            tag = sqtag;
            vr = VR.UN;
            length = len;
            handler.readValue(this, attrs);
        } else
            seq.trimToSize();
    }

    /**
     * Executes the probe explicit vr operation.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
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
        return VR.valueOf(ByteKit.bytesToVR(buf, 12)) != null;
    }

    /**
     * Adds the item pointer.
     *
     * @param sqtag          the sqtag.
     * @param privateCreator the private creator.
     * @param itemIndex      the item index.
     */
    private void addItemPointer(int sqtag, String privateCreator, int itemIndex) {
        itemPointers.add(new ItemPointer(privateCreator, sqtag, itemIndex));
    }

    /**
     * Removes the item pointer.
     */
    private void removeItemPointer() {
        itemPointers.remove(itemPointers.size() - 1);
    }

    /**
     * Reads the item.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readItem() throws IOException {
        readHeader();
        if (tag != Tag.Item)
            throw new IOException("Unexpected attribute " + Tag.toString(tag) + " #" + length + " @ " + pos);
        Attributes attrs = new Attributes(bigEndian);
        attrs.setItemPosition(tagPos);
        readItemValue(attrs, length);
        attrs.trimToSize();
        return attrs;
    }

    /**
     * Reads the item value.
     *
     * @param attrs  the attrs.
     * @param length the length.
     * @throws IOException if the operation cannot be completed.
     */
    public void readItemValue(Attributes attrs, long length) throws IOException {
        readAttributes(attrs, length, dis -> dis.tag == Tag.ItemDelimitationItem);
    }

    /**
     * Reads the fragments.
     *
     * @param attrs    the attrs.
     * @param fragsTag the frags tag.
     * @param vr       the vr.
     * @throws IOException if the operation cannot be completed.
     */
    private void readFragments(Attributes attrs, int fragsTag, VR vr) throws IOException {
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

    /**
     * Reads the value.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public byte[] readValue() throws IOException {
        int valLen = (int) length;
        if (valLen < 0) {
            throw tagValueTooLargeException();
        }
        try {
            boolean limitedStream = in instanceof LimitedInputStream;
            if (limitedStream && valLen > ((LimitedInputStream) in).getRemaining()) {
                throw new EOFException("Length " + valLen + " for tag " + Tag.toString(tag) + " @ " + tagPos
                        + " exceeds remaining " + ((LimitedInputStream) in).getRemaining() + " (pos: " + pos + ")");
            }
            int allocLen = allocateLimit != -1 && !limitedStream ? Math.min(valLen, allocateLimit) : valLen;
            byte[] value = new byte[allocLen];
            readFully(value, 0, allocLen);
            while (allocLen < valLen) {
                int newLength = allocLen << 1;
                if (newLength <= 0)
                    newLength = Integer.MAX_VALUE;
                if (newLength > valLen)
                    newLength = valLen;
                value = Arrays.copyOf(value, newLength);
                readFully(value, allocLen, newLength - allocLen);
                allocLen = newLength;
            }
            return value;
        } catch (IOException e) {
            Logger.warn(false, "Image", "IOException during read of {} #{} @ {}", Tag.toString(tag), length, tagPos, e);
            throw e;
        }
    }

    /**
     * Executes the switch transfer syntax operation.
     *
     * @param tsuid the tsuid.
     * @throws IOException if the operation cannot be completed.
     */
    private void switchTransferSyntax(String tsuid) throws IOException {
        this.tsuid = tsuid;
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndian.uid);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian.uid);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian.uid) || tsuid.equals(UID.JPIPReferencedDeflate.uid)
                || tsuid.equals(UID.JPIPHTJ2KReferencedDeflate.uid)) {
            if (hasZLIBHeader()) {
                Logger.warn(false, "Image", DEFLATED_WITH_ZLIB_HEADER);
                super.in = new InflaterInputStream(super.in);
            } else {
                super.in = new InflaterInputStream(super.in, inflater = new Inflater(true));
            }
        }
    }

    /**
     * Determines whether zlib header.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean hasZLIBHeader() throws IOException {
        if (!markSupported())
            return false;
        byte[] buf = buffer;
        mark(2);
        read(buf, 0, 2);
        reset();
        return ByteKit.bytesToUShortBE(buf, 0) == ZLIB_HEADER;
    }

    /**
     * Executes the guess transfer syntax operation.
     *
     * @param preambleLength the preamble length.
     * @throws IOException if the operation cannot be completed.
     */
    private void guessTransferSyntax(int preambleLength) throws IOException {
        byte[] b134 = new byte[preambleLength + 6];
        mark(b134.length);
        int rlen = StreamKit.readAvailable(this, b134, 0, b134.length);
        if (rlen == b134.length) {
            if (b134[preambleLength] == 'D' && b134[preambleLength + 1] == 'I' && b134[preambleLength + 2] == 'C'
                    && b134[preambleLength + 3] == 'M' && b134[preambleLength + 4] == 2
                    && b134[preambleLength + 5] == 0) {
                preamble = new byte[preambleLength];
                System.arraycopy(b134, 0, preamble, 0, preambleLength);
                reset();
                StreamKit.skipFully(this, preambleLength + 4);
                mark(b134.length);
                rlen = StreamKit.readAvailable(this, b134, 0, b134.length);
            }
        }
        if (rlen < 8 || !guessTransferSyntax(b134, rlen, false) && !guessTransferSyntax(b134, rlen, true))
            throw new IOException(NOT_A_DICOM_STREAM);
        reset();
        hasfmi = Tag.isFileMetaInformation(ByteKit.bytesToTag(b134, 0, bigEndian));
    }

    /**
     * Executes the guess transfer syntax operation.
     *
     * @param b132      the b132.
     * @param rlen      the rlen.
     * @param bigEndian the big endian.
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean guessTransferSyntax(byte[] b132, int rlen, boolean bigEndian) throws IOException {
        int tag1 = ByteKit.bytesToTag(b132, 0, bigEndian);
        VR vr = ElementDictionary.vrOf(tag1, null);
        if (vr == VR.UN)
            return false;
        if (ByteKit.bytesToVR(b132, 4) == vr.code()) {
            this.tsuid = bigEndian ? UID.ExplicitVRBigEndian.uid : UID.ExplicitVRLittleEndian.uid;
            this.bigEndian = bigEndian;
            this.explicitVR = true;
            return true;
        }
        int len = ByteKit.bytesToInt(b132, 4, bigEndian);

        // check if it is a reasonable length for ImplicitVRLittleEndian:
        // non-negative and not exceeding what we have read into the buffer so
        // far (under the assumption that the first tag value will not have more
        // than 64 bytes. That is reasonable to assume, as every Composite
        // Object will contain a SOP Class UID (0008,0016), and all tags that
        // could come before that do not have VRs that allow length > 64. In
        // fact we are reading a maximum value of 132-8=124 bytes initially, so
        // we would also accept a longer length of 124 bytes for the first tag
        // value.)
        if (len < 0 || 8 + len > rlen)
            return false;

        if (bigEndian)
            throw new IOException(IMPLICIT_VR_BIG_ENDIAN);

        this.tsuid = UID.ImplicitVRLittleEndian.uid;
        this.bigEndian = false;
        this.explicitVR = false;
        return true;
    }

    /**
     * Defines the IncludeBulkData values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum IncludeBulkData {
        /**
         * Constant for the no value.
         */
        NO,
        /**
         * Constant for the yes value.
         */
        YES,
        /**
         * Constant for the uri value.
         */
        URI

    }

}
