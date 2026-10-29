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
package org.miaixz.bus.image.nimble;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Maps EXIF metadata to DICOM attributes following the informative mapping of PS3.17 "Mapping of Visible Light
 * Photography Related Attributes to EXIF and TIFF/EP Tags" (CP-1736): General Equipment, General Image, VL Photographic
 * Equipment, VL Photographic Acquisition and VL Photographic Geolocation modules.
 *
 * <p>
 * An attribute that already has a value is never replaced. The DICOM VR comes from the data dictionary: text values go
 * to text VRs, rationals to DS, integers to IS or US.
 *
 * <p>
 * Not mapped on purpose: Orientation (the pixels are expected in the normal orientation), X/YResolution to Pixel
 * Spacing (a print resolution, not a distance in the patient), the focal plane resolution, Subject Area / Location
 * (wrong after a crop or rotation), ImageUniqueID (not a DICOM UID), Artist (free text, not a person name), MakerNote
 * and Device Setting Description (opaque) and the OECF, SFR and CFA tables.
 *
 * @author Kimi Liu
 */
public final class ExifToDicom {

    // IFD0
    /**
     * EXIF tag identifier for image description.
     */
    static final int IMAGE_DESCRIPTION = 0x010E;
    /**
     * EXIF tag identifier for make.
     */
    static final int MAKE = 0x010F;
    /**
     * EXIF tag identifier for model.
     */
    static final int MODEL = 0x0110;
    /**
     * EXIF tag identifier for software.
     */
    static final int SOFTWARE = 0x0131;
    // Exif sub-IFD
    /**
     * EXIF tag identifier for exif version.
     */
    static final int EXIF_VERSION = 0x9000;
    /**
     * EXIF tag identifier for date time original.
     */
    static final int DATE_TIME_ORIGINAL = 0x9003;
    /**
     * EXIF tag identifier for date time digitized.
     */
    static final int DATE_TIME_DIGITIZED = 0x9004;
    /**
     * EXIF tag identifier for offset time original.
     */
    static final int OFFSET_TIME_ORIGINAL = 0x9011;
    /**
     * EXIF tag identifier for offset time digitized.
     */
    static final int OFFSET_TIME_DIGITIZED = 0x9012;
    /**
     * EXIF tag identifier for flash.
     */
    static final int FLASH = 0x9209;
    /**
     * EXIF tag identifier for user comment.
     */
    static final int USER_COMMENT = 0x9286;
    /**
     * EXIF tag identifier for sub sec time original.
     */
    static final int SUB_SEC_TIME_ORIGINAL = 0x9291;
    /**
     * EXIF tag identifier for sub sec time digitized.
     */
    static final int SUB_SEC_TIME_DIGITIZED = 0x9292;
    /**
     * EXIF tag identifier for camera owner name.
     */
    static final int CAMERA_OWNER_NAME = 0xA430;
    /**
     * EXIF tag identifier for body serial number.
     */
    static final int BODY_SERIAL_NUMBER = 0xA431;
    /**
     * EXIF tag identifier for lens serial number.
     */
    static final int LENS_SERIAL_NUMBER = 0xA435;
    // GPS sub-IFD
    /**
     * EXIF tag identifier for gps time stamp.
     */
    static final int GPS_TIME_STAMP = 0x0007;
    /**
     * EXIF tag identifier for gps date stamp.
     */
    static final int GPS_DATE_STAMP = 0x001D;

    /**
     * Technical tags of IFD0 and the Exif sub-IFD, which do not identify the camera or its owner.
     */
    private static final Map<Integer, Integer> TECHNICAL = new LinkedHashMap<>();

    /**
     * Tags identifying the camera or its owner.
     */
    private static final Map<Integer, Integer> IDENTIFYING = new LinkedHashMap<>();

    /**
     * Tags of the GPS sub-IFD, except the date and time stamps.
     */
    private static final Map<Integer, Integer> GPS = new LinkedHashMap<>();

