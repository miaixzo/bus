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
package org.miaixz.bus.image.nimble.codec;

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import java.util.Objects;

import javax.imageio.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.io.*;
import org.miaixz.bus.image.metric.Property;
import org.miaixz.bus.image.nimble.BufferedImages;
import org.miaixz.bus.image.nimble.Overlays;
import org.miaixz.bus.image.nimble.PaletteColorModel;
import org.miaixz.bus.image.nimble.Photometric;
import org.miaixz.bus.image.nimble.codec.jpeg.PatchJPEGLSInputStream;
import org.miaixz.bus.image.nimble.codec.jpeg.PatchJPEGLSOutputStream;
import org.miaixz.bus.image.nimble.stream.EncapsulatedPixelDataImageInputStream;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the Transcoder type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Transcoder implements Closeable {

    /**
     * The s rgb value.
     */
    public static final ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    /**
     * The buffer size value.
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * The cm tags value.
     */
    private static final int[] cmTags = { Tag.RedPaletteColorLookupTableDescriptor,
            Tag.GreenPaletteColorLookupTableDescriptor, Tag.BluePaletteColorLookupTableDescriptor,
            Tag.PaletteColorLookupTableUID, Tag.RedPaletteColorLookupTableData, Tag.GreenPaletteColorLookupTableData,
            Tag.BluePaletteColorLookupTableData, Tag.SegmentedRedPaletteColorLookupTableData,
            Tag.SegmentedGreenPaletteColorLookupTableData, Tag.SegmentedBluePaletteColorLookupTableData,
            Tag.ICCProfile };

    /**
     * The dis value.
     */
    private final ImageInputStream dis;

    /**
     * The src transfer syntax value.
     */
    private final String srcTransferSyntax;

    /**
     * The src transfer syntax type value.
     */
    private final TransferSyntaxType srcTransferSyntaxType;

    /**
     * The dataset value.
     */
    private final Attributes dataset;

    /**
     * The retain file meta information value.
     */
    private boolean retainFileMetaInformation;

    /**
     * The include file meta information value.
     */
    private boolean includeFileMetaInformation;

    /**
     * The include implementation version name value.
     */
    private boolean includeImplementationVersionName = true;

    /**
     * The nullify pixel data value.
     */
    private boolean nullifyPixelData;

    /**
     * The enc opts value.
     */
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;

    /**
     * The close input stream value.
     */
    private boolean closeInputStream = true;

    /**
     * The close output stream value.
     */
    private boolean closeOutputStream = true;

    /**
     * The delete bulk data files value.
     */
    private boolean deleteBulkDataFiles = true;

    /**
     * The dest transfer syntax value.
     */
    private String destTransferSyntax;

    /**
     * The dest transfer syntax type value.
     */
    private TransferSyntaxType destTransferSyntaxType;

    /**
     * The lossy compression value.
     */
    private boolean lossyCompression;

    /**
     * The bits compressed value.
     */
    private int bitsCompressed = 0;

    /**
     * The max pixel value error value.
     */
    private int maxPixelValueError = -1;

    /**
     * The avg pixel value block size value.
     */
    private int avgPixelValueBlockSize = 1;

    /**
     * The file meta information value.
     */
    private Attributes fileMetaInformation;

    /**
     * The dos value.
     */
    private ImageOutputStream dos;

    /**
     * The handler value.
     */
    private Handler handler;

    /**
     * The image descriptor value.
     */
    private ImageDescriptor imageDescriptor;

    /**
     * The compressor image descriptor value.
     */
    private ImageDescriptor compressorImageDescriptor;

    /**
     * The encapsulated pixel data value.
     */
    private EncapsulatedPixelDataImageInputStream encapsulatedPixelData;

    /**
     * The decompressor param value.
     */
    private ImageReaderFactory.ImageReaderParam decompressorParam;

    /**
     * The decompressor value.
     */
    private ImageReader decompressor;

    /**
     * The decompress param value.
     */
    private ImageReadParam decompressParam;

    /**
     * The compressor param value.
     */
    private ImageWriterFactory.ImageWriterParam compressorParam;

    /**
     * The compressor value.
     */
    private ImageWriter compressor;

    /**
     * The compress param value.
     */
    private ImageWriteParam compressParam;

    /**
     * The verifier value.
     */
    private ImageReader verifier;

    /**
     * The verify param value.
     */
    private ImageReadParam verifyParam;

    /**
     * The ybr2rgb value.
     */
    private boolean ybr2rgb;

    /**
     * The palette2rgb value.
     */
    private boolean palette2rgb;

    /**
     * The original bi value.
     */
    private BufferedImage originalBi;

    /**
     * The bi value.
     */
    private BufferedImage bi;

    /**
     * The bi2 value.
     */
    private BufferedImage bi2;

    /**
     * The pixel data bulk data uri value.
     */
    private String pixelDataBulkDataURI;

    /**
     * The buffer value.
     */
    private byte[] buffer;

    /**
     * The encapsulated pixel data value total length value.
     */
    private long encapsulatedPixelDataValueTotalLength;

    /**
     * The image input handler value.
     */
    private final ImageInputHandler imageInputHandler = new ImageInputHandler() {

        @Override
        public void readValue(ImageInputStream dis, Attributes attrs) throws IOException {
            int tag = dis.tag();
            if (dis.level() == 0 && tag == Tag.PixelData) {
                if (nullifyPixelData) {
                    dataset.setNull(Tag.PixelData, dis.vr());
                    skipPixelData();
                } else {
                    imageDescriptor = new ImageDescriptor(attrs, bitsCompressed);
                    setDestinationTransferSyntax(adaptSuitableSyntax(destTransferSyntax));
                    initDicomOutputStream();
                    processPixelData();
                }
            } else {
                dis.readValue(dis, attrs);
            }
        }

        @Override
        public void readValue(ImageInputStream dis, Sequence seq) throws IOException {
            dis.readValue(dis, seq);
        }

        @Override
        public void readValue(ImageInputStream dis, Fragments frags) throws IOException {
            if (dos == null) {
                if (nullifyPixelData)
                    StreamKit.skipFully(dis, dis.unsignedLength());
                else
                    dis.readValue(dis, frags);
            } else {
                long length = dis.unsignedLength();
                dos.writeHeader(Tag.Item, null, (int) (length + 1) & ~1);
                IoKit.copy(dis, dos, length, buffer());
                if ((length & 1) != 0) {
                    Logger.info(
                            false,
                            "Image",
                            "Odd length of Pixel Data Fragment: length={} - append NULL byte to ensure even length",
                            length);
                    dos.write(0);
                }
            }
            encapsulatedPixelDataValueTotalLength += dis.unsignedLength();
        }

        @Override
        public void startDataset(ImageInputStream dis) {

        }

        @Override
        public void endDataset(ImageInputStream dis) {

        }
    };

    /**
     * Creates a new instance.
     *
     * @param f the f.
     * @throws IOException if the operation cannot be completed.
     */
    public Transcoder(File f) throws IOException {
        this(new ImageInputStream(f));
    }

    /**
     * Creates a new instance.
     *
     * @param in the in.
     * @throws IOException if the operation cannot be completed.
     */
    public Transcoder(InputStream in) throws IOException {
        this(new ImageInputStream(in));
    }

    /**
     * Creates a new instance.
     *
     * @param in    the in.
     * @param tsuid the tsuid.
     * @throws IOException if the operation cannot be completed.
     */
    public Transcoder(InputStream in, String tsuid) throws IOException {
        this(new ImageInputStream(in, tsuid));
    }

    /**
     * Creates a new instance.
     *
     * @param dis the dis.
     * @throws IOException if the operation cannot be completed.
     */
    public Transcoder(ImageInputStream dis) throws IOException {
        this.dis = dis;
        dis.readFileMetaInformation();
        dis.setDicomInputHandler(imageInputHandler);
        dataset = new Attributes(dis.bigEndian(), Normal._64);
        srcTransferSyntax = dis.getTransferSyntax();
        srcTransferSyntaxType = TransferSyntaxType.forUID(srcTransferSyntax);
        destTransferSyntax = srcTransferSyntax;
        destTransferSyntaxType = srcTransferSyntaxType;
    }

    /**
     * Converts this value to 16 bits allocated.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @param buf the buf.
     * @param j0  the j0.
     * @return the operation result.
     */
    private static byte[] to16BitsAllocated(byte[] b, int off, int len, byte[] buf, int j0) {
        for (int i = 0, j = j0; i < len; i++, j++, j++) {
            buf[j] = b[off + i];
        }
        return buf;
    }

    /**
     * Executes the bgr2rgb operation.
     *
     * @param bs the bs.
     */
    private static void bgr2rgb(byte[] bs) {
        for (int i = 0, j = 2; j < bs.length; i += 3, j += 3) {
            byte b = bs[i];
            bs[i] = bs[j];
            bs[j] = b;
        }
    }

    /**
     * Converts this value to short data.
     *
     * @param db the db.
     * @return the operation result.
     */
    private static short[] toShortData(DataBuffer db) {
        return db.getDataType() == DataBuffer.TYPE_SHORT ? ((DataBufferShort) db).getData()
                : ((DataBufferUShort) db).getData();
    }

    /**
     * Sets the encoding options.
     *
     * @param encOpts the enc opts.
     */
    public void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = Objects.requireNonNull(encOpts);
    }

    /**
     * Sets the concatenate bulk data files.
     *
     * @param catBlkFiles the cat blk files.
     */
    public void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        dis.setConcatenateBulkDataFiles(catBlkFiles);
    }

    /**
     * Sets the include bulk data.
     *
     * @param includeBulkData the include bulk data.
     */
    public void setIncludeBulkData(ImageInputStream.IncludeBulkData includeBulkData) {
        dis.setIncludeBulkData(includeBulkData);
    }

    /**
     * Sets the bulk data descriptor.
     *
     * @param bulkDataDescriptor the bulk data descriptor.
     */
    public void setBulkDataDescriptor(BulkDataDescriptor bulkDataDescriptor) {
        dis.setBulkDataDescriptor(bulkDataDescriptor);
    }

    /**
     * Sets the bulk data directory.
     *
     * @param blkDirectory the blk directory.
     */
    public void setBulkDataDirectory(File blkDirectory) {
        dis.setBulkDataDirectory(blkDirectory);
    }

    /**
     * Determines whether close input stream.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCloseInputStream() {
        return closeInputStream;
    }

    /**
     * Sets the close input stream.
     *
     * @param closeInputStream the close input stream.
     */
    public void setCloseInputStream(boolean closeInputStream) {
        this.closeInputStream = closeInputStream;
    }

    /**
     * Determines whether close output stream.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCloseOutputStream() {
        return closeOutputStream;
    }

    /**
     * Sets the close output stream.
     *
     * @param closeOutputStream the close output stream.
     */
    public void setCloseOutputStream(boolean closeOutputStream) {
        this.closeOutputStream = closeOutputStream;
    }

    /**
     * Determines whether delete bulk data files.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isDeleteBulkDataFiles() {
        return deleteBulkDataFiles;
    }

    /**
     * Sets the delete bulk data files.
     *
     * @param deleteBulkDataFiles the delete bulk data files.
     */
    public void setDeleteBulkDataFiles(boolean deleteBulkDataFiles) {
        this.deleteBulkDataFiles = deleteBulkDataFiles;
    }

    /**
     * Determines whether include file meta information.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isIncludeFileMetaInformation() {
        return includeFileMetaInformation;
    }

    /**
     * Sets the include file meta information.
     *
     * @param includeFileMetaInformation the include file meta information.
     */
    public void setIncludeFileMetaInformation(boolean includeFileMetaInformation) {
        this.includeFileMetaInformation = includeFileMetaInformation;
    }

    /**
     * Determines whether retain file meta information.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isRetainFileMetaInformation() {
        return retainFileMetaInformation;
    }

    /**
     * Sets the retain file meta information.
     *
     * @param retainFileMetaInformation the retain file meta information.
     */
    public void setRetainFileMetaInformation(boolean retainFileMetaInformation) {
        this.retainFileMetaInformation = retainFileMetaInformation;
    }

    /**
     * Determines whether include implementation version name.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isIncludeImplementationVersionName() {
        return includeImplementationVersionName;
    }

    /**
     * Sets the include implementation version name.
     *
     * @param includeImplementationVersionName the include implementation version name.
     */
    public void setIncludeImplementationVersionName(boolean includeImplementationVersionName) {
        this.includeImplementationVersionName = includeImplementationVersionName;
    }

    /**
     * Determines whether nullify pixel data.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isNullifyPixelData() {
        return nullifyPixelData;
    }

    /**
     * Sets the nullify pixel data.
     *
     * @param nullifyPixelData the nullify pixel data.
     */
    public void setNullifyPixelData(boolean nullifyPixelData) {
        this.nullifyPixelData = nullifyPixelData;
    }

    /**
     * Gets the image descriptor.
     *
     * @return the image descriptor.
     */
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    /**
     * Gets the source transfer syntax.
     *
     * @return the source transfer syntax.
     */
    public String getSourceTransferSyntax() {
        return dis.getTransferSyntax();
    }

    /**
     * Gets the source transfer syntax type.
     *
     * @return the source transfer syntax type.
     */
    public TransferSyntaxType getSourceTransferSyntaxType() {
        return srcTransferSyntaxType;
    }

    /**
     * Gets the destination transfer syntax.
     *
     * @return the destination transfer syntax.
     */
    public String getDestinationTransferSyntax() {
        return destTransferSyntax;
    }

    /**
     * Sets the destination transfer syntax.
     *
     * @param tsuid the tsuid.
     */
    public void setDestinationTransferSyntax(String tsuid) {
        if (tsuid.equals(destTransferSyntax))
            return;
        this.destTransferSyntaxType = TransferSyntaxType.forUID(tsuid);
        this.lossyCompression = TransferSyntaxType.isLossyCompression(tsuid);
        this.destTransferSyntax = tsuid;

        if (srcTransferSyntaxType.isPixeldataEncapsulated()) {
            initDecompressor();
        } else {
            if (decompressor != null) {
                decompressor.dispose();
                decompressor = null;
            }
        }

        if (destTransferSyntaxType.isPixeldataEncapsulated()) {
            initCompressor(tsuid);
        } else {
            if (compressor != null) {
                compressor.dispose();
                compressor = null;
            }
        }
    }

    /**
     * Executes the adapt suitable syntax operation.
     *
     * @param dstTsuid the dst tsuid.
     * @return the operation result.
     */
    private String adaptSuitableSyntax(String dstTsuid) {
        int bitsStored = imageDescriptor.getBitsStored();
        if (imageDescriptor.getBitsAllocated() == 1
                && TransferSyntaxType.forUID(dstTsuid) != TransferSyntaxType.NATIVE) {
            return srcTransferSyntax;
        }
        switch (UID.from(dstTsuid)) {
            case UID.JPEGBaseline8Bit:
                return bitsStored <= 8 ? dstTsuid
                        : !imageDescriptor.isSigned() && bitsStored <= 12 ? UID.JPEGExtended12Bit.uid
                                : bitsStored <= 16 ? UID.JPEGLosslessSV1.uid : UID.ExplicitVRLittleEndian.uid;

            case UID.JPEGExtended12Bit:
            case UID.JPEGSpectralSelectionNonHierarchical68:
            case UID.JPEGFullProgressionNonHierarchical1012:
                return !imageDescriptor.isSigned() && bitsStored <= 12 ? dstTsuid
                        : bitsStored <= 16 ? UID.JPEGLosslessSV1.uid : UID.ExplicitVRLittleEndian.uid;

            case UID.JPEGLossless:
            case UID.JPEGLosslessSV1:
            case UID.JPEGLSLossless:
            case UID.JPEGLSNearLossless:
            case UID.JPEG2000Lossless:
            case UID.JPEG2000:
            case UID.HTJ2KLossless:
            case UID.HTJ2KLosslessRPCL:
            case UID.HTJ2K:
                return bitsStored <= 16 ? dstTsuid : UID.ExplicitVRLittleEndian.uid;

            default:
                return dstTsuid;
        }
    }

    /**
     * Gets the pixel data bulk data uri.
     *
     * @return the pixel data bulk data uri.
     */
    public String getPixelDataBulkDataURI() {
        return pixelDataBulkDataURI;
    }

    /**
     * Sets the pixel data bulk data uri.
     *
     * @param pixelDataBulkDataURI the pixel data bulk data uri.
     */
    public void setPixelDataBulkDataURI(String pixelDataBulkDataURI) {
        this.pixelDataBulkDataURI = pixelDataBulkDataURI;
    }

    /**
     * Gets the bulk data files.
     *
     * @return the bulk data files.
     */
    public List<File> getBulkDataFiles() {
        return dis.getBulkDataFiles();
    }

    /**
     * Returns {@code Attributes} of File Meta Information written to {@code OutputStream} or {@code null}.
     *
     * @return {@code Attributes} of File Meta Information written to {@code OutputStream} or {@code null}.
     */
    public Attributes getFileMetaInformation() {
        return fileMetaInformation;
    }

    /**
     * Gets the encapsulated pixel data value total length.
     *
     * @return the encapsulated pixel data value total length.
     */
    public long getEncapsulatedPixelDataValueTotalLength() {
        return encapsulatedPixelDataValueTotalLength;
    }

    /**
     * Executes the init decompressor operation.
     */
    private void initDecompressor() {
        decompressorParam = ImageReaderFactory.getImageReaderParam(srcTransferSyntax);
        if (decompressorParam == null)
            throw new UnsupportedOperationException("Unsupported Transfer Syntax: " + srcTransferSyntax);

        this.decompressor = ImageReaderFactory.getImageReader(decompressorParam);
        Logger.debug(false, "Image", "Decompressor: class={}", decompressor.getClass().getName());

        this.decompressParam = decompressor.getDefaultReadParam();
    }

    /**
     * Executes the init compressor operation.
     *
     * @param tsuid the tsuid.
     */
    private void initCompressor(String tsuid) {
        compressorParam = ImageWriterFactory.getImageWriterParam(tsuid);
        if (compressorParam == null)
            throw new UnsupportedOperationException("Unsupported Transfer Syntax: " + tsuid);

        this.compressor = ImageWriterFactory.getImageWriter(compressorParam);
        Logger.debug(false, "Image", "Compressor: class={}", compressor.getClass().getName());

        this.compressParam = compressor.getDefaultWriteParam();
        setCompressParams(compressorParam.getImageWriteParams());
    }

    /**
     * Sets the compress params.
     *
     * @param imageWriteParams the image write params.
     */
    public void setCompressParams(Property... imageWriteParams) {
        if (compressorParam == null)
            return;
        for (Property property : imageWriteParams) {
            String name = property.getName();
            if (name.equals("maxPixelValueError"))
                this.maxPixelValueError = ((Number) property.getValue()).intValue();
            else if (name.equals("avgPixelValueBlockSize"))
                this.avgPixelValueBlockSize = ((Number) property.getValue()).intValue();
            else if (name.equals("bitsCompressed"))
                this.bitsCompressed = ((Number) property.getValue()).intValue();
            else {
                if (compressParam.getCompressionMode() != ImageWriteParam.MODE_EXPLICIT)
                    compressParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                property.setAt(compressParam);
            }
        }
        if (maxPixelValueError >= 0) {
            ImageReaderFactory.ImageReaderParam readerParam = ImageReaderFactory
                    .getImageReaderParam(destTransferSyntax);
            if (readerParam == null)
                throw new UnsupportedOperationException("Unsupported Transfer Syntax: " + destTransferSyntax);

            this.verifier = ImageReaderFactory.getImageReader(readerParam);
            this.verifyParam = verifier.getDefaultReadParam();
            Logger.debug(false, "Image", "Verifier: class={}", verifier.getClass().getName());
        }
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void close() throws IOException {
        if (decompressor != null)
            decompressor.dispose();
        if (compressor != null)
            compressor.dispose();
        if (verifier != null)
            verifier.dispose();
        if (closeInputStream)
            IoKit.close(dis);
        if (closeOutputStream)
            IoKit.close(dos);
        if (deleteBulkDataFiles) {
            for (File tmpFile : dis.getBulkDataFiles())
                tmpFile.delete();
        }
    }

    /**
     * Executes the transcode operation.
     *
     * @param handler the handler.
     * @throws IOException if the operation cannot be completed.
     */
    public void transcode(Handler handler) throws IOException {
        this.handler = handler;
        dis.readAllAttributes(dataset);

        if (dos == null) {
            if (compressor != null) { // Adjust destination Transfer Syntax if no pixeldata
                destTransferSyntax = UID.ExplicitVRLittleEndian.uid;
                destTransferSyntaxType = TransferSyntaxType.NATIVE;
                lossyCompression = false;
            }
            initDicomOutputStream();
            writeDataset();
        } else
            dataset.writePostPixelDataTo(dos);
        dos.finish();
    }

    /**
     * Processes the pixel data.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void processPixelData() throws IOException {
        if (decompressor != null)
            initEncapsulatedPixelData();
        VR vr;
        if (compressor != null) {
            vr = VR.OB;
            compressPixelData();
        } else if (decompressor != null) {
            vr = VR.OW;
            decompressPixelData();
        } else {
            vr = dis.vr();
            copyPixelData();
        }
        setPixelDataBulkData(vr);
    }

    /**
     * Executes the init encapsulated pixel data operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void initEncapsulatedPixelData() throws IOException {
        encapsulatedPixelData = new EncapsulatedPixelDataImageInputStream(dis, imageDescriptor);
    }

    /**
     * Executes the decompress pixel data operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void decompressPixelData() throws IOException {
        int length = imageDescriptor.getLength();
        int padding = length & 1;
        adjustDataset();
        writeDataset();
        dos.writeHeader(Tag.PixelData, VR.OW, length + padding);
        for (int i = 0; i < imageDescriptor.getFrames(); i++) {
            decompressFrame(i);
            writeFrame();
        }
        if (padding != 0)
            dos.write(0);
    }

    /**
     * Executes the skip pixel data operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void skipPixelData() throws IOException {
        int length = dis.length();
        if (length == -1) {
            dis.readValue(dis, dataset);
        } else {
            StreamKit.skipFully(dis, length);
        }
    }

    /**
     * Copies the pixel data.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void copyPixelData() throws IOException {
        long length = dis.unsignedLength();
        writeDataset();
        if (length == -1) {
            dos.writeHeader(Tag.PixelData, dis.vr(), -1);
            dis.readValue(dis, dataset);
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        } else {
            dos.writeHeader(Tag.PixelData, dis.vr(), (int) (length + 1) & ~1);
            if (dis.bigEndian() == dos.isBigEndian())
                IoKit.copy(dis, dos, length, buffer());
            else
                IoKit.copy(dis, dos, length, dis.vr().numEndianBytes(), buffer());
            if ((length & 1) != 0) {
                Logger.info(
                        false,
                        "Image",
                        "Odd length of Pixel Data: length={} - append NULL byte to ensure even length",
                        length);
                dos.write(0);
            }
        }
    }

    /**
     * Executes the compress pixel data operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void compressPixelData() throws IOException {
        int padding = (int) (dis.unsignedLength() - imageDescriptor.getLength());
        for (int i = 0; i < imageDescriptor.getFrames(); i++) {
            if (decompressor == null)
                readFrame();
            else
                decompressFrame(i);

            if (i == 0) {
                extractEmbeddedOverlays();
                adjustDataset();
                writeDataset();
                dos.writeHeader(Tag.PixelData, VR.OB, -1);
                dos.writeHeader(Tag.Item, null, 0);
            }
            nullifyUnusedBits();
            bi = palette2rgb ? BufferedImages.convertPalettetoRGB(originalBi, bi)
                    : ybr2rgb ? BufferedImages.convertYBRtoRGB(originalBi, bi)
                            : imageDescriptor.is16BitsAllocated8BitsStored()
                                    ? BufferedImages.convertShortsToBytes(originalBi, bi) // workaround for JPEG codec
                                    : originalBi;
            compressFrame(i);
        }
        dis.skipFully(padding);
        dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }

    /**
     * Sets the pixel data bulk data.
     *
     * @param vr the vr.
     */
    private void setPixelDataBulkData(VR vr) {
        if (pixelDataBulkDataURI != null)
            dataset.setValue(Tag.PixelData, vr, new BulkData(null, pixelDataBulkDataURI, false));
    }

    /**
     * Executes the adjust dataset operation.
     */
    private void adjustDataset() {
        Photometric pmi = imageDescriptor.getPhotometricInterpretation();
        if (decompressor != null) {
            if (imageDescriptor.getSamples() == 3) {
                if (pmi.isYBR() && TransferSyntaxType.isYBRCompression(srcTransferSyntax)) {
                    pmi = Photometric.RGB;
                    dataset.setString(Tag.PhotometricInterpretation, VR.CS, pmi.toString());
                }
                dataset.setInt(Tag.PlanarConfiguration, VR.US, srcTransferSyntaxType.getPlanarConfiguration());
            } else {
                if (srcTransferSyntaxType.adjustBitsStoredTo12(dataset)) {
                    Logger.info(
                            false,
                            "Image",
                            "Adjust invalid Bits Stored: bitsStored={} of {} to 12",
                            imageDescriptor.getBitsStored(),
                            srcTransferSyntaxType);
                }
            }
        }
        if (compressor != null) {
            if (pmi == Photometric.PALETTE_COLOR && lossyCompression) {
                palette2rgb = true;
                dataset.removeSelected(cmTags);
                dataset.setInt(Tag.SamplesPerPixel, VR.US, 3);
                dataset.setInt(Tag.BitsAllocated, VR.US, 8);
                dataset.setInt(Tag.BitsStored, VR.US, 8);
                dataset.setInt(Tag.HighBit, VR.US, 7);
                pmi = Photometric.RGB;
                Logger.warn(
                        false,
                        "Image",
                        "Converting PALETTE_COLOR model into a lossy format is not recommended, prefer a lossless format");
            } else if ((pmi.isSubSampled() && !srcTransferSyntaxType.isPixeldataEncapsulated())
                    || (pmi == Photometric.YBR_FULL && (TransferSyntaxType.isYBRCompression(destTransferSyntax)
                            || destTransferSyntaxType == TransferSyntaxType.JPEG_LS))) {
                ybr2rgb = true;
                pmi = Photometric.RGB;
                Logger.debug(false, "Image", "Conversion to an RGB color model is required before compression.");
            } else {
                if (destTransferSyntaxType.adjustBitsStoredTo12(dataset)) {
                    Logger.debug(
                            false,
                            "Image",
                            "Adjust Bits Stored: bitsStored={} for {} to 12",
                            imageDescriptor.getBitsStored(),
                            destTransferSyntaxType);
                }
            }
            dataset.setString(Tag.PhotometricInterpretation, VR.CS, pmiForCompression(pmi).toString());
            compressorImageDescriptor = new ImageDescriptor(dataset, bitsCompressed);
            pmi = pmi.compress(destTransferSyntax);
            dataset.setString(Tag.PhotometricInterpretation, VR.CS, pmi.toString());
            if (dataset.getInt(Tag.SamplesPerPixel, 1) > 1)
                dataset.setInt(Tag.PlanarConfiguration, VR.US, destTransferSyntaxType.getPlanarConfiguration());
            if (lossyCompression) {
                dataset.setString(Tag.LossyImageCompression, VR.CS, "01");
                try {
                    dataset.setFloat(
                            Tag.LossyImageCompressionRatio,
                            VR.DS,
                            ((Number) compressParam.getClass().getMethod("getCompressionRatiofactor")
                                    .invoke(compressParam)).floatValue());
                } catch (Exception ignore) {
                }
            }
        }
    }

    /**
     * Executes the pmi for compression operation.
     *
     * @param pmi the pmi.
     * @return the operation result.
     */
    private Photometric pmiForCompression(Photometric pmi) {
        return pmi.isYBR() && (destTransferSyntaxType == TransferSyntaxType.JPEG_LOSSLESS
                || destTransferSyntaxType == TransferSyntaxType.JPEG_LS) ? Photometric.RGB : pmi;
    }

    /**
     * Executes the extract embedded overlays operation.
     */
    private void extractEmbeddedOverlays() {
        for (int gg0000 : imageDescriptor.getEmbeddedOverlays()) {
            int ovlyRow = dataset.getInt(Tag.OverlayRows | gg0000, 0);
            int ovlyColumns = dataset.getInt(Tag.OverlayColumns | gg0000, 0);
            int ovlyBitPosition = dataset.getInt(Tag.OverlayBitPosition | gg0000, 0);
            int mask = 1 << ovlyBitPosition;
            int ovlyLength = ovlyRow * ovlyColumns;
            byte[] ovlyData = new byte[(((ovlyLength + 7) >>> 3) + 1) & (~1)];
            Overlays.extractFromPixeldata(originalBi.getRaster(), mask, ovlyData, 0, ovlyLength);
            dataset.setInt(Tag.OverlayBitsAllocated | gg0000, VR.US, 1);
            dataset.setInt(Tag.OverlayBitPosition | gg0000, VR.US, 0);
            dataset.setBytes(Tag.OverlayData | gg0000, VR.OB, ovlyData);
            Logger.debug(
                    false,
                    "Image",
                    "Extracted embedded overlay #{} from bit #{}",
                    (gg0000 >>> 17) + 1,
                    ovlyBitPosition);
        }
    }

    /**
     * Executes the nullify unused bits operation.
     */
    private void nullifyUnusedBits() {
        if (imageDescriptor.getBitsStored() < imageDescriptor.getBitsAllocated()) {
            DataBuffer db = originalBi.getRaster().getDataBuffer();
            switch (db.getDataType()) {
                case DataBuffer.TYPE_USHORT:
                    nullifyUnusedBits(((DataBufferUShort) db).getData());
                    break;

                case DataBuffer.TYPE_SHORT:
                    extendSignUnusedBits(((DataBufferShort) db).getData());
                    break;
            }
        }
    }

    /**
     * Executes the nullify unused bits operation.
     *
     * @param data the data.
     */
    private void nullifyUnusedBits(short[] data) {
        int mask = (1 << imageDescriptor.getBitsStored()) - 1;
        for (int i = 0; i < data.length; i++)
            data[i] &= mask;
    }

    /**
     * Executes the extend sign unused bits operation.
     *
     * @param data the data.
     */
    private void extendSignUnusedBits(short[] data) {
        int unused = 32 - imageDescriptor.getBitsStored();
        for (int i = 0; i < data.length; i++)
            data[i] = (short) ((data[i] << unused) >> unused);
    }

    /**
     * Executes the decompress frame operation.
     *
     * @param frameIndex the frame index.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private BufferedImage decompressFrame(int frameIndex) throws IOException {
        decompressor.setInput(
                decompressorParam.patchJPEGLS != null
                        ? new PatchJPEGLSInputStream(encapsulatedPixelData, decompressorParam.patchJPEGLS)
                        : encapsulatedPixelData);
        if (srcTransferSyntaxType == TransferSyntaxType.RLE)
            initBufferedImage();
        decompressParam.setDestination(originalBi);
        long start = System.currentTimeMillis();
        originalBi = adjustColorModel(decompressor.read(0, decompressParam));
        long end = System.currentTimeMillis();
        if (Logger.isDebugEnabled())
            Logger.debug(
                    false,
                    "Image",
                    "Decompressed frame #{} in {} ms, ratio 1:{}",
                    frameIndex + 1,
                    end - start,
                    (float) imageDescriptor.getFrameLength() / encapsulatedPixelData.getStreamPosition());
        encapsulatedPixelData.seekNextFrame();
        return originalBi;
    }

    /**
     * Executes the adjust color model operation.
     *
     * @param bi the bi.
     * @return the operation result.
     */
    private BufferedImage adjustColorModel(BufferedImage bi) {
        Photometric pmi = imageDescriptor.getPhotometricInterpretation();
        if (pmi == Photometric.PALETTE_COLOR && !(bi.getColorModel() instanceof PaletteColorModel)) {
            ColorModel cm;
            if (originalBi != null) {
                cm = originalBi.getColorModel();
            } else {
                int bitsStored = Math.min(imageDescriptor.getBitsStored(), destTransferSyntaxType.getMaxBitsStored());
                int dataType = bi.getSampleModel().getDataType();
                cm = pmi.createColorModel(bitsStored, dataType, sRGB, dataset);
            }
            bi = new BufferedImage(cm, bi.getRaster(), false, null);
        }
        return bi;
    }

    /**
     * Executes the compress frame operation.
     *
     * @param frameIndex the frame index.
     * @throws IOException if the operation cannot be completed.
     */
    private void compressFrame(int frameIndex) throws IOException {
        CacheImageOutputStream ios = new CacheImageOutputStream(compressorImageDescriptor);
        compressor.setOutput(
                compressorParam.patchJPEGLS != null ? new PatchJPEGLSOutputStream(ios, compressorParam.patchJPEGLS)
                        : ios);
        long start = System.currentTimeMillis();
        compressor.write(null, new IIOImage(bi, null, null), compressParam);
        long end = System.currentTimeMillis();
        int length = (int) ios.getStreamPosition();
        if (Logger.isDebugEnabled())
            Logger.debug(
                    false,
                    "Image",
                    "Compressed frame #{} in {} ms, ratio {}:1",
                    frameIndex + 1,
                    end - start,
                    (float) imageDescriptor.getFrameLength() / length);
        verify(ios, frameIndex);
        if ((length & 1) != 0) {
            ios.write(0);
            length++;
        }
        dos.writeHeader(Tag.Item, null, length);
        ios.setOutputStream(dos);
        ios.flush();
    }

    /**
     * Reads the frame.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void readFrame() throws IOException {
        initBufferedImage();
        WritableRaster raster = originalBi.getRaster();
        DataBuffer dataBuffer = raster.getDataBuffer();
        switch (dataBuffer.getDataType()) {
            case DataBuffer.TYPE_SHORT:
                readFully(((DataBufferShort) dataBuffer).getData());
                break;

            case DataBuffer.TYPE_USHORT:
                readFully(((DataBufferUShort) dataBuffer).getData());
                break;

            case DataBuffer.TYPE_BYTE:
                readFully(((DataBufferByte) dataBuffer).getBankData());
                break;
        }
    }

    /**
     * Reads the fully.
     *
     * @param bb the bb.
     * @throws IOException if the operation cannot be completed.
     */
    private void readFully(byte[][] bb) throws IOException {
        for (byte[] b : bb) {
            dis.readFully(b);
        }
        if (dis.bigEndian() && dis.vr() == VR.OW)
            ByteKit.swapShorts(bb);
    }

    /**
     * Reads the fully.
     *
     * @param s the s.
     * @throws IOException if the operation cannot be completed.
     */
    private void readFully(short[] s) throws IOException {
        int off = 0;
        int len = s.length;
        byte[] b = buffer();
        while (len > 0) {
            int nelts = Math.min(len, b.length / 2);
            dis.readFully(b, 0, nelts * 2);
            toShorts(b, s, off, nelts, dis.bigEndian());
            off += nelts;
            len -= nelts;
        }
    }

    /**
     * Executes the to shorts operation.
     *
     * @param b         the b.
     * @param s         the s.
     * @param off       the off.
     * @param len       the len.
     * @param bigEndian the big endian.
     */
    private void toShorts(byte[] b, short[] s, int off, int len, boolean bigEndian) {
        int boff = 0;
        if (bigEndian) {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff];
                int b1 = b[boff + 1] & 0xff;
                s[off + j] = (short) ((b0 << 8) | b1);
                boff += 2;
            }
        } else {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff + 1];
                int b1 = b[boff] & 0xff;
                s[off + j] = (short) ((b0 << 8) | b1);
                boff += 2;
            }
        }
    }

    /**
     * Executes the buffer operation.
     *
     * @return the operation result.
     */
    private byte[] buffer() {
        if (buffer == null)
            buffer = new byte[BUFFER_SIZE];
        return buffer;
    }

    /**
     * Writes the frame.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void writeFrame() throws IOException {
        WritableRaster raster = originalBi.getRaster();
        SampleModel sm = raster.getSampleModel();
        DataBuffer db = raster.getDataBuffer();
        switch (db.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                write(sm, ((DataBufferByte) db).getBankData());
                break;

            case DataBuffer.TYPE_USHORT:
                write(sm, ((DataBufferUShort) db).getData());
                break;

            case DataBuffer.TYPE_SHORT:
                write(sm, ((DataBufferShort) db).getData());
                break;

            case DataBuffer.TYPE_INT:
                write(sm, ((DataBufferInt) db).getData());
                break;

            default:
                throw new UnsupportedOperationException("Unsupported Datatype: " + db.getDataType());
        }
    }

    /**
     * Executes the write operation.
     *
     * @param sm       the sm.
     * @param bankData the bank data.
     * @throws IOException if the operation cannot be completed.
     */
    private void write(SampleModel sm, byte[][] bankData) throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        ComponentSampleModel csm = (ComponentSampleModel) sm;
        int len = w * csm.getPixelStride();
        int stride = csm.getScanlineStride();
        if (csm.getBandOffsets()[0] != 0)
            bgr2rgb(bankData[0]);
        if (imageDescriptor.getBitsAllocated() == 16) {
            byte[] buf = new byte[len << 1];
            int j0 = dos.isBigEndian() ? 1 : 0;
            for (byte[] b : bankData)
                for (int y = 0, off = 0; y < h; ++y, off += stride) {
                    dos.write(to16BitsAllocated(b, off, len, buf, j0));
                }
        } else {
            for (byte[] b : bankData)
                for (int y = 0, off = 0; y < h; ++y, off += stride)
                    dos.write(b, off, len);
        }
    }

    /**
     * Executes the write operation.
     *
     * @param sm   the sm.
     * @param data the data.
     * @throws IOException if the operation cannot be completed.
     */
    private void write(SampleModel sm, short[] data) throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        int stride = ((ComponentSampleModel) sm).getScanlineStride();
        byte[] b = new byte[w * 2];
        for (int y = 0; y < h; ++y) {
            for (int i = 0, j = y * stride; i < b.length;) {
                short s = data[j++];
                b[i++] = (byte) s;
                b[i++] = (byte) (s >> 8);
            }
            dos.write(b);
        }
    }

    /**
     * Executes the write operation.
     *
     * @param sm   the sm.
     * @param data the data.
     * @throws IOException if the operation cannot be completed.
     */
    private void write(SampleModel sm, int[] data) throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        int stride = ((SinglePixelPackedSampleModel) sm).getScanlineStride();
        byte[] b = new byte[w * 3];
        for (int y = 0; y < h; ++y) {
            for (int i = 0, j = y * stride; i < b.length;) {
                int s = data[j++];
                b[i++] = (byte) (s >> 16);
                b[i++] = (byte) (s >> 8);
                b[i++] = (byte) s;
            }
            dos.write(b);
        }
    }

    /**
     * Executes the init dicom output stream operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void initDicomOutputStream() throws IOException {
        dos = new ImageOutputStream(handler.newOutputStream(this, dataset),
                includeFileMetaInformation ? UID.ExplicitVRLittleEndian.uid : destTransferSyntax);
        dos.setEncodingOptions(encOpts);
    }

    /**
     * Writes the dataset.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void writeDataset() throws IOException {
        Attributes fmi = null;
        if (includeFileMetaInformation) {
            if (retainFileMetaInformation)
                fmi = dis.getFileMetaInformation();
            if (fmi == null)
                fmi = dataset.createFileMetaInformation(destTransferSyntax, includeImplementationVersionName);
            else
                fmi.setString(Tag.TransferSyntaxUID, VR.UI, destTransferSyntax);
        }
        dos.writeDataset(fmi, dataset);
        fileMetaInformation = fmi;
    }

    /**
     * Executes the init buffered image operation.
     */
    private void initBufferedImage() {
        if (originalBi != null)
            return;

        int rows = imageDescriptor.getRows();
        int cols = imageDescriptor.getColumns();
        int samples = imageDescriptor.getSamples();
        int bitsAllocated = imageDescriptor.getBitsAllocated();
        int bitsStored = Math.min(imageDescriptor.getBitsStored(), destTransferSyntaxType.getMaxBitsStored());
        boolean signed = imageDescriptor.isSigned() && destTransferSyntaxType.canEncodeSigned();
        boolean banded = imageDescriptor.isBanded() || srcTransferSyntaxType == TransferSyntaxType.RLE;
        Photometric pmi = imageDescriptor.getPhotometricInterpretation();
        int dataType = bitsAllocated > 8 ? (signed ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_USHORT)
                : DataBuffer.TYPE_BYTE;
        ColorModel cm = pmi.createColorModel(bitsStored, dataType, sRGB, dataset);
        SampleModel sm = pmi.createSampleModel(dataType, cols, rows, samples, banded);
        WritableRaster raster = Raster.createWritableRaster(sm, null);
        originalBi = new BufferedImage(cm, raster, false, null);
    }

    /**
     * Executes the verify operation.
     *
     * @param cache the cache.
     * @param index the index.
     * @throws IOException if the operation cannot be completed.
     */
    private void verify(javax.imageio.stream.ImageOutputStream cache, int index) throws IOException {
        if (verifier == null)
            return;

        long prevStreamPosition = cache.getStreamPosition();
        int prevBitOffset = cache.getBitOffset();
        cache.seek(0);
        verifier.setInput(cache);
        verifyParam.setDestination(bi2);
        long start = System.currentTimeMillis();
        bi2 = verifier.read(0, verifyParam);
        int maxDiff = maxDiff(bi.getRaster(), bi2.getRaster());
        long end = System.currentTimeMillis();
        if (Logger.isDebugEnabled())
            Logger.debug(
                    false,
                    "Image",
                    "Verified compressed frame #{} in {} ms - max pixel value error: maxPixelValueError={}",
                    index + 1,
                    end - start,
                    maxDiff);
        if (maxDiff > maxPixelValueError)
            throw new IOException("Decompressed pixel data differs up to " + maxDiff + " from original pixel data");

        cache.seek(prevStreamPosition);
        cache.setBitOffset(prevBitOffset);
    }

    /**
     * Executes the max diff operation.
     *
     * @param raster  the raster.
     * @param raster2 the raster2.
     * @return the operation result.
     */
    private int maxDiff(WritableRaster raster, WritableRaster raster2) {
        ComponentSampleModel csm = (ComponentSampleModel) raster.getSampleModel();
        ComponentSampleModel csm2 = (ComponentSampleModel) raster2.getSampleModel();
        DataBuffer db = raster.getDataBuffer();
        DataBuffer db2 = raster2.getDataBuffer();
        int blockSize = avgPixelValueBlockSize;
        if (blockSize > 1) {
            int w = csm.getWidth();
            int h = csm.getHeight();
            int maxY = (h / blockSize - 1) * blockSize;
            int maxX = (w / blockSize - 1) * blockSize;
            int[] samples = new int[blockSize * blockSize];
            int diff, maxDiff = 0;
            for (int b = 0; b < csm.getNumBands(); b++)
                for (int y = 0; y < maxY; y += blockSize) {
                    for (int x = 0; x < maxX; x += blockSize) {
                        if (maxDiff < (diff = Math.abs(
                                sum(csm.getSamples(x, y, blockSize, blockSize, b, samples, db))
                                        - sum(csm2.getSamples(x, y, blockSize, blockSize, b, samples, db2)))))
                            maxDiff = diff;
                    }
                }
            return maxDiff / samples.length;
        }
        return (db.getDataType() == DataBuffer.TYPE_BYTE)
                ? maxDiff(csm, ((DataBufferByte) db).getBankData(), csm2, ((DataBufferByte) db2).getBankData())
                : maxDiff(csm, toShortData(db), csm2, toShortData(db2));
    }

    /**
     * Executes the sum operation.
     *
     * @param samples the samples.
     * @return the operation result.
     */
    private int sum(int[] samples) {
        int sum = 0;
        for (int sample : samples)
            sum += sample;
        return sum;
    }

    /**
     * Executes the max diff operation.
     *
     * @param csm   the csm.
     * @param data  the data.
     * @param csm2  the csm2.
     * @param data2 the data2.
     * @return the operation result.
     */
    private int maxDiff(ComponentSampleModel csm, short[] data, ComponentSampleModel csm2, short[] data2) {
        int w = csm.getWidth() * csm.getPixelStride();
        int h = csm.getHeight();
        int stride = csm.getScanlineStride();
        int stride2 = csm2.getScanlineStride();
        int diff, maxDiff = 0;
        for (int y = 0; y < h; y++) {
            for (int j = w, i = y * stride, i2 = y * stride2; j-- > 0; i++, i2++) {
                if (maxDiff < (diff = Math.abs(data[i] - data2[i2])))
                    maxDiff = diff;
            }
        }
        return maxDiff;
    }

    /**
     * Executes the max diff operation.
     *
     * @param csm    the csm.
     * @param banks  the banks.
     * @param csm2   the csm2.
     * @param banks2 the banks2.
     * @return the operation result.
     */
    private int maxDiff(ComponentSampleModel csm, byte[][] banks, ComponentSampleModel csm2, byte[][] banks2) {
        int w = csm.getWidth();
        int h = csm.getHeight();
        int bands = csm.getNumBands();
        int stride = csm.getScanlineStride();
        int pixelStride = csm.getPixelStride();
        int[] bankIndices = csm.getBankIndices();
        int[] bandOffsets = csm.getBandOffsets();
        int stride2 = csm2.getScanlineStride();
        int pixelStride2 = csm2.getPixelStride();
        int[] bankIndices2 = csm2.getBankIndices();
        int[] bandOffsets2 = csm2.getBandOffsets();
        int diff, maxDiff = 0;
        for (int b = 0; b < bands; b++) {
            byte[] bank = banks[bankIndices[b]];
            byte[] bank2 = banks2[bankIndices2[b]];
            int off = bandOffsets[b];
            int off2 = bandOffsets2[b];
            for (int y = 0; y < h; y++) {
                for (int x = w, i = y * stride + off,
                        i2 = y * stride2 + off2; x-- > 0; i += pixelStride, i2 += pixelStride2) {
                    if (maxDiff < (diff = Math.abs(bank[i] - bank2[i2])))
                        maxDiff = diff;
                }
            }
        }
        return maxDiff;
    }

    /**
     * Defines the Handler contract.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public interface Handler {

        /**
         * Executes the new output stream operation.
         *
         * @param transcoder the transcoder.
         * @param dataset    the dataset.
         * @return the operation result.
         * @throws IOException if the operation cannot be completed.
         */
        OutputStream newOutputStream(Transcoder transcoder, Attributes dataset) throws IOException;

    }

}
