//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.imgcodecs;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.utils.Converters;

import java.util.List;

// C++: class Imgcodecs

public class Imgcodecs {

    // C++: enum DicomCompression
    public static final int
            DICOM_CP_UNKNOWN = 0,
            DICOM_CP_JPG = 1,
            DICOM_CP_JPLS = 2,
            DICOM_CP_J2K = 3;


    // C++: enum DicomFlags
    public static final int
            DICOM_FLAG_DEFAULT = -1,
            DICOM_FLAG_UNSIGNED = 0,
            DICOM_FLAG_SIGNED = 1,
            DICOM_FLAG_YBR = 2,
            DICOM_FLAG_BIGENDIAN = 4,
            DICOM_FLAG_FLOAT = 16,
            DICOM_FLAG_RLE = 32;


    // C++: enum ImwriteEXRTypeFlags
    public static final int
            IMWRITE_EXR_TYPE_HALF = 1,
            IMWRITE_EXR_TYPE_FLOAT = 2;


    // C++: enum ImwritePNGFlags
    public static final int
            IMWRITE_PNG_STRATEGY_DEFAULT = 0,
            IMWRITE_PNG_STRATEGY_FILTERED = 1,
            IMWRITE_PNG_STRATEGY_HUFFMAN_ONLY = 2,
            IMWRITE_PNG_STRATEGY_RLE = 3,
            IMWRITE_PNG_STRATEGY_FIXED = 4;


    // C++: enum ImwritePAMFlags
    public static final int
            IMWRITE_PAM_FORMAT_NULL = 0,
            IMWRITE_PAM_FORMAT_BLACKANDWHITE = 1,
            IMWRITE_PAM_FORMAT_GRAYSCALE = 2,
            IMWRITE_PAM_FORMAT_GRAYSCALE_ALPHA = 3,
            IMWRITE_PAM_FORMAT_RGB = 4,
            IMWRITE_PAM_FORMAT_RGB_ALPHA = 5;


    // C++: enum ImwriteFlags
    public static final int
            IMWRITE_JPEG_QUALITY = 1,
            IMWRITE_JPEG_PROGRESSIVE = 2,
            IMWRITE_JPEG_OPTIMIZE = 3,
            IMWRITE_JPEG_RST_INTERVAL = 4,
            IMWRITE_JPEG_LUMA_QUALITY = 5,
            IMWRITE_JPEG_CHROMA_QUALITY = 6,
            IMWRITE_PNG_COMPRESSION = 16,
            IMWRITE_PNG_STRATEGY = 17,
            IMWRITE_PNG_BILEVEL = 18,
            IMWRITE_PXM_BINARY = 32,
            IMWRITE_EXR_TYPE = (3 << 4) + 0,
            IMWRITE_WEBP_QUALITY = 64,
            IMWRITE_PAM_TUPLETYPE = 128,
            IMWRITE_TIFF_RESUNIT = 256,
            IMWRITE_TIFF_XDPI = 257,
            IMWRITE_TIFF_YDPI = 258,
            IMWRITE_TIFF_COMPRESSION = 259,
            IMWRITE_JPEG2000_COMPRESSION_X1000 = 272;


    // C++: enum ImreadModes
    public static final int
            IMREAD_UNCHANGED = -1,
            IMREAD_GRAYSCALE = 0,
            IMREAD_COLOR = 1,
            IMREAD_ANYDEPTH = 2,
            IMREAD_ANYCOLOR = 4,
            IMREAD_LOAD_GDAL = 8,
            IMREAD_REDUCED_GRAYSCALE_2 = 16,
            IMREAD_REDUCED_COLOR_2 = 17,
            IMREAD_REDUCED_GRAYSCALE_4 = 32,
            IMREAD_REDUCED_COLOR_4 = 33,
            IMREAD_REDUCED_GRAYSCALE_8 = 64,
            IMREAD_REDUCED_COLOR_8 = 65,
            IMREAD_IGNORE_ORIENTATION = 128;


    // C++: enum EJM_Mode
    public static final int
            JPEG_baseline = 0,
            JPEG_sequential = 1,
            JPEG_spectralSelection = 2,
            JPEG_progressive = 3,
            JPEG_lossless = 4;


