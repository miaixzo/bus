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
package org.miaixz.bus.opencv.imgcodecs;

import java.util.List;

import org.miaixz.bus.opencv.core.Mat;
import org.miaixz.bus.opencv.core.MatOfByte;
import org.miaixz.bus.opencv.core.MatOfDouble;
import org.miaixz.bus.opencv.core.MatOfInt;
import org.miaixz.bus.opencv.core.Range;
import org.miaixz.bus.opencv.utils.Converters;

/**
 * Image encoding, decoding, metadata, and DICOM pixel-frame codec utilities. DICOM entry points retain the accepted 5.0
 * compatibility behavior.
 */
public class Imgcodecs {

    /**
     * Creates a Java API container. The utility operations are available through static methods.
     */
    public Imgcodecs() {
        // No initialization required.
    }

    /**
     * Identifier of the parsed EXIF matrix, whose rows contain the legacy UTF-8 tag strings.
     */
    public static final int IMAGE_METADATA_PARSE_EXIF = 1000;

    /**
     * DICOM pixel-frame codec identifier for raw or unspecified compression.
     */
    public static final int DICOM_CP_UNKNOWN = 0;

    /**
     * DICOM pixel-frame codec identifier for JPEG.
     */
    public static final int DICOM_CP_JPG = 1;

    /**
     * DICOM pixel-frame codec identifier for JPEG-LS.
     */
    public static final int DICOM_CP_JPLS = 2;

    /**
     * DICOM pixel-frame codec identifier for JPEG 2000.
     */
    public static final int DICOM_CP_J2K = 3;

    /**
     * DICOM pixel-frame codec identifier for JPEG XL.
     */
    public static final int DICOM_CP_JXL = 4;

    /**
     * Default DICOM flag value retained by the compatibility API.
     */
    public static final int DICOM_FLAG_DEFAULT = -1;

    /**
     * Interpret integer DICOM samples as unsigned.
     */
    public static final int DICOM_FLAG_UNSIGNED = 0;

    /**
     * Interpret supported integer DICOM samples using the legacy signed-sample conversion.
     */
    public static final int DICOM_FLAG_SIGNED = 1;

    /**
     * Enable the codec-specific interpretation of YBR color components.
     */
    public static final int DICOM_FLAG_YBR = 2;

    /**
     * Read raw multibyte samples in big-endian byte order.
     */
    public static final int DICOM_FLAG_BIGENDIAN = 4;

    /**
     * Interpret raw 32-bit or 64-bit samples as floating-point values.
     */
    public static final int DICOM_FLAG_FLOAT = 16;

    /**
     * Decode the raw input as DICOM RLE pixel data.
     */
    public static final int DICOM_FLAG_RLE = 32;

    /**
     * Compatibility flag requesting RGB conversion where supported by the codec.
     */
    public static final int DICOM_FLAG_FORCE_RGB_CONVERSION = 64;

    /**
     * Index of the OpenCV image-read flags in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_IMREAD = 0;

    /**
     * Index of the DICOM interpretation flags in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_DCM_IMREAD = 1;

    /**
     * Index of the image width in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_WIDTH = 2;

    /**
     * Index of the image height in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_HEIGHT = 3;

    /**
     * Index of the pixel-frame codec identifier in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_COMPRESSION = 4;

    /**
     * Index of the component count in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_COMPONENTS = 5;

    /**
     * Index of the stored sample precision in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_BITS_PER_SAMPLE = 6;

    /**
     * Index of the component interleave layout in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_INTERLEAVE_MODE = 7;

    /**
     * Index of the raw stream value representation in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_STREAM_VR = 8;

    /**
     * Index of the photometric interpretation identifier in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_COLOR_MODEL = 9;

    /**
     * Index of the JPEG coding mode in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_JPEG_MODE = 10;

    /**
     * Index of the JPEG-LS near-lossless error bound in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_JPEGLS_LOSSY_ERROR = 11;

    /**
     * Index of the JPEG 2000 compression factor in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_J2K_COMPRESSION_FACTOR = 12;

    /**
     * Index of the JPEG quality in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_JPEG_QUALITY = 13;

    /**
     * Index of the lossless JPEG predictor in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_JPEG_PREDICTION = 14;

    /**
     * Index of the lossless JPEG point transform in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_JPEG_PT_TRANSFORM = 15;

    /**
     * Index of the JPEG XL encoder effort in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_JXL_EFFORT = 16;

    /**
     * Index of the JPEG XL decoding-speed preference in the DICOM parameter vector.
     */
    public static final int DICOM_PARAM_JXL_DECODING_SPEED = 17;

    /**
     * JPEG coding mode selecting baseline sequential compression.
     */
    public static final int JPEG_baseline = 0;

    /**
     * JPEG coding mode selecting extended sequential compression.
     */
    public static final int JPEG_sequential = 1;

    /**
     * JPEG coding mode selecting spectral-selection progressive compression.
     */
    public static final int JPEG_spectralSelection = 2;

    /**
     * JPEG coding mode selecting full progressive compression.
     */
    public static final int JPEG_progressive = 3;

    /**
     * JPEG coding mode selecting predictive lossless compression.
     */
    public static final int JPEG_lossless = 4;

    /**
     * Row index of image description in the parsed EXIF tag matrix.
     */
    public static final int POS_IMAGE_DESCRIPTION = 0;

    /**
     * Row index of make in the parsed EXIF tag matrix.
     */
    public static final int POS_MAKE = 1;

    /**
     * Row index of model in the parsed EXIF tag matrix.
     */
    public static final int POS_MODEL = 2;

    /**
     * Row index of orientation in the parsed EXIF tag matrix.
     */
    public static final int POS_ORIENTATION = 3;

    /**
     * Row index of xresolution in the parsed EXIF tag matrix.
     */
    public static final int POS_XRESOLUTION = 4;

    /**
     * Row index of yresolution in the parsed EXIF tag matrix.
     */
    public static final int POS_YRESOLUTION = 5;

    /**
     * Row index of resolution unit in the parsed EXIF tag matrix.
     */
    public static final int POS_RESOLUTION_UNIT = 6;

    /**
     * Row index of software in the parsed EXIF tag matrix.
     */
    public static final int POS_SOFTWARE = 7;

    /**
     * Row index of date time in the parsed EXIF tag matrix.
     */
    public static final int POS_DATE_TIME = 8;

    /**
     * Row index of copyright in the parsed EXIF tag matrix.
     */
    public static final int POS_COPYRIGHT = 9;

    /**
     * Used when metadata type is unrecognized or not set.
     */
    public static final int IMAGE_METADATA_UNKNOWN = -1;

    /**
     * EXIF metadata (e.g., camera info, GPS, orientation).
     */
    public static final int IMAGE_METADATA_EXIF = 0;

    /**
     * XMP metadata (eXtensible Metadata Platform - Adobe format).
     */
    public static final int IMAGE_METADATA_XMP = 1;

    /**
     * ICC Profile (color profile for color management).
     */
    public static final int IMAGE_METADATA_ICCP = 2;

    /**
     * CICP Profile (video signal type).
     */
    public static final int IMAGE_METADATA_CICP = 3;

    /**
     * Highest valid index (usually used for bounds checking).
     */
    public static final int IMAGE_METADATA_MAX = 3;

    /**
     * If set, return the loaded image as is (with alpha channel, otherwise it gets cropped). Ignore EXIF orientation.
     */
    public static final int IMREAD_UNCHANGED = -1;

    /**
     * If set, always convert image to the single channel grayscale image (codec internal conversion).
     */
    public static final int IMREAD_GRAYSCALE = 0;

    /**
     * If set, always convert image to the 3 channel BGR color image.
     */
    public static final int IMREAD_COLOR_BGR = 1;

    /**
     * Same as IMREAD_COLOR_BGR.
     */
    public static final int IMREAD_COLOR = 1;

    /**
     * If set, return 16-bit/32-bit image when the input has the corresponding depth, otherwise convert it to 8-bit.
     */
    public static final int IMREAD_ANYDEPTH = 2;

    /**
     * If set, the image is read in any possible color format.
     */
    public static final int IMREAD_ANYCOLOR = 4;

    /**
     * If set, use the gdal driver for loading the image.
     */
    public static final int IMREAD_LOAD_GDAL = 8;

    /**
     * If set, always convert image to the single channel grayscale image and the image size reduced 1/2.
     */
    public static final int IMREAD_REDUCED_GRAYSCALE_2 = 16;

    /**
     * If set, always convert image to the 3 channel BGR color image and the image size reduced 1/2.
     */
    public static final int IMREAD_REDUCED_COLOR_2 = 17;

    /**
     * If set, always convert image to the single channel grayscale image and the image size reduced 1/4.
     */
    public static final int IMREAD_REDUCED_GRAYSCALE_4 = 32;

    /**
     * If set, always convert image to the 3 channel BGR color image and the image size reduced 1/4.
     */
    public static final int IMREAD_REDUCED_COLOR_4 = 33;

    /**
     * If set, always convert image to the single channel grayscale image and the image size reduced 1/8.
     */
    public static final int IMREAD_REDUCED_GRAYSCALE_8 = 64;

    /**
     * If set, always convert image to the 3 channel BGR color image and the image size reduced 1/8.
     */
    public static final int IMREAD_REDUCED_COLOR_8 = 65;

    /**
     * If set, do not rotate the image according to EXIF's orientation flag.
     */
    public static final int IMREAD_IGNORE_ORIENTATION = 128;

    /**
     * If set, always convert image to the 3 channel RGB color image.
     */
    public static final int IMREAD_COLOR_RGB = 256;

    /**
     * Use BI_RGB. OpenCV v4.12.0 or before supports to encode with this compression only.
     */
    public static final int IMWRITE_BMP_COMPRESSION_RGB = 0;

    /**
     * Use BI_BITFIELDS. OpenCV v4.13.0 or later can support to encode with this compression. (only for 32 BPP images).
     */
    public static final int IMWRITE_BMP_COMPRESSION_BITFIELDS = 3;

    /**
     * No compression.
     */
    public static final int IMWRITE_EXR_COMPRESSION_NO = 0;

    /**
     * Run length encoding.
     */
    public static final int IMWRITE_EXR_COMPRESSION_RLE = 1;

    /**
     * Zlib compression, one scan line at a time.
     */
    public static final int IMWRITE_EXR_COMPRESSION_ZIPS = 2;

    /**
     * Zlib compression, in blocks of 16 scan lines.
     */
    public static final int IMWRITE_EXR_COMPRESSION_ZIP = 3;

    /**
     * Piz-based wavelet compression.
     */
    public static final int IMWRITE_EXR_COMPRESSION_PIZ = 4;

    /**
     * Lossy 24-bit float compression.
     */
    public static final int IMWRITE_EXR_COMPRESSION_PXR24 = 5;

    /**
     * Lossy 4-by-4 pixel block compression, fixed compression rate.
     */
    public static final int IMWRITE_EXR_COMPRESSION_B44 = 6;

    /**
     * Lossy 4-by-4 pixel block compression, flat fields are compressed more.
     */
    public static final int IMWRITE_EXR_COMPRESSION_B44A = 7;

    /**
     * Lossy DCT based compression, in blocks of 32 scanlines. More efficient for partial buffer access. Supported since
     * OpenEXR 2.2.0.
     */
    public static final int IMWRITE_EXR_COMPRESSION_DWAA = 8;

    /**
     * Lossy DCT based compression, in blocks of 256 scanlines. More efficient space wise and faster to decode full
     * frames than DWAA_COMPRESSION. Supported since OpenEXR 2.2.0.
     */
    public static final int IMWRITE_EXR_COMPRESSION_DWAB = 9;

    /**
     * Store as HALF (FP16).
     */
    public static final int IMWRITE_EXR_TYPE_HALF = 1;

    /**
     * Store as FP32 (default).
     */
    public static final int IMWRITE_EXR_TYPE_FLOAT = 2;

    /**
     * For JPEG, it can be a quality from 0 to 100 (the higher is the better). Default value is 95.
     */
    public static final int IMWRITE_JPEG_QUALITY = 1;

    /**
     * Enable JPEG features, 0 or 1, default is False.
     */
    public static final int IMWRITE_JPEG_PROGRESSIVE = 2;

    /**
     * Enable JPEG features, 0 or 1, default is False.
     */
    public static final int IMWRITE_JPEG_OPTIMIZE = 3;

    /**
     * JPEG restart interval, 0 - 65535, default is 0 - no restart.
     */
    public static final int IMWRITE_JPEG_RST_INTERVAL = 4;