    static {
        TECHNICAL.put(MAKE, Tag.Manufacturer);
        TECHNICAL.put(MODEL, Tag.ManufacturerModelName);
        TECHNICAL.put(SOFTWARE, Tag.SoftwareVersions);
        TECHNICAL.put(0x829A, Tag.ExposureTimeInSeconds);
        TECHNICAL.put(0x829D, Tag.FNumber);
        TECHNICAL.put(0x8822, Tag.ExposureProgram);
        TECHNICAL.put(0x8824, Tag.SpectralSensitivity);
        TECHNICAL.put(0x8827, Tag.PhotographicSensitivity);
        TECHNICAL.put(0x8830, Tag.SensitivityType);
        TECHNICAL.put(0x8831, Tag.StandardOutputSensitivity);
        TECHNICAL.put(0x8832, Tag.RecommendedExposureIndex);
        TECHNICAL.put(0x8833, Tag.ISOSpeed);
        TECHNICAL.put(0x8834, Tag.ISOSpeedLatitudeyyy);
        TECHNICAL.put(0x8835, Tag.ISOSpeedLatitudezzz);
        TECHNICAL.put(EXIF_VERSION, Tag.EXIFVersion);
        TECHNICAL.put(0x9201, Tag.ShutterSpeedValue);
        TECHNICAL.put(0x9202, Tag.ApertureValue);
        TECHNICAL.put(0x9203, Tag.BrightnessValue);
        TECHNICAL.put(0x9204, Tag.ExposureBiasValue);
        TECHNICAL.put(0x9205, Tag.MaxApertureValue);
        TECHNICAL.put(0x9206, Tag.SubjectDistance);
        TECHNICAL.put(0x9207, Tag.MeteringMode);
        TECHNICAL.put(0x9208, Tag.LightSource);
        TECHNICAL.put(0x920A, Tag.FocalLength);
        TECHNICAL.put(0x9400, Tag.Temperature);
        TECHNICAL.put(0x9401, Tag.Humidity);
        TECHNICAL.put(0x9402, Tag.Pressure);
        TECHNICAL.put(0x9403, Tag.WaterDepth);
        TECHNICAL.put(0x9404, Tag.Acceleration);
        TECHNICAL.put(0x9405, Tag.CameraElevationAngle);
        TECHNICAL.put(0xA20B, Tag.FlashEnergy);
        TECHNICAL.put(0xA215, Tag.PhotographicExposureIndex);
        TECHNICAL.put(0xA217, Tag.SensingMethod);
        TECHNICAL.put(0xA300, Tag.FileSource);
        TECHNICAL.put(0xA301, Tag.SceneType);
        TECHNICAL.put(0xA401, Tag.CustomRendered);
        TECHNICAL.put(0xA402, Tag.ExposureMode);
        TECHNICAL.put(0xA403, Tag.WhiteBalance);
        TECHNICAL.put(0xA404, Tag.DigitalZoomRatio);
        TECHNICAL.put(0xA405, Tag.FocalLengthIn35mmFilm);
        TECHNICAL.put(0xA406, Tag.SceneCaptureType);
        TECHNICAL.put(0xA407, Tag.GainControl);
        TECHNICAL.put(0xA408, Tag.Contrast);
        TECHNICAL.put(0xA409, Tag.Saturation);
        TECHNICAL.put(0xA40A, Tag.Sharpness);
        TECHNICAL.put(0xA40C, Tag.SubjectDistanceRange);
        TECHNICAL.put(0xA432, Tag.LensSpecification);
        TECHNICAL.put(0xA433, Tag.LensMake);
        TECHNICAL.put(0xA434, Tag.LensModel);

        IDENTIFYING.put(CAMERA_OWNER_NAME, Tag.CameraOwnerName);
        IDENTIFYING.put(BODY_SERIAL_NUMBER, Tag.DeviceSerialNumber);
        IDENTIFYING.put(LENS_SERIAL_NUMBER, Tag.LensSerialNumber);

        GPS.put(0x0000, Tag.GPSVersionID);
        GPS.put(0x0001, Tag.GPSLatitudeRef);
        GPS.put(0x0002, Tag.GPSLatitude);
        GPS.put(0x0003, Tag.GPSLongitudeRef);
        GPS.put(0x0004, Tag.GPSLongitude);
        GPS.put(0x0005, Tag.GPSAltitudeRef);
        GPS.put(0x0006, Tag.GPSAltitude);
        GPS.put(0x0008, Tag.GPSSatellites);
        GPS.put(0x0009, Tag.GPSStatus);
        GPS.put(0x000A, Tag.GPSMeasureMode);
        GPS.put(0x000B, Tag.GPSDOP);
        GPS.put(0x000C, Tag.GPSSpeedRef);
        GPS.put(0x000D, Tag.GPSSpeed);
        GPS.put(0x000E, Tag.GPSTrackRef);
        GPS.put(0x000F, Tag.GPSTrack);
        GPS.put(0x0010, Tag.GPSImgDirectionRef);
        GPS.put(0x0011, Tag.GPSImgDirection);
        GPS.put(0x0012, Tag.GPSMapDatum);
        GPS.put(0x0013, Tag.GPSDestLatitudeRef);
        GPS.put(0x0014, Tag.GPSDestLatitude);
        GPS.put(0x0015, Tag.GPSDestLongitudeRef);
        GPS.put(0x0016, Tag.GPSDestLongitude);
        GPS.put(0x0017, Tag.GPSDestBearingRef);
        GPS.put(0x0018, Tag.GPSDestBearing);
        GPS.put(0x0019, Tag.GPSDestDistanceRef);
        GPS.put(0x001A, Tag.GPSDestDistance);
        GPS.put(0x001B, Tag.GPSProcessingMethod);
        GPS.put(0x001C, Tag.GPSAreaInformation);
        GPS.put(0x001E, Tag.GPSDifferential);
    }

