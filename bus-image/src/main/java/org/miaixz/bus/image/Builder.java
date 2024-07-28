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
package org.miaixz.bus.image;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ColorKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.ProgressStatus;
import org.miaixz.bus.image.galaxy.SupplierEx;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.CIELab;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.metric.props.Props;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    private static final char[] BASE64 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '+', '/' };
    private static final byte[] INV_BASE64 = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1,
            -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29,
            30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };
    public static String LINE_SEPARATOR = System.getProperty(Keys.LINE_SEPARATOR);

    /**
     * Prepare a file to be written by creating the parent directory if necessary.
     *
     * @param file the source file
     * @throws IOException if an I/O error occurs
     */
    public static void prepareToWriteFile(File file) throws IOException {
        if (!file.exists()) {
            // Check the file that doesn't exist yet.
            // Create a new file. The file is writable if the creation succeeds.
            File outputDir = file.getParentFile();
            // necessary to check exists otherwise mkdirs() is false when dir exists
            if (outputDir != null && !outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("Cannot write parent directory of " + file.getPath());
            }
        }
    }

    /**
     * Print a byte count in a human-readable format
     *
     * @param bytes number of bytes
     * @param si    true for SI units (powers of 1000), false for binary units (powers of 1024)
     * @return the human-readable size of the byte count
     * @see <a href="https://programming.guide/worlds-most-copied-so-snippet.html">World's most copied StackOverflow
     *      snippet</a>
     */
    public static String humanReadableByte(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        long absBytes = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absBytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(absBytes) / Math.log(unit));
        long th = (long) Math.ceil(Math.pow(unit, exp) * (unit - 0.05));
        if (exp < 6 && absBytes >= th - ((th & 0xFFF) == 0xD00 ? 51 : 0))
            exp++;
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        if (exp > 4) {
            bytes /= unit;
            exp -= 1;
        }
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static Properties loadProperties(String url, Properties p) throws IOException {
        if (p == null) {
            p = new Properties();
        }
        try (InputStream in = IoKit.openFileOrURL(url)) {
            p.load(in);
        }
        return p;
    }

    public static void addAttributes(Attributes attrs, int[] tags, String... ss) {
        Attributes item = attrs;
        for (int i = 0; i < tags.length - 1; i++) {
            int tag = tags[i];
            Sequence sq = item.getSequence(tag);
            if (sq == null) {
                sq = item.newSequence(tag, 1);
            }
            if (sq.isEmpty()) {
                sq.add(new Attributes());
            }
            item = sq.get(0);
        }
        int tag = tags[tags.length - 1];
        VR vr = ElementDictionary.vrOf(tag, item.getPrivateCreator(tag));
        if (ss.length == 0 || ss.length == 1 && ss[0].isEmpty()) {
            if (vr == VR.SQ) {
                item.newSequence(tag, 1).add(new Attributes(0));
            } else {
                item.setNull(tag, vr);
            }
        } else {
            item.setString(tag, vr, ss);
        }
    }

    public static void addAttributes(Attributes attrs, String[] optVals) {
        if (optVals != null)
            for (String optVal : optVals) {
                int delim = optVal.indexOf('=');
                if (delim < 0) {
                    addAttributes(attrs, Tag.toTags(StringKit.splitToArray(optVal, "/")));
                } else {
                    addAttributes(attrs, Tag.toTags(StringKit.splitToArray(optVal.substring(0, delim), "/")),
                            optVal.substring(delim + 1));
                }
            }
    }

    public static void addEmptyAttributes(Attributes attrs, String[] optVals) {
        if (optVals != null) {
            for (int i = 0; i < optVals.length; i++) {
                addAttributes(attrs, Tag.toTags(StringKit.splitToArray(optVals[i], "/")));
            }
        }
    }

    public static boolean updateAttributes(Attributes data, Attributes attrs, String uidSuffix) {
        if (attrs.isEmpty() && uidSuffix == null) {
            return false;
        }
        if (uidSuffix != null) {
            data.setString(Tag.StudyInstanceUID, VR.UI, data.getString(Tag.StudyInstanceUID) + uidSuffix);
            data.setString(Tag.SeriesInstanceUID, VR.UI, data.getString(Tag.SeriesInstanceUID) + uidSuffix);
            data.setString(Tag.SOPInstanceUID, VR.UI, data.getString(Tag.SOPInstanceUID) + uidSuffix);
        }
        data.update(UpdatePolicy.OVERWRITE, attrs, null);
        return true;
    }

    public static void shutdown(ExecutorService executorService) {
        if (executorService != null) {
            try {
                executorService.shutdown();
            } catch (Exception e) {
                Logger.error("ExecutorService shutdown", e);
            }
        }
    }

    public static void encode(byte[] src, int srcPos, int srcLen, char[] dest, int destPos) {
        if (srcPos < 0 || srcLen < 0 || srcLen > src.length - srcPos)
            throw new IndexOutOfBoundsException();
        int destLen = (srcLen * 4 / 3 + 3) & ~3;
        if (destPos < 0 || destLen > dest.length - destPos)
            throw new IndexOutOfBoundsException();
        byte b1, b2, b3;
        int n = srcLen / 3;
        int r = srcLen - 3 * n;
        while (n-- > 0) {
            dest[destPos++] = BASE64[((b1 = src[srcPos++]) >>> 2) & 0x3F];
            dest[destPos++] = BASE64[((b1 & 0x03) << 4) | (((b2 = src[srcPos++]) >>> 4) & 0x0F)];
            dest[destPos++] = BASE64[((b2 & 0x0F) << 2) | (((b3 = src[srcPos++]) >>> 6) & 0x03)];
            dest[destPos++] = BASE64[b3 & 0x3F];
        }
        if (r > 0)
            if (r == 1) {
                dest[destPos++] = BASE64[((b1 = src[srcPos]) >>> 2) & 0x3F];
                dest[destPos++] = BASE64[((b1 & 0x03) << 4)];
                dest[destPos++] = '=';
                dest[destPos++] = '=';
            } else {
                dest[destPos++] = BASE64[((b1 = src[srcPos++]) >>> 2) & 0x3F];
                dest[destPos++] = BASE64[((b1 & 0x03) << 4) | (((b2 = src[srcPos]) >>> 4) & 0x0F)];
                dest[destPos++] = BASE64[(b2 & 0x0F) << 2];
                dest[destPos++] = '=';
            }
    }

    public static void decode(char[] ch, int off, int len, OutputStream out) throws IOException {
        byte b2, b3;
        while ((len -= 2) >= 0) {
            out.write((byte) ((INV_BASE64[ch[off++]] << 2) | ((b2 = INV_BASE64[ch[off++]]) >>> 4)));
            if ((len-- == 0) || ch[off] == '=')
                break;
            out.write((byte) ((b2 << 4) | ((b3 = INV_BASE64[ch[off++]]) >>> 2)));
            if ((len-- == 0) || ch[off] == '=')
                break;
            out.write((byte) ((b3 << 6) | INV_BASE64[ch[off++]]));
        }
    }

    public static boolean getEmptytoFalse(String val) {
        if (StringKit.hasText(val)) {
            return getBoolean(val);
        }
        return false;
    }

    private static boolean getBoolean(String val) {
        return Boolean.TRUE.toString().equalsIgnoreCase(val);
    }

    public static OptionalDouble getOptionalDouble(Double val) {
        return val == null ? OptionalDouble.empty() : OptionalDouble.of(val);
    }

    public static OptionalInt getOptionalInteger(Integer val) {
        return val == null ? OptionalInt.empty() : OptionalInt.of(val);
    }

    public static boolean isVideo(String uid) {
        return switch (UID.from(uid)) {
        case UID.MPEG2MPML, UID.MPEG2MPMLF, UID.MPEG2MPHL, UID.MPEG2MPHLF, UID.MPEG4HP41, UID.MPEG4HP41F, UID.MPEG4HP41BD, UID.MPEG4HP41BDF, UID.MPEG4HP422D, UID.MPEG4HP422DF, UID.MPEG4HP423D, UID.MPEG4HP423DF, UID.MPEG4HP42STEREO, UID.MPEG4HP42STEREOF, UID.HEVCMP51, UID.HEVCM10P51 -> true;
        default -> false;
        };
    }

    public static boolean isJpeg2000(String uid) {
        return switch (UID.from(uid)) {
        case UID.JPEG2000Lossless, UID.JPEG2000, UID.JPEG2000MCLossless, UID.JPEG2000MC, UID.HTJ2KLossless, UID.HTJ2KLosslessRPCL, UID.HTJ2K -> true;
        default -> false;
        };
    }

    public static boolean isNative(String uid) {
        return switch (UID.from(uid)) {
        case UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian -> true;
        default -> false;
        };
    }

    public static String getFormattedText(Object value, String format) {
        return getFormattedText(value, format, Locale.getDefault());
    }

    public static String getFormattedText(Object value, String format, Locale locale) {
        if (value == null) {
            return Normal.EMPTY;
        }

        String str;

        if (value instanceof String string) {
            str = string;
        } else if (value instanceof String[] strings) {
            str = String.join("\\", Arrays.asList(strings));
        } else if (value instanceof TemporalAccessor temporal) {
            str = Format.formatDateTime(temporal, locale);
        } else if (value instanceof TemporalAccessor[] temporal) {
            str = Stream.of(temporal).map(v -> Format.formatDateTime(v, locale)).collect(Collectors.joining(", "));
        } else if (value instanceof float[] array) {
            str = IntStream.range(0, array.length).mapToObj(i -> String.valueOf(array[i]))
                    .collect(Collectors.joining(", "));
        } else if (value instanceof double[] array) {
            str = DoubleStream.of(array).mapToObj(String::valueOf).collect(Collectors.joining(", "));
        } else if (value instanceof int[] array) {
            str = IntStream.of(array).mapToObj(String::valueOf).collect(Collectors.joining(", "));
        } else {
            str = value.toString();
        }

        if (StringKit.hasText(format) && !"$V".equals(format.trim())) {
            return formatValue(str, value instanceof Float || value instanceof Double, format);
        }

        return str == null ? Normal.EMPTY : str;
    }

    protected static String formatValue(String value, boolean decimal, String format) {
        String str = value;
        int index = format.indexOf("$V");
        int fmLength = 2;
        if (index != -1) {
            boolean suffix = format.length() > index + fmLength;
            // If the value ($V) is followed by ':' that means a number formatter is used
            if (suffix && format.charAt(index + fmLength) == ':') {
                fmLength++;
                if (format.charAt(index + fmLength) == 'f' && decimal) {
                    fmLength++;
                    String pattern = getPattern(index + fmLength, format);
                    if (pattern != null) {
                        fmLength += pattern.length() + 2;
                        try {
                            str = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance())
                                    .format(Double.parseDouble(str));
                        } catch (NumberFormatException e) {
                            Logger.warn("Cannot apply pattern to decimal value", e);
                        }
                    }
                } else if (format.charAt(index + fmLength) == 'l') {
                    fmLength++;
                    String pattern = getPattern(index + fmLength, format);
                    if (pattern != null) {
                        fmLength += pattern.length() + 2;
                        try {
                            int limit = Integer.parseInt(pattern);
                            int size = str.length();
                            if (size > limit) {
                                str = str.substring(0, limit) + "...";
                            }
                        } catch (NumberFormatException e) {
                            Logger.warn("Cannot apply pattern to decimal value", e);
                        }
                    }
                }
            }
            str = format.substring(0, index) + str;
            if (format.length() > index + fmLength) {
                str += format.substring(index + fmLength);
            }
        }
        return str;
    }

    private static String getPattern(int startIndex, String format) {
        int beginIndex = format.indexOf('$', startIndex);
        int endIndex = format.indexOf('$', startIndex + 2);
        if (beginIndex == -1 || endIndex == -1) {
            return null;
        }
        return format.substring(beginIndex + 1, endIndex);
    }

    public static String getStringFromDicomElement(Attributes dicom, int tag) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return null;
        }

        String[] s = dicom.getStrings(tag);
        if (s == null || s.length == 0) {
            return null;
        }
        if (s.length == 1) {
            return s[0];
        }
        StringBuilder sb = new StringBuilder(s[0]);
        for (int i = 1; i < s.length; i++) {
            sb.append("\\").append(s[i]);
        }
        return sb.toString();
    }

    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag) {
        return getStringArrayFromDicomElement(dicom, tag, (String) null);
    }

    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return null;
        }
        return dicom.getStrings(privateCreatorID, tag);
    }

    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag, String[] defaultValue) {
        return getStringArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID,
            String[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        String[] val = dicom.getStrings(privateCreatorID, tag);
        if (val == null || val.length == 0) {
            return defaultValue;
        }
        return val;
    }

    public static Date getDateFromDicomElement(Attributes dicom, int tag, Date defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        return dicom.getDate(tag, defaultValue);
    }

    public static Date[] getDatesFromDicomElement(Attributes dicom, int tag, String privateCreatorID,
            Date[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        Date[] val = dicom.getDates(privateCreatorID, tag);
        if (val == null || val.length == 0) {
            return defaultValue;
        }
        return val;
    }

    public static String getPatientAgeInPeriod(Attributes dicom, int tag, boolean computeOnlyIfNull) {
        return getPatientAgeInPeriod(dicom, tag, null, null, computeOnlyIfNull);
    }

    public static String getPatientAgeInPeriod(Attributes dicom, int tag, String privateCreatorID, String defaultValue,
            boolean computeOnlyIfNull) {
        if (dicom == null) {
            return defaultValue;
        }

        if (computeOnlyIfNull) {
            String s = dicom.getString(privateCreatorID, tag, defaultValue);
            if (StringKit.hasText(s)) {
                return s;
            }
        }

        Date date = getDate(dicom, Tag.ContentDate, Tag.AcquisitionDate, Tag.DateOfSecondaryCapture, Tag.SeriesDate,
                Tag.StudyDate);

        if (date != null) {
            Date bithdate = dicom.getDate(Tag.PatientBirthDate);
            if (bithdate != null) {
                return getPeriod(Format.toLocalDate(bithdate), Format.toLocalDate(date));
            }
        }
        return null;
    }

    private static Date getDate(Attributes dicom, int... tagID) {
        Date date = null;
        for (int i : tagID) {
            date = dicom.getDate(i);
            if (date != null) {
                return date;
            }
        }
        return date;
    }

    public static String getPeriod(LocalDate first, LocalDate last) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(last);

        long years = ChronoUnit.YEARS.between(first, last);
        if (years < 2) {
            long months = ChronoUnit.MONTHS.between(first, last);
            if (months < 2) {
                return String.format("%03dD", ChronoUnit.DAYS.between(first, last));
            }
            return String.format("%03dM", months);
        }
        return String.format("%03dY", years);
    }

    public static Float getFloatFromDicomElement(Attributes dicom, int tag, Float defaultValue) {
        return getFloatFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static Float getFloatFromDicomElement(Attributes dicom, int tag, String privateCreatorID,
            Float defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        try {
            return dicom.getFloat(privateCreatorID, tag, defaultValue == null ? 0.0F : defaultValue);
        } catch (NumberFormatException e) {
            Logger.error("Cannot parse Float of {}: {} ", Tag.toString(tag), e.getMessage());
        }
        return defaultValue;
    }

    public static Integer getIntegerFromDicomElement(Attributes dicom, int tag, Integer defaultValue) {
        return getIntegerFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static Integer getIntegerFromDicomElement(Attributes dicom, int tag, String privateCreatorID,
            Integer defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        try {
            return dicom.getInt(privateCreatorID, tag, defaultValue == null ? 0 : defaultValue);
        } catch (NumberFormatException e) {
            Logger.error("Cannot parse Integer of {}: {} ", Tag.toString(tag), e.getMessage());
        }
        return defaultValue;
    }

    public static Double getDoubleFromDicomElement(Attributes dicom, int tag, Double defaultValue) {
        return getDoubleFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static Double getDoubleFromDicomElement(Attributes dicom, int tag, String privateCreatorID,
            Double defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        try {
            return dicom.getDouble(privateCreatorID, tag, defaultValue == null ? 0.0 : defaultValue);
        } catch (NumberFormatException e) {
            Logger.error("Cannot parse Double of {}: {} ", Tag.toString(tag), e.getMessage());
        }
        return defaultValue;
    }

    public static int[] getIntArrayFromDicomElement(Attributes dicom, int tag, int[] defaultValue) {
        return getIntArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static int[] getIntArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID,
            int[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        try {
            int[] val = dicom.getInts(privateCreatorID, tag);
            if (val != null && val.length != 0) {
                return val;
            }
        } catch (NumberFormatException e) {
            Logger.error("Cannot parse int[] of {}: {} ", Tag.toString(tag), e.getMessage());
        }
        return defaultValue;
    }

    public static float[] getFloatArrayFromDicomElement(Attributes dicom, int tag, float[] defaultValue) {
        return getFloatArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static float[] getFloatArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID,
            float[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        try {
            float[] val = dicom.getFloats(privateCreatorID, tag);
            if (val != null && val.length != 0) {
                return val;
            }
        } catch (NumberFormatException e) {
            Logger.error("Cannot parse float[] of {}: {} ", Tag.toString(tag), e.getMessage());
        }
        return defaultValue;
    }

    public static double[] getDoubleArrayFromDicomElement(Attributes dicom, int tag, double[] defaultValue) {
        return getDoubleArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static double[] getDoubleArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID,
            double[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        try {
            double[] val = dicom.getDoubles(privateCreatorID, tag);
            if (val != null && val.length != 0) {
                return val;
            }
        } catch (NumberFormatException e) {
            Logger.error("Cannot parse double[] of {}: {} ", Tag.toString(tag), e.getMessage());
        }
        return defaultValue;
    }

    public static List<Attributes> getSequence(Attributes dicom, int tagSeq) {
        if (dicom != null) {
            Sequence item = dicom.getSequence(tagSeq);
            if (item != null) {
                return item;
            }
        }
        return Collections.emptyList();
    }

    public static boolean isImageFrameApplicableToReferencedImageSequence(List<Attributes> refImgSeq, int childTag,
            String sopInstanceUID, int frame, boolean required) {
        if (!required && (refImgSeq == null || refImgSeq.isEmpty())) {
            return true;
        }
        if (StringKit.hasText(sopInstanceUID)) {
            for (Attributes sopUID : refImgSeq) {
                if (sopInstanceUID.equals(sopUID.getString(Tag.ReferencedSOPInstanceUID))) {
                    int[] frames = sopUID.getInts(childTag);
                    if (frames == null || frames.length == 0) {
                        return true;
                    }
                    for (int f : frames) {
                        if (f == frame) {
                            return true;
                        }
                    }
                    // if the frame has been excluded
                    return false;
                }
            }
        }
        return false;
    }

    public static <T, E extends Exception> SupplierEx<T, E> memoize(SupplierEx<T, E> original) {
        return new SupplierEx<>() {
            boolean initialized;

            public T get() throws E {
                return delegate.get();
            }

            private synchronized T firstTime() throws E {
                if (!initialized) {
                    T value = original.get();
                    delegate = () -> value;
                    initialized = true;
                }
                return delegate.get();
            }

            SupplierEx<T, E> delegate = this::firstTime;
        };
    }

    public static LocalDate getDicomDate(String date) {
        if (StringKit.hasText(date)) {
            try {
                return Format.parseDA(date);
            } catch (Exception e) {
                Logger.error("Parse DICOM date", e); // $NON-NLS-1$
            }
        }
        return null;
    }

    public static LocalTime getDicomTime(String time) {
        if (StringKit.hasText(time)) {
            try {
                return Format.parseTM(time);
            } catch (Exception e1) {
                Logger.error("Parse DICOM time", e1); // $NON-NLS-1$
            }
        }
        return null;
    }

    public static LocalDateTime dateTime(Attributes dcm, int dateID, int timeID) {
        if (dcm == null) {
            return null;
        }
        LocalDate date = getDicomDate(dcm.getString(dateID));
        if (date == null) {
            return null;
        }
        LocalTime time = getDicomTime(dcm.getString(timeID));
        if (time == null) {
            return date.atStartOfDay();
        }
        return LocalDateTime.of(date, time);
    }

    /**
     * Build the shape from DICOM Shutter
     *
     * @param dcm the DICOM attributes
     * @return the shape
     * @see <a href="http://dicom.nema.org/MEDICAL/DICOM/current/output/chtml/part03/sect_C.7.6.11.html">C.7.6.11
     *      Display Shutter Module</a>
     */
    public static Area getShutterShape(Attributes dcm) {
        Area shape = null;
        String shutterShape = getStringFromDicomElement(dcm, Tag.ShutterShape);
        if (shutterShape != null) {
            if (shutterShape.contains("RECTANGULAR") || shutterShape.contains("RECTANGLE")) { // $NON-NLS-1$
                                                                                              // //$NON-NLS-2$
                Rectangle2D rect = new Rectangle2D.Double();
                rect.setFrameFromDiagonal(dcm.getInt(Tag.ShutterLeftVerticalEdge, 0),
                        dcm.getInt(Tag.ShutterUpperHorizontalEdge, 0), dcm.getInt(Tag.ShutterRightVerticalEdge, 0),
                        dcm.getInt(Tag.ShutterLowerHorizontalEdge, 0));
                if (rect.isEmpty()) {
                    Logger.error("Shutter rectangle has an empty area!");
                } else {
                    shape = new Area(rect);
                }
            }
            if (shutterShape.contains("CIRCULAR")) { // $NON-NLS-1$
                int[] centerOfCircularShutter = dcm.getInts(Tag.CenterOfCircularShutter);
                if (centerOfCircularShutter != null && centerOfCircularShutter.length >= 2) {
                    Ellipse2D ellipse = new Ellipse2D.Double();
                    double radius = dcm.getInt(Tag.RadiusOfCircularShutter, 0);
                    // Thanks DICOM for reversing x,y by row,column
                    ellipse.setFrameFromCenter(centerOfCircularShutter[1], centerOfCircularShutter[0],
                            centerOfCircularShutter[1] + radius, centerOfCircularShutter[0] + radius);
                    if (ellipse.isEmpty()) {
                        Logger.error("Shutter ellipse has an empty area!");
                    } else {
                        if (shape == null) {
                            shape = new Area(ellipse);
                        } else {
                            shape.intersect(new Area(ellipse));
                        }
                    }
                }
            }
            if (shutterShape.contains("POLYGONAL")) { // $NON-NLS-1$
                int[] points = dcm.getInts(Tag.VerticesOfThePolygonalShutter);
                if (points != null && points.length >= 6) {
                    Polygon polygon = new Polygon();
                    for (int i = 0; i < points.length / 2; i++) {
                        // Thanks DICOM for reversing x,y by row,column
                        polygon.addPoint(points[i * 2 + 1], points[i * 2]);
                    }
                    if (isPolygonValid(polygon)) {
                        if (shape == null) {
                            shape = new Area(polygon);
                        } else {
                            shape.intersect(new Area(polygon));
                        }
                    } else {
                        Logger.error("Shutter rectangle has an empty area!");
                    }
                }
            }
        }
        return shape;
    }

    private static boolean isPolygonValid(Polygon polygon) {
        int[] xPoints = polygon.xpoints;
        int[] yPoints = polygon.ypoints;
        double area = 0;
        for (int i = 0; i < polygon.npoints; i++) {
            area += (xPoints[i] * yPoints[(i + 1) % polygon.npoints])
                    - (xPoints[(i + 1) % polygon.npoints] * yPoints[i]);
        }
        // Evaluating if this is a polygon, not a line
        return area != 0 && polygon.npoints > 2;
    }

    /**
     * Extract the shutter color from ShutterPresentationColorCIELabValue or ShutterPresentationValue
     *
     * @param dcm the DICOM attributes
     * @return the color
     */
    public static Color getShutterColor(Attributes dcm) {
        int[] rgb = CIELab.dicomLab2rgb(dcm.getInts(Tag.ShutterPresentationColorCIELabValue));
        return ColorKit.getColor(dcm.getInt(Tag.ShutterPresentationValue, 0x0000), rgb);
    }

    public static void forceGettingAttributes(Status dcmState, AutoCloseable closeable) {
        ImageProgress p = dcmState.getProgress();
        if (p != null) {
            IoKit.close(closeable);
        }
    }

    public static void notifyProgession(ImageProgress p, Attributes cmd, ProgressStatus ps, int numberOfSuboperations) {
        if (p != null && cmd != null) {
            int c;
            int f;
            int r;
            int w;
            if (p.getAttributes() == null) {
                c = 0;
                f = 0;
                w = 0;
                r = numberOfSuboperations;
            } else {
                c = p.getNumberOfCompletedSuboperations();
                f = p.getNumberOfFailedSuboperations();
                w = p.getNumberOfWarningSuboperations();
                r = numberOfSuboperations - (c + f + w);
            }

            if (r < 1) {
                r = 1;
            }

            if (ps == ProgressStatus.COMPLETED) {
                c++;
            } else if (ps == ProgressStatus.FAILED) {
                f++;
            } else if (ps == ProgressStatus.WARNING) {
                w++;
            }
            cmd.setInt(Tag.NumberOfCompletedSuboperations, VR.US, c);
            cmd.setInt(Tag.NumberOfFailedSuboperations, VR.US, f);
            cmd.setInt(Tag.NumberOfWarningSuboperations, VR.US, w);
            cmd.setInt(Tag.NumberOfRemainingSuboperations, VR.US, r - 1);
        }
    }

    public static void notifyProgession(Status state, String iuid, String cuid, int status, ProgressStatus ps,
            int numberOfSuboperations) {
        state.setStatus(status);
        ImageProgress p = state.getProgress();
        if (p != null) {
            Attributes cmd = Optional.ofNullable(p.getAttributes()).orElseGet(Attributes::new);
            cmd.setInt(Tag.Status, VR.US, status);
            cmd.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
            cmd.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
            notifyProgession(p, cmd, ps, numberOfSuboperations);
            p.setAttributes(cmd);
        }
    }

    public static int getTotalOfSuboperations(Attributes cmd) {
        if (cmd != null) {
            int c = cmd.getInt(Tag.NumberOfCompletedSuboperations, 0);
            int f = cmd.getInt(Tag.NumberOfFailedSuboperations, 0);
            int w = cmd.getInt(Tag.NumberOfWarningSuboperations, 0);
            int r = cmd.getInt(Tag.NumberOfRemainingSuboperations, 0);
            return r + c + f + w;
        }
        return 0;
    }

    public static String concat(String[] ss, char delim) {
        int n = ss.length;
        if (n == 0)
            return "";

        if (n == 1) {
            String s = ss[0];
            return s != null ? s : "";
        }
        int len = n - 1;
        for (String s : ss)
            if (s != null)
                len += s.length();

        char[] cs = new char[len];
        int off = 0;
        int i = 0;
        for (String s : ss) {
            if (i++ != 0)
                cs[off++] = delim;
            if (s != null) {
                int l = s.length();
                s.getChars(0, l, cs, off);
                off += l;
            }
        }
        return new String(cs);
    }

    public static StringBuilder appendLine(StringBuilder sb, Object... ss) {
        for (Object s : ss)
            sb.append(s);
        return sb.append(LINE_SEPARATOR);
    }

    public static Object splitAndTrim(String s, char delim) {
        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos + 1)) >= 0)
            count++;

        if (count == 1)
            return substring(s, 0, s.length());

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2 - 1);
            ss[count] = substring(s, delimPos + 1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    public static String[] split(String s, char delim) {
        if (s == null || s.isEmpty())
            return Normal.EMPTY_STRING_ARRAY;

        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos + 1)) >= 0)
            count++;

        if (count == 1)
            return new String[] { s };

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2 - 1);
            ss[count] = s.substring(delimPos + 1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    private static String substring(String s, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && s.charAt(beginIndex) <= ' ')
            beginIndex++;
        while (beginIndex < endIndex && s.charAt(endIndex - 1) <= ' ')
            endIndex--;
        return beginIndex < endIndex ? s.substring(beginIndex, endIndex) : "";
    }

    public static String trimTrailing(String s) {
        int endIndex = s.length();
        while (endIndex > 0 && s.charAt(endIndex - 1) <= ' ')
            endIndex--;
        return s.substring(0, endIndex);
    }

    public static long parseIS(String s) {
        return s != null && s.length() != 0 ? Long.parseLong(s) : 0L;
    }

    public static long parseUV(String s) {
        return s != null && s.length() != 0 ? Long.parseUnsignedLong(s) : 0L;
    }

    public static double parseDS(String s) {
        return s != null && s.length() != 0 ? Double.parseDouble(s.replace(',', '.')) : 0;
    }

    public static String formatDS(float f) {
        String s = Float.toString(f);
        int l = s.length();
        if (s.startsWith(".0", l - 2))
            return s.substring(0, l - 2);
        int e = s.indexOf('E', l - 5);
        return e > 0 && s.startsWith(".0", e - 2) ? cut(s, e - 2, e) : s;
    }

    public static String formatDS(double d) {
        String s = Double.toString(d);
        int l = s.length();
        if (s.startsWith(".0", l - 2))
            return s.substring(0, l - 2);
        int skip = l - 16;
        int e = s.indexOf('E', l - 5);
        return e < 0 ? (skip > 0 ? s.substring(0, 16) : s)
                : s.startsWith(".0", e - 2) ? cut(s, e - 2, e) : skip > 0 ? cut(s, e - skip, e) : s;
    }

    private static String cut(String s, int begin, int end) {
        int l = s.length();
        char[] ch = new char[l - (end - begin)];
        s.getChars(0, begin, ch, 0);
        s.getChars(end, l, ch, begin);
        return new String(ch);
    }

    public static boolean matches(String s, String key, boolean matchNullOrEmpty, boolean ignoreCase) {
        if (key == null || key.isEmpty())
            return true;

        if (s == null || s.isEmpty())
            return matchNullOrEmpty;

        return containsWildCard(key) ? compilePattern(key, ignoreCase).matcher(s).matches()
                : ignoreCase ? key.equalsIgnoreCase(s) : key.equals(s);
    }

    public static Pattern compilePattern(String key, boolean ignoreCase) {
        StringTokenizer stk = new StringTokenizer(key, "*?", true);
        StringBuilder regex = new StringBuilder();
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            char ch1 = tk.charAt(0);
            if (ch1 == '*') {
                regex.append(".*");
            } else if (ch1 == '?') {
                regex.append(".");
            } else {
                regex.append("\\Q").append(tk).append("\\E");
            }
        }
        return Pattern.compile(regex.toString(), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
    }

    public static boolean containsWildCard(String s) {
        return (s.indexOf('*') >= 0 || s.indexOf('?') >= 0);
    }

    public static String[] maskNull(String[] ss) {
        return maskNull(ss, Normal.EMPTY_STRING_ARRAY);
    }

    public static <T> T maskNull(T o, T mask) {
        return o == null ? mask : o;
    }

    public static String maskEmpty(String s, String mask) {
        return s == null || s.isEmpty() ? mask : s;
    }

    public static String truncate(String s, int maxlen) {
        return s.length() > maxlen ? s.substring(0, maxlen) : s;
    }

    /**
     * Returns a {@code String} resulting from replacing all non-ASCII and non-printable characters in the specified
     * {@code String} with {@code replacement} character.
     *
     * @param s           - the specified string
     * @param replacement - the replacement character
     * @return a string
     */
    public static String replaceNonPrintASCII(String s, char replacement) {
        char[] cs = s.toCharArray();
        int count = 0;
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] > 0x20 && cs[i] < 0x7F)
                continue;
            cs[i] = replacement;
            count++;
        }
        return count > 0 ? new String(cs) : s;
    }

    public static <T> boolean equals(T o1, T o2) {
        return Objects.equals(o1, o2);
    }

    public static String replaceSystemProperties(String s) {
        int i = s.indexOf("${");
        if (i == -1)
            return s;

        StringBuilder sb = new StringBuilder(s.length());
        int j = -1;
        do {
            sb.append(s, j + 1, i);
            if ((j = s.indexOf('}', i + 2)) == -1) {
                j = i - 1;
                break;
            }
            int k = s.lastIndexOf(':', j);
            String val = s.startsWith("env.", i + 2) ? System.getenv(s.substring(i + 6, k < i ? j : k))
                    : System.getProperty(s.substring(i + 2, k < i ? j : k));
            sb.append(val != null ? val : k < 0 ? s.substring(i, j + 1) : s.substring(k + 1, j));
            i = s.indexOf("${", j + 1);
        } while (i != -1);
        sb.append(s.substring(j + 1));
        return sb.toString();
    }

    public static <T> boolean contains(T[] a, T o) {
        for (T t : a)
            if (Objects.equals(t, o))
                return true;
        return false;
    }

    public static <T> T[] requireNotEmpty(T[] a, String message) {
        if (a.length == 0)
            throw new IllegalArgumentException(message);
        return a;
    }

    public static String requireNotEmpty(String s, String message) {
        if (s.isEmpty())
            throw new IllegalArgumentException(message);
        return s;
    }

    public static String[] requireContainsNoEmpty(String[] ss, String message) {
        for (String s : ss)
            requireNotEmpty(s, message);
        return ss;
    }

    public static MediaType forTransferSyntax(String ts) {
        MediaType type;
        switch (UID.from(ts)) {
        case UID.ExplicitVRLittleEndian:
        case UID.ImplicitVRLittleEndian:
            return MediaType.APPLICATION_OCTET_STREAM_TYPE;
        case UID.RLELossless:
            return MediaType.IMAGE_DICOM_RLE_TYPE;
        case UID.JPEGBaseline8Bit:
        case UID.JPEGExtended12Bit:
        case UID.JPEGLossless:
        case UID.JPEGLosslessSV1:
            type = MediaType.IMAGE_JPEG_TYPE;
            break;
        case UID.JPEGLSLossless:
        case UID.JPEGLSNearLossless:
            type = MediaType.IMAGE_JLS_TYPE;
            break;
        case UID.JPEG2000Lossless:
        case UID.JPEG2000:
            type = MediaType.IMAGE_JP2_TYPE;
            break;
        case UID.JPEG2000MCLossless:
        case UID.JPEG2000MC:
            type = MediaType.IMAGE_JPX_TYPE;
            break;
        case UID.HTJ2KLossless:
        case UID.HTJ2KLosslessRPCL:
        case UID.HTJ2K:
            type = MediaType.IMAGE_JPHC_TYPE;
            break;
        case UID.MPEG2MPML:
        case UID.MPEG2MPHL:
            type = MediaType.VIDEO_MPEG_TYPE;
            break;
        case UID.MPEG4HP41:
        case UID.MPEG4HP41BD:
            type = MediaType.VIDEO_MP4_TYPE;
            break;
        default:
            throw new IllegalArgumentException("ts: " + ts);
        }
        return new MediaType(type.getType(), type.getSubtype(), Collections.singletonMap("transfer-syntax", ts));
    }

    public static String transferSyntaxOf(MediaType bulkdataMediaType) {
        String tsuid = bulkdataMediaType.getParameters().get("transfer-syntax");
        if (tsuid != null)
            return tsuid;

        String type = bulkdataMediaType.getType().toLowerCase();
        String subtype = bulkdataMediaType.getSubtype().toLowerCase();
        switch (type) {
        case "image":
            switch (subtype) {
            case "jpeg":
                return UID.JPEGLosslessSV1.uid;
            case "jls":
            case "x-jls":
                return UID.JPEGLSLossless.uid;
            case "jp2":
                return UID.JPEG2000Lossless.uid;
            case "jpx":
                return UID.JPEG2000MCLossless.uid;
            case "x-dicom-rle":
            case "dicom-rle":
                return UID.RLELossless.uid;
            }
        case "video":
            switch (subtype) {
            case "mpeg":
                return UID.MPEG2MPML.uid;
            case "mp4":
            case "quicktime":
                return UID.MPEG4HP41.uid;
            }
        }
        return UID.ExplicitVRLittleEndian.uid;
    }

    public static String sopClassOf(MediaType bulkdataMediaType) {
        String type = bulkdataMediaType.getType().toLowerCase();
        return type.equals("image") ? UID.SecondaryCaptureImageStorage.uid
                : type.equals("video") ? UID.VideoPhotographicImageStorage.uid
                        : MediaType.equalsIgnoreParameters(bulkdataMediaType, MediaType.APPLICATION_PDF_TYPE)
                                ? UID.EncapsulatedPDFStorage.uid
                                : MediaType.equalsIgnoreParameters(bulkdataMediaType, MediaType.APPLICATION_XML_TYPE)
                                        ? UID.EncapsulatedCDAStorage.uid
                                        : MediaType.isSTLType(bulkdataMediaType) ? UID.EncapsulatedSTLStorage.uid
                                                : MediaType.equalsIgnoreParameters(bulkdataMediaType,
                                                        MediaType.MODEL_OBJ_TYPE)
                                                                ? UID.EncapsulatedOBJStorage.uid
                                                                : MediaType.equalsIgnoreParameters(bulkdataMediaType,
                                                                        MediaType.MODEL_MTL_TYPE)
                                                                                ? UID.EncapsulatedMTLStorage.uid
                                                                                : null;
    }

    public static String getTransferSyntax(MediaType type) {
        return type != null && MediaType.equalsIgnoreParameters(MediaType.APPLICATION_DICOM_TYPE, type)
                ? type.getParameters().get("transfer-syntax")
                : null;
    }

    public static MediaType applicationDicomWithTransferSyntax(String tsuid) {
        return new MediaType("application", "dicom", Collections.singletonMap("transfer-syntax", tsuid));
    }

    public void validate(File file, IOD iod) {
        if (iod == null)
            throw new IllegalStateException("IOD net initialized");
        ImageInputStream dis = null;
        try {
            System.out.print("Validate: " + file + " ... ");
            dis = new ImageInputStream(file);
            Attributes attrs = dis.readDataset();
            ValidationResult result = attrs.validate(iod);
            if (result.isValid())
                System.out.println("OK");
            else {
                System.out.println("FAILED:");
                System.out.println(result.asText(attrs));
            }
        } catch (IOException e) {
            System.out.println("FAILED: " + e.getMessage());
        } finally {
            IoKit.close(dis);
        }
    }

    public static Props loadRelSoap(URL url) {
        String name = "sop-classes-uid.properties";
        return null;
    }

}
