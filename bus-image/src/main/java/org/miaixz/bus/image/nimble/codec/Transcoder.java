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
package org.miaixz.bus.image.nimble.codec;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
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

import javax.imageio.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Transcoder implements Closeable {

    public static final ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    private static final int BUFFER_SIZE = 8192;
    private static final int[] cmTags = {
            Tag.RedPaletteColorLookupTableDescriptor,
            Tag.GreenPaletteColorLookupTableDescriptor,
            Tag.BluePaletteColorLookupTableDescriptor,
            Tag.PaletteColorLookupTableUID,
            Tag.RedPaletteColorLookupTableData,
            Tag.GreenPaletteColorLookupTableData,
            Tag.BluePaletteColorLookupTableData,
            Tag.SegmentedRedPaletteColorLookupTableData,
            Tag.SegmentedGreenPaletteColorLookupTableData,
            Tag.SegmentedBluePaletteColorLookupTableData,
            Tag.ICCProfile
    };
    private final ImageInputStream dis;
    private final String srcTransferSyntax;
    private final TransferSyntaxType srcTransferSyntaxType;
    private final Attributes dataset;
    private boolean retainFileMetaInformation;
    private boolean includeFileMetaInformation;
    private boolean includeImplementationVersionName = true;
    private boolean nullifyPixelData;
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;
    private boolean closeInputStream = true;
    private boolean closeOutputStream = true;
    private boolean deleteBulkDataFiles = true;
    private String destTransferSyntax;
    private TransferSyntaxType destTransferSyntaxType;
    private boolean lossyCompression;
    private int bitsCompressed = 0;
    private int maxPixelValueError = -1;
    private int avgPixelValueBlockSize = 1;
    private Attributes fileMetaInformation;
    private ImageOutputStream dos;
    private Handler handler;
    private ImageDescriptor imageDescriptor;
    private ImageDescriptor compressorImageDescriptor;
    private EncapsulatedPixelDataImageInputStream encapsulatedPixelData;
    private ImageReaderFactory.ImageReaderParam decompressorParam;
    private ImageReader decompressor;
    private ImageReadParam decompressParam;
    private ImageWriterFactory.ImageWriterParam compressorParam;
    private ImageWriter compressor;
    private ImageWriteParam compressParam;
    private ImageReader verifier;
    private ImageReadParam verifyParam;
    private boolean ybr2rgb;
    private boolean palette2rgb;
    private BufferedImage originalBi;
    private BufferedImage bi;
    private BufferedImage bi2;
    private String pixelDataBulkDataURI;
    private byte[] buffer;
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
                    Logger.info("Odd length of Pixel Data Fragment: {} - append NULL byte to ensure even length", length);
                    dos.write(0);
                }
            }
        }

        @Override
        public void startDataset(ImageInputStream dis) {

        }

        @Override
        public void endDataset(ImageInputStream dis) {

        }
    };

    public Transcoder(File f) throws IOException {
        this(new ImageInputStream(f));
    }

    public Transcoder(InputStream in) throws IOException {
        this(new ImageInputStream(in));
    }

    public Transcoder(InputStream in, String tsuid) throws IOException {
        this(new ImageInputStream(in, tsuid));
    }

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

    private static byte[] to16BitsAllocated(byte[] b, int off, int len, byte[] buf, int j0) {
        for (int i = 0, j = j0; i < len; i++, j++, j++) {
            buf[j] = b[off + i];
        }
        return buf;
    }

    private static void bgr2rgb(byte[] bs) {
        for (int i = 0, j = 2; j < bs.length; i += 3, j += 3) {
            byte b = bs[i];
            bs[i] = bs[j];
            bs[j] = b;
        }
    }

    private static short[] toShortData(DataBuffer db) {
        return db.getDataType() == DataBuffer.TYPE_SHORT
                ? ((DataBufferShort) db).getData()
                : ((DataBufferUShort) db).getData();
    }

    public void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = Objects.requireNonNull(encOpts);
    }

    public void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        dis.setConcatenateBulkDataFiles(catBlkFiles);
    }

    public void setIncludeBulkData(ImageInputStream.IncludeBulkData includeBulkData) {
        dis.setIncludeBulkData(includeBulkData);
    }

    public void setBulkDataDescriptor(BulkDataDescriptor bulkDataDescriptor) {
        dis.setBulkDataDescriptor(bulkDataDescriptor);
    }

    public void setBulkDataDirectory(File blkDirectory) {
        dis.setBulkDataDirectory(blkDirectory);
    }

    public boolean isCloseInputStream() {
        return closeInputStream;
    }

    public void setCloseInputStream(boolean closeInputStream) {
        this.closeInputStream = closeInputStream;
    }

    public boolean isCloseOutputStream() {
        return closeOutputStream;
    }

    public void setCloseOutputStream(boolean closeOutputStream) {
        this.closeOutputStream = closeOutputStream;
    }

    public boolean isDeleteBulkDataFiles() {
        return deleteBulkDataFiles;
    }

    public void setDeleteBulkDataFiles(boolean deleteBulkDataFiles) {
        this.deleteBulkDataFiles = deleteBulkDataFiles;
    }

    public boolean isIncludeFileMetaInformation() {
        return includeFileMetaInformation;
    }

    public void setIncludeFileMetaInformation(boolean includeFileMetaInformation) {
        this.includeFileMetaInformation = includeFileMetaInformation;
    }

    public boolean isRetainFileMetaInformation() {
        return retainFileMetaInformation;
    }

    public void setRetainFileMetaInformation(boolean retainFileMetaInformation) {
        this.retainFileMetaInformation = retainFileMetaInformation;
    }

    public boolean isIncludeImplementationVersionName() {
        return includeImplementationVersionName;
    }

    public void setIncludeImplementationVersionName(boolean includeImplementationVersionName) {
        this.includeImplementationVersionName = includeImplementationVersionName;
    }

    public boolean isNullifyPixelData() {
        return nullifyPixelData;
    }

    public void setNullifyPixelData(boolean nullifyPixelData) {
        this.nullifyPixelData = nullifyPixelData;
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public String getSourceTransferSyntax() {
        return dis.getTransferSyntax();
    }

    public TransferSyntaxType getSourceTransferSyntaxType() {
        return srcTransferSyntaxType;
    }

    public String getDestinationTransferSyntax() {
        return destTransferSyntax;
    }

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

    private String adaptSuitableSyntax(String dstTsuid) {
        int bitsStored = imageDescriptor.getBitsStored();
        if (bitsStored == 1 && imageDescriptor.getBitsAllocated() == 1) {
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
                return !imageDescriptor.isSigned() && bitsStored <= 12 ? dstTsuid :
                        bitsStored <= 16 ? UID.JPEGLosslessSV1.uid : UID.ExplicitVRLittleEndian.uid;
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

    public String getPixelDataBulkDataURI() {
        return pixelDataBulkDataURI;
    }

    public void setPixelDataBulkDataURI(String pixelDataBulkDataURI) {
        this.pixelDataBulkDataURI = pixelDataBulkDataURI;
    }

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

    private void initDecompressor() {
        decompressorParam = ImageReaderFactory.getImageReaderParam(srcTransferSyntax);
        if (decompressorParam == null)
            throw new UnsupportedOperationException(
                    "Unsupported Transfer Syntax: " + srcTransferSyntax);

        this.decompressor = ImageReaderFactory.getImageReader(decompressorParam);
        Logger.debug("Decompressor: {}", decompressor.getClass().getName());

        this.decompressParam = decompressor.getDefaultReadParam();
    }

    private void initCompressor(String tsuid) {
        compressorParam = ImageWriterFactory.getImageWriterParam(tsuid);
        if (compressorParam == null)
            throw new UnsupportedOperationException(
                    "Unsupported Transfer Syntax: " + tsuid);

        this.compressor = ImageWriterFactory.getImageWriter(compressorParam);
        Logger.debug("Compressor: {}", compressor.getClass().getName());

        this.compressParam = compressor.getDefaultWriteParam();
        setCompressParams(compressorParam.getImageWriteParams());
    }

    public void setCompressParams(Property... imageWriteParams) {
        if (compressorParam == null) return;
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
            ImageReaderFactory.ImageReaderParam readerParam =
                    ImageReaderFactory.getImageReaderParam(destTransferSyntax);
            if (readerParam == null)
                throw new UnsupportedOperationException(
                        "Unsupported Transfer Syntax: " + destTransferSyntax);

            this.verifier = ImageReaderFactory.getImageReader(readerParam);
            this.verifyParam = verifier.getDefaultReadParam();
            Logger.debug("Verifier: {}", verifier.getClass().getName());
        }
    }

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

    private void initEncapsulatedPixelData() throws IOException {
        encapsulatedPixelData = new EncapsulatedPixelDataImageInputStream(dis, imageDescriptor);
    }

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

    private void skipPixelData() throws IOException {
        int length = dis.length();
        if (length == -1) {
            dis.readValue(dis, dataset);
        } else {
            StreamKit.skipFully(dis, length);
        }
    }

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
                Logger.info("Odd length of Pixel Data: {} - append NULL byte to ensure even length", length);
                dos.write(0);
            }
        }
    }

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
                    ? BufferedImages.convertShortsToBytes(originalBi, bi) // workaround for JPEG codec issue
                    : originalBi;
            compressFrame(i);
        }
        dis.skipFully(padding);
        dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }

    private void setPixelDataBulkData(VR vr) {
        if (pixelDataBulkDataURI != null)
            dataset.setValue(Tag.PixelData, vr, new BulkData(null, pixelDataBulkDataURI, false));
    }

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
                    Logger.info("Adjust invalid Bits Stored: {} of {} to 12",
                            imageDescriptor.getBitsStored(), srcTransferSyntaxType);
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
                Logger.warn("Converting PALETTE_COLOR model into a lossy format is not recommended, prefer a lossless format");
            } else if ((pmi.isSubSampled() && !srcTransferSyntaxType.isPixeldataEncapsulated())
                    || (pmi == Photometric.YBR_FULL
                    && (TransferSyntaxType.isYBRCompression(destTransferSyntax) ||
                    destTransferSyntaxType == TransferSyntaxType.JPEG_LS))) {
                ybr2rgb = true;
                pmi = Photometric.RGB;
                Logger.debug("Conversion to an RGB color model is required before compression.");
            } else {
                if (destTransferSyntaxType.adjustBitsStoredTo12(dataset)) {
                    Logger.debug("Adjust Bits Stored: {} for {} to 12",
                            imageDescriptor.getBitsStored(), destTransferSyntaxType);
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
                    dataset.setFloat(Tag.LossyImageCompressionRatio, VR.DS,
                            ((Number) compressParam.getClass()
                                    .getMethod("getCompressionRatiofactor")
                                    .invoke(compressParam)).floatValue());
                } catch (Exception ignore) {
                }
            }
        }
    }

    private Photometric pmiForCompression(Photometric pmi) {
        return pmi.isYBR() && (destTransferSyntaxType == TransferSyntaxType.JPEG_LOSSLESS ||
                destTransferSyntaxType == TransferSyntaxType.JPEG_LS) ? Photometric.RGB : pmi;
    }

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
            Logger.debug("Extracted embedded overlay #{} from bit #{}", (gg0000 >>> 17) + 1, ovlyBitPosition);
        }
    }

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

    private void nullifyUnusedBits(short[] data) {
        int mask = (1 << imageDescriptor.getBitsStored()) - 1;
        for (int i = 0; i < data.length; i++)
            data[i] &= mask;
    }

    private void extendSignUnusedBits(short[] data) {
        int unused = 32 - imageDescriptor.getBitsStored();
        for (int i = 0; i < data.length; i++)
            data[i] = (short) ((data[i] << unused) >> unused);
    }

    private BufferedImage decompressFrame(int frameIndex) throws IOException {
        decompressor.setInput(decompressorParam.patchJPEGLS != null
                ? new PatchJPEGLSInputStream(encapsulatedPixelData, decompressorParam.patchJPEGLS)
                : encapsulatedPixelData);
        if (srcTransferSyntaxType == TransferSyntaxType.RLE)
            initBufferedImage();
        decompressParam.setDestination(originalBi);
        long start = System.currentTimeMillis();
        originalBi = adjustColorModel(decompressor.read(0, decompressParam));
        long end = System.currentTimeMillis();
        if (Logger.isDebug())
            Logger.debug("Decompressed frame #{} in {} ms, ratio 1:{}", frameIndex + 1, end - start,
                    (float) imageDescriptor.getFrameLength() / encapsulatedPixelData.getStreamPosition());
        encapsulatedPixelData.seekNextFrame();
        return originalBi;
    }

    private BufferedImage adjustColorModel(BufferedImage bi) {
        Photometric pmi = imageDescriptor.getPhotometricInterpretation();
        if (pmi == Photometric.PALETTE_COLOR
                && !(bi.getColorModel() instanceof PaletteColorModel)) {
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

    private void compressFrame(int frameIndex) throws IOException {
        CacheImageOutputStream ios = new CacheImageOutputStream(compressorImageDescriptor);
        compressor.setOutput(compressorParam.patchJPEGLS != null
                ? new PatchJPEGLSOutputStream(ios, compressorParam.patchJPEGLS)
                : ios);
        long start = System.currentTimeMillis();
        compressor.write(null, new IIOImage(bi, null, null), compressParam);
        long end = System.currentTimeMillis();
        int length = (int) ios.getStreamPosition();
        if (Logger.isDebug())
            Logger.debug("Compressed frame #{} in {} ms, ratio {}:1", frameIndex + 1, end - start,
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

    private void readFully(byte[][] bb) throws IOException {
        for (byte[] b : bb) {
            dis.readFully(b);
        }
        if (dis.bigEndian() && dis.vr() == VR.OW)
            ByteKit.swapShorts(bb);
    }

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

    private byte[] buffer() {
        if (buffer == null)
            buffer = new byte[BUFFER_SIZE];
        return buffer;
    }

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

    private void write(SampleModel sm, short[] data) throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        int stride = ((ComponentSampleModel) sm).getScanlineStride();
        byte[] b = new byte[w * 2];
        for (int y = 0; y < h; ++y) {
            for (int i = 0, j = y * stride; i < b.length; ) {
                short s = data[j++];
                b[i++] = (byte) s;
                b[i++] = (byte) (s >> 8);
            }
            dos.write(b);
        }
    }

    private void write(SampleModel sm, int[] data) throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        int stride = ((SinglePixelPackedSampleModel) sm).getScanlineStride();
        byte[] b = new byte[w * 3];
        for (int y = 0; y < h; ++y) {
            for (int i = 0, j = y * stride; i < b.length; ) {
                int s = data[j++];
                b[i++] = (byte) (s >> 16);
                b[i++] = (byte) (s >> 8);
                b[i++] = (byte) s;
            }
            dos.write(b);
        }
    }

    private void initDicomOutputStream() throws IOException {
        dos = new ImageOutputStream(handler.newOutputStream(this, dataset),
                includeFileMetaInformation ? UID.ExplicitVRLittleEndian.uid : destTransferSyntax);
        dos.setEncodingOptions(encOpts);
    }

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
        int dataType = bitsAllocated > 8
                ? (signed ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_USHORT)
                : DataBuffer.TYPE_BYTE;
        ColorModel cm = pmi.createColorModel(bitsStored, dataType, sRGB, dataset);
        SampleModel sm = pmi.createSampleModel(dataType, cols, rows, samples, banded);
        WritableRaster raster = Raster.createWritableRaster(sm, null);
        originalBi = new BufferedImage(cm, raster, false, null);
    }

    private void verify(javax.imageio.stream.ImageOutputStream cache, int index)
            throws IOException {
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
        if (Logger.isDebug())
            Logger.debug("Verified compressed frame #{} in {} ms - max pixel value error: {}",
                    index + 1, end - start, maxDiff);
        if (maxDiff > maxPixelValueError)
            throw new InternalException("Decompressed pixel data differs up to " + maxDiff + " from original pixel data");

        cache.seek(prevStreamPosition);
        cache.setBitOffset(prevBitOffset);
    }

    private int maxDiff(WritableRaster raster, WritableRaster raster2) {
        ComponentSampleModel csm =
                (ComponentSampleModel) raster.getSampleModel();
        ComponentSampleModel csm2 =
                (ComponentSampleModel) raster2.getSampleModel();
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
                                sum(csm.getSamples(
                                        x, y, blockSize, blockSize, b, samples, db))
                                        - sum(csm2.getSamples(
                                        x, y, blockSize, blockSize, b, samples, db2)))))
                            maxDiff = diff;
                    }
                }
            return maxDiff / samples.length;
        }
        return (db.getDataType() == DataBuffer.TYPE_BYTE)
                ? maxDiff(csm, ((DataBufferByte) db).getBankData(), csm2, ((DataBufferByte) db2).getBankData())
                : maxDiff(csm, toShortData(db), csm2, toShortData(db2));
    }

    private int sum(int[] samples) {
        int sum = 0;
        for (int sample : samples)
            sum += sample;
        return sum;
    }

    private int maxDiff(ComponentSampleModel csm, short[] data,
                        ComponentSampleModel csm2, short[] data2) {
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

    private int maxDiff(ComponentSampleModel csm, byte[][] banks,
                        ComponentSampleModel csm2, byte[][] banks2) {
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
                for (int x = w, i = y * stride + off, i2 = y * stride2 + off2;
                     x-- > 0; i += pixelStride, i2 += pixelStride2) {
                    if (maxDiff < (diff = Math.abs(bank[i] - bank2[i2])))
                        maxDiff = diff;
                }
            }
        }
        return maxDiff;
    }

    public interface Handler {
        OutputStream newOutputStream(Transcoder transcoder, Attributes dataset) throws IOException;
    }

}
