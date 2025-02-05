/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org opencv.org and other contributors.         ~
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
package org.opencv.imgcodecs;

import java.util.List;

import org.opencv.core.*;
import org.opencv.utils.Converters;

// C++: class Imgcodecs

public class Imgcodecs {

    // C++: enum DicomCompression (cv.DicomCompression)
    public static final int DICOM_CP_UNKNOWN = 0, DICOM_CP_JPG = 1, DICOM_CP_JPLS = 2, DICOM_CP_J2K = 3;

    // C++: enum DicomFlags (cv.DicomFlags)
    public static final int DICOM_FLAG_DEFAULT = -1, DICOM_FLAG_UNSIGNED = 0, DICOM_FLAG_SIGNED = 1, DICOM_FLAG_YBR = 2,
            DICOM_FLAG_BIGENDIAN = 4, DICOM_FLAG_FLOAT = 16, DICOM_FLAG_RLE = 32, DICOM_FLAG_FORCE_RGB_CONVERSION = 64;

    // C++: enum DicomParams (cv.DicomParams)
    public static final int DICOM_PARAM_IMREAD = 0, DICOM_PARAM_DCM_IMREAD = 1, DICOM_PARAM_WIDTH = 2,
            DICOM_PARAM_HEIGHT = 3, DICOM_PARAM_COMPRESSION = 4, DICOM_PARAM_COMPONENTS = 5,
            DICOM_PARAM_BITS_PER_SAMPLE = 6, DICOM_PARAM_INTERLEAVE_MODE = 7, DICOM_PARAM_STREAM_VR = 8,
            DICOM_PARAM_COLOR_MODEL = 9, DICOM_PARAM_JPEG_MODE = 10, DICOM_PARAM_JPEGLS_LOSSY_ERROR = 11,
            DICOM_PARAM_J2K_COMPRESSION_FACTOR = 12, DICOM_PARAM_JPEG_QUALITY = 13, DICOM_PARAM_JPEG_PREDICTION = 14,
            DICOM_PARAM_JPEG_PT_TRANSFORM = 15;

    // C++: enum EJM_Mode (cv.EJM_Mode)
    public static final int JPEG_baseline = 0, JPEG_sequential = 1, JPEG_spectralSelection = 2, JPEG_progressive = 3,
            JPEG_lossless = 4;

    // C++: enum ExifTagPosition (cv.ExifTagPosition)
    public static final int POS_IMAGE_DESCRIPTION = 0, POS_MAKE = 1, POS_MODEL = 2, POS_ORIENTATION = 3,
            POS_XRESOLUTION = 4, POS_YRESOLUTION = 5, POS_RESOLUTION_UNIT = 6, POS_SOFTWARE = 7, POS_DATE_TIME = 8,
            POS_COPYRIGHT = 9;

    // C++: enum ImreadModes (cv.ImreadModes)
    public static final int IMREAD_UNCHANGED = -1, IMREAD_GRAYSCALE = 0, IMREAD_COLOR = 1, IMREAD_ANYDEPTH = 2,
            IMREAD_ANYCOLOR = 4, IMREAD_LOAD_GDAL = 8, IMREAD_REDUCED_GRAYSCALE_2 = 16, IMREAD_REDUCED_COLOR_2 = 17,
            IMREAD_REDUCED_GRAYSCALE_4 = 32, IMREAD_REDUCED_COLOR_4 = 33, IMREAD_REDUCED_GRAYSCALE_8 = 64,
            IMREAD_REDUCED_COLOR_8 = 65, IMREAD_IGNORE_ORIENTATION = 128;

    // C++: enum ImwriteEXRCompressionFlags (cv.ImwriteEXRCompressionFlags)
    public static final int IMWRITE_EXR_COMPRESSION_NO = 0, IMWRITE_EXR_COMPRESSION_RLE = 1,
            IMWRITE_EXR_COMPRESSION_ZIPS = 2, IMWRITE_EXR_COMPRESSION_ZIP = 3, IMWRITE_EXR_COMPRESSION_PIZ = 4,
            IMWRITE_EXR_COMPRESSION_PXR24 = 5, IMWRITE_EXR_COMPRESSION_B44 = 6, IMWRITE_EXR_COMPRESSION_B44A = 7,
            IMWRITE_EXR_COMPRESSION_DWAA = 8, IMWRITE_EXR_COMPRESSION_DWAB = 9;

    // C++: enum ImwriteEXRTypeFlags (cv.ImwriteEXRTypeFlags)
    public static final int IMWRITE_EXR_TYPE_HALF = 1, IMWRITE_EXR_TYPE_FLOAT = 2;

    // C++: enum ImwriteFlags (cv.ImwriteFlags)
    public static final int IMWRITE_JPEG_QUALITY = 1, IMWRITE_JPEG_PROGRESSIVE = 2, IMWRITE_JPEG_OPTIMIZE = 3,
            IMWRITE_JPEG_RST_INTERVAL = 4, IMWRITE_JPEG_LUMA_QUALITY = 5, IMWRITE_JPEG_CHROMA_QUALITY = 6,
            IMWRITE_JPEG_SAMPLING_FACTOR = 7, IMWRITE_PNG_COMPRESSION = 16, IMWRITE_PNG_STRATEGY = 17,
            IMWRITE_PNG_BILEVEL = 18, IMWRITE_PXM_BINARY = 32, IMWRITE_EXR_TYPE = (3 << 4) + 0,
            IMWRITE_EXR_COMPRESSION = (3 << 4) + 1, IMWRITE_EXR_DWA_COMPRESSION_LEVEL = (3 << 4) + 2,
            IMWRITE_WEBP_QUALITY = 64, IMWRITE_HDR_COMPRESSION = (5 << 4) + 0, IMWRITE_PAM_TUPLETYPE = 128,
            IMWRITE_TIFF_RESUNIT = 256, IMWRITE_TIFF_XDPI = 257, IMWRITE_TIFF_YDPI = 258,
            IMWRITE_TIFF_COMPRESSION = 259, IMWRITE_JPEG2000_COMPRESSION_X1000 = 272, IMWRITE_AVIF_QUALITY = 512,
            IMWRITE_AVIF_DEPTH = 513, IMWRITE_AVIF_SPEED = 514;

