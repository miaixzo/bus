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
package org.miaixz.bus.image.nimble.reader;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.io.BulkDataDescriptor;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageInputStream.IncludeBulkData;
import org.miaixz.bus.image.nimble.LookupTable;
import org.miaixz.bus.image.nimble.*;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.image.nimble.codec.ImageReaderFactory;
import org.miaixz.bus.image.nimble.codec.TransferSyntaxType;
import org.miaixz.bus.image.nimble.codec.jpeg.PatchJPEGLS;
import org.miaixz.bus.image.nimble.codec.jpeg.PatchJPEGLSImageInputStream;
import org.miaixz.bus.image.nimble.opencv.NativeImageReader;
import org.miaixz.bus.image.nimble.stream.ImageInputStreamAdapter;
import org.miaixz.bus.image.nimble.stream.ImagePixelInputStream;
import org.miaixz.bus.image.nimble.stream.SegmentedImageStream;
import org.miaixz.bus.logger.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileImageInputStream;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeDCMImageReader extends ImageReader implements Closeable {

    public static final String POST_PIXEL_DATA = "postPixelData";

    private javax.imageio.stream.ImageInputStream iis;

    private ImageInputStream dis;

    private ImagePixelInputStream epdiis;

    private DicomMetaData metadata;

    private BulkData pixelData;

    private Fragments pixelDataFragments;

    private byte[] pixeldataBytes;

    private int pixelDataLength;

    private VR pixelDataVR;

    private File pixelDataFile;

    private int frames;

    private int flushedFrames;

    private int width;

    private int height;

    private ImageReader decompressor;

    private boolean rle;

    private PatchJPEGLS patchJpegLS;

    private int samples;

    private boolean banded;

    private int bitsStored;

    private int bitsAllocated;

    private int dataType;

    private int frameLength;

    private Photometric pmi;
    private Photometric pmiAfterDecompression;
    private ImageDescriptor imageDescriptor;

    public NativeDCMImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    public static void generateOffsetLengths(Fragments pixelData, int frames, byte[] basicOffsetTable, long start) {
        long lastOffset = 0;
        BulkData lastFrag = null;
        for (int frame = 0; frame < frames; frame++) {
            long offset = frame > 0 ? 1 : 0;
            int offsetStart = frame * 4;
            if (basicOffsetTable.length >= offsetStart + 4) {
                offset = ByteKit.bytesToIntLE(basicOffsetTable, offsetStart);
                if (offset != 1) {
                    // Handle > 4 gb total image size by assuming incrementing modulo 4gb
                    offset = offset | (lastOffset & 0xFFFFFF00000000l);
                    if (offset < lastOffset) offset += 0x100000000l;
                    lastOffset = offset;
                    Logger.trace("Found offset {} for frame {}", offset, frame);
                }
            }
            long position = -1;
            if (offset != 1) {
                position = start + offset + 8;
            }
            BulkData frag = new BulkData("compressedPixelData://", position, -1, false);
            if (null != lastFrag && position != -1) {
                lastFrag.setLength(position - 8 - lastFrag.offset());
            }
            lastFrag = frag;
            pixelData.add(frag);
            if (offset == 0 && frame > 0) {
                start = -1;
            }
        }
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly,
                         boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        resetInternalState();
        if (input instanceof InputStream) {
            try {
                dis = (input instanceof ImageInputStream)
                        ? (ImageInputStream) input
                        : new ImageInputStream((InputStream) input);
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        } else if (input instanceof DicomMetaData) {
            DicomMetaData metadata = (DicomMetaData) input;
            initPixelDataFromAttributes(metadata.getAttributes());
            initPixelDataFile();
            setMetadata(metadata);
        } else {
            iis = (javax.imageio.stream.ImageInputStream) input;
        }
    }

    private void initPixelDataFromAttributes(Attributes ds) {
        VR.Holder holder = new VR.Holder();
        Object value = ds.getValue(Tag.PixelData, holder);
        if (null != value) {
            imageDescriptor = new ImageDescriptor(ds);
            pixelDataVR = holder.vr;
            if (value instanceof BulkData) {
                pixelData = (BulkData) value;
                pixelDataLength = pixelData.length();
            } else if (value instanceof byte[]) {
                pixeldataBytes = (byte[]) value;
                pixelDataLength = pixeldataBytes.length;
            } else { // value instanceof Fragments
                pixelDataFragments = (Fragments) value;
                pixelDataLength = -1;
            }
        }
    }

    private void initPixelDataFile() {
        if (null != pixelData)
            pixelDataFile = pixelData.getFile();
        else if (null != pixelDataFragments)
            pixelDataFile = pixelDataFragmentsFile(pixelDataFragments);
    }

    private File pixelDataFragmentsFile(Fragments pixelDataFragments) {
        File f = null;
        for (Object frag : pixelDataFragments) {
            if (frag instanceof BulkData)
                if (null == f)
                    f = ((BulkData) frag).getFile();
                else if (!f.equals(((BulkData) frag).getFile()))
                    throw new UnsupportedOperationException(
                            "data fragments in individual bulk data files not supported");
        }
        return f;
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        readMetadata();
        return frames;
    }

    @Override
    public int getWidth(int frameIndex) throws IOException {
        readMetadata();
        checkIndex(frameIndex);
        return width;
    }

    @Override
    public int getHeight(int frameIndex) throws IOException {
        readMetadata();
        checkIndex(frameIndex);
        return height;
    }

    @Override
    public ImageTypeSpecifier getRawImageType(int frameIndex)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);

        if (null == decompressor)
            return createImageType(bitsStored, dataType, banded);

        if (rle)
            return createImageType(bitsStored, dataType, true);

        openiis();
        try {
            decompressor.setInput(iisOfFrame(0));
            return decompressor.getRawImageType(0);
        } finally {
            closeiis();
        }
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);

        ImageTypeSpecifier imageType;
        if (pmi.isMonochrome())
            imageType = createImageType(8, DataBuffer.TYPE_BYTE, false);
        else if (null == decompressor)
            imageType = createImageType(bitsStored, dataType, banded);
        else if (rle)
            imageType = createImageType(bitsStored, dataType, true);
        else {
            openiis();
            try {
                decompressor.setInput(iisOfFrame(0));
                return decompressor.getImageTypes(0);
            } finally {
                closeiis();
            }
        }

        return Collections.singletonList(imageType).iterator();
    }

    private void openiis() throws IOException {
        if (null == iis) {
            if (null != pixelDataFile) {
                iis = new FileImageInputStream(pixelDataFile);
            } else if (null != pixeldataBytes) {
                iis = new SegmentedImageStream(pixeldataBytes);
            }
        }
    }

    private void closeiis() throws IOException {
        if ((null != pixelDataFile || null != pixeldataBytes) && null != iis) {
            iis.close();
            iis = null;
        }
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new NativeDCMImageReadParam();
    }

    /**
     * Gets the stream metadata.  May not contain post pixel data unless
     * there are no images or the getStreamMetadata has been called with the post pixel data
     * node being specified.
     */
    @Override
    public DicomMetaData getStreamMetadata() throws IOException {
        readMetadata();
        return metadata;
    }

    /**
     * Gets the stream metadata.
     * If nodeNames contains POST_PIXEL_DATA consts "postPixelData" then
     * read the post pixel data as well.  In an InputStream instance that can
     * only safely be done after all pixel data is read.  On imageInputStream it
     * may be slow for large multiframes, but can safely be done at any time.
     */
    @Override
    public DicomMetaData getStreamMetadata(String formatName,
                                           Set<String> nodeNames)
            throws IOException {
        DicomMetaData ret = getStreamMetadata();
        if (null != nodeNames && nodeNames.contains(POST_PIXEL_DATA)) {
            readPostPixeldata();
            return getStreamMetadata();
        }
        return ret;
    }

    @Override
    public IIOMetadata getImageMetadata(int frameIndex) {
        return null;
    }

    @Override
    public boolean canReadRaster() {
        return true;
    }

    @Override
    public Raster readRaster(int frameIndex, ImageReadParam param)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);

        openiis();
        try {
            if (null != decompressor) {
                decompressor.setInput(iisOfFrame(frameIndex));


                Logger.debug("Start decompressing frame #" + (frameIndex + 1));
                Raster wr = pmiAfterDecompression == pmi && decompressor.canReadRaster()
                        ? decompressor.readRaster(0, decompressParam(param))
                        : decompressor.read(0, decompressParam(param)).getRaster();

                Logger.debug("Finished decompressing frame #" + (frameIndex + 1));
                return wr;
            }
            WritableRaster wr = Raster.createWritableRaster(
                    createSampleModel(dataType, banded), null);
            DataBuffer buf = wr.getDataBuffer();
            if (null != dis) {
                dis.skipFully((frameIndex - flushedFrames) * frameLength);
                flushedFrames = frameIndex + 1;
            } else if (null != pixeldataBytes) {
                iis.setByteOrder(bigEndian()
                        ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN);
                iis.seek(frameIndex * frameLength);
            } else {
                iis.setByteOrder(bigEndian()
                        ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN);
                iis.seek(pixelData.offset() + frameIndex * frameLength);
            }
            if (buf instanceof DataBufferByte) {
                byte[][] data = ((DataBufferByte) buf).getBankData();
                for (byte[] bs : data)
                    if (null != dis)
                        dis.readFully(bs);
                    else
                        iis.readFully(bs);
                if (pixelDataVR == VR.OW && bigEndian())
                    ByteKit.swapShorts(data);
            } else {
                short[] data = ((DataBufferUShort) buf).getData();
                if (null != dis)
                    dis.readFully(data, 0, data.length);
                else
                    iis.readFully(data, 0, data.length);
            }
            return wr;
        } finally {
            closeiis();
        }
    }

    private boolean bigEndian() {
        return metadata.bigEndian();
    }

    private String getTransferSyntaxUID() {
        return metadata.getTransferSyntaxUID();
    }

    private ImageReadParam decompressParam(ImageReadParam param) {
        ImageReadParam decompressParam = decompressor.getDefaultReadParam();
        ImageTypeSpecifier imageType = null;
        BufferedImage dest = null;
        if (null != param) {
            imageType = param.getDestinationType();
            dest = param.getDestination();
        }
        if (rle && null == imageType && null == dest)
            imageType = createImageType(bitsStored, dataType, true);
        decompressParam.setDestinationType(imageType);
        decompressParam.setDestination(dest);
        return decompressParam;
    }

    @Override
    public BufferedImage read(int frameIndex, ImageReadParam param)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);

        WritableRaster raster;
        if (null != decompressor) {
            openiis();
            try {
                javax.imageio.stream.ImageInputStream iisOfFrame = iisOfFrame(frameIndex);
                // Reading this up front sets the required values so that opencv succeeds - it is less than optimal performance wise
                iisOfFrame.length();
                decompressor.setInput(iisOfFrame);
                Logger.debug("Start decompressing frame #{}", (frameIndex + 1));
                BufferedImage bi = decompressor.read(0, decompressParam(param));
                Logger.debug("Finished decompressing frame #{}", (frameIndex + 1));
                if (samples > 1 && bi.getColorModel().getColorSpace().getType() ==
                        (pmiAfterDecompression.isYBR() ? ColorSpace.TYPE_YCbCr : ColorSpace.TYPE_RGB))
                    return bi;

                raster = bi.getRaster();
            } finally {
                closeiis();
            }
        } else
            raster = (WritableRaster) readRaster(frameIndex, param);

        ColorModel cm;
        if (pmi.isMonochrome()) {
            int[] overlayGroupOffsets = getActiveOverlayGroupOffsets(param);
            byte[][] overlayData = new byte[overlayGroupOffsets.length][];
            for (int i = 0; i < overlayGroupOffsets.length; i++) {
                overlayData[i] = extractOverlay(overlayGroupOffsets[i], raster);
            }
            cm = createColorModel(8, DataBuffer.TYPE_BYTE);
            SampleModel sm = createSampleModel(DataBuffer.TYPE_BYTE, false);
            raster = applyLUTs(raster, frameIndex, param, sm, 8);
            for (int i = 0; i < overlayGroupOffsets.length; i++) {
                applyOverlay(overlayGroupOffsets[i],
                        raster, frameIndex, param, 8, overlayData[i]);
            }
        } else {
            cm = createColorModel(bitsStored, dataType);
        }
        return new BufferedImage(cm, raster, false, null);
    }

    private byte[] extractOverlay(int gg0000, WritableRaster raster) {
        Attributes attrs = metadata.getAttributes();

        if (attrs.getInt(Tag.OverlayBitsAllocated | gg0000, 1) == 1)
            return null;

        int ovlyRows = attrs.getInt(Tag.OverlayRows | gg0000, 0);
        int ovlyColumns = attrs.getInt(Tag.OverlayColumns | gg0000, 0);
        int bitPosition = attrs.getInt(Tag.OverlayBitPosition | gg0000, 0);

        int mask = 1 << bitPosition;
        int length = ovlyRows * ovlyColumns;

        byte[] ovlyData = new byte[(((length + 7) >>> 3) + 1) & (~1)];
        if (bitPosition < bitsStored)
            Logger.info("Ignore embedded overlay #{} from bit #{} < bits stored: {}",
                    (gg0000 >>> 17) + 1, bitPosition, bitsStored);
        else
            Overlays.extractFromPixeldata(raster, mask, ovlyData, 0, length);
        return ovlyData;
    }

    public javax.imageio.stream.ImageInputStream iisOfFrame(int frameIndex) throws IOException {
        javax.imageio.stream.ImageInputStream iisOfFrame;
        if (null != epdiis) {
            seekFrame(frameIndex);
            iisOfFrame = epdiis;
        } else if (null == pixelDataFragments) {
            return null;
        } else {
            iisOfFrame = new SegmentedImageStream(
                    iis, pixelDataFragments, frames == 1 ? -1 : frameIndex);
            ((SegmentedImageStream) iisOfFrame).setImageDescriptor(imageDescriptor);
        }
        return null != patchJpegLS
                ? new PatchJPEGLSImageInputStream(iisOfFrame, patchJpegLS)
                : iisOfFrame;
    }

    private void seekFrame(int frameIndex) throws IOException {
        assert frameIndex >= flushedFrames;
        if (frameIndex == flushedFrames)
            epdiis.seekCurrentFrame();
        else while (frameIndex > flushedFrames) {
            if (!epdiis.seekNextFrame()) {
                throw new IOException("Data Fragments only contains " + (flushedFrames + 1) + " frames");
            }
            flushedFrames++;
        }
    }

    private void applyOverlay(int gg0000, WritableRaster raster,
                              int frameIndex, ImageReadParam param, int outBits, byte[] ovlyData) {
        Attributes ovlyAttrs = metadata.getAttributes();
        int grayscaleValue = 0xffff;
        if (param instanceof NativeDCMImageReadParam) {
            NativeDCMImageReadParam dParam = (NativeDCMImageReadParam) param;
            Attributes psAttrs = dParam.getPresentationState();
            if (null != psAttrs) {
                if (psAttrs.containsValue(Tag.OverlayData | gg0000))
                    ovlyAttrs = psAttrs;
                grayscaleValue = Overlays.getRecommendedDisplayGrayscaleValue(
                        psAttrs, gg0000);
            } else
                grayscaleValue = dParam.getOverlayGrayscaleValue();
        }
        Overlays.applyOverlay(null != ovlyData ? 0 : frameIndex, raster,
                ovlyAttrs, gg0000, grayscaleValue >>> (Normal._16 - outBits), ovlyData);
    }

    private int[] getActiveOverlayGroupOffsets(ImageReadParam param) {
        if (param instanceof NativeDCMImageReadParam) {
            NativeDCMImageReadParam dParam = (NativeDCMImageReadParam) param;
            Attributes psAttrs = dParam.getPresentationState();
            if (null != psAttrs)
                return Overlays.getActiveOverlayGroupOffsets(psAttrs);
            else
                return Overlays.getActiveOverlayGroupOffsets(
                        metadata.getAttributes(),
                        dParam.getOverlayActivationMask());
        }
        return Overlays.getActiveOverlayGroupOffsets(
                metadata.getAttributes(),
                0xffff);
    }

    private WritableRaster applyLUTs(WritableRaster raster,
                                     int frameIndex, ImageReadParam param, SampleModel sm, int outBits) {
        WritableRaster destRaster =
                sm.getDataType() == raster.getSampleModel().getDataType()
                        ? raster
                        : Raster.createWritableRaster(sm, null);
        Attributes imgAttrs = metadata.getAttributes();
        StoredValue sv = StoredValue.valueOf(imgAttrs);
        LookupTableFactory lutParam = new LookupTableFactory(sv);
        NativeDCMImageReadParam dParam = param instanceof NativeDCMImageReadParam
                ? (NativeDCMImageReadParam) param
                : new NativeDCMImageReadParam();
        Attributes psAttrs = dParam.getPresentationState();
        if (null != psAttrs) {
            lutParam.setModalityLUT(psAttrs);
            lutParam.setVOI(
                    selectVOILUT(psAttrs,
                            imgAttrs.getString(Tag.SOPInstanceUID),
                            frameIndex + 1),
                    0, 0, false);
            lutParam.setPresentationLUT(psAttrs);
        } else {
            Attributes sharedFctGroups = imgAttrs.getNestedDataset(
                    Tag.SharedFunctionalGroupsSequence);
            Attributes frameFctGroups = imgAttrs.getNestedDataset(
                    Tag.PerFrameFunctionalGroupsSequence, frameIndex);
            lutParam.setModalityLUT(
                    selectFctGroup(imgAttrs, sharedFctGroups, frameFctGroups,
                            Tag.PixelValueTransformationSequence));
            if (dParam.getWindowWidth() != 0) {
                lutParam.setWindowCenter(dParam.getWindowCenter());
                lutParam.setWindowWidth(dParam.getWindowWidth());
            } else
                lutParam.setVOI(
                        selectFctGroup(imgAttrs, sharedFctGroups, frameFctGroups,
                                Tag.FrameVOILUTSequence),
                        dParam.getWindowIndex(),
                        dParam.getVOILUTIndex(),
                        dParam.isPreferWindow());
            if (dParam.isAutoWindowing())
                lutParam.autoWindowing(imgAttrs, raster);
            lutParam.setPresentationLUT(imgAttrs);
        }
        LookupTable lut = lutParam.createLUT(outBits);
        lut.lookup(raster, destRaster);
        return destRaster;
    }

    private Attributes selectFctGroup(Attributes imgAttrs,
                                      Attributes sharedFctGroups,
                                      Attributes frameFctGroups,
                                      int tag) {
        if (null == frameFctGroups) {
            return imgAttrs;
        }
        Attributes group = frameFctGroups.getNestedDataset(tag);
        if (null == group && null != sharedFctGroups) {
            group = sharedFctGroups.getNestedDataset(tag);
        }
        return null != group ? group : imgAttrs;
    }

    private Attributes selectVOILUT(Attributes psAttrs, String iuid, int frame) {
        Sequence voiLUTs = psAttrs.getSequence(Tag.SoftcopyVOILUTSequence);
        if (null != voiLUTs)
            for (Attributes voiLUT : voiLUTs) {
                Sequence refImgs = voiLUT.getSequence(Tag.ReferencedImageSequence);
                if (null == refImgs || refImgs.isEmpty()) {
                    return voiLUT;
                }

                for (Attributes refImg : refImgs) {
                    if (iuid.equals(refImg.getString(Tag.ReferencedSOPInstanceUID))) {
                        int[] refFrames = refImg.getInts(Tag.ReferencedFrameNumber);
                        if (null == refFrames || refFrames.length == 0) {
                            return voiLUT;
                        }

                        for (int refFrame : refFrames) {
                            if (refFrame == frame) {
                                return voiLUT;
                            }
                        }
                    }
                }
            }
        return null;
    }

    private void readMetadata() throws IOException {
        if (null != metadata)
            return;

        if (null != dis) {
            Attributes fmi = dis.readFileMetaInformation();
            Attributes ds = dis.readDataset(-1, Tag.PixelData);
            if (dis.tag() == Tag.PixelData) {
                imageDescriptor = new ImageDescriptor(ds);
                pixelDataVR = dis.vr();
                pixelDataLength = dis.length();
                if (pixelDataLength == -1)
                    epdiis = new ImagePixelInputStream(dis, imageDescriptor);
            } else {
                try {
                    dis.readAttributes(ds, -1, -1);
                } catch (EOFException e) {
                }
            }
            setMetadata(new DicomMetaData(fmi, ds));
            return;
        }
        if (null == iis)
            throw new IllegalStateException("Input not set");

        ImageInputStream dis = new ImageInputStream(new ImageInputStreamAdapter(iis));
        dis.setIncludeBulkData(IncludeBulkData.URI);
        dis.setBulkDataDescriptor(BulkDataDescriptor.PIXELDATA);
        dis.setURI("java:iis"); // avoid copy of pixeldata to temporary file
        Attributes fmi = dis.readFileMetaInformation();
        Attributes ds = dis.readDataset(-1, Tag.PixelData);
        if (dis.tag() == Tag.PixelData) {
            imageDescriptor = new ImageDescriptor(ds);
            pixelDataVR = dis.vr();
            pixelDataLength = dis.length();
        } else {
            try {
                dis.readAttributes(ds, -1, -1);
            } catch (EOFException e) {
            }
        }
        setMetadata(new DicomMetaData(fmi, ds));
        initPixelDataIIS(dis);
    }

    private void initPixelDataIIS(ImageInputStream dis) throws IOException {
        if (pixelDataLength == 0) return;
        if (pixelDataLength > 0) {
            pixelData = new BulkData("pixeldata://", dis.getPosition(), dis.length(), dis.bigEndian());
            metadata.getAttributes().setValue(Tag.PixelData, pixelDataVR, pixelData);
            return;
        }
        dis.readItemHeader();
        byte[] b = new byte[dis.length()];
        dis.readFully(b);

        long start = dis.getPosition();
        pixelDataFragments = new Fragments(pixelDataVR, dis.bigEndian(), frames);
        pixelDataFragments.add(b);

        generateOffsetLengths(pixelDataFragments, frames, b, start);
    }

    private void setMetadata(DicomMetaData metadata) {
        this.metadata = metadata;
        Attributes ds = metadata.getAttributes();
        if (pixelDataLength != 0) {
            frames = ds.getInt(Tag.NumberOfFrames, 1);
            width = ds.getInt(Tag.Columns, 0);
            height = ds.getInt(Tag.Rows, 0);
            samples = ds.getInt(Tag.SamplesPerPixel, 1);
            banded = samples > 1 && ds.getInt(Tag.PlanarConfiguration, 0) != 0;
            bitsAllocated = ds.getInt(Tag.BitsAllocated, 8);
            bitsStored = ds.getInt(Tag.BitsStored, bitsAllocated);
            dataType = bitsAllocated <= 8 ? DataBuffer.TYPE_BYTE
                    : DataBuffer.TYPE_USHORT;
            pmi = Photometric.fromString(
                    ds.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
            if (pixelDataLength != -1) {
                pmiAfterDecompression = pmi;
                this.frameLength = pmi.frameLength(width, height, samples, bitsAllocated);
            } else {
                Attributes fmi = metadata.getFileMetaInformation();
                if (null == fmi)
                    throw new IllegalArgumentException("Missing File Meta Information for Data Set with compressed Pixel Data");

                String tsuid = fmi.getString(Tag.TransferSyntaxUID);
                ImageReaderFactory.ImageReaderParam param =
                        ImageReaderFactory.getImageReaderParam(tsuid);
                if (null == param)
                    throw new UnsupportedOperationException("Unsupported Transfer Syntax: " + tsuid);
                pmiAfterDecompression = pmi.isYBR() && TransferSyntaxType.isYBRCompression(tsuid)
                        ? Photometric.RGB
                        : pmi;
                this.rle = tsuid.equals(UID.RLELossless);
                this.decompressor = ImageReaderFactory.getImageReader(param);
                Logger.debug("Decompressor: {}", decompressor.getClass().getName());
                this.patchJpegLS = param.patchJPEGLS;
            }
        }
    }

    private SampleModel createSampleModel(int dataType, boolean banded) {
        return pmi.createSampleModel(dataType, width, height, samples, banded);
    }

    private ImageTypeSpecifier createImageType(int bits, int dataType, boolean banded) {
        return new ImageTypeSpecifier(
                createColorModel(bits, dataType),
                createSampleModel(dataType, banded));
    }

    private ColorModel createColorModel(int bits, int dataType) {
        return pmiAfterDecompression.createColorModel(bits, dataType, metadata.getAttributes());
    }

    private void resetInternalState() {
        dis = null;
        metadata = null;
        pixelData = null;
        pixelDataFragments = null;
        pixelDataVR = null;
        pixelDataLength = 0;
        pixeldataBytes = null;
        pixelDataFile = null;
        frames = 0;
        flushedFrames = 0;
        width = 0;
        height = 0;
        if (null != decompressor) {
            decompressor.dispose();
            decompressor = null;
        }
        patchJpegLS = null;
        pmi = null;
    }

    private void checkIndex(int frameIndex) {
        if (frames == 0)
            throw new IllegalStateException("Missing Pixel Data");

        if (frameIndex < 0 || frameIndex >= frames)
            throw new IndexOutOfBoundsException("imageIndex: " + frameIndex);

        if (null != dis && frameIndex < flushedFrames)
            throw new IllegalStateException(
                    "input stream position already after requested frame #" + (frameIndex + 1));
    }

    public Attributes readPostPixeldata() throws IOException {
        if (frames == 0) return metadata.getAttributes();

        if (null != dis) {
            if (flushedFrames > frames) {
                return metadata.getAttributes();
            }
            dis.skipFully((frames - flushedFrames) * frameLength);
            flushedFrames = frames + 1;
            return readPostAttr(dis);
        }
        long offset;
        if (null != pixelData) {
            offset = pixelData.offset() + pixelData.longLength();
        } else {
            SegmentedImageStream siis = (SegmentedImageStream) iisOfFrame(-1);
            offset = siis.getOffsetPostPixelData();
        }
        iis.seek(offset);

        ImageInputStream dis = new ImageInputStream(new ImageInputStreamAdapter(iis), getTransferSyntaxUID());
        return readPostAttr(dis);
    }

    private Attributes readPostAttr(ImageInputStream dis) throws IOException {
        Attributes postAttr = dis.readDataset(-1, -1);
        postAttr.addAll(metadata.getAttributes());
        metadata = new DicomMetaData(metadata.getFileMetaInformation(), postAttr);
        return postAttr;
    }

    @Override
    public void dispose() {
        resetInternalState();
    }

    @Override
    public void close() {
        dispose();
    }

    /**
     * 获取图片像素点的像素值,用于计算ct值
     *
     * @param frameIndex 图像在dcm文件中的索引
     * @param row        像素点行号
     * @param column     像素点列号
     * @return pixel value 像素值
     * @throws IOException io异常
     */
    public double getPixelValue(int frameIndex, int row, int column) throws IOException {
        readMetadata();
        checkIndex(frameIndex);
        openiis();
        javax.imageio.stream.ImageInputStream iisOfFrame = iisOfFrame(frameIndex);
        iisOfFrame.length();
        decompressor.setInput(iisOfFrame);
        return ((NativeImageReader) decompressor).getNativeImage(decompressParam(null)).get(row, column)[0];
    }

    /**
     * @author Kimi Liu
     * @since Java 17+
     */
    public static class NativeDCMImageReadParam extends ImageReadParam {

        private float windowCenter;
        private float windowWidth;
        private boolean autoWindowing = true;
        private boolean preferWindow = true;
        private int windowIndex;
        private int voiLUTIndex;
        private int overlayActivationMask = 0xf;
        private int overlayGrayscaleValue = 0xffff;
        private Attributes presentationState;

        public float getWindowCenter() {
            return windowCenter;
        }

        public void setWindowCenter(float windowCenter) {
            this.windowCenter = windowCenter;
        }

        public float getWindowWidth() {
            return windowWidth;
        }

        public void setWindowWidth(float windowWidth) {
            this.windowWidth = windowWidth;
        }

        public boolean isAutoWindowing() {
            return autoWindowing;
        }

        public void setAutoWindowing(boolean autoWindowing) {
            this.autoWindowing = autoWindowing;
        }

        public boolean isPreferWindow() {
            return preferWindow;
        }

        public void setPreferWindow(boolean preferWindow) {
            this.preferWindow = preferWindow;
        }

        public int getWindowIndex() {
            return windowIndex;
        }

        public void setWindowIndex(int windowIndex) {
            this.windowIndex = Math.max(windowIndex, 0);
        }

        public int getVOILUTIndex() {
            return voiLUTIndex;
        }

        public void setVOILUTIndex(int voiLUTIndex) {
            this.voiLUTIndex = Math.max(voiLUTIndex, 0);
        }

        public Attributes getPresentationState() {
            return presentationState;
        }

        public void setPresentationState(Attributes presentationState) {
            this.presentationState = presentationState;
        }

        public int getOverlayActivationMask() {
            return overlayActivationMask;
        }

        public void setOverlayActivationMask(int overlayActivationMask) {
            this.overlayActivationMask = overlayActivationMask;
        }

        public int getOverlayGrayscaleValue() {
            return overlayGrayscaleValue;
        }

        public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
            this.overlayGrayscaleValue = overlayGrayscaleValue;
        }

    }

}
