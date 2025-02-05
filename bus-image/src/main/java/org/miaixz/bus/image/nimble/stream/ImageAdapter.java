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
package org.miaixz.bus.image.nimble.stream;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.EditorContext;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.BulkData;
import org.miaixz.bus.image.galaxy.data.Fragments;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.DataWriter;
import org.miaixz.bus.image.metric.Editable;
import org.miaixz.bus.image.nimble.*;
import org.miaixz.bus.image.nimble.codec.TransferSyntaxType;
import org.miaixz.bus.image.nimble.codec.jpeg.JPEGParser;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageAdapter {

    protected static final byte[] EMPTY_BYTES = {};
    private static final ImageReadParam IMAGE_READ_PARAM = new ImageReadParam();

    static {
        IMAGE_READ_PARAM.setReleaseImageAfterProcessing(true);
    }

    private ImageAdapter() {
    }

    public static boolean writeDicomFile(Attributes data, AdaptTransferSyntax syntax, Editable<PlanarImage> editable,
            BytesWithImageDescriptor desc, File file) {
        if (desc == null) {
            if (UID.ImplicitVRLittleEndian.equals(syntax.suitable) || UID.ExplicitVRBigEndian.equals(syntax.suitable)) {
                syntax.suitable = UID.ImplicitVRLittleEndian.uid;
            }
            try (ImageOutputStream writer = new ImageOutputStream(file)) {
                writer.writeDataset(data.createFileMetaInformation(syntax.suitable), data);
                writer.finish();
                return true;
            } catch (Exception e) {
                Logger.error("Writing DICOM file", e);
                FileKit.remove(file);
                return false;
            }
        }

        ImageReader reader = new ImageReader(Transcoder.IMAGE_READER_SPI);
        ImageOutputData imgData;
        try {
            imgData = geDicomOutputData(reader, syntax.requested, desc, editable);
        } catch (IOException e) {
            Logger.error("Get DicomOutputData", e);
            return false;
        }
        checkSyntax(syntax, imgData);

        Attributes dataSet = new Attributes(data);
        dataSet.remove(Tag.PixelData);
        String dstTsuid = syntax.suitable;
        try (ImageOutputStream dos = new ImageOutputStream(new BufferedOutputStream(new FileOutputStream(file)),
                dstTsuid)) {
            dos.writeFileMetaInformation(dataSet.createFileMetaInformation(dstTsuid));
            writeImage(syntax, desc, imgData, dataSet, dstTsuid, dos);
        } catch (Exception e) {
            Logger.error("Transcoding image data", e);
            FileKit.remove(file);
            return false;
        } finally {
            reader.dispose();
        }

        return true;
    }

    private static void writeImage(AdaptTransferSyntax syntax, BytesWithImageDescriptor desc, ImageOutputData imgData,
            Attributes dataSet, String dstTsuid, ImageOutputStream dos) throws IOException {
        if (ImageOutputData.isNativeSyntax(dstTsuid)) {
            imgData.writRawImageData(dos, dataSet);
        } else {
            JpegWriteParam jpegWriteParam = JpegWriteParam.buildDicomImageWriteParam(dstTsuid);
            if (jpegWriteParam.getCompressionQuality() > 0) {
                int quality = syntax.getJpegQuality() <= 0 ? 85 : syntax.getJpegQuality();
                jpegWriteParam.setCompressionQuality(quality);
            }
            if (jpegWriteParam.getCompressionRatioFactor() > 0 && syntax.getCompressionRatioFactor() > 0) {
                jpegWriteParam.setCompressionRatioFactor(syntax.getCompressionRatioFactor());
            }
            int[] jpegWriteParams = imgData.adaptTagsToCompressedImage(dataSet, imgData.getFirstImage().get(),
                    desc.getImageDescriptor(), jpegWriteParam);
            imgData.writeCompressedImageData(dos, dataSet, jpegWriteParams);
        }
    }

    public static void checkSyntax(AdaptTransferSyntax syntax, ImageOutputData imgData) {
        if (!syntax.requested.equals(imgData.getTsuid())) {
            syntax.suitable = imgData.getTsuid();
            Logger.warn("Transcoding into {} is not possible, used instead {}", syntax.requested, syntax.suitable);
        }
    }

    public static DataWriter buildDataWriter(Attributes data, AdaptTransferSyntax syntax,
            Editable<PlanarImage> editable, BytesWithImageDescriptor desc) throws IOException {
        if (desc == null) {
            syntax.suitable = syntax.original;
            return (out, tsuid) -> {
                try (ImageOutputStream writer = new ImageOutputStream(out, tsuid)) {
                    writer.writeDataset(null, data);
                    writer.finish();
                }
            };
        }

        ImageReader reader = new ImageReader(Transcoder.IMAGE_READER_SPI);
        ImageOutputData imgData = geDicomOutputData(reader, syntax.requested, desc, editable);
        checkSyntax(syntax, imgData);

        return (out, tsuid) -> {
            Attributes dataSet = new Attributes(data);
            dataSet.remove(Tag.PixelData);
            try (ImageOutputStream dos = new ImageOutputStream(out, tsuid)) {
                writeImage(syntax, desc, imgData, dataSet, tsuid, dos);
            } catch (Exception e) {
                Logger.error("Transcoding image data", e);
            } finally {
                reader.dispose();
            }
        };
    }

    private static boolean isTranscodable(String origUid, String desUid) {
        if (!desUid.equals(origUid)) {
            return !(Builder.isNative(origUid) && Builder.isNative(desUid));
        }
        return false;
    }

    public static BytesWithImageDescriptor imageTranscode(Attributes data, AdaptTransferSyntax syntax,
            EditorContext context) {

        VR.Holder pixeldataVR = new VR.Holder();
        Object pixdata = data.getValue(Tag.PixelData, pixeldataVR);
        if (pixdata != null && ImageReader.isSupportedSyntax(syntax.original)
                && ImageOutputData.isSupportedSyntax(syntax.requested)
                && (context.hasPixelProcessing() || isTranscodable(syntax.original, syntax.requested))) {

            ImageDescriptor imdDesc = new ImageDescriptor(data);
            ByteBuffer[] mfByteBuffer = new ByteBuffer[1];
            ArrayList<Integer> fragmentsPositions = new ArrayList<>();
            return new BytesWithImageDescriptor() {
                @Override
                public ImageDescriptor getImageDescriptor() {
                    return imdDesc;
                }

                @Override
                public boolean bigEndian() {
                    if (pixdata instanceof BulkData bulkData) {
                        return bulkData.bigEndian();
                    } else if (pixdata instanceof Fragments fragments) {
                        return fragments.bigEndian();
                    }
                    return false;
                }

                @Override
                public VR getPixelDataVR() {
                    return pixeldataVR.vr;
                }

                @Override
                public ByteBuffer getBytes(int frame) throws IOException {
                    ImageDescriptor desc = getImageDescriptor();
                    int bitsStored = desc.getBitsStored();
                    if (bitsStored < 1) {
                        return ByteBuffer.wrap(EMPTY_BYTES);
                    } else {
                        Fragments fragments = null;
                        BulkData bulkData = null;
                        boolean bigEndian = bigEndian();
                        if (pixdata instanceof BulkData bck) {
                            bulkData = bck;
                        } else if (pixdata instanceof Fragments frag) {
                            fragments = frag;
                        }

                        boolean hasFragments = fragments != null;
                        if (!hasFragments && bulkData != null) {
                            int frameLength = desc.getPhotometricInterpretation().frameLength(desc.getColumns(),
                                    desc.getRows(), desc.getSamples(), desc.getBitsAllocated());
                            if (mfByteBuffer[0] == null) {
                                mfByteBuffer[0] = ByteBuffer.wrap(bulkData.toBytes(pixeldataVR.vr, bigEndian));
                            }

                            if (mfByteBuffer[0].limit() < frame * frameLength + frameLength) {
                                throw new IOException("Frame out of the stream limit");
                            }

                            byte[] bytes = new byte[frameLength];
                            mfByteBuffer[0].position(frame * frameLength);
                            mfByteBuffer[0].get(bytes, 0, frameLength);
                            return ByteBuffer.wrap(bytes);
                        } else if (hasFragments) {
                            int nbFragments = fragments.size();
                            int numberOfFrame = desc.getFrames();
                            if (numberOfFrame == 1) {
                                int length = 0;
                                for (int i = 0; i < nbFragments - 1; i++) {
                                    BulkData b = (BulkData) fragments.get(i + 1);
                                    length += b.length();
                                }
                                ByteArrayOutputStream out = new ByteArrayOutputStream(length);
                                for (int i = 0; i < nbFragments - 1; i++) {
                                    BulkData b = (BulkData) fragments.get(i + 1);
                                    byte[] bytes = b.toBytes(pixeldataVR.vr, bigEndian);
                                    out.write(bytes, 0, bytes.length);
                                }
                                return ByteBuffer.wrap(out.toByteArray());
                            } else {
                                // Multi-frames where each frames can have multiple fragments.
                                if (fragmentsPositions.isEmpty()) {
                                    if (UID.RLELossless.equals(syntax.original)) {
                                        for (int i = 1; i < nbFragments; i++) {
                                            fragmentsPositions.add(i);
                                        }
                                    } else {
                                        for (int i = 1; i < nbFragments; i++) {
                                            BulkData b = (BulkData) fragments.get(i);
                                            try (ByteArrayOutputStream out = new ByteArrayOutputStream(b.length())) {
                                                byte[] bytes = b.toBytes(pixeldataVR.vr, bigEndian);
                                                out.write(bytes, 0, bytes.length);
                                                try (SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(
                                                        out.toByteArray())) {
                                                    new JPEGParser(channel);
                                                    fragmentsPositions.add(i);
                                                }
                                            } catch (Exception e) {
                                                // Not jpeg stream
                                            }
                                        }
                                    }
                                }

                                if (fragmentsPositions.size() == numberOfFrame) {
                                    int start = fragmentsPositions.get(frame);
                                    int end = (frame + 1) >= fragmentsPositions.size() ? nbFragments
                                            : fragmentsPositions.get(frame + 1);

                                    int length = 0;
                                    for (int i = 0; i < end - start; i++) {
                                        BulkData b = (BulkData) fragments.get(start + i);
                                        length += b.length();
                                    }
                                    ByteArrayOutputStream out = new ByteArrayOutputStream(length);
                                    for (int i = 0; i < end - start; i++) {
                                        BulkData b = (BulkData) fragments.get(start + i);
                                        byte[] bytes = b.toBytes(pixeldataVR.vr, bigEndian);
                                        out.write(bytes, 0, bytes.length);
                                    }
                                    return ByteBuffer.wrap(out.toByteArray());
                                } else {
                                    throw new IOException("Cannot match all the fragments to all the frames!");
                                }
                            }
                        }
                    }
                    throw new IOException("Neither fragments nor BulkData!");
                }

                @Override
                public String getTransferSyntax() {
                    return syntax.original;
                }

                @Override
                public Attributes getPaletteColorLookupTable() {
                    Attributes dcm = new Attributes(9);
                    copyValue(data, dcm, Tag.RedPaletteColorLookupTableDescriptor);
                    copyValue(data, dcm, Tag.GreenPaletteColorLookupTableDescriptor);
                    copyValue(data, dcm, Tag.BluePaletteColorLookupTableDescriptor);
                    copyValue(data, dcm, Tag.RedPaletteColorLookupTableData);
                    copyValue(data, dcm, Tag.GreenPaletteColorLookupTableData);
                    copyValue(data, dcm, Tag.BluePaletteColorLookupTableData);
                    copyValue(data, dcm, Tag.SegmentedRedPaletteColorLookupTableData);
                    copyValue(data, dcm, Tag.SegmentedGreenPaletteColorLookupTableData);
                    copyValue(data, dcm, Tag.SegmentedBluePaletteColorLookupTableData);
                    return dcm;
                }
            };
        }
        return null;
    }

    private static void copyValue(Attributes original, Attributes copy, int tag) {
        if (original.containsValue(tag)) {
            copy.setValue(tag, original.getVR(tag), original.getValue(tag));
        }
    }

    private static ImageOutputData geDicomOutputData(ImageReader reader, String outputTsuid,
            BytesWithImageDescriptor desc, Editable<PlanarImage> editable) throws IOException {
        reader.setInput(desc);
        var images = reader.getLazyPlanarImages(IMAGE_READ_PARAM, editable);
        return new ImageOutputData(images, desc.getImageDescriptor(), outputTsuid);
    }

    public static class AdaptTransferSyntax {
        private final String original;
        private final String requested;
        private String suitable;
        private int jpegQuality;
        private int compressionRatioFactor;

        public AdaptTransferSyntax(String original, String requested) {
            if (!StringKit.hasText(original) || !StringKit.hasText(requested)) {
                throw new IllegalArgumentException("A non empty value is required");
            }
            this.original = original;
            this.requested = requested;
            this.suitable = requested;
            this.jpegQuality = 85;
        }

        public String getOriginal() {
            return original;
        }

        public String getRequested() {
            return requested;
        }

        public String getSuitable() {
            return suitable;
        }

        public void setSuitable(String suitable) {
            if (TransferSyntaxType.forUID(suitable) != TransferSyntaxType.UNKNOWN) {
                this.suitable = suitable;
            }
        }

        public int getJpegQuality() {
            return jpegQuality;
        }

        public void setJpegQuality(int jpegQuality) {
            this.jpegQuality = jpegQuality;
        }

        public int getCompressionRatioFactor() {
            return compressionRatioFactor;
        }

        public void setCompressionRatioFactor(int compressionRatioFactor) {
            this.compressionRatioFactor = compressionRatioFactor;
        }
    }

}