    /**
     * Pattern validating the EXIF date and time representation.
     */
    private static final Pattern EXIF_DATE_TIME = Pattern
            .compile("(\\d{4}):(\\d{2}):(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})");
    /**
     * Pattern validating a GPS date stamp.
     */
    private static final Pattern EXIF_DATE = Pattern.compile("(\\d{4}):(\\d{2}):(\\d{2})");
    /**
     * Pattern validating an EXIF UTC offset.
     */
    private static final Pattern EXIF_OFFSET = Pattern.compile("([+-])(\\d{2}):(\\d{2})");
    /**
     * Pattern accepting decimal digits only.
     */
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    /**
     * Maximum fractional-second precision supported by DICOM date-time values.
     */
    private static final int MAX_FRACTION_DIGITS = 6;
    /**
     * Length in bytes of the EXIF UserComment character-code prefix.
     */
    private static final int USER_COMMENT_CODE_LENGTH = 8;

    /**
     * Prevents construction of the EXIF-to-DICOM mapping utility.
     */
    private ExifToDicom() {
    }

    /**
     * Adds the DICOM attributes derived from the EXIF metadata. Attributes already present with a value are kept.
     *
     * @param exif               the EXIF metadata
     * @param attrs              the attributes to complete
     * @param includeIdentifying true to also map the tags that identify the camera, its owner or the place of
     *                           acquisition: Camera Owner Name, Device Serial Number, Lens Serial Number and the GPS
     *                           attributes. They are listed in the PS3.15 Basic Application Level Confidentiality
     *                           Profile.
     */
    public static void fill(ExifData exif, Attributes attrs, boolean includeIdentifying) {
        if (exif == null || exif.isEmpty() || attrs == null) {
            return;
        }
        TECHNICAL.forEach((exifTag, dicomTag) -> put(attrs, dicomTag, exif.get(exifTag)));
        putImageComments(exif, attrs);
        putFlash(exif, attrs);
        putDateTimes(exif, attrs);

        if (includeIdentifying) {
            IDENTIFYING.forEach((exifTag, dicomTag) -> put(attrs, dicomTag, exif.get(exifTag)));
            GPS.forEach((exifTag, dicomTag) -> put(attrs, dicomTag, exif.getGps(exifTag)));
            putGpsDateTime(exif, attrs);
        }
    }

    /**
     * Copies a compatible EXIF numeric value into a DICOM attribute without overwriting existing values.
     *
     * @param attrs the mutable DICOM dataset
     * @param tag   the DICOM attribute tag or EXIF tag identifier
     * @param value the decoded EXIF value to map, if present
     */
    private static void put(Attributes attrs, int tag, Optional<ExifValue> value) {
        if (value.isEmpty() || attrs.containsValue(tag)) {
            return;
        }
        ExifValue v = value.get();
        VR vr = ElementDictionary.vrOf(tag, null);
        switch (vr) {
            case DS -> {
                // EXIF encodes all these values as rationals: another type is a malformed entry
                double[] values = v.isFractional() ? v.asDoubles() : new double[0];
                if (values.length > 0 && Arrays.stream(values).allMatch(Double::isFinite)) {
                    attrs.setDouble(tag, vr, values);
                }
            }
            case US, IS -> {
                long[] values = v.asLongs();
                if (values.length > 0 && fits(vr, values[0])) {
                    attrs.setInt(tag, vr, (int) values[0]);
                }
            }
            case OB -> attrs.setBytes(tag, vr, v.bytes());
            default -> putString(attrs, tag, vr, v.asString());
        }
    }