    // C++: enum ImwriteHDRCompressionFlags (cv.ImwriteHDRCompressionFlags)
    public static final int IMWRITE_HDR_COMPRESSION_NONE = 0, IMWRITE_HDR_COMPRESSION_RLE = 1;

    // C++: enum ImwriteJPEGSamplingFactorParams (cv.ImwriteJPEGSamplingFactorParams)
    public static final int IMWRITE_JPEG_SAMPLING_FACTOR_411 = 0x411111, IMWRITE_JPEG_SAMPLING_FACTOR_420 = 0x221111,
            IMWRITE_JPEG_SAMPLING_FACTOR_422 = 0x211111, IMWRITE_JPEG_SAMPLING_FACTOR_440 = 0x121111,
            IMWRITE_JPEG_SAMPLING_FACTOR_444 = 0x111111;

    // C++: enum ImwritePAMFlags (cv.ImwritePAMFlags)
    public static final int IMWRITE_PAM_FORMAT_NULL = 0, IMWRITE_PAM_FORMAT_BLACKANDWHITE = 1,
            IMWRITE_PAM_FORMAT_GRAYSCALE = 2, IMWRITE_PAM_FORMAT_GRAYSCALE_ALPHA = 3, IMWRITE_PAM_FORMAT_RGB = 4,
            IMWRITE_PAM_FORMAT_RGB_ALPHA = 5;

    // C++: enum ImwritePNGFlags (cv.ImwritePNGFlags)
    public static final int IMWRITE_PNG_STRATEGY_DEFAULT = 0, IMWRITE_PNG_STRATEGY_FILTERED = 1,
            IMWRITE_PNG_STRATEGY_HUFFMAN_ONLY = 2, IMWRITE_PNG_STRATEGY_RLE = 3, IMWRITE_PNG_STRATEGY_FIXED = 4;

    // C++: enum Photometric_Interpretation (cv.Photometric_Interpretation)
    public static final int EPI_Unknown = 0, EPI_Missing = 1, EPI_Monochrome1 = 2, EPI_Monochrome2 = 3,
            EPI_PaletteColor = 4, EPI_RGB = 5, EPI_HSV = 6, EPI_ARGB = 7, EPI_CMYK = 8, EPI_YBR_Full = 9,
            EPI_YBR_Full_422 = 10, EPI_YBR_Partial_422 = 11;

    // C++: enum interleavemode (cv.interleavemode)
    public static final int ILV_NONE = 0, ILV_LINE = 1, ILV_SAMPLE = 2;

    //
    // C++: Mat cv::imread(String filename, vector_String& tags, int flags = IMREAD_UNCHANGED)
    //

    /**
     * Loads an image from a file.
     *
     * imread
     *
     * The function imread loads an image from the specified file and returns it. If the image cannot be read (because
     * of missing file, improper permissions, unsupported or invalid format), the function returns an empty matrix (
     * Mat::data==NULL ).
     *
     * Currently, the following file formats are supported:
     *
     * <ul>
     * <li>Windows bitmaps - \*.bmp, \*.dib (always supported)</li>
     * <li>JPEG files - \*.jpeg, \*.jpg, \*.jpe (see the *Note* section)</li>
     * <li>JPEG 2000 files - \*.jp2 (see the *Note* section)</li>
     * <li>Portable Network Graphics - \*.png (see the *Note* section)</li>
     * <li>WebP - \*.webp (see the *Note* section)</li>
     * <li>AVIF - \*.avif (see the *Note* section)</li>
     * <li>Portable image format - \*.pbm, \*.pgm, \*.ppm \*.pxm, \*.pnm (always supported)</li>
     * <li>PFM files - \*.pfm (see the *Note* section)</li>
     * <li>Sun rasters - \*.sr, \*.ras (always supported)</li>
     * <li>TIFF files - \*.tiff, \*.tif (see the *Note* section)</li>
     * <li>OpenEXR Image files - \*.exr (see the *Note* section)</li>
     * <li>Radiance HDR - \*.hdr, \*.pic (always supported)</li>
     * <li>Raster and Vector geospatial data supported by GDAL (see the *Note* section)</li>
     * </ul>
     *
     * <b>Note:</b>
     * <ul>
     * <li>The function determines the type of an image by the content, not by the file extension.</li>
     * <li>In the case of color images, the decoded images will have the channels stored in <b>B G R</b> order.</li>
     * <li>When using IMREAD_GRAYSCALE, the codec's internal grayscale conversion will be used, if available. Results
     * may differ to the output of cvtColor()</li>
     * <li>On Microsoft Windows\* OS and MacOSX\*, the codecs shipped with an OpenCV image (libjpeg, libpng, libtiff,
     * and libjasper) are used by default. So, OpenCV can always read JPEGs, PNGs, and TIFFs. On MacOSX, there is also
     * an option to use native MacOSX image readers. But beware that currently these native image loaders give images
     * with different pixel values because of the color management embedded into MacOSX.</li>
     * <li>On Linux\*, BSD flavors and other Unix-like open-source operating systems, OpenCV looks for codecs supplied
     * with an OS image. Install the relevant packages (do not forget the development files, for example, "libjpeg-dev",
     * in Debian\* and Ubuntu\*) to get the codec support or turn on the OPENCV_BUILD_3RDPARTY_LIBS flag in CMake.</li>
     * <li>In the case you set *WITH_GDAL* flag to true in CMake and REF: IMREAD_LOAD_GDAL to load the image, then the
     * [GDAL](http://www.gdal.org) driver will be used in order to decode the image, supporting the following formats:
     * [Raster](http://www.gdal.org/formats_list.html), [Vector](http://www.gdal.org/ogr_formats.html).</li>
     * <li>If EXIF information is embedded in the image file, the EXIF orientation will be taken into account and thus
     * the image will be rotated accordingly except if the flags REF: IMREAD_IGNORE_ORIENTATION or REF: IMREAD_UNCHANGED
     * are passed.</li>
     * <li>Use the IMREAD_UNCHANGED flag to keep the floating point values from PFM image.</li>
     * <li>By default number of pixels must be less than 2^30. Limit can be set using system variable
     * OPENCV_IO_MAX_IMAGE_PIXELS</li>
     * </ul>
     *
     * @param filename Name of file to be loaded.
     * @param tags     Output parameter for returning the list of EXIF tags
     * @param flags    Flag that can take values of cv::ImreadModes
     * @return automatically generated
     */
    public static Mat imread(String filename, List<String> tags, int flags) {
        return new Mat(imread_0(filename, tags, flags));
    }