    // C++: enum Photometric_Interpretation
    public static final int
            EPI_Unknown = 0,
            EPI_Missing = 1,
            EPI_Monochrome1 = 2,
            EPI_Monochrome2 = 3,
            EPI_PaletteColor = 4,
            EPI_RGB = 5,
            EPI_HSV = 6,
            EPI_ARGB = 7,
            EPI_CMYK = 8,
            EPI_YBR_Full = 9,
            EPI_YBR_Full_422 = 10,
            EPI_YBR_Partial_422 = 11;


    // C++: enum interleavemode
    public static final int
            ILV_NONE = 0,
            ILV_LINE = 1,
            ILV_SAMPLE = 2;


    // C++: enum DicomParams
    public static final int
            DICOM_PARAM_IMREAD = 0,
            DICOM_PARAM_DCM_IMREAD = 1,
            DICOM_PARAM_WIDTH = 2,
            DICOM_PARAM_HEIGHT = 3,
            DICOM_PARAM_COMPRESSION = 4,
            DICOM_PARAM_COMPONENTS = 5,
            DICOM_PARAM_BITS_PER_SAMPLE = 6,
            DICOM_PARAM_INTERLEAVE_MODE = 7,
            DICOM_PARAM_COLOR_MODEL = 8,
            DICOM_PARAM_JPEG_MODE = 9,
            DICOM_PARAM_JPEGLS_LOSSY_ERROR = 10,
            DICOM_PARAM_J2K_COMPRESSION_FACTOR = 11,
            DICOM_PARAM_JPEG_QUALITY = 12,
            DICOM_PARAM_JPEG_PREDICTION = 13,
            DICOM_PARAM_JPEG_PT_TRANSFORM = 14;