    /**
     * Checks whether an integer fits the numeric range of the target DICOM VR.
     *
     * @param vr    the target DICOM value representation
     * @param value the integer to validate against the target VR
     * @return whether the integer can be represented by the target VR
     */
    private static boolean fits(VR vr, long value) {
        if (vr == VR.US) {
            return value >= 0 && value <= 0xFFFF;
        }
        return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
    }

    /**
     * Writes nonblank mapped text only when the DICOM attribute has no value.
     *
     * @param attrs the mutable DICOM dataset
     * @param tag   the DICOM attribute tag or EXIF tag identifier
     * @param vr    the target DICOM value representation
     * @param value the optional text to write
     */
    private static void putString(Attributes attrs, int tag, VR vr, String value) {
        if (value == null || value.isBlank() || attrs.containsValue(tag)) {
            return;
        }
        String text = value;
        if (vr == VR.CS || vr == VR.LO || vr == VR.SH) {
            // Single-valued: the backslash is the DICOM value delimiter
            text = text.replace('\\', '/');
            int max = vr == VR.LO ? 64 : 16;
            if (text.length() > max) {
                text = text.substring(0, max).trim();
            }
            if (vr == VR.CS) {
                text = StringKit.toUpperCase(text);
            }
        }
        attrs.setString(tag, vr, text);
    }

    /**
     * ImageDescription, or UserComment when there is no description, to Image Comments.
     *
     * @param exif  the decoded standard and GPS EXIF tags
     * @param attrs the mutable DICOM dataset
     */
    private static void putImageComments(ExifData exif, Attributes attrs) {
        String comment = exif.get(IMAGE_DESCRIPTION).map(ExifValue::asString).orElse("");
        if (comment.isEmpty()) {
            comment = exif.get(USER_COMMENT).map(ExifToDicom::decodeUserComment).orElse("");
        }
        putString(attrs, Tag.ImageComments, VR.LT, comment);
    }

    /**
     * Decodes UserComment: an 8-byte character code ("ASCII", "UNICODE", "JIS" or undefined) followed by the text.
     *
     * @param value the encoded EXIF comment value
     * @return the decoded comment text, or null for an unsupported character code
     */
    static String decodeUserComment(ExifValue value) {
        byte[] data = value.bytes();
        if (data.length <= USER_COMMENT_CODE_LENGTH) {
            return "";
        }
        String code = new String(data, 0, USER_COMMENT_CODE_LENGTH, StandardCharsets.US_ASCII).replace("\0", "").trim();
        byte[] text = Arrays.copyOfRange(data, USER_COMMENT_CODE_LENGTH, data.length);
        String comment;
        if ("UNICODE".equals(code)) {
            // UCS-2, in practice in the byte order of the TIFF block. Guess it from the zero bytes.
            boolean littleEndian = text.length > 1 && text[0] != 0 && text[1] == 0;
            comment = new String(text, littleEndian ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_16BE);
        } else if ("JIS".equals(code)) {
            return ""; // Not supported
        } else {
            comment = new String(text, StandardCharsets.UTF_8);
        }
        return comment.replace("\0", "").trim();
    }

    /**
     * Splits the EXIF Flash bit field into the five attributes of the acquisition module.
     *
     * @param exif  the decoded standard and GPS EXIF tags
     * @param attrs the mutable DICOM dataset
     */
    private static void putFlash(ExifData exif, Attributes attrs) {
        Optional<ExifValue> flash = exif.get(FLASH);
        long[] values = flash.map(ExifValue::asLongs).orElse(new long[0]);
        if (values.length == 0) {
            return;
        }
        int bits = (int) values[0];
        putUS(attrs, Tag.FlashFiringStatus, bits & 0x01);
        putUS(attrs, Tag.FlashReturnStatus, (bits >> 1) & 0x03);
        putUS(attrs, Tag.FlashMode, (bits >> 3) & 0x03);
        putUS(attrs, Tag.FlashFunctionPresent, (bits >> 5) & 0x01);
        putUS(attrs, Tag.FlashRedEyeMode, (bits >> 6) & 0x01);
    }

    /**
     * Writes an unsigned-short EXIF-derived acquisition value without replacing existing data.
     *
     * @param attrs the mutable DICOM dataset
     * @param tag   the DICOM attribute tag or EXIF tag identifier
     * @param value the unsigned-short acquisition value
     */
    private static void putUS(Attributes attrs, int tag, int value) {
        if (!attrs.containsValue(tag)) {
            attrs.setInt(tag, VR.US, value);
        }
    }