    /**
     * Loads an image from a file.
     *
     * imread
     *
     * The function imread loads an image from the specified file and returns it. If the image cannot be read (because
     * of missing file, improper permissions, unsupported or invalid format), the function returns an empty matrix (
     * Mat::data==NULL ).
     *
     * Currently, the following file formats are supported:
     *
     * <ul>
     * <li>Windows bitmaps - \*.bmp, \*.dib (always supported)</li>
     * <li>JPEG files - \*.jpeg, \*.jpg, \*.jpe (see the *Note* section)</li>
     * <li>JPEG 2000 files - \*.jp2 (see the *Note* section)</li>
     * <li>Portable Network Graphics - \*.png (see the *Note* section)</li>
     * <li>WebP - \*.webp (see the *Note* section)</li>
     * <li>AVIF - \*.avif (see the *Note* section)</li>
     * <li>Portable image format - \*.pbm, \*.pgm, \*.ppm \*.pxm, \*.pnm (always supported)</li>
     * <li>PFM files - \*.pfm (see the *Note* section)</li>
     * <li>Sun rasters - \*.sr, \*.ras (always supported)</li>
     * <li>TIFF files - \*.tiff, \*.tif (see the *Note* section)</li>
     * <li>OpenEXR Image files - \*.exr (see the *Note* section)</li>
     * <li>Radiance HDR - \*.hdr, \*.pic (always supported)</li>
     * <li>Raster and Vector geospatial data supported by GDAL (see the *Note* section)</li>
     * </ul>
     *
     * <b>Note:</b>
     * <ul>
     * <li>The function determines the type of an image by the content, not by the file extension.</li>
     * <li>In the case of color images, the decoded images will have the channels stored in <b>B G R</b> order.</li>
     * <li>When using IMREAD_GRAYSCALE, the codec's internal grayscale conversion will be used, if available. Results
     * may differ to the output of cvtColor()</li>
     * <li>On Microsoft Windows\* OS and MacOSX\*, the codecs shipped with an OpenCV image (libjpeg, libpng, libtiff,
     * and libjasper) are used by default. So, OpenCV can always read JPEGs, PNGs, and TIFFs. On MacOSX, there is also
     * an option to use native MacOSX image readers. But beware that currently these native image loaders give images
     * with different pixel values because of the color management embedded into MacOSX.</li>
     * <li>On Linux\*, BSD flavors and other Unix-like open-source operating systems, OpenCV looks for codecs supplied
     * with an OS image. Install the relevant packages (do not forget the development files, for example, "libjpeg-dev",
     * in Debian\* and Ubuntu\*) to get the codec support or turn on the OPENCV_BUILD_3RDPARTY_LIBS flag in CMake.</li>
     * <li>In the case you set *WITH_GDAL* flag to true in CMake and REF: IMREAD_LOAD_GDAL to load the image, then the
     * [GDAL](http://www.gdal.org) driver will be used in order to decode the image, supporting the following formats:
     * [Raster](http://www.gdal.org/formats_list.html), [Vector](http://www.gdal.org/ogr_formats.html).</li>
     * <li>If EXIF information is embedded in the image file, the EXIF orientation will be taken into account and thus
     * the image will be rotated accordingly except if the flags REF: IMREAD_IGNORE_ORIENTATION or REF: IMREAD_UNCHANGED
     * are passed.</li>
     * <li>Use the IMREAD_UNCHANGED flag to keep the floating point values from PFM image.</li>
     * <li>By default number of pixels must be less than 2^30. Limit can be set using system variable
     * OPENCV_IO_MAX_IMAGE_PIXELS</li>
     * </ul>
     *
     * @param filename Name of file to be loaded.
     * @param tags     Output parameter for returning the list of EXIF tags
     * @return automatically generated
     */
    public static Mat imread(String filename, List<String> tags) {
        return new Mat(imread_1(filename, tags));
    }