    /**
     * Separate luma quality level, 0 - 100, default is -1 - don't use. If JPEG_LIB_VERSION &lt; 70, Not supported.
     */
    public static final int IMWRITE_JPEG_LUMA_QUALITY = 5;

    /**
     * Separate chroma quality level, 0 - 100, default is -1 - don't use. If JPEG_LIB_VERSION &lt; 70, Not supported.
     */
    public static final int IMWRITE_JPEG_CHROMA_QUALITY = 6;

    /**
     * For JPEG, set sampling factor. See ImwriteJPEGSamplingFactorParams.
     */
    public static final int IMWRITE_JPEG_SAMPLING_FACTOR = 7;

    /**
     * For PNG, it can be the compression level from 0 to 9. A higher value means a smaller size and longer compression
     * time. If specified, strategy is changed to IMWRITE_PNG_STRATEGY_DEFAULT (Z_DEFAULT_STRATEGY). Default value is 1
     * (best speed setting).
     */
    public static final int IMWRITE_PNG_COMPRESSION = 16;

    /**
     * For PNG, One of ImwritePNGFlags, default is IMWRITE_PNG_STRATEGY_RLE.
     */
    public static final int IMWRITE_PNG_STRATEGY = 17;

    /**
     * For PNG, Binary level PNG, 0 or 1, default is 0. For APNG, it is not supported.
     */
    public static final int IMWRITE_PNG_BILEVEL = 18;

    /**
     * For PNG, One of ImwritePNGFilterFlags, default is IMWRITE_PNG_FILTER_SUB. For APNG, it is not supported.
     */
    public static final int IMWRITE_PNG_FILTER = 19;

    /**
     * For PNG with libpng, sets the size of the internal zlib compression buffer in bytes, from 6 to 1048576(1024 KiB).
     * Default is 8192(8 KiB). For normal use, 131072(128 KiB) or 262144(256 KiB) may be sufficient. If WITH_SPNG=ON, it
     * is not supported. For APNG, it is not supported.
     */
    public static final int IMWRITE_PNG_ZLIBBUFFER_SIZE = 20;

    /**
     * For PPM, PGM, or PBM, it can be a binary format flag, 0 or 1. Default value is 1.
     */
    public static final int IMWRITE_PXM_BINARY = 32;

    /**
     * Override EXR storage type (FLOAT (FP32) is default).
     */
    public static final int IMWRITE_EXR_TYPE = (3 << 4) + 0;

    /**
     * Override EXR compression type (ZIP_COMPRESSION = 3 is default).
     */
    public static final int IMWRITE_EXR_COMPRESSION = (3 << 4) + 1;

    /**
     * Override EXR DWA compression level (45 is default).
     */
    public static final int IMWRITE_EXR_DWA_COMPRESSION_LEVEL = (3 << 4) + 2;

    /**
     * For WEBP, it can be a lossy quality from 1 to 100 (the higher is the better) for IMWRITE_WEBP_LOSSLESS_OFF. By
     * default (without this parameter) or if quality &gt; 100, IMWRITE_WEBP_LOSSLESS_ON is used instead.
     */
    public static final int IMWRITE_WEBP_QUALITY = 64;

    /**
     * For WEBP, it can be a lossless compression strategy. See ImwriteWEBPLosslessMode. Default is
     * IMWRITE_WEBP_LOSSLESS_OFF. For Animated WEBP, it is not supported.
     */
    public static final int IMWRITE_WEBP_LOSSLESS_MODE = 65;

    /**
     * Specify HDR compression.
     */
    public static final int IMWRITE_HDR_COMPRESSION = (5 << 4) + 0;

    /**
     * For PAM, sets the TUPLETYPE field to the corresponding string value that is defined for the format.
     */
    public static final int IMWRITE_PAM_TUPLETYPE = 128;

    /**
     * For TIFF, use to specify which DPI resolution unit to set. See ImwriteTiffResolutionUnitFlags. Default is
     * IMWRITE_TIFF_RESOLUTION_UNIT_INCH.
     */
    public static final int IMWRITE_TIFF_RESUNIT = 256;

    /**
     * For TIFF, use to specify the X direction DPI.
     */
    public static final int IMWRITE_TIFF_XDPI = 257;

    /**
     * For TIFF, use to specify the Y direction DPI.
     */
    public static final int IMWRITE_TIFF_YDPI = 258;

    /**
     * For TIFF, use to specify the image compression scheme. See ImwriteTiffCompressionFlags. The compression scheme
     * can be specified by this flag; the default is LZW compression, except for 32F depth where it is NONE.
     */
    public static final int IMWRITE_TIFF_COMPRESSION = 259;

    /**
     * For TIFF, if IMWRITE_TIFF_COMPRESSION was used and supports it, sets a compression level. Valid range and meaning
     * depends on the compression scheme. It is currently 1-9 for DEFLATE, 0-9 for LZMA, 1-22 for ZSTD, 1-100 for WEBP.
     * -1 can be used as default.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_LEVEL = 260;

    /**
     * For TIFF, use to specify the number of rows per strip.
     */
    public static final int IMWRITE_TIFF_ROWSPERSTRIP = 278;

    /**
     * For TIFF, use to specify predictor. See ImwriteTiffPredictorFlags. Default is IMWRITE_TIFF_PREDICTOR_HORIZONTAL .
     */
    public static final int IMWRITE_TIFF_PREDICTOR = 317;

    /**
     * For JPEG2000, use to specify the target compression rate (multiplied by 1000). The value can be from 0 to 1000.
     * Default is 1000.
     */
    public static final int IMWRITE_JPEG2000_COMPRESSION_X1000 = 272;

    /**
     * For AVIF, it can be a **color-planes** quality between 0 and 100 (the higher the better). Default is 95.
     */
    public static final int IMWRITE_AVIF_QUALITY = 512;

    /**
     * For AVIF, it can be 8, 10, 12 or 16. 16 requires libavif v1.4.0 or later (for a single still image only, not
     * animation). If &gt;8, it is stored/read as CV_16U. Default is 8.
     */
    public static final int IMWRITE_AVIF_DEPTH = 513;

    /**
     * For AVIF, it is between 0 (slowest) and 10(fastest). Default is 9.
     */
    public static final int IMWRITE_AVIF_SPEED = 514;

    /**
     * For AVIF, it can be an **alpha-plane** quality between 0 and 100 (the higher the better). If not specified, it is
     * synced with the color plane quality. Default is 95.
     */
    public static final int IMWRITE_AVIF_ALPHA_QUALITY = 515;

    /**
     * For JPEG XL, it can be a quality from 0 to 100 (the higher is the better). Default value is 95. If set, distance
     * parameter is re-calicurated from quality level automatically. This parameter request libjxl v0.10 or later.
     */
    public static final int IMWRITE_JPEGXL_QUALITY = 640;

    /**
     * For JPEG XL, encoder effort/speed level without affecting decoding speed; it is between 1 (fastest) and 10
     * (slowest). Default is 7.
     */
    public static final int IMWRITE_JPEGXL_EFFORT = 641;

    /**
     * For JPEG XL, distance level for lossy compression: target max butteraugli distance, lower = higher quality, 0 =
     * lossless; range: 0 .. 25. Default is 1.
     */
    public static final int IMWRITE_JPEGXL_DISTANCE = 642;

    /**
     * For JPEG XL, decoding speed tier for the provided options; minimum is 0 (slowest to decode, best
     * quality/density), and maximum is 4 (fastest to decode, at the cost of some quality/density). Default is 0.
     */
    public static final int IMWRITE_JPEGXL_DECODING_SPEED = 643;

    /**
     * For BMP, use to specify compress parameter for 32bpp image. Default is IMWRITE_BMP_COMPRESSION_BITFIELDS. See
     * ImwriteBMPCompressionFlags.
     */
    public static final int IMWRITE_BMP_COMPRESSION = 768;

    /**
     * Not functional since 4.12.0. Replaced by Animation::loop_count.
     */
    public static final int IMWRITE_GIF_LOOP = 1024;

    /**
     * Not functional since 4.12.0. Replaced by Animation::durations.
     */
    public static final int IMWRITE_GIF_SPEED = 1025;

    /**
     * For GIF, it can be a quality from 1 to 8. Default is 2. See ImwriteGifCompressionFlags.
     */
    public static final int IMWRITE_GIF_QUALITY = 1026;

    /**
     * For GIF, it can be a quality from -1(most dither) to 3(no dither). Default is 0.
     */
    public static final int IMWRITE_GIF_DITHER = 1027;

    /**
     * For GIF, the alpha channel lower than this will be set to transparent. Default is 1.
     */
    public static final int IMWRITE_GIF_TRANSPARENCY = 1028;

    /**
     * For GIF, 0 means global color table is used, 1 means local color table is used. Default is 0.
     */
    public static final int IMWRITE_GIF_COLORTABLE = 1029;

    /**
     * GIF fast quantization without dithering.
     */
    public static final int IMWRITE_GIF_FAST_NO_DITHER = 1;

    /**
     * GIF fast quantization with Floyd-Steinberg error-diffusion dithering.
     */
    public static final int IMWRITE_GIF_FAST_FLOYD_DITHER = 2;

    /**
     * Request a GIF color table containing 8 entries.
     */
    public static final int IMWRITE_GIF_COLORTABLE_SIZE_8 = 3;

    /**
     * Request a GIF color table containing 16 entries.
     */
    public static final int IMWRITE_GIF_COLORTABLE_SIZE_16 = 4;

    /**
     * Request a GIF color table containing 32 entries.
     */
    public static final int IMWRITE_GIF_COLORTABLE_SIZE_32 = 5;

    /**
     * Request a GIF color table containing 64 entries.
     */
    public static final int IMWRITE_GIF_COLORTABLE_SIZE_64 = 6;

    /**
     * Request a GIF color table containing 128 entries.
     */
    public static final int IMWRITE_GIF_COLORTABLE_SIZE_128 = 7;

    /**
     * Request a GIF color table containing 256 entries.
     */
    public static final int IMWRITE_GIF_COLORTABLE_SIZE_256 = 8;

    /**
     * Write Radiance HDR pixel data without run-length compression.
     */
    public static final int IMWRITE_HDR_COMPRESSION_NONE = 0;

    /**
     * Write Radiance HDR pixel data with run-length compression.
     */
    public static final int IMWRITE_HDR_COMPRESSION_RLE = 1;

    /**
     * 4x1,1x1,1x1.
     */
    public static final int IMWRITE_JPEG_SAMPLING_FACTOR_411 = 4264209;

    /**
     * 2x2,1x1,1x1(Default).
     */
    public static final int IMWRITE_JPEG_SAMPLING_FACTOR_420 = 2232593;

    /**
     * 2x1,1x1,1x1.
     */
    public static final int IMWRITE_JPEG_SAMPLING_FACTOR_422 = 2167057;

    /**
     * 1x2,1x1,1x1.
     */
    public static final int IMWRITE_JPEG_SAMPLING_FACTOR_440 = 1184017;

    /**
     * 1x1,1x1,1x1(No subsampling).
     */
    public static final int IMWRITE_JPEG_SAMPLING_FACTOR_444 = 1118481;

    /**
     * Omit a named PAM tuple type from the output header.
     */
    public static final int IMWRITE_PAM_FORMAT_NULL = 0;

    /**
     * Use the PAM BLACKANDWHITE tuple type.
     */
    public static final int IMWRITE_PAM_FORMAT_BLACKANDWHITE = 1;

    /**
     * Use the PAM GRAYSCALE tuple type.
     */
    public static final int IMWRITE_PAM_FORMAT_GRAYSCALE = 2;

    /**
     * Use the PAM GRAYSCALE_ALPHA tuple type.
     */
    public static final int IMWRITE_PAM_FORMAT_GRAYSCALE_ALPHA = 3;

    /**
     * Use the PAM RGB tuple type.
     */
    public static final int IMWRITE_PAM_FORMAT_RGB = 4;

    /**
     * Use the PAM RGB_ALPHA tuple type.
     */
    public static final int IMWRITE_PAM_FORMAT_RGB_ALPHA = 5;

    /**
     * Applies no filter to the PNG image (useful when you want to save the raw pixel data without any compression
     * filter).
     */
    public static final int IMWRITE_PNG_FILTER_NONE = 8;

    /**
     * Applies the "sub" filter, which calculates the difference between the current byte and the previous byte in the
     * row.
     */
    public static final int IMWRITE_PNG_FILTER_SUB = 16;

    /**
     * Applies the "up" filter, which calculates the difference between the current byte and the corresponding byte
     * directly above it.
     */
    public static final int IMWRITE_PNG_FILTER_UP = 32;

    /**
     * Applies the "average" filter, which calculates the average of the byte to the left and the byte above.
     */
    public static final int IMWRITE_PNG_FILTER_AVG = 64;