    /**
     * DateTimeOriginal to Acquisition DateTime, DateTimeDigitized to Content Date and Time. The offset from UTC is kept
     * in the DT value itself rather than in Timezone Offset From UTC, which would apply to every date and time of the
     * instance.
     *
     * @param exif  the decoded standard and GPS EXIF tags
     * @param attrs the mutable DICOM dataset
     */
    private static void putDateTimes(ExifData exif, Attributes attrs) {
        String original = toDicomDateTime(
                text(exif, DATE_TIME_ORIGINAL),
                text(exif, SUB_SEC_TIME_ORIGINAL),
                text(exif, OFFSET_TIME_ORIGINAL));
        putString(attrs, Tag.AcquisitionDateTime, VR.DT, original);

        if (!attrs.containsValue(Tag.ContentDate) && !attrs.containsValue(Tag.ContentTime)) {
            String digitized = toDicomDateTime(text(exif, DATE_TIME_DIGITIZED), text(exif, SUB_SEC_TIME_DIGITIZED), "");
            if (digitized != null) {
                attrs.setString(Tag.ContentDate, VR.DA, digitized.substring(0, 8));
                attrs.setString(Tag.ContentTime, VR.TM, digitized.substring(8));
            }
        }
    }

    /**
     * GPSDateStamp and GPSTimeStamp, which are in UTC, to GPS Date Stamp and GPS Time Stamp (DT).
     *
     * @param exif  the decoded standard and GPS EXIF tags
     * @param attrs the mutable DICOM dataset
     */
    private static void putGpsDateTime(ExifData exif, Attributes attrs) {
        Matcher date = EXIF_DATE.matcher(exif.getGps(GPS_DATE_STAMP).map(ExifValue::asString).orElse(""));
        if (!date.matches() || "0000".equals(date.group(1))) {
            return;
        }
        String day = date.group(1) + date.group(2) + date.group(3);
        putString(attrs, Tag.GPSDateStamp, VR.DT, day);

        double[] time = exif.getGps(GPS_TIME_STAMP).map(ExifValue::asDoubles).orElse(new double[0]);
        if (time.length == 3 && Arrays.stream(time).allMatch(t -> Double.isFinite(t) && t >= 0)) {
            long micros = Math.round(time[2] * 1_000_000);
            String value = String.format(
                    Locale.ROOT,
                    "%s%02d%02d%02d.%06d+0000",
                    day,
                    (int) time[0],
                    (int) time[1],
                    micros / 1_000_000,
                    micros % 1_000_000);
            putString(attrs, Tag.GPSTimeStamp, VR.DT, value);
        }
    }

    /**
     * Extracts a standard EXIF tag as text, or returns null when absent.
     *
     * @param exif the decoded standard and GPS EXIF tags
     * @param tag  the DICOM attribute tag or EXIF tag identifier
     * @return the decoded text or null
     */
    private static String text(ExifData exif, int tag) {
        return exif.get(tag).map(ExifValue::asString).orElse("");
    }

    /**
     * Converts an EXIF date and time ({@code YYYY:MM:DD HH:MM:SS}) to a DICOM DT value
     * ({@code YYYYMMDDHHMMSS.FFFFFF&ZZXX}).
     *
     * @return the DT value, null when the date is missing or unknown (blank or zero-filled)
     * @param dateTime the EXIF date and time in yyyy:MM:dd HH:mm:ss form
     * @param subSec   the optional decimal fractional-second digits
     * @param offset   the number of results to skip
     */
    static String toDicomDateTime(String dateTime, String subSec, String offset) {
        Matcher m = EXIF_DATE_TIME.matcher(dateTime == null ? "" : dateTime.trim());
        if (!m.matches() || "0000".equals(m.group(1)) || "00".equals(m.group(2))) {
            return null;
        }
        StringBuilder dt = new StringBuilder(26);
        for (int i = 1; i <= 6; i++) {
            dt.append(m.group(i));
        }
        String fraction = subSec == null ? "" : subSec.trim();
        if (DIGITS.matcher(fraction).matches()) {
            dt.append('.').append(fraction, 0, Math.min(fraction.length(), MAX_FRACTION_DIGITS));
        }
        Matcher o = EXIF_OFFSET.matcher(offset == null ? "" : offset.trim());
        if (o.matches()) {
            dt.append(o.group(1)).append(o.group(2)).append(o.group(3));
        }
        return dt.toString();
    }

}