    //
    // C++: Mat cv::dicomJpgFileRead(String filename, vector_double segposition, vector_double seglength, int dicomflags
    // = 0, int flags = IMREAD_UNCHANGED)
    //

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from file segments.
     *
     * The function dicomJpgRead loads a DICOM image from the specified file into Mat.
     * 
     * @param filename    Name of file to be loaded.
     * @param segposition A vector of double holding the position of each fragment to read.
     * @param seglength   A vector of double holding the length of each fragment to read.
     * @param dicomflags  specific DICOM Flags (signed, ybr). Default is unsigned data. See DICOM_IMREAD in
     *                    grfmt_dcm_dicom.hpp.
     * @param flags       Flag that can take values of cv::ImreadModes.
     * @return automatically generated
     */
    public static Mat dicomJpgFileRead(String filename, MatOfDouble segposition, MatOfDouble seglength, int dicomflags,
            int flags) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        return new Mat(
                dicomJpgFileRead_0(filename, segposition_mat.nativeObj, seglength_mat.nativeObj, dicomflags, flags));
    }

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from file segments.
     *
     * The function dicomJpgRead loads a DICOM image from the specified file into Mat.
     * 
     * @param filename    Name of file to be loaded.
     * @param segposition A vector of double holding the position of each fragment to read.
     * @param seglength   A vector of double holding the length of each fragment to read.
     * @param dicomflags  specific DICOM Flags (signed, ybr). Default is unsigned data. See DICOM_IMREAD in
     *                    grfmt_dcm_dicom.hpp.
     * @return automatically generated
     */
    public static Mat dicomJpgFileRead(String filename, MatOfDouble segposition, MatOfDouble seglength,
            int dicomflags) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        return new Mat(dicomJpgFileRead_1(filename, segposition_mat.nativeObj, seglength_mat.nativeObj, dicomflags));
    }

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from file segments.
     *
     * The function dicomJpgRead loads a DICOM image from the specified file into Mat.
     * 
     * @param filename    Name of file to be loaded.
     * @param segposition A vector of double holding the position of each fragment to read.
     * @param seglength   A vector of double holding the length of each fragment to read.
     * @return automatically generated
     */
    public static Mat dicomJpgFileRead(String filename, MatOfDouble segposition, MatOfDouble seglength) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        return new Mat(dicomJpgFileRead_2(filename, segposition_mat.nativeObj, seglength_mat.nativeObj));
    }

    //
    // C++: Mat cv::dicomJpgMatRead(Mat buf, int dicomflags = 0, int flags = IMREAD_UNCHANGED)
    //

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from Mat.
     *
     * The function dicomJpgRead loads a DICOM image from a specified byte array into Mat.
     * 
     * @param buf        the raw byte data of jpg image (1 raw, x column).
     * @param dicomflags specific DICOM Flags (signed, ybr). Default is unsigned data. See DICOM_IMREAD in
     *                   grfmt_dcm_dicom.hpp.
     * @param flags      Flag that can take values of cv::ImreadModes.
     * @return automatically generated
     */
    public static Mat dicomJpgMatRead(Mat buf, int dicomflags, int flags) {
        return new Mat(dicomJpgMatRead_0(buf.nativeObj, dicomflags, flags));
    }

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from Mat.
     *
     * The function dicomJpgRead loads a DICOM image from a specified byte array into Mat.
     * 
     * @param buf        the raw byte data of jpg image (1 raw, x column).
     * @param dicomflags specific DICOM Flags (signed, ybr). Default is unsigned data. See DICOM_IMREAD in
     *                   grfmt_dcm_dicom.hpp.
     * @return automatically generated
     */
    public static Mat dicomJpgMatRead(Mat buf, int dicomflags) {
        return new Mat(dicomJpgMatRead_1(buf.nativeObj, dicomflags));
    }

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from Mat.
     *
     * The function dicomJpgRead loads a DICOM image from a specified byte array into Mat.
     * 
     * @param buf the raw byte data of jpg image (1 raw, x column).
     * @return automatically generated
     */
    public static Mat dicomJpgMatRead(Mat buf) {
        return new Mat(dicomJpgMatRead_2(buf.nativeObj));
    }

    //
    // C++: Mat cv::dicomRawFileRead(String filename, vector_double segposition, vector_double seglength, vector_int
    // dicomparams, String colormodel)
    //

    /**
     * Loads a raw image (include RLE compressed image) from file segments.
     *
     * The function dicomRawRead loads a DICOM image from the specified file into Mat.
     * 
     * @param filename    Name of file to be loaded.
     * @param segposition The position of the image to read.
     * @param seglength   The length image to read.
     * @param colormodel  The image color model.
     * @param dicomparams automatically generated
     * @return automatically generated
     */
    public static Mat dicomRawFileRead(String filename, MatOfDouble segposition, MatOfDouble seglength,
            MatOfInt dicomparams, String colormodel) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        Mat dicomparams_mat = dicomparams;
        return new Mat(dicomRawFileRead_0(filename, segposition_mat.nativeObj, seglength_mat.nativeObj,
                dicomparams_mat.nativeObj, colormodel));
    }

    //
    // C++: Mat cv::dicomRawMatRead(Mat buf, vector_int dicomParams, String colormodel)
    //

    /**
     * Loads a raw image (include RLE compressed image) from Mat.
     *
     * The function dicomRawRead loads a DICOM image from a specified byte array into Mat.
     * 
     * @param buf         the raw byte data of jpg image (1 raw, x column).
     * @param dicomParams A vector of int containing the specific DICOM parameters. See DICOM_PARAM in
     *                    grfmt_dcm_dicom.hpp.
     * @param colormodel  The image color model.
     * @return automatically generated
     */
    public static Mat dicomRawMatRead(Mat buf, MatOfInt dicomParams, String colormodel) {
        Mat dicomParams_mat = dicomParams;
        return new Mat(dicomRawMatRead_0(buf.nativeObj, dicomParams_mat.nativeObj, colormodel));
    }

    //
    // C++: Mat cv::dicomJpgWrite(Mat image, vector_int dicomParams, String colormodel)
    //

    /**
     * Encodes an DICOM image into a memory buffer.
     *
     * @param colormodel  The image color model.
     * @param image       automatically generated
     * @param dicomParams automatically generated
     * @return automatically generated
     */
    public static Mat dicomJpgWrite(Mat image, MatOfInt dicomParams, String colormodel) {
        Mat dicomParams_mat = dicomParams;
        return new Mat(dicomJpgWrite_0(image.nativeObj, dicomParams_mat.nativeObj, colormodel));
    }

    //
    // C++: bool cv::imreadmulti(String filename, vector_Mat& mats, int flags = IMREAD_COLOR)
    //

    /**
     * Loads a multi-page image from a file.
     *
     * The function imreadmulti loads a multi-page image from the specified file into a vector of Mat objects.
     * 
     * @param filename Name of file to be loaded.
     * @param mats     A vector of Mat objects holding each page.
     * @param flags    Flag that can take values of cv::ImreadModes, default with cv::IMREAD_ANYCOLOR. SEE: cv::imread
     * @return automatically generated
     */
    public static boolean imreadmulti(String filename, List<Mat> mats, int flags) {
        Mat mats_mat = new Mat();
        boolean retVal = imreadmulti_0(filename, mats_mat.nativeObj, flags);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    /**
     * Loads a multi-page image from a file.
     *
     * The function imreadmulti loads a multi-page image from the specified file into a vector of Mat objects.
     * 
     * @param filename Name of file to be loaded.
     * @param mats     A vector of Mat objects holding each page. SEE: cv::imread
     * @return automatically generated
     */
    public static boolean imreadmulti(String filename, List<Mat> mats) {
        Mat mats_mat = new Mat();
        boolean retVal = imreadmulti_1(filename, mats_mat.nativeObj);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    //
    // C++: bool cv::imreadmulti(String filename, vector_Mat& mats, int start, int count, int flags = IMREAD_ANYCOLOR)
    //

    /**
     * Loads a of images of a multi-page image from a file.
     *
     * The function imreadmulti loads a specified range from a multi-page image from the specified file into a vector of
     * Mat objects.
     * 
     * @param filename Name of file to be loaded.
     * @param mats     A vector of Mat objects holding each page.
     * @param start    Start index of the image to load
     * @param count    Count number of images to load
     * @param flags    Flag that can take values of cv::ImreadModes, default with cv::IMREAD_ANYCOLOR. SEE: cv::imread
     * @return automatically generated
     */
    public static boolean imreadmulti(String filename, List<Mat> mats, int start, int count, int flags) {
        Mat mats_mat = new Mat();
        boolean retVal = imreadmulti_2(filename, mats_mat.nativeObj, start, count, flags);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    /**
     * Loads a of images of a multi-page image from a file.
     *
     * The function imreadmulti loads a specified range from a multi-page image from the specified file into a vector of
     * Mat objects.
     * 
     * @param filename Name of file to be loaded.
     * @param mats     A vector of Mat objects holding each page.
     * @param start    Start index of the image to load
     * @param count    Count number of images to load SEE: cv::imread
     * @return automatically generated
     */
    public static boolean imreadmulti(String filename, List<Mat> mats, int start, int count) {
        Mat mats_mat = new Mat();
        boolean retVal = imreadmulti_3(filename, mats_mat.nativeObj, start, count);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    //
    // C++: size_t cv::imcount(String filename, int flags = IMREAD_ANYCOLOR)
    //

    /**
     * Returns the number of images inside the give file
     *
     * The function imcount will return the number of pages in a multi-page image, or 1 for single-page images
     * 
     * @param filename Name of file to be loaded.
     * @param flags    Flag that can take values of cv::ImreadModes, default with cv::IMREAD_ANYCOLOR.
     * @return automatically generated
     */
    public static long imcount(String filename, int flags) {
        return imcount_0(filename, flags);
    }

    /**
     * Returns the number of images inside the give file
     *
     * The function imcount will return the number of pages in a multi-page image, or 1 for single-page images
     * 
     * @param filename Name of file to be loaded.
     * @return automatically generated
     */
    public static long imcount(String filename) {
        return imcount_1(filename);
    }

    //
    // C++: bool cv::imwrite(String filename, Mat img, vector_int params = std::vector<int>())
    //

    /**
     * Saves an image to a specified file.
     *
     * The function imwrite saves the image to the specified file. The image format is chosen based on the filename
     * extension (see cv::imread for the list of extensions). In general, only 8-bit unsigned (CV_8U) single-channel or
     * 3-channel (with 'BGR' channel order) images can be saved using this function, with these exceptions:
     *
     * <ul>
     * <li>With OpenEXR encoder, only 32-bit float (CV_32F) images can be saved.
     * <ul>
     * <li>8-bit unsigned (CV_8U) images are not supported.</li>
     * </ul>
     * <li>With Radiance HDR encoder, non 64-bit float (CV_64F) images can be saved.
     * <ul>
     * <li>All images will be converted to 32-bit float (CV_32F).</li>
     * </ul>
     * <li>With JPEG 2000 encoder, 8-bit unsigned (CV_8U) and 16-bit unsigned (CV_16U) images can be saved.</li>
     * <li>With PAM encoder, 8-bit unsigned (CV_8U) and 16-bit unsigned (CV_16U) images can be saved.</li>
     * <li>With PNG encoder, 8-bit unsigned (CV_8U) and 16-bit unsigned (CV_16U) images can be saved.
     * <ul>
     * <li>PNG images with an alpha channel can be saved using this function. To do this, create 8-bit (or 16-bit)
     * 4-channel image BGRA, where the alpha channel goes last. Fully transparent pixels should have alpha set to 0,
     * fully opaque pixels should have alpha set to 255/65535 (see the code sample below).</li>
     * </ul>
     * <li>With PGM/PPM encoder, 8-bit unsigned (CV_8U) and 16-bit unsigned (CV_16U) images can be saved.</li>
     * <li>With TIFF encoder, 8-bit unsigned (CV_8U), 16-bit unsigned (CV_16U), 32-bit float (CV_32F) and 64-bit float
     * (CV_64F) images can be saved.
     * <ul>
     * <li>Multiple images (vector of Mat) can be saved in TIFF format (see the code sample below).</li>
     * <li>32-bit float 3-channel (CV_32FC3) TIFF images will be saved using the LogLuv high dynamic range encoding (4
     * bytes per pixel)</li>
     * </ul>
     *
     * If the image format is not supported, the image will be converted to 8-bit unsigned (CV_8U) and saved that way.
     * </li>
     * </ul>
     *
     * If the format, depth or channel order is different, use Mat::convertTo and cv::cvtColor to convert it before
     * saving. Or, use the universal FileStorage I/O functions to save the image to XML or YAML format.
     *
     * The sample below shows how to create a BGRA image, how to set custom compression parameters and save it to a PNG
     * file. It also demonstrates how to save multiple images in a TIFF file: INCLUDE: snippets/imgcodecs_imwrite.cpp
     * 
     * @param filename Name of the file.
     * @param img      (Mat or vector of Mat) Image or Images to be saved.
     * @param params   Format-specific parameters encoded as pairs (paramId_1, paramValue_1, paramId_2, paramValue_2,
     *                 ... .) see cv::ImwriteFlags
     * @return automatically generated
     */
    public static boolean imwrite(String filename, Mat img, MatOfInt params) {
        Mat params_mat = params;
        return imwrite_0(filename, img.nativeObj, params_mat.nativeObj);
    }

    /**
     * Saves an image to a specified file.
     *
     * The function imwrite saves the image to the specified file. The image format is chosen based on the filename
     * extension (see cv::imread for the list of extensions). In general, only 8-bit unsigned (CV_8U) single-channel or
     * 3-channel (with 'BGR' channel order) images can be saved using this function, with these exceptions:
     *
     * <ul>
     * <li>With OpenEXR encoder, only 32-bit float (CV_32F) images can be saved.
     * <ul>
     * <li>8-bit unsigned (CV_8U) images are not supported.</li>
     * </ul>
     * <li>With Radiance HDR encoder, non 64-bit float (CV_64F) images can be saved.
     * <ul>
     * <li>All images will be converted to 32-bit float (CV_32F).</li>
     * </ul>
     * <li>With JPEG 2000 encoder, 8-bit unsigned (CV_8U) and 16-bit unsigned (CV_16U) images can be saved.</li>
     * <li>With PAM encoder, 8-bit unsigned (CV_8U) and 16-bit unsigned (CV_16U) images can be saved.</li>
     * <li>With PNG encoder, 8-bit unsigned (CV_8U) and 16-bit unsigned (CV_16U) images can be saved.
     * <ul>
     * <li>PNG images with an alpha channel can be saved using this function. To do this, create 8-bit (or 16-bit)
     * 4-channel image BGRA, where the alpha channel goes last. Fully transparent pixels should have alpha set to 0,
     * fully opaque pixels should have alpha set to 255/65535 (see the code sample below).</li>
     * </ul>
     * <li>With PGM/PPM encoder, 8-bit unsigned (CV_8U) and 16-bit unsigned (CV_16U) images can be saved.</li>
     * <li>With TIFF encoder, 8-bit unsigned (CV_8U), 16-bit unsigned (CV_16U), 32-bit float (CV_32F) and 64-bit float
     * (CV_64F) images can be saved.
     * <ul>
     * <li>Multiple images (vector of Mat) can be saved in TIFF format (see the code sample below).</li>
     * <li>32-bit float 3-channel (CV_32FC3) TIFF images will be saved using the LogLuv high dynamic range encoding (4
     * bytes per pixel)</li>
     * </ul>
     *
     * If the image format is not supported, the image will be converted to 8-bit unsigned (CV_8U) and saved that way.
     * </li>
     * </ul>
     *
     * If the format, depth or channel order is different, use Mat::convertTo and cv::cvtColor to convert it before
     * saving. Or, use the universal FileStorage I/O functions to save the image to XML or YAML format.
     *
     * The sample below shows how to create a BGRA image, how to set custom compression parameters and save it to a PNG
     * file. It also demonstrates how to save multiple images in a TIFF file: INCLUDE: snippets/imgcodecs_imwrite.cpp
     * 
     * @param filename Name of the file.
     * @param img      (Mat or vector of Mat) Image or Images to be saved.
     * @return automatically generated
     */
    public static boolean imwrite(String filename, Mat img) {
        return imwrite_1(filename, img.nativeObj);
    }

    //
    // C++: bool cv::imwritemulti(String filename, vector_Mat img, vector_int params = std::vector<int>())
    //

    public static boolean imwritemulti(String filename, List<Mat> img, MatOfInt params) {
        Mat img_mat = Converters.vector_Mat_to_Mat(img);
        Mat params_mat = params;
        return imwritemulti_0(filename, img_mat.nativeObj, params_mat.nativeObj);
    }

    public static boolean imwritemulti(String filename, List<Mat> img) {
        Mat img_mat = Converters.vector_Mat_to_Mat(img);
        return imwritemulti_1(filename, img_mat.nativeObj);
    }

    //
    // C++: Mat cv::imdecode(Mat buf, int flags)
    //

    /**
     * Reads an image from a buffer in memory.
     *
     * The function imdecode reads an image from the specified buffer in the memory. If the buffer is too short or
     * contains invalid data, the function returns an empty matrix ( Mat::data==NULL ).
     *
     * See cv::imread for the list of supported formats and flags description.
     *
     * <b>Note:</b> In the case of color images, the decoded images will have the channels stored in <b>B G R</b> order.
     * 
     * @param buf   Input array or vector of bytes.
     * @param flags The same flags as in cv::imread, see cv::ImreadModes.
     * @return automatically generated
     */
    public static Mat imdecode(Mat buf, int flags) {
        return new Mat(imdecode_0(buf.nativeObj, flags));
    }

    //
    // C++: bool cv::imdecodemulti(Mat buf, int flags, vector_Mat& mats, Range range = Range::all())
    //

    /**
     * Reads a multi-page image from a buffer in memory.
     *
     * The function imdecodemulti reads a multi-page image from the specified buffer in the memory. If the buffer is too
     * short or contains invalid data, the function returns false.
     *
     * See cv::imreadmulti for the list of supported formats and flags description.
     *
     * <b>Note:</b> In the case of color images, the decoded images will have the channels stored in <b>B G R</b> order.
     * 
     * @param buf   Input array or vector of bytes.
     * @param flags The same flags as in cv::imread, see cv::ImreadModes.
     * @param mats  A vector of Mat objects holding each page, if more than one.
     * @param range A continuous selection of pages.
     * @return automatically generated
     */
    public static boolean imdecodemulti(Mat buf, int flags, List<Mat> mats, Range range) {
        Mat mats_mat = new Mat();
        boolean retVal = imdecodemulti_0(buf.nativeObj, flags, mats_mat.nativeObj, range.start, range.end);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    /**
     * Reads a multi-page image from a buffer in memory.
     *
     * The function imdecodemulti reads a multi-page image from the specified buffer in the memory. If the buffer is too
     * short or contains invalid data, the function returns false.
     *
     * See cv::imreadmulti for the list of supported formats and flags description.
     *
     * <b>Note:</b> In the case of color images, the decoded images will have the channels stored in <b>B G R</b> order.
     * 
     * @param buf   Input array or vector of bytes.
     * @param flags The same flags as in cv::imread, see cv::ImreadModes.
     * @param mats  A vector of Mat objects holding each page, if more than one.
     * @return automatically generated
     */
    public static boolean imdecodemulti(Mat buf, int flags, List<Mat> mats) {
        Mat mats_mat = new Mat();
        boolean retVal = imdecodemulti_1(buf.nativeObj, flags, mats_mat.nativeObj);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    //
    // C++: bool cv::imencode(String ext, Mat img, vector_uchar& buf, vector_int params = std::vector<int>())
    //

    /**
     * Encodes an image into a memory buffer.
     *
     * The function imencode compresses the image and stores it in the memory buffer that is resized to fit the result.
     * See cv::imwrite for the list of supported formats and flags description.
     *
     * @param ext    File extension that defines the output format. Must include a leading period.
     * @param img    Image to be written.
     * @param buf    Output buffer resized to fit the compressed image.
     * @param params Format-specific parameters. See cv::imwrite and cv::ImwriteFlags.
     * @return automatically generated
     */
    public static boolean imencode(String ext, Mat img, MatOfByte buf, MatOfInt params) {
        Mat buf_mat = buf;
        Mat params_mat = params;
        return imencode_0(ext, img.nativeObj, buf_mat.nativeObj, params_mat.nativeObj);
    }

    /**
     * Encodes an image into a memory buffer.
     *
     * The function imencode compresses the image and stores it in the memory buffer that is resized to fit the result.
     * See cv::imwrite for the list of supported formats and flags description.
     *
     * @param ext File extension that defines the output format. Must include a leading period.
     * @param img Image to be written.
     * @param buf Output buffer resized to fit the compressed image.
     * @return automatically generated
     */
    public static boolean imencode(String ext, Mat img, MatOfByte buf) {
        Mat buf_mat = buf;
        return imencode_1(ext, img.nativeObj, buf_mat.nativeObj);
    }

    //
    // C++: bool cv::haveImageReader(String filename)
    //

    /**
     * Returns true if the specified image can be decoded by OpenCV
     *
     * @param filename File name of the image
     * @return automatically generated
     */
    public static boolean haveImageReader(String filename) {
        return haveImageReader_0(filename);
    }

    //
    // C++: bool cv::haveImageWriter(String filename)
    //

    /**
     * Returns true if an image with the specified filename can be encoded by OpenCV
     *
     * @param filename File name of the image
     * @return automatically generated
     */
    public static boolean haveImageWriter(String filename) {
        return haveImageWriter_0(filename);
    }

    // C++: Mat cv::imread(String filename, vector_String& tags, int flags = IMREAD_UNCHANGED)
    private static native long imread_0(String filename, List<String> tags, int flags);

    private static native long imread_1(String filename, List<String> tags);

    // C++: Mat cv::dicomJpgFileRead(String filename, vector_double segposition, vector_double seglength, int dicomflags
    // = 0, int flags = IMREAD_UNCHANGED)
    private static native long dicomJpgFileRead_0(String filename, long segposition_mat_nativeObj,
            long seglength_mat_nativeObj, int dicomflags, int flags);

    private static native long dicomJpgFileRead_1(String filename, long segposition_mat_nativeObj,
            long seglength_mat_nativeObj, int dicomflags);

    private static native long dicomJpgFileRead_2(String filename, long segposition_mat_nativeObj,
            long seglength_mat_nativeObj);

    // C++: Mat cv::dicomJpgMatRead(Mat buf, int dicomflags = 0, int flags = IMREAD_UNCHANGED)
    private static native long dicomJpgMatRead_0(long buf_nativeObj, int dicomflags, int flags);

    private static native long dicomJpgMatRead_1(long buf_nativeObj, int dicomflags);

    private static native long dicomJpgMatRead_2(long buf_nativeObj);

    // C++: Mat cv::dicomRawFileRead(String filename, vector_double segposition, vector_double seglength, vector_int
    // dicomparams, String colormodel)
    private static native long dicomRawFileRead_0(String filename, long segposition_mat_nativeObj,
            long seglength_mat_nativeObj, long dicomparams_mat_nativeObj, String colormodel);

    // C++: Mat cv::dicomRawMatRead(Mat buf, vector_int dicomParams, String colormodel)
    private static native long dicomRawMatRead_0(long buf_nativeObj, long dicomParams_mat_nativeObj, String colormodel);

    // C++: Mat cv::dicomJpgWrite(Mat image, vector_int dicomParams, String colormodel)
    private static native long dicomJpgWrite_0(long image_nativeObj, long dicomParams_mat_nativeObj, String colormodel);

    // C++: bool cv::imreadmulti(String filename, vector_Mat& mats, int flags = IMREAD_COLOR)
    private static native boolean imreadmulti_0(String filename, long mats_mat_nativeObj, int flags);

    private static native boolean imreadmulti_1(String filename, long mats_mat_nativeObj);

    // C++: bool cv::imreadmulti(String filename, vector_Mat& mats, int start, int count, int flags = IMREAD_ANYCOLOR)
    private static native boolean imreadmulti_2(String filename, long mats_mat_nativeObj, int start, int count,
            int flags);

    private static native boolean imreadmulti_3(String filename, long mats_mat_nativeObj, int start, int count);

    // C++: size_t cv::imcount(String filename, int flags = IMREAD_ANYCOLOR)
    private static native long imcount_0(String filename, int flags);

    private static native long imcount_1(String filename);

    // C++: bool cv::imwrite(String filename, Mat img, vector_int params = std::vector<int>())
    private static native boolean imwrite_0(String filename, long img_nativeObj, long params_mat_nativeObj);

    private static native boolean imwrite_1(String filename, long img_nativeObj);

    // C++: bool cv::imwritemulti(String filename, vector_Mat img, vector_int params = std::vector<int>())
    private static native boolean imwritemulti_0(String filename, long img_mat_nativeObj, long params_mat_nativeObj);

    private static native boolean imwritemulti_1(String filename, long img_mat_nativeObj);

    // C++: Mat cv::imdecode(Mat buf, int flags)
    private static native long imdecode_0(long buf_nativeObj, int flags);

    // C++: bool cv::imdecodemulti(Mat buf, int flags, vector_Mat& mats, Range range = Range::all())
    private static native boolean imdecodemulti_0(long buf_nativeObj, int flags, long mats_mat_nativeObj,
            int range_start, int range_end);

    private static native boolean imdecodemulti_1(long buf_nativeObj, int flags, long mats_mat_nativeObj);

    // C++: bool cv::imencode(String ext, Mat img, vector_uchar& buf, vector_int params = std::vector<int>())
    private static native boolean imencode_0(String ext, long img_nativeObj, long buf_mat_nativeObj,
            long params_mat_nativeObj);

    private static native boolean imencode_1(String ext, long img_nativeObj, long buf_mat_nativeObj);

    // C++: bool cv::haveImageReader(String filename)
    private static native boolean haveImageReader_0(String filename);

    // C++: bool cv::haveImageWriter(String filename)
    private static native boolean haveImageWriter_0(String filename);

}