    //
    // C++:  Mat cv::dicomJpgFileRead(String filename, vector_double segposition, vector_double seglength, int dicomflags = 0, int flags = IMREAD_UNCHANGED)
    //

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from file segments.
     * <p>
     * The function dicomJpgRead loads a DICOM image from the specified file into Mat.
     *
     * @param filename    Name of file to be loaded.
     * @param segposition A vector of double holding the position of each fragment to read.
     * @param seglength   A vector of double holding the length of each fragment to read.
     * @param dicomflags  specific DICOM Flags (signed, ybr). Default is unsigned data. See DICOM_IMREAD in grfmt_dcm_dicom.hpp.
     * @param flags       Flag that can take values of cv::ImreadModes.
     * @return automatically generated
     */
    public static Mat dicomJpgFileRead(String filename, MatOfDouble segposition, MatOfDouble seglength, int dicomflags, int flags) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        return new Mat(dicomJpgFileRead_0(filename, segposition_mat.nativeObj, seglength_mat.nativeObj, dicomflags, flags));
    }

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from file segments.
     * <p>
     * The function dicomJpgRead loads a DICOM image from the specified file into Mat.
     *
     * @param filename    Name of file to be loaded.
     * @param segposition A vector of double holding the position of each fragment to read.
     * @param seglength   A vector of double holding the length of each fragment to read.
     * @param dicomflags  specific DICOM Flags (signed, ybr). Default is unsigned data. See DICOM_IMREAD in grfmt_dcm_dicom.hpp.
     * @return automatically generated
     */
    public static Mat dicomJpgFileRead(String filename, MatOfDouble segposition, MatOfDouble seglength, int dicomflags) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        return new Mat(dicomJpgFileRead_1(filename, segposition_mat.nativeObj, seglength_mat.nativeObj, dicomflags));
    }

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from file segments.
     * <p>
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
    // C++:  Mat cv::dicomJpgMatRead(Mat buf, int dicomflags = 0, int flags = IMREAD_UNCHANGED)
    //

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from Mat.
     * <p>
     * The function dicomJpgRead loads a DICOM image from a specified byte array into Mat.
     *
     * @param buf        the raw byte data of jpg image (1 raw, x column).
     * @param dicomflags specific DICOM Flags (signed, ybr). Default is unsigned data. See DICOM_IMREAD in grfmt_dcm_dicom.hpp.
     * @param flags      Flag that can take values of cv::ImreadModes.
     * @return automatically generated
     */
    public static Mat dicomJpgMatRead(Mat buf, int dicomflags, int flags) {
        return new Mat(dicomJpgMatRead_0(buf.nativeObj, dicomflags, flags));
    }

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from Mat.
     * <p>
     * The function dicomJpgRead loads a DICOM image from a specified byte array into Mat.
     *
     * @param buf        the raw byte data of jpg image (1 raw, x column).
     * @param dicomflags specific DICOM Flags (signed, ybr). Default is unsigned data. See DICOM_IMREAD in grfmt_dcm_dicom.hpp.
     * @return automatically generated
     */
    public static Mat dicomJpgMatRead(Mat buf, int dicomflags) {
        return new Mat(dicomJpgMatRead_1(buf.nativeObj, dicomflags));
    }

    /**
     * Loads a jpeg image (jpeg, jpeg-losseless, jpeg-ls and jpeg-2000) from Mat.
     * <p>
     * The function dicomJpgRead loads a DICOM image from a specified byte array into Mat.
     *
     * @param buf the raw byte data of jpg image (1 raw, x column).
     * @return automatically generated
     */
    public static Mat dicomJpgMatRead(Mat buf) {
        return new Mat(dicomJpgMatRead_2(buf.nativeObj));
    }


    //
    // C++:  Mat cv::dicomJpgWrite(Mat image, vector_int dicomParams, String colormodel)
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
    // C++:  Mat cv::dicomRawFileRead(String filename, vector_double segposition, vector_double seglength, vector_int dicomparams, String colormodel)
    //

    /**
     * Loads a raw image (include RLE compressed image) from file segments.
     * <p>
     * The function dicomRawRead loads a DICOM image from the specified file into Mat.
     *
     * @param filename    Name of file to be loaded.
     * @param segposition The position of the image to read.
     * @param seglength   The length image to read.
     * @param colormodel  The image color model.
     * @param dicomparams automatically generated
     * @return automatically generated
     */
    public static Mat dicomRawFileRead(String filename, MatOfDouble segposition, MatOfDouble seglength, MatOfInt dicomparams, String colormodel) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        Mat dicomparams_mat = dicomparams;
        return new Mat(dicomRawFileRead_0(filename, segposition_mat.nativeObj, seglength_mat.nativeObj, dicomparams_mat.nativeObj, colormodel));
    }


    //
    // C++:  Mat cv::dicomRawMatRead(Mat buf, vector_int dicomParams, String colormodel)
    //

    /**
     * Loads a raw image (include RLE compressed image) from Mat.
     * <p>
     * The function dicomRawRead loads a DICOM image from a specified byte array into Mat.
     *
     * @param buf         the raw byte data of jpg image (1 raw, x column).
     * @param dicomParams A vector of int containing the specific DICOM parameters. See DICOM_PARAM in grfmt_dcm_dicom.hpp.
     * @param colormodel  The image color model.
     * @return automatically generated
     */
    public static Mat dicomRawMatRead(Mat buf, MatOfInt dicomParams, String colormodel) {
        Mat dicomParams_mat = dicomParams;
        return new Mat(dicomRawMatRead_0(buf.nativeObj, dicomParams_mat.nativeObj, colormodel));
    }


    //
    // C++:  Mat cv::imdecode(Mat buf, int flags)
    //

    /**
     * Reads an image from a buffer in memory.
     * <p>
     * The function imdecode reads an image from the specified buffer in the memory. If the buffer is too short or
     * contains invalid data, the function returns an empty matrix ( Mat::data==NULL ).
     * <p>
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
    // C++:  Mat cv::imread(String filename, int flags = IMREAD_UNCHANGED)
    //

    /**
     * Loads an image from a file.
     * <p>
     * imread
     * <p>
     * The function imread loads an image from the specified file and returns it. If the image cannot be
     * read (because of missing file, improper permissions, unsupported or invalid format), the function
     * returns an empty matrix ( Mat::data==NULL ).
     * <p>
     * Currently, the following file formats are supported:
     *
     * <ul>
     *   <li>
     *    Windows bitmaps - \*.bmp, \*.dib (always supported)
     *   </li>
     *   <li>
     *    JPEG files - \*.jpeg, \*.jpg, \*.jpe (see the *Note* section)
     *   </li>
     *   <li>
     *    JPEG 2000 files - \*.jp2 (see the *Note* section)
     *   </li>
     *   <li>
     *    Portable Network Graphics - \*.png (see the *Note* section)
     *   </li>
     *   <li>
     *    WebP - \*.webp (see the *Note* section)
     *   </li>
     *   <li>
     *    Portable image format - \*.pbm, \*.pgm, \*.ppm \*.pxm, \*.pnm (always supported)
     *   </li>
     *   <li>
     *    PFM files - \*.pfm (see the *Note* section)
     *   </li>
     *   <li>
     *    Sun rasters - \*.sr, \*.ras (always supported)
     *   </li>
     *   <li>
     *    TIFF files - \*.tiff, \*.tif (see the *Note* section)
     *   </li>
     *   <li>
     *    OpenEXR Image files - \*.exr (see the *Note* section)
     *   </li>
     *   <li>
     *    Radiance HDR - \*.hdr, \*.pic (always supported)
     *   </li>
     *   <li>
     *    Raster and Vector geospatial data supported by GDAL (see the *Note* section)
     *   </li>
     * </ul>
     *
     * <b>Note:</b>
     * <ul>
     *   <li>
     *    The function determines the type of an image by the content, not by the file extension.
     *   </li>
     *   <li>
     *    In the case of color images, the decoded images will have the channels stored in <b>B G R</b> order.
     *   </li>
     *   <li>
     *    When using IMREAD_GRAYSCALE, the codec's internal grayscale conversion will be used, if available.
     *     Results may differ to the output of cvtColor()
     *   </li>
     *   <li>
     *    On Microsoft Windows\* OS and MacOSX\*, the codecs shipped with an OpenCV image (libjpeg,
     *     libpng, libtiff, and libjasper) are used by default. So, OpenCV can always read JPEGs, PNGs,
     *     and TIFFs. On MacOSX, there is also an option to use native MacOSX image readers. But beware
     *     that currently these native image loaders give images with different pixel values because of
     *     the color management embedded into MacOSX.
     *   </li>
     *   <li>
     *    On Linux\*, BSD flavors and other Unix-like open-source operating systems, OpenCV looks for
     *     codecs supplied with an OS image. Install the relevant packages (do not forget the development
     *     files, for example, "libjpeg-dev", in Debian\* and Ubuntu\*) to get the codec support or turn
     *     on the OPENCV_BUILD_3RDPARTY_LIBS flag in CMake.
     *   </li>
     *   <li>
     *    In the case you set *WITH_GDAL* flag to true in CMake and REF: IMREAD_LOAD_GDAL to load the image,
     *     then the [GDAL](http://www.gdal.org) driver will be used in order to decode the image, supporting
     *     the following formats: [Raster](http://www.gdal.org/formats_list.html),
     *     [Vector](http://www.gdal.org/ogr_formats.html).
     *   </li>
     *   <li>
     *    If EXIF information is embedded in the image file, the EXIF orientation will be taken into account
     *     and thus the image will be rotated accordingly except if the flags REF: IMREAD_IGNORE_ORIENTATION
     *     or REF: IMREAD_UNCHANGED are passed.
     *   </li>
     *   <li>
     *    Use the IMREAD_UNCHANGED flag to keep the floating point values from PFM image.
     *   </li>
     *   <li>
     *    By default number of pixels must be less than 2^30. Limit can be set using system
     *     variable OPENCV_IO_MAX_IMAGE_PIXELS
     *   </li>
     * </ul>
     *
     * @param filename Name of file to be loaded.
     * @param flags    Flag that can take values of cv::ImreadModes
     * @return automatically generated
     */
    public static Mat imread(String filename, int flags) {
        return new Mat(imread_0(filename, flags));
    }

    /**
     * Loads an image from a file.
     * <p>
     * imread
     * <p>
     * The function imread loads an image from the specified file and returns it. If the image cannot be
     * read (because of missing file, improper permissions, unsupported or invalid format), the function
     * returns an empty matrix ( Mat::data==NULL ).
     * <p>
     * Currently, the following file formats are supported:
     *
     * <ul>
     *   <li>
     *    Windows bitmaps - \*.bmp, \*.dib (always supported)
     *   </li>
     *   <li>
     *    JPEG files - \*.jpeg, \*.jpg, \*.jpe (see the *Note* section)
     *   </li>
     *   <li>
     *    JPEG 2000 files - \*.jp2 (see the *Note* section)
     *   </li>
     *   <li>
     *    Portable Network Graphics - \*.png (see the *Note* section)
     *   </li>
     *   <li>
     *    WebP - \*.webp (see the *Note* section)
     *   </li>
     *   <li>
     *    Portable image format - \*.pbm, \*.pgm, \*.ppm \*.pxm, \*.pnm (always supported)
     *   </li>
     *   <li>
     *    PFM files - \*.pfm (see the *Note* section)
     *   </li>
     *   <li>
     *    Sun rasters - \*.sr, \*.ras (always supported)
     *   </li>
     *   <li>
     *    TIFF files - \*.tiff, \*.tif (see the *Note* section)
     *   </li>
     *   <li>
     *    OpenEXR Image files - \*.exr (see the *Note* section)
     *   </li>
     *   <li>
     *    Radiance HDR - \*.hdr, \*.pic (always supported)
     *   </li>
     *   <li>
     *    Raster and Vector geospatial data supported by GDAL (see the *Note* section)
     *   </li>
     * </ul>
     *
     * <b>Note:</b>
     * <ul>
     *   <li>
     *    The function determines the type of an image by the content, not by the file extension.
     *   </li>
     *   <li>
     *    In the case of color images, the decoded images will have the channels stored in <b>B G R</b> order.
     *   </li>
     *   <li>
     *    When using IMREAD_GRAYSCALE, the codec's internal grayscale conversion will be used, if available.
     *     Results may differ to the output of cvtColor()
     *   </li>
     *   <li>
     *    On Microsoft Windows\* OS and MacOSX\*, the codecs shipped with an OpenCV image (libjpeg,
     *     libpng, libtiff, and libjasper) are used by default. So, OpenCV can always read JPEGs, PNGs,
     *     and TIFFs. On MacOSX, there is also an option to use native MacOSX image readers. But beware
     *     that currently these native image loaders give images with different pixel values because of
     *     the color management embedded into MacOSX.
     *   </li>
     *   <li>
     *    On Linux\*, BSD flavors and other Unix-like open-source operating systems, OpenCV looks for
     *     codecs supplied with an OS image. Install the relevant packages (do not forget the development
     *     files, for example, "libjpeg-dev", in Debian\* and Ubuntu\*) to get the codec support or turn
     *     on the OPENCV_BUILD_3RDPARTY_LIBS flag in CMake.
     *   </li>
     *   <li>
     *    In the case you set *WITH_GDAL* flag to true in CMake and REF: IMREAD_LOAD_GDAL to load the image,
     *     then the [GDAL](http://www.gdal.org) driver will be used in order to decode the image, supporting
     *     the following formats: [Raster](http://www.gdal.org/formats_list.html),
     *     [Vector](http://www.gdal.org/ogr_formats.html).
     *   </li>
     *   <li>
     *    If EXIF information is embedded in the image file, the EXIF orientation will be taken into account
     *     and thus the image will be rotated accordingly except if the flags REF: IMREAD_IGNORE_ORIENTATION
     *     or REF: IMREAD_UNCHANGED are passed.
     *   </li>
     *   <li>
     *    Use the IMREAD_UNCHANGED flag to keep the floating point values from PFM image.
     *   </li>
     *   <li>
     *    By default number of pixels must be less than 2^30. Limit can be set using system
     *     variable OPENCV_IO_MAX_IMAGE_PIXELS
     *   </li>
     * </ul>
     *
     * @param filename Name of file to be loaded.
     * @return automatically generated
     */
    public static Mat imread(String filename) {
        return new Mat(imread_1(filename));
    }


    //
    // C++:  bool cv::haveImageReader(String filename)
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
    // C++:  bool cv::haveImageWriter(String filename)
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


    //
    // C++:  bool cv::imencode(String ext, Mat img, vector_uchar& buf, vector_int params = std::vector<int>())
    //

    /**
     * Encodes an image into a memory buffer.
     * <p>
     * The function imencode compresses the image and stores it in the memory buffer that is resized to fit the
     * result. See cv::imwrite for the list of supported formats and flags description.
     *
     * @param ext    File extension that defines the output format.
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
     * <p>
     * The function imencode compresses the image and stores it in the memory buffer that is resized to fit the
     * result. See cv::imwrite for the list of supported formats and flags description.
     *
     * @param ext File extension that defines the output format.
     * @param img Image to be written.
     * @param buf Output buffer resized to fit the compressed image.
     * @return automatically generated
     */
    public static boolean imencode(String ext, Mat img, MatOfByte buf) {
        Mat buf_mat = buf;
        return imencode_1(ext, img.nativeObj, buf_mat.nativeObj);
    }


    //
    // C++:  bool cv::imreadmulti(String filename, vector_Mat& mats, int flags = IMREAD_COLOR)
    //

    /**
     * Loads a multi-page image from a file.
     * <p>
     * The function imreadmulti loads a multi-page image from the specified file into a vector of Mat objects.
     *
     * @param filename Name of file to be loaded.
     * @param flags    Flag that can take values of cv::ImreadModes, default with cv::IMREAD_ANYCOLOR.
     * @param mats     A vector of Mat objects holding each page, if more than one.
     *                 SEE: cv::imread
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
     * <p>
     * The function imreadmulti loads a multi-page image from the specified file into a vector of Mat objects.
     *
     * @param filename Name of file to be loaded.
     * @param mats     A vector of Mat objects holding each page, if more than one.
     *                 SEE: cv::imread
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
    // C++:  bool cv::imwrite(String filename, Mat img, vector_int params = std::vector<int>())
    //

    /**
     * Saves an image to a specified file.
     * <p>
     * The function imwrite saves the image to the specified file. The image format is chosen based on the
     * filename extension (see cv::imread for the list of extensions). In general, only 8-bit
     * single-channel or 3-channel (with 'BGR' channel order) images
     * can be saved using this function, with these exceptions:
     *
     * <ul>
     *   <li>
     *  16-bit unsigned (CV_16U) images can be saved in the case of PNG, JPEG 2000, and TIFF formats
     *   </li>
     *   <li>
     *  32-bit float (CV_32F) images can be saved in PFM, TIFF, OpenEXR, and Radiance HDR formats;
     *   3-channel (CV_32FC3) TIFF images will be saved using the LogLuv high dynamic range encoding
     *   (4 bytes per pixel)
     *   </li>
     *   <li>
     *  PNG images with an alpha channel can be saved using this function. To do this, create
     * 8-bit (or 16-bit) 4-channel image BGRA, where the alpha channel goes last. Fully transparent pixels
     * should have alpha set to 0, fully opaque pixels should have alpha set to 255/65535 (see the code sample below).
     *   </li>
     * </ul>
     * <p>
     * If the format, depth or channel order is different, use
     * Mat::convertTo and cv::cvtColor to convert it before saving. Or, use the universal FileStorage I/O
     * functions to save the image to XML or YAML format.
     * <p>
     * The sample below shows how to create a BGRA image and save it to a PNG file. It also demonstrates how to set custom
     * compression parameters:
     * INCLUDE: snippets/imgcodecs_imwrite.cpp
     *
     * @param filename Name of the file.
     * @param img      Image to be saved.
     * @param params   Format-specific parameters encoded as pairs (paramId_1, paramValue_1, paramId_2, paramValue_2, ... .) see cv::ImwriteFlags
     * @return automatically generated
     */
    public static boolean imwrite(String filename, Mat img, MatOfInt params) {
        Mat params_mat = params;
        return imwrite_0(filename, img.nativeObj, params_mat.nativeObj);
    }

    /**
     * Saves an image to a specified file.
     * <p>
     * The function imwrite saves the image to the specified file. The image format is chosen based on the
     * filename extension (see cv::imread for the list of extensions). In general, only 8-bit
     * single-channel or 3-channel (with 'BGR' channel order) images
     * can be saved using this function, with these exceptions:
     *
     * <ul>
     *   <li>
     *  16-bit unsigned (CV_16U) images can be saved in the case of PNG, JPEG 2000, and TIFF formats
     *   </li>
     *   <li>
     *  32-bit float (CV_32F) images can be saved in PFM, TIFF, OpenEXR, and Radiance HDR formats;
     *   3-channel (CV_32FC3) TIFF images will be saved using the LogLuv high dynamic range encoding
     *   (4 bytes per pixel)
     *   </li>
     *   <li>
     *  PNG images with an alpha channel can be saved using this function. To do this, create
     * 8-bit (or 16-bit) 4-channel image BGRA, where the alpha channel goes last. Fully transparent pixels
     * should have alpha set to 0, fully opaque pixels should have alpha set to 255/65535 (see the code sample below).
     *   </li>
     * </ul>
     * <p>
     * If the format, depth or channel order is different, use
     * Mat::convertTo and cv::cvtColor to convert it before saving. Or, use the universal FileStorage I/O
     * functions to save the image to XML or YAML format.
     * <p>
     * The sample below shows how to create a BGRA image and save it to a PNG file. It also demonstrates how to set custom
     * compression parameters:
     * INCLUDE: snippets/imgcodecs_imwrite.cpp
     *
     * @param filename Name of the file.
     * @param img      Image to be saved.
     * @return automatically generated
     */
    public static boolean imwrite(String filename, Mat img) {
        return imwrite_1(filename, img.nativeObj);
    }


    // C++:  Mat cv::dicomJpgFileRead(String filename, vector_double segposition, vector_double seglength, int dicomflags = 0, int flags = IMREAD_UNCHANGED)
    private static native long dicomJpgFileRead_0(String filename, long segposition_mat_nativeObj, long seglength_mat_nativeObj, int dicomflags, int flags);

    private static native long dicomJpgFileRead_1(String filename, long segposition_mat_nativeObj, long seglength_mat_nativeObj, int dicomflags);

    private static native long dicomJpgFileRead_2(String filename, long segposition_mat_nativeObj, long seglength_mat_nativeObj);

    // C++:  Mat cv::dicomJpgMatRead(Mat buf, int dicomflags = 0, int flags = IMREAD_UNCHANGED)
    private static native long dicomJpgMatRead_0(long buf_nativeObj, int dicomflags, int flags);

    private static native long dicomJpgMatRead_1(long buf_nativeObj, int dicomflags);

    private static native long dicomJpgMatRead_2(long buf_nativeObj);

    // C++:  Mat cv::dicomJpgWrite(Mat image, vector_int dicomParams, String colormodel)
    private static native long dicomJpgWrite_0(long image_nativeObj, long dicomParams_mat_nativeObj, String colormodel);

    // C++:  Mat cv::dicomRawFileRead(String filename, vector_double segposition, vector_double seglength, vector_int dicomparams, String colormodel)
    private static native long dicomRawFileRead_0(String filename, long segposition_mat_nativeObj, long seglength_mat_nativeObj, long dicomparams_mat_nativeObj, String colormodel);

    // C++:  Mat cv::dicomRawMatRead(Mat buf, vector_int dicomParams, String colormodel)
    private static native long dicomRawMatRead_0(long buf_nativeObj, long dicomParams_mat_nativeObj, String colormodel);

    // C++:  Mat cv::imdecode(Mat buf, int flags)
    private static native long imdecode_0(long buf_nativeObj, int flags);

    // C++:  Mat cv::imread(String filename, int flags = IMREAD_UNCHANGED)
    private static native long imread_0(String filename, int flags);

    private static native long imread_1(String filename);

    // C++:  bool cv::haveImageReader(String filename)
    private static native boolean haveImageReader_0(String filename);

    // C++:  bool cv::haveImageWriter(String filename)
    private static native boolean haveImageWriter_0(String filename);

    // C++:  bool cv::imencode(String ext, Mat img, vector_uchar& buf, vector_int params = std::vector<int>())
    private static native boolean imencode_0(String ext, long img_nativeObj, long buf_mat_nativeObj, long params_mat_nativeObj);

    private static native boolean imencode_1(String ext, long img_nativeObj, long buf_mat_nativeObj);

    // C++:  bool cv::imreadmulti(String filename, vector_Mat& mats, int flags = IMREAD_COLOR)
    private static native boolean imreadmulti_0(String filename, long mats_mat_nativeObj, int flags);

    private static native boolean imreadmulti_1(String filename, long mats_mat_nativeObj);

    // C++:  bool cv::imwrite(String filename, Mat img, vector_int params = std::vector<int>())
    private static native boolean imwrite_0(String filename, long img_nativeObj, long params_mat_nativeObj);

    private static native boolean imwrite_1(String filename, long img_nativeObj);

}
