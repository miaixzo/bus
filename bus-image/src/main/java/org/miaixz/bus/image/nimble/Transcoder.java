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
package org.miaixz.bus.image.nimble;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.SupplierEx;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.Editable;
import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.op.MaskArea;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.miaixz.bus.image.nimble.stream.ImageFileInputStream;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.CvType;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Transcoder {

    public static final ImageReaderSpi IMAGE_READER_SPI = new ImageReaderSpi();
    private static final ImageReadParam IMAGE_READ_PARAM = new ImageReadParam();

    static {
        IMAGE_READ_PARAM.setReleaseImageAfterProcessing(true);
    }

    /**
     * Convert a DICOM image to a standard image with some rendering parameters
     *
     * @param srcPath the path of the source image
     * @param dstPath the path of the destination image or the path of a directory in which the source image filename
     *                will be used
     * @param params  the standard image conversion parameters
     * @return
     * @throws Exception
     */
    public static List<Path> dcm2image(Path srcPath, Path dstPath, TranscodeParam params) throws Exception {
        List<Path> outFiles = new ArrayList<>();
        Format format = params.getFormat();
        ImageReader reader = new ImageReader(IMAGE_READER_SPI);
        try (ImageFileInputStream inputStream = new ImageFileInputStream(srcPath)) {
            MatOfInt map = format == Format.JPEG
                    ? new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, getCompressionRatio(params))
                    : null;
            reader.setInput(inputStream);
            int nbFrames = reader.getImageDescriptor().getFrames();
            int indexSize = (int) Math.log10(nbFrames);
            indexSize = nbFrames > 1 ? indexSize + 1 : 0;
            for (int i = 0; i < nbFrames; i++) {
                PlanarImage img = reader.getPlanarImage(i, params.getReadParam());
                boolean rawImg = isPreserveRawImage(params, format, img.type());
                if (rawImg) {
                    img = ImageRendering.getRawRenderedImage(img, reader.getImageDescriptor(), params.getReadParam());
                } else {
                    img = ImageRendering.getDefaultRenderedImage(img, reader.getImageDescriptor(),
                            params.getReadParam(), i);
                }
                Path outPath = writeImage(img, getOutputPath(srcPath, dstPath), format, map, i + 1, indexSize);
                outFiles.add(outPath);
            }
        } finally {
            reader.dispose();
        }
        return outFiles;
    }

    /**
     * Convert a DICOM image to another DICOM image with a specific transfer syntax
     *
     * @param srcPath the path of the source image
     * @param dstPath the path of the destination image or the path of a directory in which the source image filename
     *                will be used
     * @param params  the DICOM conversion parameters
     * @throws IOException if an I/O error occurs
     */
    public static Path dcm2dcm(Path srcPath, Path dstPath, TranscodeParam params) throws IOException {
        Path outPath = adaptFileExtension(getOutputPath(srcPath, dstPath), ".dcm", ".dcm");

        try {
            dcm2dcm(srcPath, Files.newOutputStream(outPath), params);
        } catch (Exception e) {
            FileKit.remove(outPath);
            throw e;
        }

        return outPath;
    }

    /**
     * Convert a DICOM image to another DICOM image with a specific transfer syntax
     *
     * @param srcPath      the path of the source image
     * @param outputStream the output stream where the transcoded data will be written
     * @param params       the DICOM conversion parameters
     * @throws IOException if an I/O error occurs
     */
    public static void dcm2dcm(Path srcPath, OutputStream outputStream, TranscodeParam params) throws IOException {
        ImageReader reader = new ImageReader(IMAGE_READER_SPI);
        reader.setInput(new ImageFileInputStream(srcPath));

        ImageMetaData imageMetaData = reader.getStreamMetadata();
        Attributes dataSet = new Attributes(imageMetaData.getDicomObject());
        dataSet.remove(Tag.PixelData);

        Editable<PlanarImage> mask = getMask(dataSet, params);
        ImageReadParam dicomParams = params.getReadParam();
        if (dicomParams == null) {
            dicomParams = IMAGE_READ_PARAM;
        } else {
            dicomParams.setReleaseImageAfterProcessing(true);
        }
        List<SupplierEx<PlanarImage, IOException>> images = reader.getLazyPlanarImages(dicomParams, mask);
        String dstTsuid = params.getOutputTsuid();
        JpegWriteParam writeParams = params.getWriteJpegParam();
        ImageDescriptor desc = imageMetaData.getImageDescriptor();

        ImageOutputData imgData = new ImageOutputData(images, desc, dstTsuid);
        if (!dstTsuid.equals(imgData.getTsuid())) {
            dstTsuid = imgData.getTsuid();
            if (!ImageOutputData.isNativeSyntax(dstTsuid)) {
                writeParams = JpegWriteParam.buildDicomImageWriteParam(dstTsuid);
            }
            Logger.warn("Transcoding into {} is not possible, decompressing {}", dstTsuid, srcPath);
        }
        try (ImageOutputStream dos = new ImageOutputStream(outputStream, dstTsuid)) {
            dos.writeFileMetaInformation(dataSet.createFileMetaInformation(dstTsuid));
            if (ImageOutputData.isNativeSyntax(dstTsuid)) {
                imgData.writRawImageData(dos, dataSet);
            } else {
                int[] jpegWriteParams = imgData.adaptTagsToCompressedImage(dataSet, imgData.getFirstImage().get(), desc,
                        writeParams);
                imgData.writeCompressedImageData(dos, dataSet, jpegWriteParams);
            }
        } catch (Exception e) {
            Logger.error("Transcoding image data", e);
        } finally {
            reader.dispose();
        }
    }

    public static Editable<PlanarImage> getMaskedImage(MaskArea m) {
        if (m != null) {
            return img -> {
                ImageCV mask = MaskArea.drawShape(img.toMat(), m);
                if (img.isReleasedAfterProcessing()) {
                    img.release();
                    mask.setReleasedAfterProcessing(true);
                }
                return mask;
            };
        }
        return null;
    }

    private static Editable<PlanarImage> getMask(Attributes dataSet, TranscodeParam params) {
        String stationName = dataSet.getString(Tag.StationName, "*");
        return getMaskedImage(params.getMask(stationName));
    }

    private static int getCompressionRatio(TranscodeParam params) {
        if (params == null) {
            return 80;
        }
        return params.getJpegCompressionQuality().orElse(80);
    }

    private static boolean isPreserveRawImage(TranscodeParam params, Format format, int cvType) {
        if (params == null) {
            return false;
        }
        boolean value = params.isPreserveRawImage().orElse(false);
        if (value) {
            if (format == Format.HDR || cvType == CvType.CV_8U) {
                return true; // Convert all values in double so do not apply W/L
            } else if (cvType == CvType.CV_16U) {
                return format.support16U;
            } else if (cvType == CvType.CV_16S) {
                return format.support16S;
            } else if (cvType == CvType.CV_32F) {
                return format.support32F;
            } else if (cvType == CvType.CV_64F) {
                return format.support64F;
            }
        }
        return value;
    }

    private static Path adaptFileExtension(Path path, String inExt, String outExt) {
        String fname = path.getFileName().toString();
        String suffix = FileName.getSuffix(fname);
        if (suffix.equals(outExt)) {
            return path;
        }
        if (suffix.endsWith(inExt)) {
            return FileSystems.getDefault().getPath(path.getParent().toString(),
                    fname.substring(0, fname.length() - inExt.length()) + outExt);
        }
        return path.resolveSibling(fname + outExt);
    }

    private static Path writeImage(PlanarImage img, Path path, Format ext, MatOfInt map, int index, int indexSize) {
        Path outPath = adaptFileExtension(path, ".dcm", ext.extension);
        outPath = addFileIndex(outPath, index, indexSize);
        if (map == null) {
            if (!ImageProcessor.writeImage(img.toMat(), outPath.toFile())) {
                Logger.error("Cannot Transform to {} {}", ext, img);
                FileKit.remove(outPath);
            }
        } else {
            if (!ImageProcessor.writeImage(img.toMat(), outPath.toFile(), map)) {
                Logger.error("Cannot Transform to {} {}", ext, img);
                FileKit.remove(outPath);
            }
        }
        return outPath;
    }

    /**
     * Get the combined path. If output is a file, it is returned as is. If output is a directory, the input filename is
     * added to the output path.
     *
     * @param input  The input filename
     * @param output The output path
     * @return The name of the file without extension.
     */
    public static Path getOutputPath(Path input, Path output) {
        if (Files.isDirectory(output)) {
            return FileSystems.getDefault().getPath(output.toString(), input.getFileName().toString());
        } else {
            return output;
        }
    }

    /**
     * Add a file index to the file name. The index number is added before the file extension.
     *
     * @param path      the file path
     * @param index     the index to add the filename
     * @param indexSize the minimal number of digits of the index (0 padding)
     * @return the new path
     */
    public static Path addFileIndex(Path path, int index, int indexSize) {
        if (indexSize < 1) {
            return path;
        }
        String pattern = "$1-%0" + indexSize + "d$2";
        String insert = String.format(pattern, index);
        return path.resolveSibling(path.getFileName().toString().replaceFirst("(.*?)(\\.[^.]+)?$", insert));
    }

    public enum Format {
        JPEG(".jpg", false, false, false, false), PNG(".png", true, false, false, false),
        TIF(".tif", true, false, true, true), JP2(".jp2", true, false, false, false),
        PNM(".pnm", true, false, false, false), BMP(".bmp", false, false, false, false),
        HDR(".hdr", false, false, false, true);

        final String extension;
        final boolean support16U;
        final boolean support16S;
        final boolean support32F;
        final boolean support64F;

        Format(String ext, boolean support16U, boolean support16S, boolean support32F, boolean support64F) {
            this.extension = ext;
            this.support16U = support16U;
            this.support16S = support16S;
            this.support32F = support32F;
            this.support64F = support64F;
        }
    }

}