    /**
     * Applies the "Paeth" filter, a more complex filter that predicts the next pixel value based on neighboring pixels.
     */
    public static final int IMWRITE_PNG_FILTER_PAETH = 128;

    /**
     * This is a combination of IMWRITE_PNG_FILTER_NONE, IMWRITE_PNG_FILTER_SUB, and IMWRITE_PNG_FILTER_UP, typically
     * used for faster compression.
     */
    public static final int IMWRITE_PNG_FAST_FILTERS = (IMWRITE_PNG_FILTER_NONE | IMWRITE_PNG_FILTER_SUB
            | IMWRITE_PNG_FILTER_UP);

    /**
     * This combines all available filters (NONE, SUB, UP, AVG, and PAETH), which will attempt to apply all of them for
     * the best possible compression.
     */
    public static final int IMWRITE_PNG_ALL_FILTERS = (IMWRITE_PNG_FAST_FILTERS | IMWRITE_PNG_FILTER_AVG
            | IMWRITE_PNG_FILTER_PAETH);

    /**
     * Use this value for normal data.
     */
    public static final int IMWRITE_PNG_STRATEGY_DEFAULT = 0;

    /**
     * Use this value for data produced by a filter (or predictor).Filtered data consists mostly of small values with a
     * somewhat random distribution. In this case, the compression algorithm is tuned to compress them better.
     */
    public static final int IMWRITE_PNG_STRATEGY_FILTERED = 1;

    /**
     * Use this value to force Huffman encoding only (no string match).
     */
    public static final int IMWRITE_PNG_STRATEGY_HUFFMAN_ONLY = 2;

    /**
     * Use this value to limit match distances to one (run-length encoding).
     */
    public static final int IMWRITE_PNG_STRATEGY_RLE = 3;

    /**
     * Using this value prevents the use of dynamic Huffman codes, allowing for a simpler decoder for special
     * applications.
     */
    public static final int IMWRITE_PNG_STRATEGY_FIXED = 4;

    /**
     * Dump mode.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_NONE = 1;

    /**
     * CCITT modified Huffman RLE.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_CCITTRLE = 2;

    /**
     * CCITT Group 3 fax encoding.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_CCITTFAX3 = 3;

    /**
     * CCITT T.4 (TIFF 6 name).
     */
    public static final int IMWRITE_TIFF_COMPRESSION_CCITT_T4 = 3;

    /**
     * CCITT Group 4 fax encoding.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_CCITTFAX4 = 4;

    /**
     * CCITT T.6 (TIFF 6 name).
     */
    public static final int IMWRITE_TIFF_COMPRESSION_CCITT_T6 = 4;

    /**
     * Lempel-Ziv &amp; Welch.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_LZW = 5;

    /**
     * !6.0 JPEG.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_OJPEG = 6;

    /**
     * %JPEG DCT compression.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_JPEG = 7;

    /**
     * !TIFF/FX T.85 JBIG compression.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_T85 = 9;

    /**
     * !TIFF/FX T.43 colour by layered JBIG compression.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_T43 = 10;

    /**
     * NeXT 2-bit RLE.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_NEXT = 32766;

    /**
     * #1 w/ word alignment.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_CCITTRLEW = 32771;

    /**
     * Macintosh RLE.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_PACKBITS = 32773;

    /**
     * ThunderScan RLE.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_THUNDERSCAN = 32809;

    /**
     * IT8 CT w/padding.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_IT8CTPAD = 32895;

    /**
     * IT8 Linework RLE.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_IT8LW = 32896;

    /**
     * IT8 Monochrome picture.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_IT8MP = 32897;

    /**
     * IT8 Binary line art.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_IT8BL = 32898;

    /**
     * Pixar companded 10bit LZW.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_PIXARFILM = 32908;

    /**
     * Pixar companded 11bit ZIP.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_PIXARLOG = 32909;

    /**
     * Deflate compression, legacy tag.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_DEFLATE = 32946;

    /**
     * Deflate compression, as recognized by Adobe.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_ADOBE_DEFLATE = 8;

    /**
     * Kodak DCS encoding.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_DCS = 32947;

    /**
     * ISO JBIG.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_JBIG = 34661;

    /**
     * SGI Log Luminance RLE.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_SGILOG = 34676;

    /**
     * SGI Log 24-bit packed.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_SGILOG24 = 34677;

    /**
     * Leadtools JPEG2000.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_JP2000 = 34712;

    /**
     * ESRI Lerc codec: https://github.com/Esri/lerc.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_LERC = 34887;

    /**
     * Request LZMA compression for TIFF output when supported by the TIFF codec.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_LZMA = 34925;

    /**
     * ZSTD: WARNING not registered in Adobe-maintained registry.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_ZSTD = 50000;

    /**
     * WEBP: WARNING not registered in Adobe-maintained registry.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_WEBP = 50001;

    /**
     * JPEGXL: WARNING not registered in Adobe-maintained registry.
     */
    public static final int IMWRITE_TIFF_COMPRESSION_JXL = 50002;

    /**
     * No prediction scheme used.
     */
    public static final int IMWRITE_TIFF_PREDICTOR_NONE = 1;

    /**
     * Horizontal differencing.
     */
    public static final int IMWRITE_TIFF_PREDICTOR_HORIZONTAL = 2;

    /**
     * Floating point predictor.
     */
    public static final int IMWRITE_TIFF_PREDICTOR_FLOATINGPOINT = 3;

    /**
     * No absolute unit of measurement.
     */
    public static final int IMWRITE_TIFF_RESOLUTION_UNIT_NONE = 1;

    /**
     * Interpret TIFF horizontal and vertical resolution values as pixels per inch.
     */
    public static final int IMWRITE_TIFF_RESOLUTION_UNIT_INCH = 2;

    /**
     * Centimeter.
     */
    public static final int IMWRITE_TIFF_RESOLUTION_UNIT_CENTIMETER = 3;

    /**
     * Lossy compression mode. Uses IMWRITE_WEBP_QUALITY to control compression. (Default).
     */
    public static final int IMWRITE_WEBP_LOSSLESS_OFF = 0;

    /**
     * Standard lossless compression. May modify or discard RGB values of fully transparent pixels to improve
     * compression ratio.
     */
    public static final int IMWRITE_WEBP_LOSSLESS_ON = 1;

    /**
     * Exact lossless compression. Preserves all RGB data even for pixels with 0 alpha (equivalent to WebP's exact
     * flag).
     */
    public static final int IMWRITE_WEBP_LOSSLESS_PRESERVE_COLOR = 2;

    /**
     * DICOM photometric interpretation identifier for Unknown.
     */
    public static final int EPI_Unknown = 0;

    /**
     * DICOM photometric interpretation identifier for Missing.
     */
    public static final int EPI_Missing = 1;

    /**
     * DICOM photometric interpretation identifier for Monochrome1.
     */
    public static final int EPI_Monochrome1 = 2;

    /**
     * DICOM photometric interpretation identifier for Monochrome2.
     */
    public static final int EPI_Monochrome2 = 3;

    /**
     * DICOM photometric interpretation identifier for Palette Color.
     */
    public static final int EPI_PaletteColor = 4;

    /**
     * DICOM photometric interpretation identifier for RGB.
     */
    public static final int EPI_RGB = 5;

    /**
     * DICOM photometric interpretation identifier for HSV.
     */
    public static final int EPI_HSV = 6;

    /**
     * DICOM photometric interpretation identifier for ARGB.
     */
    public static final int EPI_ARGB = 7;

    /**
     * DICOM photometric interpretation identifier for CMYK.
     */
    public static final int EPI_CMYK = 8;

    /**
     * DICOM photometric interpretation identifier for YBR Full.
     */
    public static final int EPI_YBR_Full = 9;

    /**
     * DICOM photometric interpretation identifier for YBR Full 422.
     */
    public static final int EPI_YBR_Full_422 = 10;

    /**
     * DICOM photometric interpretation identifier for YBR Partial 422.
     */
    public static final int EPI_YBR_Partial_422 = 11;

    /**
     * Component layout using separate component planes.
     */
    public static final int ILV_NONE = 0;

    /**
     * Component layout using component lines.
     */
    public static final int ILV_LINE = 1;

    /**
     * Component layout using interleaved samples.
     */
    public static final int ILV_SAMPLE = 2;

    /**
     * Loads an image from a file. This is an overloaded member function, provided for convenience. It differs from the
     * above function only in what argument(s) it accepts and the return value.
     *
     * @param filename Name of file to be loaded
     * @param flags    Flag that can take values of ImreadModes, default with IMREAD_COLOR_BGR. Note: The image passing
     *                 through the img parameter can be pre-allocated. The memory is reused if the shape and the type
     *                 match with the load image
     * @return the loaded image, or an empty matrix when reading or decoding fails
     */
    public static Mat imread(String filename, int flags) {
        return new Mat(imread_0(filename, flags));
    }

    /**
     * Loads an image from a file. This is an overloaded member function, provided for convenience. It differs from the
     * above function only in what argument(s) it accepts and the return value.
     *
     * @param filename Name of file to be loaded
     * @return the loaded image, or an empty matrix when reading or decoding fails
     */
    public static Mat imread(String filename) {
        return new Mat(imread_1(filename));
    }

    /**
     * Loads an image from a file. This is an overloaded member function, provided for convenience. It differs from the
     * above function only in what argument(s) it accepts and the return value.
     *
     * @param filename Name of file to be loaded
     * @param dst      object in which the image will be loaded
     * @param flags    Flag that can take values of ImreadModes, default with IMREAD_COLOR_BGR. Note: The image passing
     *                 through the img parameter can be pre-allocated. The memory is reused if the shape and the type
     *                 match with the load image
     * @throws NullPointerException if dst is null
     */
    public static void imread(String filename, Mat dst, int flags) {
        imread_2(filename, dst.nativeObj, flags);
    }

    /**
     * Loads an image from a file. This is an overloaded member function, provided for convenience. It differs from the
     * above function only in what argument(s) it accepts and the return value.
     *
     * @param filename Name of file to be loaded
     * @param dst      object in which the image will be loaded
     * @throws NullPointerException if dst is null
     */
    public static void imread(String filename, Mat dst) {
        imread_3(filename, dst.nativeObj);
    }

    /**
     * Loads an image and its available metadata blocks. Metadata identifiers correspond to the returned block list.
     * When EXIF is present, the compatibility reader also appends the parsed UTF-8 tag matrix identified by
     * IMAGE_METADATA_PARSE_EXIF. The overload without flags uses IMREAD_UNCHANGED.
     *
     * @param filename      Name of the file to be loaded
     * @param metadataTypes Output vector with types of metadata chunks returned in metadata, see ImageMetadataType
     * @param metadata      Output nested collection or vector of matrices to store the retrieved metadata
     * @param flags         Flag that can take values of ImreadModes
     * @return The loaded image as a Mat object. If the image cannot be read, the function returns an empty matrix
     */
    public static Mat imreadWithMetadata(String filename, MatOfInt metadataTypes, List<Mat> metadata, int flags) {
        Mat metadataTypes_mat = metadataTypes;
        Mat metadata_mat = new Mat();
        Mat retVal = new Mat(
                imreadWithMetadata_0(filename, metadataTypes_mat.nativeObj, metadata_mat.nativeObj, flags));
        Converters.Mat_to_vector_Mat(metadata_mat, metadata);
        metadata_mat.release();
        return retVal;
    }

    /**
     * Loads an image and its available metadata blocks. Metadata identifiers correspond to the returned block list.
     * When EXIF is present, the compatibility reader also appends the parsed UTF-8 tag matrix identified by
     * IMAGE_METADATA_PARSE_EXIF. The overload without flags uses IMREAD_UNCHANGED.
     *
     * @param filename      Name of the file to be loaded
     * @param metadataTypes Output vector with types of metadata chunks returned in metadata, see ImageMetadataType
     * @param metadata      Output nested collection or vector of matrices to store the retrieved metadata
     * @return The loaded image as a Mat object. If the image cannot be read, the function returns an empty matrix
     */
    public static Mat imreadWithMetadata(String filename, MatOfInt metadataTypes, List<Mat> metadata) {
        Mat metadataTypes_mat = metadataTypes;
        Mat metadata_mat = new Mat();
        Mat retVal = new Mat(imreadWithMetadata_1(filename, metadataTypes_mat.nativeObj, metadata_mat.nativeObj));
        Converters.Mat_to_vector_Mat(metadata_mat, metadata);
        metadata_mat.release();
        return retVal;
    }

    /**
     * Loads images of a multi-page image from a file. The function imreadmulti loads a specified range from a
     * multi-page image from the specified file into a vector of Mat objects.
     *
     * @param filename Name of file to be loaded
     * @param mats     A vector of Mat objects holding each page
     * @param flags    Flag that can take values of ImreadModes, default with IMREAD_ANYCOLOR
     * @return true if the requested pages are loaded successfully
     */
    public static boolean imreadmulti(String filename, List<Mat> mats, int flags) {
        Mat mats_mat = new Mat();
        boolean retVal = imreadmulti_0(filename, mats_mat.nativeObj, flags);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    /**
     * Loads images of a multi-page image from a file. The function imreadmulti loads a specified range from a
     * multi-page image from the specified file into a vector of Mat objects.
     *
     * @param filename Name of file to be loaded
     * @param mats     A vector of Mat objects holding each page
     * @return true if the requested pages are loaded successfully
     */
    public static boolean imreadmulti(String filename, List<Mat> mats) {
        Mat mats_mat = new Mat();
        boolean retVal = imreadmulti_1(filename, mats_mat.nativeObj);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    /**
     * Loads images of a multi-page image from a file. The function imreadmulti loads a specified range from a
     * multi-page image from the specified file into a vector of Mat objects.
     *
     * @param filename Name of file to be loaded
     * @param mats     A vector of Mat objects holding each page
     * @param start    Start index of the image to load
     * @param count    Count number of images to load
     * @param flags    Flag that can take values of ImreadModes, default with IMREAD_ANYCOLOR
     * @return true if the requested pages are loaded successfully
     */
    public static boolean imreadmulti(String filename, List<Mat> mats, int start, int count, int flags) {
        Mat mats_mat = new Mat();
        boolean retVal = imreadmulti_2(filename, mats_mat.nativeObj, start, count, flags);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    /**
     * Loads images of a multi-page image from a file. The function imreadmulti loads a specified range from a
     * multi-page image from the specified file into a vector of Mat objects.
     *
     * @param filename Name of file to be loaded
     * @param mats     A vector of Mat objects holding each page
     * @param start    Start index of the image to load
     * @param count    Count number of images to load
     * @return true if the requested pages are loaded successfully
     */
    public static boolean imreadmulti(String filename, List<Mat> mats, int start, int count) {
        Mat mats_mat = new Mat();
        boolean retVal = imreadmulti_3(filename, mats_mat.nativeObj, start, count);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    /**
     * Loads frames from an animated image file into an Animation structure. The function imreadanimation loads frames
     * from an animated image file (e.g., GIF, AVIF, APNG, WEBP) into the provided Animation struct.
     *
     * @param filename  A string containing the path to the file
     * @param animation A reference to an Animation structure where the loaded frames will be stored. It should be
     *                  initialized before the function is called
     * @param start     The index of the first frame to load. This is optional and defaults to 0
     * @param count     The number of frames to load. This is optional and defaults to 32767
     * @return Returns true if the file was successfully loaded and frames were extracted; returns false otherwise
     * @throws NullPointerException if animation is null
     */
    public static boolean imreadanimation(String filename, Animation animation, int start, int count) {
        return imreadanimation_0(filename, animation.getNativeObjAddr(), start, count);
    }

    /**
     * Loads frames from an animated image file into an Animation structure. The function imreadanimation loads frames
     * from an animated image file (e.g., GIF, AVIF, APNG, WEBP) into the provided Animation struct.
     *
     * @param filename  A string containing the path to the file
     * @param animation A reference to an Animation structure where the loaded frames will be stored. It should be
     *                  initialized before the function is called
     * @param start     The index of the first frame to load. This is optional and defaults to 0
     * @return Returns true if the file was successfully loaded and frames were extracted; returns false otherwise
     * @throws NullPointerException if animation is null
     */
    public static boolean imreadanimation(String filename, Animation animation, int start) {
        return imreadanimation_1(filename, animation.getNativeObjAddr(), start);
    }

    /**
     * Loads frames from an animated image file into an Animation structure. The function imreadanimation loads frames
     * from an animated image file (e.g., GIF, AVIF, APNG, WEBP) into the provided Animation struct.
     *
     * @param filename  A string containing the path to the file
     * @param animation A reference to an Animation structure where the loaded frames will be stored. It should be
     *                  initialized before the function is called
     * @return Returns true if the file was successfully loaded and frames were extracted; returns false otherwise
     * @throws NullPointerException if animation is null
     */
    public static boolean imreadanimation(String filename, Animation animation) {
        return imreadanimation_2(filename, animation.getNativeObjAddr());
    }

    /**
     * Loads frames from an animated image buffer into an Animation structure. The function imdecodeanimation loads
     * frames from an animated image buffer (e.g., GIF, AVIF, APNG, WEBP) into the provided Animation struct.
     *
     * @param buf       A reference to an matrix containing the image buffer
     * @param animation A reference to an Animation structure where the loaded frames will be stored. It should be
     *                  initialized before the function is called
     * @param start     The index of the first frame to load. This is optional and defaults to 0
     * @param count     The number of frames to load. This is optional and defaults to 32767
     * @return Returns true if the buffer was successfully loaded and frames were extracted; returns false otherwise
     * @throws NullPointerException if buf, animation is null
     */
    public static boolean imdecodeanimation(Mat buf, Animation animation, int start, int count) {
        return imdecodeanimation_0(buf.nativeObj, animation.getNativeObjAddr(), start, count);
    }

    /**
     * Loads frames from an animated image buffer into an Animation structure. The function imdecodeanimation loads
     * frames from an animated image buffer (e.g., GIF, AVIF, APNG, WEBP) into the provided Animation struct.
     *
     * @param buf       A reference to an matrix containing the image buffer
     * @param animation A reference to an Animation structure where the loaded frames will be stored. It should be
     *                  initialized before the function is called
     * @param start     The index of the first frame to load. This is optional and defaults to 0
     * @return Returns true if the buffer was successfully loaded and frames were extracted; returns false otherwise
     * @throws NullPointerException if buf, animation is null
     */
    public static boolean imdecodeanimation(Mat buf, Animation animation, int start) {
        return imdecodeanimation_1(buf.nativeObj, animation.getNativeObjAddr(), start);
    }

    /**
     * Loads frames from an animated image buffer into an Animation structure. The function imdecodeanimation loads
     * frames from an animated image buffer (e.g., GIF, AVIF, APNG, WEBP) into the provided Animation struct.
     *
     * @param buf       A reference to an matrix containing the image buffer
     * @param animation A reference to an Animation structure where the loaded frames will be stored. It should be
     *                  initialized before the function is called
     * @return Returns true if the buffer was successfully loaded and frames were extracted; returns false otherwise
     * @throws NullPointerException if buf, animation is null
     */
    public static boolean imdecodeanimation(Mat buf, Animation animation) {
        return imdecodeanimation_2(buf.nativeObj, animation.getNativeObjAddr());
    }

    /**
     * Saves an Animation to a specified file. The function imwriteanimation saves the provided Animation data to the
     * specified file in an animated format. Supported formats depend on the implementation and may include formats like
     * GIF, AVIF, APNG, or WEBP.
     *
     * @param filename  The name of the file where the animation will be saved. The file extension determines the format
     * @param animation A constant reference to an Animation struct containing the frames and metadata to be saved
     * @param params    Optional format-specific parameters encoded as pairs (paramId_1, paramValue_1, paramId_2,
     *                  paramValue_2, ...). These parameters are used to specify additional options for the encoding
     *                  process. Refer to for details on possible parameters
     * @return Returns true if the animation was successfully saved; returns false otherwise
     * @throws NullPointerException if animation is null
     */
    public static boolean imwriteanimation(String filename, Animation animation, MatOfInt params) {
        Mat params_mat = params;
        return imwriteanimation_0(filename, animation.getNativeObjAddr(), params_mat.nativeObj);
    }

    /**
     * Saves an Animation to a specified file. The function imwriteanimation saves the provided Animation data to the
     * specified file in an animated format. Supported formats depend on the implementation and may include formats like
     * GIF, AVIF, APNG, or WEBP.
     *
     * @param filename  The name of the file where the animation will be saved. The file extension determines the format
     * @param animation A constant reference to an Animation struct containing the frames and metadata to be saved
     * @return Returns true if the animation was successfully saved; returns false otherwise
     * @throws NullPointerException if animation is null
     */
    public static boolean imwriteanimation(String filename, Animation animation) {
        return imwriteanimation_1(filename, animation.getNativeObjAddr());
    }

    /**
     * Encodes an Animation to a memory buffer. The function imencodeanimation encodes the provided Animation data into
     * a memory buffer in an animated format. Supported formats depend on the implementation and may include formats
     * like GIF, AVIF, APNG, or WEBP.
     *
     * @param ext       The file extension that determines the format of the encoded data
     * @param animation A constant reference to an Animation struct containing the frames and metadata to be encoded
     * @param buf       A reference to a vector of unsigned chars where the encoded data will be stored
     * @param params    Optional format-specific parameters encoded as pairs (paramId_1, paramValue_1, paramId_2,
     *                  paramValue_2, ...). These parameters are used to specify additional options for the encoding
     *                  process. Refer to for details on possible parameters
     * @return Returns true if the animation was successfully encoded; returns false otherwise
     * @throws NullPointerException if animation is null
     */
    public static boolean imencodeanimation(String ext, Animation animation, MatOfByte buf, MatOfInt params) {
        Mat buf_mat = buf;
        Mat params_mat = params;
        return imencodeanimation_0(ext, animation.getNativeObjAddr(), buf_mat.nativeObj, params_mat.nativeObj);
    }

    /**
     * Encodes an Animation to a memory buffer. The function imencodeanimation encodes the provided Animation data into
     * a memory buffer in an animated format. Supported formats depend on the implementation and may include formats
     * like GIF, AVIF, APNG, or WEBP.
     *
     * @param ext       The file extension that determines the format of the encoded data
     * @param animation A constant reference to an Animation struct containing the frames and metadata to be encoded
     * @param buf       A reference to a vector of unsigned chars where the encoded data will be stored
     * @return Returns true if the animation was successfully encoded; returns false otherwise
     * @throws NullPointerException if animation is null
     */
    public static boolean imencodeanimation(String ext, Animation animation, MatOfByte buf) {
        Mat buf_mat = buf;
        return imencodeanimation_1(ext, animation.getNativeObjAddr(), buf_mat.nativeObj);
    }

    /**
     * Returns the number of images inside the given file The function imcount returns the number of pages in a
     * multi-page image (e.g. TIFF), the number of frames in an animation (e.g. AVIF), and 1 otherwise. If the image
     * cannot be decoded, 0 is returned.
     *
     * @param filename Name of file to be loaded
     * @param flags    Flag that can take values of ImreadModes, default with IMREAD_COLOR_BGR. Note: The default flags
     *                 value was changed from IMREAD_ANYCOLOR to IMREAD_COLOR_BGR for unification
     * @return the number of readable pages or frames; zero indicates a decoding failure
     */
    public static long imcount(String filename, int flags) {
        return imcount_0(filename, flags);
    }

    /**
     * Returns the number of images inside the given file The function imcount returns the number of pages in a
     * multi-page image (e.g. TIFF), the number of frames in an animation (e.g. AVIF), and 1 otherwise. If the image
     * cannot be decoded, 0 is returned.
     *
     * @param filename Name of file to be loaded
     * @return the number of readable pages or frames; zero indicates a decoding failure
     */
    public static long imcount(String filename) {
        return imcount_1(filename);
    }

    /**
     * Encodes an image and writes it to the specified file. The filename extension selects the encoder. Supported
     * element depths and channel counts depend on that encoder. Color images use BGR or BGRA channel order. Encoding
     * options are supplied as integer key/value pairs.
     *
     * @param filename Name of the file
     * @param img      (Mat or vector of Mat) Image or Images to be saved
     * @param params   Format-specific parameters encoded as pairs (paramId_1, paramValue_1, paramId_2, paramValue_2,
     *                 ... .) see ImwriteFlags
     * @return true if the image is successfully written to the specified file; false otherwise
     * @throws NullPointerException if img is null
     */
    public static boolean imwrite(String filename, Mat img, MatOfInt params) {
        Mat params_mat = params;
        return imwrite_0(filename, img.nativeObj, params_mat.nativeObj);
    }

    /**
     * Encodes an image and writes it to the specified file. The filename extension selects the encoder. Supported
     * element depths and channel counts depend on that encoder. Color images use BGR or BGRA channel order. Encoding
     * options are supplied as integer key/value pairs.
     *
     * @param filename Name of the file
     * @param img      (Mat or vector of Mat) Image or Images to be saved
     * @return true if the image is successfully written to the specified file; false otherwise
     * @throws NullPointerException if img is null
     */
    public static boolean imwrite(String filename, Mat img) {
        return imwrite_1(filename, img.nativeObj);
    }

    /**
     * Saves an image to a specified file with metadata The function imwriteWithMetadata saves the image to the
     * specified file. It does the same thing as imwrite, but additionally writes metadata if the corresponding format
     * supports it.
     *
     * @param filename      Name of the file. As with imwrite, image format is determined by the file extension
     * @param img           (Mat or vector of Mat) Image or Images to be saved
     * @param metadataTypes Vector with types of metadata chucks stored in metadata to write, see ImageMetadataType
     * @param metadata      Vector of vectors or vector of matrices with chunks of metadata to store into the file
     * @param params        Format-specific parameters encoded as pairs (paramId_1, paramValue_1, paramId_2,
     *                      paramValue_2, ... .) see ImwriteFlags
     * @return true if the image and supported metadata are written successfully
     * @throws NullPointerException if img is null
     */
    public static boolean imwriteWithMetadata(
            String filename,
            Mat img,
            MatOfInt metadataTypes,
            List<Mat> metadata,
            MatOfInt params) {
        Mat metadataTypes_mat = metadataTypes;
        Mat metadata_mat = Converters.vector_Mat_to_Mat(metadata);
        Mat params_mat = params;
        return imwriteWithMetadata_0(
                filename,
                img.nativeObj,
                metadataTypes_mat.nativeObj,
                metadata_mat.nativeObj,
                params_mat.nativeObj);
    }

    /**
     * Saves an image to a specified file with metadata The function imwriteWithMetadata saves the image to the
     * specified file. It does the same thing as imwrite, but additionally writes metadata if the corresponding format
     * supports it.
     *
     * @param filename      Name of the file. As with imwrite, image format is determined by the file extension
     * @param img           (Mat or vector of Mat) Image or Images to be saved
     * @param metadataTypes Vector with types of metadata chucks stored in metadata to write, see ImageMetadataType
     * @param metadata      Vector of vectors or vector of matrices with chunks of metadata to store into the file
     * @return true if the image and supported metadata are written successfully
     * @throws NullPointerException if img is null
     */
    public static boolean imwriteWithMetadata(String filename, Mat img, MatOfInt metadataTypes, List<Mat> metadata) {
        Mat metadataTypes_mat = metadataTypes;
        Mat metadata_mat = Converters.vector_Mat_to_Mat(metadata);
        return imwriteWithMetadata_1(filename, img.nativeObj, metadataTypes_mat.nativeObj, metadata_mat.nativeObj);
    }

    /**
     * Writes a collection of images to a format that supports multiple pages.
     *
     * @param filename path of the input or output file
     * @param img      image to process or annotate
     * @param params   codec-specific encoding options as integer key/value pairs
     * @return true if the image collection is written successfully
     */
    public static boolean imwritemulti(String filename, List<Mat> img, MatOfInt params) {
        Mat img_mat = Converters.vector_Mat_to_Mat(img);
        Mat params_mat = params;
        return imwritemulti_0(filename, img_mat.nativeObj, params_mat.nativeObj);
    }

    /**
     * Writes a collection of images to a format that supports multiple pages.
     *
     * @param filename path of the input or output file
     * @param img      image to process or annotate
     * @return true if the image collection is written successfully
     */
    public static boolean imwritemulti(String filename, List<Mat> img) {
        Mat img_mat = Converters.vector_Mat_to_Mat(img);
        return imwritemulti_1(filename, img_mat.nativeObj);
    }

    /**
     * Reads an image from a buffer in memory. The function imdecode reads an image from the specified buffer in the
     * memory. If the buffer is too short or contains invalid data, the function returns an empty matrix (
     * Mat.data==NULL ). See imread for the list of supported formats and flags description. Note: In the case of color
     * images, the decoded images will have the channels stored in B G R order.
     *
     * @param buf   Input array or vector of bytes
     * @param flags Flag that can take values of ImreadModes
     * @return the decoded image, or an empty matrix when decoding fails
     * @throws NullPointerException if buf is null
     */
    public static Mat imdecode(Mat buf, int flags) {
        return new Mat(imdecode_0(buf.nativeObj, flags));
    }

    /**
     * Reads an image from a memory buffer and extracts associated metadata. This function decodes an image from the
     * specified memory buffer. If the buffer is too short or contains invalid data, the function returns an empty
     * matrix ( Mat.data==NULL ). See imread for the list of supported formats and flags description. Note: In the case
     * of color images, the decoded images will have the channels stored in B G R order.
     *
     * @param buf           Input array or vector of bytes containing the encoded image data
     * @param metadataTypes Output vector with types of metadata chucks returned in metadata, see ImageMetadataType
     * @param metadata      Output nested collection or vector of matrices to store the retrieved metadata
     * @param flags         Flag that can take values of ImreadModes
     * @return The decoded image as a Mat object. If decoding fails, the function returns an empty matrix
     * @throws NullPointerException if buf is null
     */
    public static Mat imdecodeWithMetadata(Mat buf, MatOfInt metadataTypes, List<Mat> metadata, int flags) {
        Mat metadataTypes_mat = metadataTypes;
        Mat metadata_mat = new Mat();
        Mat retVal = new Mat(
                imdecodeWithMetadata_0(buf.nativeObj, metadataTypes_mat.nativeObj, metadata_mat.nativeObj, flags));
        Converters.Mat_to_vector_Mat(metadata_mat, metadata);
        metadata_mat.release();
        return retVal;
    }

    /**
     * Reads a multi-page image from a buffer in memory. The function imdecodemulti reads a multi-page image from the
     * specified buffer in the memory. If the buffer is too short or contains invalid data, the function returns false.
     * See imreadmulti for the list of supported formats and flags description. Note: In the case of color images, the
     * decoded images will have the channels stored in B G R order.
     *
     * @param buf   Input array or vector of bytes
     * @param flags Flag that can take values of ImreadModes
     * @param mats  A vector of Mat objects holding each page, if more than one
     * @param range A continuous selection of pages
     * @return true if the requested pages are decoded successfully
     * @throws NullPointerException if buf, range is null
     */
    public static boolean imdecodemulti(Mat buf, int flags, List<Mat> mats, Range range) {
        Mat mats_mat = new Mat();
        boolean retVal = imdecodemulti_0(buf.nativeObj, flags, mats_mat.nativeObj, range.start, range.end);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    /**
     * Reads a multi-page image from a buffer in memory. The function imdecodemulti reads a multi-page image from the
     * specified buffer in the memory. If the buffer is too short or contains invalid data, the function returns false.
     * See imreadmulti for the list of supported formats and flags description. Note: In the case of color images, the
     * decoded images will have the channels stored in B G R order.
     *
     * @param buf   Input array or vector of bytes
     * @param flags Flag that can take values of ImreadModes
     * @param mats  A vector of Mat objects holding each page, if more than one
     * @return true if the requested pages are decoded successfully
     * @throws NullPointerException if buf is null
     */
    public static boolean imdecodemulti(Mat buf, int flags, List<Mat> mats) {
        Mat mats_mat = new Mat();
        boolean retVal = imdecodemulti_1(buf.nativeObj, flags, mats_mat.nativeObj);
        Converters.Mat_to_vector_Mat(mats_mat, mats);
        mats_mat.release();
        return retVal;
    }

    /**
     * Encodes an image into a memory buffer. The function imencode compresses the image and stores it in the memory
     * buffer that is resized to fit the result. See imwrite for the list of supported formats and flags description.
     *
     * @param ext    File extension that defines the output format. Must include a leading period
     * @param img    Image to be compressed
     * @param buf    Output buffer resized to fit the compressed image
     * @param params Format-specific parameters. See imwrite and ImwriteFlags
     * @return true if the encoded bytes are written to buf
     * @throws NullPointerException if img is null
     */
    public static boolean imencode(String ext, Mat img, MatOfByte buf, MatOfInt params) {
        Mat buf_mat = buf;
        Mat params_mat = params;
        return imencode_0(ext, img.nativeObj, buf_mat.nativeObj, params_mat.nativeObj);
    }

    /**
     * Encodes an image into a memory buffer. The function imencode compresses the image and stores it in the memory
     * buffer that is resized to fit the result. See imwrite for the list of supported formats and flags description.
     *
     * @param ext File extension that defines the output format. Must include a leading period
     * @param img Image to be compressed
     * @param buf Output buffer resized to fit the compressed image
     * @return true if the encoded bytes are written to buf
     * @throws NullPointerException if img is null
     */
    public static boolean imencode(String ext, Mat img, MatOfByte buf) {
        Mat buf_mat = buf;
        return imencode_1(ext, img.nativeObj, buf_mat.nativeObj);
    }

    /**
     * Encodes an image into a memory buffer. The function imencode compresses the image and stores it in the memory
     * buffer that is resized to fit the result. See imwrite for the list of supported formats and flags description.
     *
     * @param ext           File extension that defines the output format. Must include a leading period
     * @param img           Image to be compressed
     * @param metadataTypes Vector with types of metadata chucks stored in metadata to write, see ImageMetadataType
     * @param metadata      Vector of vectors or vector of matrices with chunks of metadata to store into the file
     * @param buf           Output buffer resized to fit the compressed image
     * @param params        Format-specific parameters. See imwrite and ImwriteFlags
     * @return true if the encoded image and supported metadata are written to buf
     * @throws NullPointerException if img is null
     */
    public static boolean imencodeWithMetadata(
            String ext,
            Mat img,
            MatOfInt metadataTypes,
            List<Mat> metadata,
            MatOfByte buf,
            MatOfInt params) {
        Mat metadataTypes_mat = metadataTypes;
        Mat metadata_mat = Converters.vector_Mat_to_Mat(metadata);
        Mat buf_mat = buf;
        Mat params_mat = params;
        return imencodeWithMetadata_0(
                ext,
                img.nativeObj,
                metadataTypes_mat.nativeObj,
                metadata_mat.nativeObj,
                buf_mat.nativeObj,
                params_mat.nativeObj);
    }

    /**
     * Encodes an image into a memory buffer. The function imencode compresses the image and stores it in the memory
     * buffer that is resized to fit the result. See imwrite for the list of supported formats and flags description.
     *
     * @param ext           File extension that defines the output format. Must include a leading period
     * @param img           Image to be compressed
     * @param metadataTypes Vector with types of metadata chucks stored in metadata to write, see ImageMetadataType
     * @param metadata      Vector of vectors or vector of matrices with chunks of metadata to store into the file
     * @param buf           Output buffer resized to fit the compressed image
     * @return true if the encoded image and supported metadata are written to buf
     * @throws NullPointerException if img is null
     */
    public static boolean imencodeWithMetadata(
            String ext,
            Mat img,
            MatOfInt metadataTypes,
            List<Mat> metadata,
            MatOfByte buf) {
        Mat metadataTypes_mat = metadataTypes;
        Mat metadata_mat = Converters.vector_Mat_to_Mat(metadata);
        Mat buf_mat = buf;
        return imencodeWithMetadata_1(
                ext,
                img.nativeObj,
                metadataTypes_mat.nativeObj,
                metadata_mat.nativeObj,
                buf_mat.nativeObj);
    }

    /**
     * Encodes array of images into a memory buffer. The function is analog to imencode for in-memory multi-page image
     * compression. See imwrite for the list of supported formats and flags description.
     *
     * @param ext    File extension that defines the output format. Must include a leading period
     * @param imgs   Vector of images to be written
     * @param buf    Output buffer resized to fit the compressed data
     * @param params Format-specific parameters. See imwrite and ImwriteFlags
     * @return true if the supplied images are encoded into buf
     */
    public static boolean imencodemulti(String ext, List<Mat> imgs, MatOfByte buf, MatOfInt params) {
        Mat imgs_mat = Converters.vector_Mat_to_Mat(imgs);
        Mat buf_mat = buf;
        Mat params_mat = params;
        return imencodemulti_0(ext, imgs_mat.nativeObj, buf_mat.nativeObj, params_mat.nativeObj);
    }

    /**
     * Encodes array of images into a memory buffer. The function is analog to imencode for in-memory multi-page image
     * compression. See imwrite for the list of supported formats and flags description.
     *
     * @param ext  File extension that defines the output format. Must include a leading period
     * @param imgs Vector of images to be written
     * @param buf  Output buffer resized to fit the compressed data
     * @return true if the supplied images are encoded into buf
     */
    public static boolean imencodemulti(String ext, List<Mat> imgs, MatOfByte buf) {
        Mat imgs_mat = Converters.vector_Mat_to_Mat(imgs);
        Mat buf_mat = buf;
        return imencodemulti_1(ext, imgs_mat.nativeObj, buf_mat.nativeObj);
    }

    /**
     * Checks if the specified image file can be decoded by OpenCV. The function haveImageReader checks if OpenCV is
     * capable of reading the specified file. This can be useful for verifying support for a given image format before
     * attempting to load an image.
     *
     * @param filename The name of the file to be checked
     * @return true if an image reader for the specified file is available and the file can be opened, false otherwise.
     *         Note: The function checks the availability of image codecs that are either built into OpenCV or
     *         dynamically loaded. It does not load the image codec implementation and decode data, but uses signature
     *         check. If the file cannot be opened or the format is unsupported, the function will return false
     */
    public static boolean haveImageReader(String filename) {
        return haveImageReader_0(filename);
    }

    /**
     * Checks if the specified image file or specified file extension can be encoded by OpenCV. The function
     * haveImageWriter checks if OpenCV is capable of writing images with the specified file extension. This can be
     * useful for verifying support for a given image format before attempting to save an image.
     *
     * @param filename The name of the file or the file extension (e.g., ".jpg", ".png"). It is recommended to provide
     *                 the file extension rather than the full file name
     * @return true if an image writer for the specified extension is available, false otherwise. Note: The function
     *         checks the availability of image codecs that are either built into OpenCV or dynamically loaded. It does
     *         not check for the actual existence of the file but rather the ability to write files of the given type
     */
    public static boolean haveImageWriter(String filename) {
        return haveImageWriter_0(filename);
    }

    /**
     * Decodes a compressed DICOM pixel frame assembled from the specified file fragments. Fragment offsets and lengths
     * identify the encoded payload, excluding the DICOM container structure.
     *
     * @param filename    path of the input or output file
     * @param segposition byte offsets of the pixel-data fragments in the file
     * @param seglength   byte lengths of the corresponding pixel-data fragments
     * @param dicomflags  bit mask controlling DICOM pixel interpretation
     * @param flags       image-decoding options from the IMREAD flags; DICOM sample interpretation is selected
     *                    separately
     * @return the decoded image, or an empty matrix when reading or decoding fails
     */
    public static Mat dicomJpgFileRead(
            String filename,
            MatOfDouble segposition,
            MatOfDouble seglength,
            int dicomflags,
            int flags) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        return new Mat(
                dicomJpgFileRead_0(filename, segposition_mat.nativeObj, seglength_mat.nativeObj, dicomflags, flags));
    }

    /**
     * Decodes a compressed DICOM pixel frame assembled from the specified file fragments. Fragment offsets and lengths
     * identify the encoded payload, excluding the DICOM container structure.
     *
     * @param filename    path of the input or output file
     * @param segposition byte offsets of the pixel-data fragments in the file
     * @param seglength   byte lengths of the corresponding pixel-data fragments
     * @param dicomflags  bit mask controlling DICOM pixel interpretation
     * @return the decoded image, or an empty matrix when reading or decoding fails
     */
    public static Mat dicomJpgFileRead(
            String filename,
            MatOfDouble segposition,
            MatOfDouble seglength,
            int dicomflags) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        return new Mat(dicomJpgFileRead_1(filename, segposition_mat.nativeObj, seglength_mat.nativeObj, dicomflags));
    }

    /**
     * Decodes a compressed DICOM pixel frame assembled from the specified file fragments. Fragment offsets and lengths
     * identify the encoded payload, excluding the DICOM container structure.
     *
     * @param filename    path of the input or output file
     * @param segposition byte offsets of the pixel-data fragments in the file
     * @param seglength   byte lengths of the corresponding pixel-data fragments
     * @return the decoded image, or an empty matrix when reading or decoding fails
     */
    public static Mat dicomJpgFileRead(String filename, MatOfDouble segposition, MatOfDouble seglength) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        return new Mat(dicomJpgFileRead_2(filename, segposition_mat.nativeObj, seglength_mat.nativeObj));
    }

    /**
     * Decodes a compressed DICOM pixel frame from a matrix buffer. A successful decode consumes the input buffer; a
     * failed decode returns an empty matrix without consuming it. Read flags retain the legacy codec-specific
     * interpretation.
     *
     * @param buf        matrix containing the encoded input bytes
     * @param dicomflags bit mask controlling DICOM pixel interpretation
     * @param flags      image-decoding options from the IMREAD flags; DICOM sample interpretation is selected
     *                   separately
     * @return the decoded image, or an empty matrix when decoding fails
     * @throws NullPointerException if buf is null
     */
    public static Mat dicomJpgMatRead(Mat buf, int dicomflags, int flags) {
        return new Mat(dicomJpgMatRead_0(buf.nativeObj, dicomflags, flags));
    }

    /**
     * Decodes a compressed DICOM pixel frame from a matrix buffer. A successful decode consumes the input buffer; a
     * failed decode returns an empty matrix without consuming it. Read flags retain the legacy codec-specific
     * interpretation.
     *
     * @param buf        matrix containing the encoded input bytes
     * @param dicomflags bit mask controlling DICOM pixel interpretation
     * @return the decoded image, or an empty matrix when decoding fails
     * @throws NullPointerException if buf is null
     */
    public static Mat dicomJpgMatRead(Mat buf, int dicomflags) {
        return new Mat(dicomJpgMatRead_1(buf.nativeObj, dicomflags));
    }

    /**
     * Decodes a compressed DICOM pixel frame from a matrix buffer. A successful decode consumes the input buffer; a
     * failed decode returns an empty matrix without consuming it. Read flags retain the legacy codec-specific
     * interpretation.
     *
     * @param buf matrix containing the encoded input bytes
     * @return the decoded image, or an empty matrix when decoding fails
     * @throws NullPointerException if buf is null
     */
    public static Mat dicomJpgMatRead(Mat buf) {
        return new Mat(dicomJpgMatRead_2(buf.nativeObj));
    }

    /**
     * Reads raw or RLE-compressed DICOM pixel data assembled from file fragments. Unlike the memory overload, this
     * reader supports planar multicomponent input. It preserves the accepted 5.0 signed-sample conversion and RLE
     * byte-plane ordering.
     *
     * @param filename    path of the input or output file
     * @param segposition byte offsets of the pixel-data fragments in the file
     * @param seglength   byte lengths of the corresponding pixel-data fragments
     * @param dicomParams ordered DICOM settings indexed by the DICOM_PARAM constants
     * @param colormodel  DICOM photometric interpretation label for the pixel data
     * @return the decoded image, or an empty matrix when reading or decoding fails
     */
    public static Mat dicomRawFileRead(
            String filename,
            MatOfDouble segposition,
            MatOfDouble seglength,
            MatOfInt dicomParams,
            String colormodel) {
        Mat segposition_mat = segposition;
        Mat seglength_mat = seglength;
        Mat dicomParams_mat = dicomParams;
        return new Mat(dicomRawFileRead_0(
                filename,
                segposition_mat.nativeObj,
                seglength_mat.nativeObj,
                dicomParams_mat.nativeObj,
                colormodel));
    }

    /**
     * Reads uncompressed or RLE-compressed DICOM pixel data from a matrix buffer without consuming that buffer. The
     * legacy memory reader returns an empty matrix for planar multicomponent input. Signed stored widths and multibyte
     * color RLE ordering follow the accepted 5.0 behavior.
     *
     * @param buf         matrix containing the encoded input bytes
     * @param dicomParams ordered DICOM settings indexed by the DICOM_PARAM constants
     * @param colormodel  DICOM photometric interpretation label for the pixel data
     * @return the decoded image, or an empty matrix for unsupported or invalid input
     * @throws NullPointerException if buf is null
     */
    public static Mat dicomRawMatRead(Mat buf, MatOfInt dicomParams, String colormodel) {
        Mat dicomParams_mat = dicomParams;
        return new Mat(dicomRawMatRead_0(buf.nativeObj, dicomParams_mat.nativeObj, colormodel));
    }

    /**
     * Encodes an image as a compressed DICOM pixel frame using the codec and settings selected in the parameter vector.
     * Image layout and color interpretation follow that codec's legacy conventions.
     *
     * @param image       input image
     * @param dicomParams ordered DICOM settings indexed by the DICOM_PARAM constants
     * @param colormodel  DICOM photometric interpretation label for the pixel data
     * @return a matrix containing the encoded pixel-frame bytes
     * @throws NullPointerException if image is null
     */
    public static Mat dicomJpgWrite(Mat image, MatOfInt dicomParams, String colormodel) {
        Mat dicomParams_mat = dicomParams;
        return new Mat(dicomJpgWrite_0(image.nativeObj, dicomParams_mat.nativeObj, colormodel));
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imread(String,int)}.
     *
     * @param filename Name of file to be loaded
     * @param flags    Flag that can take values of ImreadModes, default with IMREAD_COLOR_BGR. Note: The image passing
     *                 through the img parameter can be pre-allocated. The memory is reused if the shape and the type
     *                 match with the load image
     * @return native binding handle for the loaded image, or an empty matrix when reading or decoding fails
     */
    private static native long imread_0(String filename, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imread(String)}.
     *
     * @param filename Name of file to be loaded
     * @return native binding handle for the loaded image, or an empty matrix when reading or decoding fails
     */
    private static native long imread_1(String filename);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imread(String,Mat,int)}.
     *
     * @param filename      Name of file to be loaded
     * @param dst_nativeObj native matrix or object handle for {@code dst}; object in which the image will be loaded
     * @param flags         Flag that can take values of ImreadModes, default with IMREAD_COLOR_BGR. Note: The image
     *                      passing through the img parameter can be pre-allocated. The memory is reused if the shape
     *                      and the type match with the load image
     */
    private static native void imread_2(String filename, long dst_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imread(String,Mat)}.
     *
     * @param filename      Name of file to be loaded
     * @param dst_nativeObj native matrix or object handle for {@code dst}; object in which the image will be loaded
     */
    private static native void imread_3(String filename, long dst_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imreadWithMetadata(String,MatOfInt,List<Mat>,int)}.
     *
     * @param filename                    Name of the file to be loaded
     * @param metadataTypes_mat_nativeObj native matrix or object handle for {@code metadataTypes_mat}; Output vector
     *                                    with types of metadata chunks returned in metadata, see ImageMetadataType
     * @param metadata_mat_nativeObj      native matrix or object handle for {@code metadata_mat}; Output nested
     *                                    collection or vector of matrices to store the retrieved metadata
     * @param flags                       Flag that can take values of ImreadModes
     * @return native binding handle for The loaded image as a Mat object. If the image cannot be read, the function
     *         returns an empty matrix
     */
    private static native long imreadWithMetadata_0(
            String filename,
            long metadataTypes_mat_nativeObj,
            long metadata_mat_nativeObj,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imreadWithMetadata(String,MatOfInt,List<Mat>)}.
     *
     * @param filename                    Name of the file to be loaded
     * @param metadataTypes_mat_nativeObj native matrix or object handle for {@code metadataTypes_mat}; Output vector
     *                                    with types of metadata chunks returned in metadata, see ImageMetadataType
     * @param metadata_mat_nativeObj      native matrix or object handle for {@code metadata_mat}; Output nested
     *                                    collection or vector of matrices to store the retrieved metadata
     * @return native binding handle for The loaded image as a Mat object. If the image cannot be read, the function
     *         returns an empty matrix
     */
    private static native long imreadWithMetadata_1(
            String filename,
            long metadataTypes_mat_nativeObj,
            long metadata_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imreadmulti(String,List<Mat>,int)}.
     *
     * @param filename           Name of file to be loaded
     * @param mats_mat_nativeObj native matrix or object handle for {@code mats_mat}; A vector of Mat objects holding
     *                           each page
     * @param flags              Flag that can take values of ImreadModes, default with IMREAD_ANYCOLOR
     * @return true if the requested pages are loaded successfully
     */
    private static native boolean imreadmulti_0(String filename, long mats_mat_nativeObj, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imreadmulti(String,List<Mat>)}.
     *
     * @param filename           Name of file to be loaded
     * @param mats_mat_nativeObj native matrix or object handle for {@code mats_mat}; A vector of Mat objects holding
     *                           each page
     * @return true if the requested pages are loaded successfully
     */
    private static native boolean imreadmulti_1(String filename, long mats_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imreadmulti(String,List<Mat>,int,int,int)}.
     *
     * @param filename           Name of file to be loaded
     * @param mats_mat_nativeObj native matrix or object handle for {@code mats_mat}; A vector of Mat objects holding
     *                           each page
     * @param start              Start index of the image to load
     * @param count              Count number of images to load
     * @param flags              Flag that can take values of ImreadModes, default with IMREAD_ANYCOLOR
     * @return true if the requested pages are loaded successfully
     */
    private static native boolean imreadmulti_2(
            String filename,
            long mats_mat_nativeObj,
            int start,
            int count,
            int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imreadmulti(String,List<Mat>,int,int)}.
     *
     * @param filename           Name of file to be loaded
     * @param mats_mat_nativeObj native matrix or object handle for {@code mats_mat}; A vector of Mat objects holding
     *                           each page
     * @param start              Start index of the image to load
     * @param count              Count number of images to load
     * @return true if the requested pages are loaded successfully
     */
    private static native boolean imreadmulti_3(String filename, long mats_mat_nativeObj, int start, int count);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imreadanimation(String,Animation,int,int)}.
     *
     * @param filename            A string containing the path to the file
     * @param animation_nativeObj {@code animation.getNativeObjAddr()} component of A reference to an Animation
     *                            structure where the loaded frames will be stored. It should be initialized before the
     *                            function is called
     * @param start               The index of the first frame to load. This is optional and defaults to 0
     * @param count               The number of frames to load. This is optional and defaults to 32767
     * @return Returns true if the file was successfully loaded and frames were extracted; returns false otherwise
     */
    private static native boolean imreadanimation_0(String filename, long animation_nativeObj, int start, int count);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imreadanimation(String,Animation,int)}.
     *
     * @param filename            A string containing the path to the file
     * @param animation_nativeObj {@code animation.getNativeObjAddr()} component of A reference to an Animation
     *                            structure where the loaded frames will be stored. It should be initialized before the
     *                            function is called
     * @param start               The index of the first frame to load. This is optional and defaults to 0
     * @return Returns true if the file was successfully loaded and frames were extracted; returns false otherwise
     */
    private static native boolean imreadanimation_1(String filename, long animation_nativeObj, int start);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imreadanimation(String,Animation)}.
     *
     * @param filename            A string containing the path to the file
     * @param animation_nativeObj {@code animation.getNativeObjAddr()} component of A reference to an Animation
     *                            structure where the loaded frames will be stored. It should be initialized before the
     *                            function is called
     * @return Returns true if the file was successfully loaded and frames were extracted; returns false otherwise
     */
    private static native boolean imreadanimation_2(String filename, long animation_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imdecodeanimation(Mat,Animation,int,int)}.
     *
     * @param buf_nativeObj       native matrix or object handle for {@code buf}; A reference to an matrix containing
     *                            the image buffer
     * @param animation_nativeObj {@code animation.getNativeObjAddr()} component of A reference to an Animation
     *                            structure where the loaded frames will be stored. It should be initialized before the
     *                            function is called
     * @param start               The index of the first frame to load. This is optional and defaults to 0
     * @param count               The number of frames to load. This is optional and defaults to 32767
     * @return Returns true if the buffer was successfully loaded and frames were extracted; returns false otherwise
     */
    private static native boolean imdecodeanimation_0(
            long buf_nativeObj,
            long animation_nativeObj,
            int start,
            int count);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imdecodeanimation(Mat,Animation,int)}.
     *
     * @param buf_nativeObj       native matrix or object handle for {@code buf}; A reference to an matrix containing
     *                            the image buffer
     * @param animation_nativeObj {@code animation.getNativeObjAddr()} component of A reference to an Animation
     *                            structure where the loaded frames will be stored. It should be initialized before the
     *                            function is called
     * @param start               The index of the first frame to load. This is optional and defaults to 0
     * @return Returns true if the buffer was successfully loaded and frames were extracted; returns false otherwise
     */
    private static native boolean imdecodeanimation_1(long buf_nativeObj, long animation_nativeObj, int start);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imdecodeanimation(Mat,Animation)}.
     *
     * @param buf_nativeObj       native matrix or object handle for {@code buf}; A reference to an matrix containing
     *                            the image buffer
     * @param animation_nativeObj {@code animation.getNativeObjAddr()} component of A reference to an Animation
     *                            structure where the loaded frames will be stored. It should be initialized before the
     *                            function is called
     * @return Returns true if the buffer was successfully loaded and frames were extracted; returns false otherwise
     */
    private static native boolean imdecodeanimation_2(long buf_nativeObj, long animation_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imwriteanimation(String,Animation,MatOfInt)}.
     *
     * @param filename             The name of the file where the animation will be saved. The file extension determines
     *                             the format
     * @param animation_nativeObj  {@code animation.getNativeObjAddr()} component of A constant reference to an
     *                             Animation struct containing the frames and metadata to be saved
     * @param params_mat_nativeObj native matrix or object handle for {@code params_mat}; Optional format-specific
     *                             parameters encoded as pairs (paramId_1, paramValue_1, paramId_2, paramValue_2, ...).
     *                             These parameters are used to specify additional options for the encoding process.
     *                             Refer to for details on possible parameters
     * @return Returns true if the animation was successfully saved; returns false otherwise
     */
    private static native boolean imwriteanimation_0(
            String filename,
            long animation_nativeObj,
            long params_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imwriteanimation(String,Animation)}.
     *
     * @param filename            The name of the file where the animation will be saved. The file extension determines
     *                            the format
     * @param animation_nativeObj {@code animation.getNativeObjAddr()} component of A constant reference to an Animation
     *                            struct containing the frames and metadata to be saved
     * @return Returns true if the animation was successfully saved; returns false otherwise
     */
    private static native boolean imwriteanimation_1(String filename, long animation_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imencodeanimation(String,Animation,MatOfByte,MatOfInt)}.
     *
     * @param ext                  The file extension that determines the format of the encoded data
     * @param animation_nativeObj  {@code animation.getNativeObjAddr()} component of A constant reference to an
     *                             Animation struct containing the frames and metadata to be encoded
     * @param buf_mat_nativeObj    native matrix or object handle for {@code buf_mat}; A reference to a vector of
     *                             unsigned chars where the encoded data will be stored
     * @param params_mat_nativeObj native matrix or object handle for {@code params_mat}; Optional format-specific
     *                             parameters encoded as pairs (paramId_1, paramValue_1, paramId_2, paramValue_2, ...).
     *                             These parameters are used to specify additional options for the encoding process.
     *                             Refer to for details on possible parameters
     * @return Returns true if the animation was successfully encoded; returns false otherwise
     */
    private static native boolean imencodeanimation_0(
            String ext,
            long animation_nativeObj,
            long buf_mat_nativeObj,
            long params_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imencodeanimation(String,Animation,MatOfByte)}.
     *
     * @param ext                 The file extension that determines the format of the encoded data
     * @param animation_nativeObj {@code animation.getNativeObjAddr()} component of A constant reference to an Animation
     *                            struct containing the frames and metadata to be encoded
     * @param buf_mat_nativeObj   native matrix or object handle for {@code buf_mat}; A reference to a vector of
     *                            unsigned chars where the encoded data will be stored
     * @return Returns true if the animation was successfully encoded; returns false otherwise
     */
    private static native boolean imencodeanimation_1(String ext, long animation_nativeObj, long buf_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imcount(String,int)}.
     *
     * @param filename Name of file to be loaded
     * @param flags    Flag that can take values of ImreadModes, default with IMREAD_COLOR_BGR. Note: The default flags
     *                 value was changed from IMREAD_ANYCOLOR to IMREAD_COLOR_BGR for unification
     * @return the number of readable pages or frames; zero indicates a decoding failure
     */
    private static native long imcount_0(String filename, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imcount(String)}.
     *
     * @param filename Name of file to be loaded
     * @return the number of readable pages or frames; zero indicates a decoding failure
     */
    private static native long imcount_1(String filename);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imwrite(String,Mat,MatOfInt)}.
     *
     * @param filename             Name of the file
     * @param img_nativeObj        native matrix or object handle for {@code img}; (Mat or vector of Mat) Image or
     *                             Images to be saved
     * @param params_mat_nativeObj native matrix or object handle for {@code params_mat}; Format-specific parameters
     *                             encoded as pairs (paramId_1, paramValue_1, paramId_2, paramValue_2, ... .) see
     *                             ImwriteFlags
     * @return true if the image is successfully written to the specified file; false otherwise
     */
    private static native boolean imwrite_0(String filename, long img_nativeObj, long params_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imwrite(String,Mat)}.
     *
     * @param filename      Name of the file
     * @param img_nativeObj native matrix or object handle for {@code img}; (Mat or vector of Mat) Image or Images to be
     *                      saved
     * @return true if the image is successfully written to the specified file; false otherwise
     */
    private static native boolean imwrite_1(String filename, long img_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imwriteWithMetadata(String,Mat,MatOfInt,List<Mat>,MatOfInt)}.
     *
     * @param filename                    Name of the file. As with imwrite, image format is determined by the file
     *                                    extension
     * @param img_nativeObj               native matrix or object handle for {@code img}; (Mat or vector of Mat) Image
     *                                    or Images to be saved
     * @param metadataTypes_mat_nativeObj native matrix or object handle for {@code metadataTypes_mat}; Vector with
     *                                    types of metadata chucks stored in metadata to write, see ImageMetadataType
     * @param metadata_mat_nativeObj      native matrix or object handle for {@code metadata_mat}; Vector of vectors or
     *                                    vector of matrices with chunks of metadata to store into the file
     * @param params_mat_nativeObj        native matrix or object handle for {@code params_mat}; Format-specific
     *                                    parameters encoded as pairs (paramId_1, paramValue_1, paramId_2, paramValue_2,
     *                                    ... .) see ImwriteFlags
     * @return true if the image and supported metadata are written successfully
     */
    private static native boolean imwriteWithMetadata_0(
            String filename,
            long img_nativeObj,
            long metadataTypes_mat_nativeObj,
            long metadata_mat_nativeObj,
            long params_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imwriteWithMetadata(String,Mat,MatOfInt,List<Mat>)}.
     *
     * @param filename                    Name of the file. As with imwrite, image format is determined by the file
     *                                    extension
     * @param img_nativeObj               native matrix or object handle for {@code img}; (Mat or vector of Mat) Image
     *                                    or Images to be saved
     * @param metadataTypes_mat_nativeObj native matrix or object handle for {@code metadataTypes_mat}; Vector with
     *                                    types of metadata chucks stored in metadata to write, see ImageMetadataType
     * @param metadata_mat_nativeObj      native matrix or object handle for {@code metadata_mat}; Vector of vectors or
     *                                    vector of matrices with chunks of metadata to store into the file
     * @return true if the image and supported metadata are written successfully
     */
    private static native boolean imwriteWithMetadata_1(
            String filename,
            long img_nativeObj,
            long metadataTypes_mat_nativeObj,
            long metadata_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imwritemulti(String,List<Mat>,MatOfInt)}.
     *
     * @param filename             path of the input or output file
     * @param img_mat_nativeObj    native matrix or object handle for {@code img_mat}; image to process or annotate
     * @param params_mat_nativeObj native matrix or object handle for {@code params_mat}; codec-specific encoding
     *                             options as integer key/value pairs
     * @return true if the image collection is written successfully
     */
    private static native boolean imwritemulti_0(String filename, long img_mat_nativeObj, long params_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imwritemulti(String,List<Mat>)}.
     *
     * @param filename          path of the input or output file
     * @param img_mat_nativeObj native matrix or object handle for {@code img_mat}; image to process or annotate
     * @return true if the image collection is written successfully
     */
    private static native boolean imwritemulti_1(String filename, long img_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imdecode(Mat,int)}.
     *
     * @param buf_nativeObj native matrix or object handle for {@code buf}; Input array or vector of bytes
     * @param flags         Flag that can take values of ImreadModes
     * @return native binding handle for the decoded image, or an empty matrix when decoding fails
     */
    private static native long imdecode_0(long buf_nativeObj, int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imdecodeWithMetadata(Mat,MatOfInt,List<Mat>,int)}.
     *
     * @param buf_nativeObj               native matrix or object handle for {@code buf}; Input array or vector of bytes
     *                                    containing the encoded image data
     * @param metadataTypes_mat_nativeObj native matrix or object handle for {@code metadataTypes_mat}; Output vector
     *                                    with types of metadata chucks returned in metadata, see ImageMetadataType
     * @param metadata_mat_nativeObj      native matrix or object handle for {@code metadata_mat}; Output nested
     *                                    collection or vector of matrices to store the retrieved metadata
     * @param flags                       Flag that can take values of ImreadModes
     * @return native binding handle for The decoded image as a Mat object. If decoding fails, the function returns an
     *         empty matrix
     */
    private static native long imdecodeWithMetadata_0(
            long buf_nativeObj,
            long metadataTypes_mat_nativeObj,
            long metadata_mat_nativeObj,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imdecodemulti(Mat,int,List<Mat>,Range)}.
     *
     * @param buf_nativeObj      native matrix or object handle for {@code buf}; Input array or vector of bytes
     * @param flags              Flag that can take values of ImreadModes
     * @param mats_mat_nativeObj native matrix or object handle for {@code mats_mat}; A vector of Mat objects holding
     *                           each page, if more than one
     * @param range_start        {@code range.start} component of A continuous selection of pages
     * @param range_end          {@code range.end} component of A continuous selection of pages
     * @return true if the requested pages are decoded successfully
     */
    private static native boolean imdecodemulti_0(
            long buf_nativeObj,
            int flags,
            long mats_mat_nativeObj,
            int range_start,
            int range_end);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imdecodemulti(Mat,int,List<Mat>)}.
     *
     * @param buf_nativeObj      native matrix or object handle for {@code buf}; Input array or vector of bytes
     * @param flags              Flag that can take values of ImreadModes
     * @param mats_mat_nativeObj native matrix or object handle for {@code mats_mat}; A vector of Mat objects holding
     *                           each page, if more than one
     * @return true if the requested pages are decoded successfully
     */
    private static native boolean imdecodemulti_1(long buf_nativeObj, int flags, long mats_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imencode(String,Mat,MatOfByte,MatOfInt)}.
     *
     * @param ext                  File extension that defines the output format. Must include a leading period
     * @param img_nativeObj        native matrix or object handle for {@code img}; Image to be compressed
     * @param buf_mat_nativeObj    native matrix or object handle for {@code buf_mat}; Output buffer resized to fit the
     *                             compressed image
     * @param params_mat_nativeObj native matrix or object handle for {@code params_mat}; Format-specific parameters.
     *                             See imwrite and ImwriteFlags
     * @return true if the encoded bytes are written to buf
     */
    private static native boolean imencode_0(
            String ext,
            long img_nativeObj,
            long buf_mat_nativeObj,
            long params_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imencode(String,Mat,MatOfByte)}.
     *
     * @param ext               File extension that defines the output format. Must include a leading period
     * @param img_nativeObj     native matrix or object handle for {@code img}; Image to be compressed
     * @param buf_mat_nativeObj native matrix or object handle for {@code buf_mat}; Output buffer resized to fit the
     *                          compressed image
     * @return true if the encoded bytes are written to buf
     */
    private static native boolean imencode_1(String ext, long img_nativeObj, long buf_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imencodeWithMetadata(String,Mat,MatOfInt,List<Mat>,MatOfByte,MatOfInt)}.
     *
     * @param ext                         File extension that defines the output format. Must include a leading period
     * @param img_nativeObj               native matrix or object handle for {@code img}; Image to be compressed
     * @param metadataTypes_mat_nativeObj native matrix or object handle for {@code metadataTypes_mat}; Vector with
     *                                    types of metadata chucks stored in metadata to write, see ImageMetadataType
     * @param metadata_mat_nativeObj      native matrix or object handle for {@code metadata_mat}; Vector of vectors or
     *                                    vector of matrices with chunks of metadata to store into the file
     * @param buf_mat_nativeObj           native matrix or object handle for {@code buf_mat}; Output buffer resized to
     *                                    fit the compressed image
     * @param params_mat_nativeObj        native matrix or object handle for {@code params_mat}; Format-specific
     *                                    parameters. See imwrite and ImwriteFlags
     * @return true if the encoded image and supported metadata are written to buf
     */
    private static native boolean imencodeWithMetadata_0(
            String ext,
            long img_nativeObj,
            long metadataTypes_mat_nativeObj,
            long metadata_mat_nativeObj,
            long buf_mat_nativeObj,
            long params_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imencodeWithMetadata(String,Mat,MatOfInt,List<Mat>,MatOfByte)}.
     *
     * @param ext                         File extension that defines the output format. Must include a leading period
     * @param img_nativeObj               native matrix or object handle for {@code img}; Image to be compressed
     * @param metadataTypes_mat_nativeObj native matrix or object handle for {@code metadataTypes_mat}; Vector with
     *                                    types of metadata chucks stored in metadata to write, see ImageMetadataType
     * @param metadata_mat_nativeObj      native matrix or object handle for {@code metadata_mat}; Vector of vectors or
     *                                    vector of matrices with chunks of metadata to store into the file
     * @param buf_mat_nativeObj           native matrix or object handle for {@code buf_mat}; Output buffer resized to
     *                                    fit the compressed image
     * @return true if the encoded image and supported metadata are written to buf
     */
    private static native boolean imencodeWithMetadata_1(
            String ext,
            long img_nativeObj,
            long metadataTypes_mat_nativeObj,
            long metadata_mat_nativeObj,
            long buf_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imencodemulti(String,List<Mat>,MatOfByte,MatOfInt)}.
     *
     * @param ext                  File extension that defines the output format. Must include a leading period
     * @param imgs_mat_nativeObj   native matrix or object handle for {@code imgs_mat}; Vector of images to be written
     * @param buf_mat_nativeObj    native matrix or object handle for {@code buf_mat}; Output buffer resized to fit the
     *                             compressed data
     * @param params_mat_nativeObj native matrix or object handle for {@code params_mat}; Format-specific parameters.
     *                             See imwrite and ImwriteFlags
     * @return true if the supplied images are encoded into buf
     */
    private static native boolean imencodemulti_0(
            String ext,
            long imgs_mat_nativeObj,
            long buf_mat_nativeObj,
            long params_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#imencodemulti(String,List<Mat>,MatOfByte)}.
     *
     * @param ext                File extension that defines the output format. Must include a leading period
     * @param imgs_mat_nativeObj native matrix or object handle for {@code imgs_mat}; Vector of images to be written
     * @param buf_mat_nativeObj  native matrix or object handle for {@code buf_mat}; Output buffer resized to fit the
     *                           compressed data
     * @return true if the supplied images are encoded into buf
     */
    private static native boolean imencodemulti_1(String ext, long imgs_mat_nativeObj, long buf_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#haveImageReader(String)}.
     *
     * @param filename The name of the file to be checked
     * @return true if an image reader for the specified file is available and the file can be opened, false otherwise.
     *         Note: The function checks the availability of image codecs that are either built into OpenCV or
     *         dynamically loaded. It does not load the image codec implementation and decode data, but uses signature
     *         check. If the file cannot be opened or the format is unsupported, the function will return false
     */
    private static native boolean haveImageReader_0(String filename);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#haveImageWriter(String)}.
     *
     * @param filename The name of the file or the file extension (e.g., ".jpg", ".png"). It is recommended to provide
     *                 the file extension rather than the full file name
     * @return true if an image writer for the specified extension is available, false otherwise. Note: The function
     *         checks the availability of image codecs that are either built into OpenCV or dynamically loaded. It does
     *         not check for the actual existence of the file but rather the ability to write files of the given type
     */
    private static native boolean haveImageWriter_0(String filename);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#dicomJpgFileRead(String,MatOfDouble,MatOfDouble,int,int)}.
     *
     * @param filename                  path of the input or output file
     * @param segposition_mat_nativeObj native matrix or object handle for {@code segposition_mat}; byte offsets of the
     *                                  pixel-data fragments in the file
     * @param seglength_mat_nativeObj   native matrix or object handle for {@code seglength_mat}; byte lengths of the
     *                                  corresponding pixel-data fragments
     * @param dicomflags                bit mask controlling DICOM pixel interpretation
     * @param flags                     image-decoding options from the IMREAD flags; DICOM sample interpretation is
     *                                  selected separately
     * @return native binding handle for the decoded image, or an empty matrix when reading or decoding fails
     */
    private static native long dicomJpgFileRead_0(
            String filename,
            long segposition_mat_nativeObj,
            long seglength_mat_nativeObj,
            int dicomflags,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#dicomJpgFileRead(String,MatOfDouble,MatOfDouble,int)}.
     *
     * @param filename                  path of the input or output file
     * @param segposition_mat_nativeObj native matrix or object handle for {@code segposition_mat}; byte offsets of the
     *                                  pixel-data fragments in the file
     * @param seglength_mat_nativeObj   native matrix or object handle for {@code seglength_mat}; byte lengths of the
     *                                  corresponding pixel-data fragments
     * @param dicomflags                bit mask controlling DICOM pixel interpretation
     * @return native binding handle for the decoded image, or an empty matrix when reading or decoding fails
     */
    private static native long dicomJpgFileRead_1(
            String filename,
            long segposition_mat_nativeObj,
            long seglength_mat_nativeObj,
            int dicomflags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#dicomJpgFileRead(String,MatOfDouble,MatOfDouble)}.
     *
     * @param filename                  path of the input or output file
     * @param segposition_mat_nativeObj native matrix or object handle for {@code segposition_mat}; byte offsets of the
     *                                  pixel-data fragments in the file
     * @param seglength_mat_nativeObj   native matrix or object handle for {@code seglength_mat}; byte lengths of the
     *                                  corresponding pixel-data fragments
     * @return native binding handle for the decoded image, or an empty matrix when reading or decoding fails
     */
    private static native long dicomJpgFileRead_2(
            String filename,
            long segposition_mat_nativeObj,
            long seglength_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#dicomJpgMatRead(Mat,int,int)}.
     *
     * @param buf_nativeObj native matrix or object handle for {@code buf}; matrix containing the encoded input bytes
     * @param dicomflags    bit mask controlling DICOM pixel interpretation
     * @param flags         image-decoding options from the IMREAD flags; DICOM sample interpretation is selected
     *                      separately
     * @return native binding handle for the decoded image, or an empty matrix when decoding fails
     */
    private static native long dicomJpgMatRead_0(long buf_nativeObj, int dicomflags, int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#dicomJpgMatRead(Mat,int)}.
     *
     * @param buf_nativeObj native matrix or object handle for {@code buf}; matrix containing the encoded input bytes
     * @param dicomflags    bit mask controlling DICOM pixel interpretation
     * @return native binding handle for the decoded image, or an empty matrix when decoding fails
     */
    private static native long dicomJpgMatRead_1(long buf_nativeObj, int dicomflags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#dicomJpgMatRead(Mat)}.
     *
     * @param buf_nativeObj native matrix or object handle for {@code buf}; matrix containing the encoded input bytes
     * @return native binding handle for the decoded image, or an empty matrix when decoding fails
     */
    private static native long dicomJpgMatRead_2(long buf_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#dicomRawFileRead(String,MatOfDouble,MatOfDouble,MatOfInt,String)}.
     *
     * @param filename                  path of the input or output file
     * @param segposition_mat_nativeObj native matrix or object handle for {@code segposition_mat}; byte offsets of the
     *                                  pixel-data fragments in the file
     * @param seglength_mat_nativeObj   native matrix or object handle for {@code seglength_mat}; byte lengths of the
     *                                  corresponding pixel-data fragments
     * @param dicomParams_mat_nativeObj native matrix or object handle for {@code dicomParams_mat}; ordered DICOM
     *                                  settings indexed by the DICOM_PARAM constants
     * @param colormodel                DICOM photometric interpretation label for the pixel data
     * @return native binding handle for the decoded image, or an empty matrix when reading or decoding fails
     */
    private static native long dicomRawFileRead_0(
            String filename,
            long segposition_mat_nativeObj,
            long seglength_mat_nativeObj,
            long dicomParams_mat_nativeObj,
            String colormodel);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#dicomRawMatRead(Mat,MatOfInt,String)}.
     *
     * @param buf_nativeObj             native matrix or object handle for {@code buf}; matrix containing the encoded
     *                                  input bytes
     * @param dicomParams_mat_nativeObj native matrix or object handle for {@code dicomParams_mat}; ordered DICOM
     *                                  settings indexed by the DICOM_PARAM constants
     * @param colormodel                DICOM photometric interpretation label for the pixel data
     * @return native binding handle for the decoded image, or an empty matrix for unsupported or invalid input
     */
    private static native long dicomRawMatRead_0(long buf_nativeObj, long dicomParams_mat_nativeObj, String colormodel);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Imgcodecs#dicomJpgWrite(Mat,MatOfInt,String)}.
     *
     * @param image_nativeObj           native matrix or object handle for {@code image}; input image
     * @param dicomParams_mat_nativeObj native matrix or object handle for {@code dicomParams_mat}; ordered DICOM
     *                                  settings indexed by the DICOM_PARAM constants
     * @param colormodel                DICOM photometric interpretation label for the pixel data
     * @return native binding handle for a matrix containing the encoded pixel-frame bytes
     */
    private static native long dicomJpgWrite_0(long image_nativeObj, long dicomParams_mat_nativeObj, String colormodel);

}
