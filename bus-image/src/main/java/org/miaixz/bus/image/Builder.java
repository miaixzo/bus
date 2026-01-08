/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

/**
 * A utility class providing a collection of static helper methods for common tasks within the DICOM toolkit. This
 * includes data conversion, string manipulation, DICOM attribute handling, date/time formatting, file I/O, and other
 * miscellaneous functions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * Characters for Base64 encoding.
     */
    private static final char[] BASE64 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '+', '/' };
    /**
     * Lookup table for Base64 decoding.
     */
    private static final byte[] INV_BASE64 = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1,
            -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29,
            30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };
    /**
     * The system-dependent line separator string.
     */
    public static String LINE_SEPARATOR = System.getProperty(Keys.LINE_SEPARATOR);

    /**
     * Prepares a file for writing by creating its parent directories if they do not exist.
     *
     * @param file the file to be written.
     * @throws IOException if the parent directories cannot be created.
     */
    public static void prepareToWriteFile(File file) throws IOException {
        if (!file.exists()) {
            File outputDir = file.getParentFile();
            if (outputDir != null && !outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("Cannot write parent directory of " + file.getPath());
            }
        }
    }

    /**
     * Formats a byte count into a human-readable string (e.g., "1.2 kB", "3.4 MiB").
     *
     * @param bytes the number of bytes.
     * @param si    if {@code true}, use SI units (powers of 1000); if {@code false}, use binary units (powers of 1024).
     * @return a human-readable string representation of the byte count.
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

    /**
     * Loads properties from a given URL or file path into a {@link Properties} object.
     *
     * @param url a string representing the URL or file path of the properties file.
     * @param p   an existing {@link Properties} object to load into, or {@code null} to create a new one.
     * @return the loaded {@link Properties} object.
     * @throws IOException if an I/O error occurs while reading from the stream.
     */
    public static Properties loadProperties(String url, Properties p) throws IOException {
        if (p == null) {
            p = new Properties();
        }
        try (InputStream in = IoKit.openFileOrURL(url)) {
            p.load(in);
        }
        return p;
    }

    /**
     * Adds or updates attributes in a DICOM dataset based on a tag path and string values.
     *
     * @param attrs the DICOM dataset to modify.
     * @param tags  an array of tags representing the path to the attribute.
     * @param ss    the string values to set for the attribute.
     */
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

    /**
     * Adds or updates attributes in a DICOM dataset from an array of string options. Each string can be a tag path
     * (e.g., "00100010") or a path with a value (e.g., "00100010=Doe^John").
     *
     * @param attrs   the DICOM dataset to modify.
     * @param optVals an array of string options.
     */
    public static void addAttributes(Attributes attrs, String[] optVals) {
        if (optVals != null)
            for (String optVal : optVals) {
                int delim = optVal.indexOf('=');
                if (delim < 0) {
                    addAttributes(attrs, Tag.toTags(StringKit.splitToArray(optVal, "/")));
                } else {
                    addAttributes(
                            attrs,
                            Tag.toTags(StringKit.splitToArray(optVal.substring(0, delim), "/")),
                            optVal.substring(delim + 1));
                }
            }
    }

    /**
     * Adds empty attributes to a DICOM dataset based on an array of tag paths.
     *
     * @param attrs   the DICOM dataset to modify.
     * @param optVals an array of strings, each representing a tag path.
     */
    public static void addEmptyAttributes(Attributes attrs, String[] optVals) {
        if (optVals != null) {
            for (String optVal : optVals) {
                addAttributes(attrs, Tag.toTags(StringKit.splitToArray(optVal, "/")));
            }
        }
    }

    /**
     * Updates a target DICOM dataset with attributes from another dataset and optionally appends a suffix to UIDs.
     *
     * @param data      the target dataset to be updated.
     * @param attrs     the source attributes to merge.
     * @param uidSuffix a suffix to append to Study, Series, and SOP Instance UIDs, or {@code null} if no change.
     * @return {@code true} if the target dataset was modified.
     */
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

    /**
     * Shuts down an {@link ExecutorService} gracefully.
     *
     * @param executorService the executor service to shut down.
     */
    public static void shutdown(ExecutorService executorService) {
        if (executorService != null) {
            try {
                executorService.shutdown();
            } catch (Exception e) {
                Logger.error("ExecutorService shutdown failed", e);
            }
        }
    }

    /**
     * Base64 encodes a byte array into a character array.
     *
     * @param src     the source byte array.
     * @param srcPos  the starting position in the source array.
     * @param srcLen  the number of bytes to encode.
     * @param dest    the destination character array.
     * @param destPos the starting position in the destination array.
     */
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
        if (r > 0) {
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
    }

    /**
     * Base64 decodes a character array and writes the result to an output stream.
     *
     * @param ch  the source character array.
     * @param off the starting offset in the source array.
     * @param len the number of characters to decode.
     * @param out the output stream to write the decoded bytes to.
     * @throws IOException if an I/O error occurs.
     */
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

    /**
     * Converts a string to a boolean, returning {@code false} if the string is empty or null.
     *
     * @param val the string to convert.
     * @return the boolean value.
     */
    public static boolean getEmptytoFalse(String val) {
        if (StringKit.hasText(val)) {
            return getBoolean(val);
        }
        return false;
    }

    /**
     * Converts a string to a boolean.
     *
     * @param val the string to convert (case-insensitive "true").
     * @return the boolean value.
     */
    private static boolean getBoolean(String val) {
        return Boolean.TRUE.toString().equalsIgnoreCase(val);
    }

    /**
     * Wraps a {@link Double} in an {@link OptionalDouble}.
     *
     * @param val the Double value.
     * @return an {@link OptionalDouble} containing the value, or an empty optional if the input is null.
     */
    public static OptionalDouble getOptionalDouble(Double val) {
        return val == null ? OptionalDouble.empty() : OptionalDouble.of(val);
    }

    /**
     * Wraps an {@link Integer} in an {@link OptionalInt}.
     *
     * @param val the Integer value.
     * @return an {@link OptionalInt} containing the value, or an empty optional if the input is null.
     */
    public static OptionalInt getOptionalInteger(Integer val) {
        return val == null ? OptionalInt.empty() : OptionalInt.of(val);
    }

    /**
     * Checks if the given SOP Class UID corresponds to a video storage type.
     *
     * @param uid the SOP Class UID.
     * @return {@code true} if the UID is a known video storage UID.
     */
    public static boolean isVideo(String uid) {
        return switch (UID.from(uid)) {
            case UID.MPEG2MPML, UID.MPEG2MPMLF, UID.MPEG2MPHL, UID.MPEG2MPHLF, UID.MPEG4HP41, UID.MPEG4HP41F, UID.MPEG4HP41BD, UID.MPEG4HP41BDF, UID.MPEG4HP422D, UID.MPEG4HP422DF, UID.MPEG4HP423D, UID.MPEG4HP423DF, UID.MPEG4HP42STEREO, UID.MPEG4HP42STEREOF, UID.HEVCMP51, UID.HEVCM10P51 -> true;
            default -> false;
        };
    }

    /**
     * Checks if the given Transfer Syntax UID corresponds to a JPEG 2000 compression type.
     *
     * @param uid the Transfer Syntax UID.
     * @return {@code true} if the UID is a known JPEG 2000 Transfer Syntax.
     */
    public static boolean isJpeg2000(String uid) {
        return switch (UID.from(uid)) {
            case UID.JPEG2000Lossless, UID.JPEG2000, UID.JPEG2000MCLossless, UID.JPEG2000MC, UID.HTJ2KLossless, UID.HTJ2KLosslessRPCL, UID.HTJ2K -> true;
            default -> false;
        };
    }

    /**
     * Checks if the given Transfer Syntax UID is a native (uncompressed) DICOM format.
     *
     * @param uid the Transfer Syntax UID.
     * @return {@code true} if the UID is a native Transfer Syntax.
     */
    public static boolean isNative(String uid) {
        return switch (UID.from(uid)) {
            case UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian -> true;
            default -> false;
        };
    }

    /**
     * Formats a value into a string using a custom format pattern.
     *
     * @param value  the value to format.
     * @param format the custom format string.
     * @return the formatted string.
     */
    public static String getFormattedText(Object value, String format) {
        return getFormattedText(value, format, Locale.getDefault());
    }

    /**
     * Formats a value into a string using a custom format pattern and a specific locale.
     *
     * @param value  the value to format.
     * @param format the custom format string.
     * @param locale the locale to use for formatting.
     * @return the formatted string.
     */
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

    /**
     * Applies a format pattern to a value string.
     *
     * @param value   the string representation of the value.
     * @param decimal whether the value is a decimal type.
     * @param format  the format pattern.
     * @return the formatted string.
     */
    protected static String formatValue(String value, boolean decimal, String format) {
        String str = value;
        int index = format.indexOf("$V");
        int fmLength = 2;
        if (index != -1) {
            boolean suffix = format.length() > index + fmLength;
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

    /**
     * Extracts a pattern enclosed in '$' characters from a format string.
     *
     * @param startIndex the index to start searching from.
     * @param format     the format string.
     * @return the extracted pattern, or {@code null}.
     */
    private static String getPattern(int startIndex, String format) {
        int beginIndex = format.indexOf('$', startIndex);
        int endIndex = format.indexOf('$', startIndex + 2);
        if (beginIndex == -1 || endIndex == -1) {
            return null;
        }
        return format.substring(beginIndex + 1, endIndex);
    }

    /**
     * Retrieves the string value of a DICOM attribute. If the attribute is multi-valued, the values are concatenated
     * with a backslash separator.
     *
     * @param dicom the DICOM dataset.
     * @param tag   the attribute tag.
     * @return the string value, or {@code null} if the attribute is not present.
     */
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

    /**
     * Retrieves the string array value of a DICOM attribute.
     *
     * @param dicom the DICOM dataset.
     * @param tag   the attribute tag.
     * @return the string array, or {@code null} if not present.
     */
    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag) {
        return getStringArrayFromDicomElement(dicom, tag, (String) null);
    }

    /**
     * Retrieves the string array value of a private DICOM attribute.
     *
     * @param dicom            the DICOM dataset.
     * @param tag              the attribute tag.
     * @param privateCreatorID the private creator ID.
     * @return the string array, or {@code null} if not present.
     */
    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return null;
        }
        return dicom.getStrings(privateCreatorID, tag);
    }

    /**
     * Retrieves the string array value of a DICOM attribute, returning a default value if not present.
     *
     * @param dicom        the DICOM dataset.
     * @param tag          the attribute tag.
     * @param defaultValue the default value to return.
     * @return the string array, or the default value.
     */
    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag, String[] defaultValue) {
        return getStringArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    /**
     * Retrieves the string array value of a private DICOM attribute, returning a default value if not present.
     *
     * @param dicom            the DICOM dataset.
     * @param tag              the attribute tag.
     * @param privateCreatorID the private creator ID.
     * @param defaultValue     the default value to return.
     * @return the string array, or the default value.
     */
    public static String[] getStringArrayFromDicomElement(
            Attributes dicom,
            int tag,
            String privateCreatorID,
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

    /**
     * Retrieves the date value of a DICOM attribute.
     *
     * @param dicom        the DICOM dataset.
     * @param tag          the attribute tag.
     * @param defaultValue the default value.
     * @return the date, or the default value.
     */
    public static Date getDateFromDicomElement(Attributes dicom, int tag, Date defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        return dicom.getDate(tag, defaultValue);
    }

    /**
     * Retrieves an array of date values from a private DICOM attribute.
     *
     * @param dicom            the DICOM dataset.
     * @param tag              the attribute tag.
     * @param privateCreatorID the private creator ID.
     * @param defaultValue     the default value.
     * @return the date array, or the default value.
     */
    public static Date[] getDatesFromDicomElement(
            Attributes dicom,
            int tag,
            String privateCreatorID,
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

    /**
     * Gets or computes the patient's age as a DICOM age string (e.g., "035Y").
     *
     * @param dicom             the DICOM dataset.
     * @param tag               the tag of the Patient Age attribute.
     * @param computeOnlyIfNull if {@code true}, computation is only performed if the attribute is absent or empty.
     * @return the patient's age string.
     */
    public static String getPatientAgeInPeriod(Attributes dicom, int tag, boolean computeOnlyIfNull) {
        return getPatientAgeInPeriod(dicom, tag, null, null, computeOnlyIfNull);
    }

    /**
     * Gets or computes the patient's age as a DICOM age string.
     *
     * @param dicom             the DICOM dataset.
     * @param tag               the tag of the Patient Age attribute.
     * @param privateCreatorID  the private creator ID, if applicable.
     * @param defaultValue      the default value.
     * @param computeOnlyIfNull if {@code true}, computation is only performed if the attribute is absent or empty.
     * @return the patient's age string.
     */
    public static String getPatientAgeInPeriod(
            Attributes dicom,
            int tag,
            String privateCreatorID,
            String defaultValue,
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

        Date date = getDate(
                dicom,
                Tag.ContentDate,
                Tag.AcquisitionDate,
                Tag.DateOfSecondaryCapture,
                Tag.SeriesDate,
                Tag.StudyDate);

        if (date != null) {
            Date bithdate = dicom.getDate(Tag.PatientBirthDate);
            if (bithdate != null) {
                return getPeriod(Format.toLocalDate(bithdate), Format.toLocalDate(date));
            }
        }
        return null;
    }

    /**
     * Retrieves the first available date from a list of date tags in a DICOM dataset.
     *
     * @param dicom the DICOM dataset.
     * @param tagID a list of date tags to check in order.
     * @return the first found date, or {@code null}.
     */
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

    /**
     * Calculates the period between two dates and formats it as a DICOM age string (e.g., "035Y", "006M", "021D").
     *
     * @param first the start date.
     * @param last  the end date.
     * @return the formatted age string.
     */
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

    /**
     * Retrieves the float value of a DICOM attribute.
     *
     * @param dicom        the DICOM dataset.
     * @param tag          the attribute tag.
     * @param defaultValue the default value.
     * @return the float value, or the default value.
     */
    public static Float getFloatFromDicomElement(Attributes dicom, int tag, Float defaultValue) {
        return getFloatFromDicomElement(dicom, tag, null, defaultValue);
    }

    /**
     * Retrieves the float value of a private DICOM attribute.
     *
     * @param dicom            the DICOM dataset.
     * @param tag              the attribute tag.
     * @param privateCreatorID the private creator ID.
     * @param defaultValue     the default value.
     * @return the float value, or the default value.
     */
    public static Float getFloatFromDicomElement(
            Attributes dicom,
            int tag,
            String privateCreatorID,
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

    /**
     * Retrieves the integer value of a DICOM attribute.
     *
     * @param dicom        the DICOM dataset.
     * @param tag          the attribute tag.
     * @param defaultValue the default value.
     * @return the integer value, or the default value.
     */
    public static Integer getIntegerFromDicomElement(Attributes dicom, int tag, Integer defaultValue) {
        return getIntegerFromDicomElement(dicom, tag, null, defaultValue);
    }

    /**
     * Retrieves the integer value of a private DICOM attribute.
     *
     * @param dicom            the DICOM dataset.
     * @param tag              the attribute tag.
     * @param privateCreatorID the private creator ID.
     * @param defaultValue     the default value.
     * @return the integer value, or the default value.
     */
    public static Integer getIntegerFromDicomElement(
            Attributes dicom,
            int tag,
            String privateCreatorID,
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

    /**
     * Retrieves the double value of a DICOM attribute.
     *
     * @param dicom        the DICOM dataset.
     * @param tag          the attribute tag.
     * @param defaultValue the default value.
     * @return the double value, or the default value.
     */
    public static Double getDoubleFromDicomElement(Attributes dicom, int tag, Double defaultValue) {
        return getDoubleFromDicomElement(dicom, tag, null, defaultValue);
    }

    /**
     * Retrieves the double value of a private DICOM attribute.
     *
     * @param dicom            the DICOM dataset.
     * @param tag              the attribute tag.
     * @param privateCreatorID the private creator ID.
     * @param defaultValue     the default value.
     * @return the double value, or the default value.
     */
    public static Double getDoubleFromDicomElement(
            Attributes dicom,
            int tag,
            String privateCreatorID,
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

    /**
     * Retrieves an array of integer values from a DICOM attribute.
     *
     * @param dicom        the DICOM dataset.
     * @param tag          the attribute tag.
     * @param defaultValue the default value.
     * @return the int array, or the default value.
     */
    public static int[] getIntArrayFromDicomElement(Attributes dicom, int tag, int[] defaultValue) {
        return getIntArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    /**
     * Retrieves an array of integer values from a private DICOM attribute.
     *
     * @param dicom            the DICOM dataset.
     * @param tag              the attribute tag.
     * @param privateCreatorID the private creator ID.
     * @param defaultValue     the default value.
     * @return the int array, or the default value.
     */
    public static int[] getIntArrayFromDicomElement(
            Attributes dicom,
            int tag,
            String privateCreatorID,
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

    /**
     * Retrieves an array of float values from a DICOM attribute.
     *
     * @param dicom        the DICOM dataset.
     * @param tag          the attribute tag.
     * @param defaultValue the default value.
     * @return the float array, or the default value.
     */
    public static float[] getFloatArrayFromDicomElement(Attributes dicom, int tag, float[] defaultValue) {
        return getFloatArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    /**
     * Retrieves an array of float values from a private DICOM attribute.
     *
     * @param dicom            the DICOM dataset.
     * @param tag              the attribute tag.
     * @param privateCreatorID the private creator ID.
     * @param defaultValue     the default value.
     * @return the float array, or the default value.
     */
    public static float[] getFloatArrayFromDicomElement(
            Attributes dicom,
            int tag,
            String privateCreatorID,
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

    /**
     * Retrieves an array of double values from a DICOM attribute.
     *
     * @param dicom        the DICOM dataset.
     * @param tag          the attribute tag.
     * @param defaultValue the default value.
     * @return the double array, or the default value.
     */
    public static double[] getDoubleArrayFromDicomElement(Attributes dicom, int tag, double[] defaultValue) {
        return getDoubleArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    /**
     * Retrieves an array of double values from a private DICOM attribute.
     *
     * @param dicom            the DICOM dataset.
     * @param tag              the attribute tag.
     * @param privateCreatorID the private creator ID.
     * @param defaultValue     the default value.
     * @return the double array, or the default value.
     */
    public static double[] getDoubleArrayFromDicomElement(
            Attributes dicom,
            int tag,
            String privateCreatorID,
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

    /**
     * Retrieves a sequence (SQ attribute) from a DICOM dataset.
     *
     * @param dicom  the DICOM dataset.
     * @param tagSeq the tag of the sequence attribute.
     * @return a list of {@link Attributes} representing the items in the sequence, or an empty list if not found.
     */
    public static List<Attributes> getSequence(Attributes dicom, int tagSeq) {
        if (dicom != null) {
            Sequence item = dicom.getSequence(tagSeq);
            if (item != null) {
                return item;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Checks if a specific image frame is applicable according to a Referenced Image Sequence.
     *
     * @param refImgSeq      the Referenced Image Sequence.
     * @param childTag       the tag of the referenced frame numbers within a sequence item.
     * @param sopInstanceUID the SOP Instance UID of the image.
     * @param frame          the frame number to check.
     * @param required       if {@code true}, the sequence must exist for the check to pass.
     * @return {@code true} if the frame is applicable.
     */
    public static boolean isImageFrameApplicableToReferencedImageSequence(
            List<Attributes> refImgSeq,
            int childTag,
            String sopInstanceUID,
            int frame,
            boolean required) {
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
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Creates a memoized (caching) supplier from an original supplier. The original supplier is only called once.
     *
     * @param <T>      the type of the result.
     * @param <E>      the type of the exception thrown.
     * @param original the original supplier.
     * @return a memoized supplier.
     */
    public static <T, E extends Exception> SupplierEx<T, E> memoize(SupplierEx<T, E> original) {
        return new SupplierEx<>() {

            boolean initialized;

            @Override
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

    /**
     * Parses a DICOM DA (Date) string into a {@link LocalDate}.
     *
     * @param date the DA string.
     * @return the parsed {@link LocalDate}, or {@code null} on failure.
     */
    public static LocalDate getDicomDate(String date) {
        if (StringKit.hasText(date)) {
            try {
                return Format.parseDA(date);
            } catch (Exception e) {
                Logger.error("Failed to parse DICOM date: {}", date, e);
            }
        }
        return null;
    }

    /**
     * Parses a DICOM TM (Time) string into a {@link LocalTime}.
     *
     * @param time the TM string.
     * @return the parsed {@link LocalTime}, or {@code null} on failure.
     */
    public static LocalTime getDicomTime(String time) {
        if (StringKit.hasText(time)) {
            try {
                return Format.parseTM(time);
            } catch (Exception e1) {
                Logger.error("Failed to parse DICOM time: {}", time, e1);
            }
        }
        return null;
    }

    /**
     * Combines date and time attributes from a DICOM dataset into a {@link LocalDateTime}.
     *
     * @param dcm    the DICOM dataset.
     * @param dateID the tag for the date attribute.
     * @param timeID the tag for the time attribute.
     * @return the combined {@link LocalDateTime}, or {@code null} if the date is not present.
     */
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
     * Builds a {@link Shape} from DICOM Display Shutter attributes.
     *
     * @param dcm the DICOM dataset.
     * @return the shutter shape as an {@link Area}, or {@code null} if no valid shutter is defined.
     */
    public static Area getShutterShape(Attributes dcm) {
        Area shape = null;
        String shutterShape = getStringFromDicomElement(dcm, Tag.ShutterShape);
        if (shutterShape != null) {
            if (shutterShape.contains("RECTANGULAR") || shutterShape.contains("RECTANGLE")) {
                Rectangle2D rect = new Rectangle2D.Double();
                rect.setFrameFromDiagonal(
                        dcm.getInt(Tag.ShutterLeftVerticalEdge, 0),
                        dcm.getInt(Tag.ShutterUpperHorizontalEdge, 0),
                        dcm.getInt(Tag.ShutterRightVerticalEdge, 0),
                        dcm.getInt(Tag.ShutterLowerHorizontalEdge, 0));
                if (rect.isEmpty()) {
                    Logger.error("Shutter rectangle has an empty area!");
                } else {
                    shape = new Area(rect);
                }
            }
            if (shutterShape.contains("CIRCULAR")) {
                int[] centerOfCircularShutter = dcm.getInts(Tag.CenterOfCircularShutter);
                if (centerOfCircularShutter != null && centerOfCircularShutter.length >= 2) {
                    Ellipse2D ellipse = new Ellipse2D.Double();
                    double radius = dcm.getInt(Tag.RadiusOfCircularShutter, 0);
                    ellipse.setFrameFromCenter(
                            centerOfCircularShutter[1],
                            centerOfCircularShutter[0],
                            centerOfCircularShutter[1] + radius,
                            centerOfCircularShutter[0] + radius);
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
            if (shutterShape.contains("POLYGONAL")) {
                int[] points = dcm.getInts(Tag.VerticesOfThePolygonalShutter);
                if (points != null && points.length >= 6) {
                    Polygon polygon = new Polygon();
                    for (int i = 0; i < points.length / 2; i++) {
                        polygon.addPoint(points[i * 2 + 1], points[i * 2]);
                    }
                    if (isPolygonValid(polygon)) {
                        if (shape == null) {
                            shape = new Area(polygon);
                        } else {
                            shape.intersect(new Area(polygon));
                        }
                    } else {
                        Logger.error("Shutter polygon is invalid or has an empty area!");
                    }
                }
            }
        }
        return shape;
    }

    /**
     * Validates a {@link Polygon} to ensure it represents a valid area.
     *
     * @param polygon the polygon to validate.
     * @return {@code true} if the polygon is valid.
     */
    private static boolean isPolygonValid(Polygon polygon) {
        int[] xPoints = polygon.xpoints;
        int[] yPoints = polygon.ypoints;
        double area = 0;
        for (int i = 0; i < polygon.npoints; i++) {
            area += (xPoints[i] * yPoints[(i + 1) % polygon.npoints])
                    - (xPoints[(i + 1) % polygon.npoints] * yPoints[i]);
        }
        return area != 0 && polygon.npoints > 2;
    }

    /**
     * Extracts the shutter color from DICOM attributes.
     *
     * @param dcm the DICOM dataset.
     * @return the shutter color as a {@link Color}.
     */
    public static Color getShutterColor(Attributes dcm) {
        int[] rgb = CIELab.dicomLab2rgb(dcm.getInts(Tag.ShutterPresentationColorCIELabValue));
        return ColorKit.getColor(dcm.getInt(Tag.ShutterPresentationValue, 0x0000), rgb);
    }

    /**
     * Safely closes a resource if the progress tracking in the {@link Status} object is active.
     *
     * @param dcmState  the current operation status.
     * @param closeable the resource to close.
     */
    public static void forceGettingAttributes(Status dcmState, AutoCloseable closeable) {
        ImageProgress p = dcmState.getProgress();
        if (p != null) {
            IoKit.close(closeable);
        }
    }

    /**
     * Updates a DIMSE command dataset with progress information.
     *
     * @param p                     the progress tracking object.
     * @param cmd                   the DIMSE command dataset.
     * @param ps                    the status of the current sub-operation.
     * @param numberOfSuboperations the initial total number of sub-operations.
     */
    public static void notifyProgession(ImageProgress p, Attributes cmd, ProgressStatus ps, int numberOfSuboperations) {
        if (p != null && cmd != null) {
            int c, f, r, w;
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

    /**
     * Notifies progress by updating the {@link Status} object and its associated {@link ImageProgress}.
     *
     * @param state                 the operation status object.
     * @param iuid                  the Affected SOP Instance UID.
     * @param cuid                  the Affected SOP Class UID.
     * @param status                the DICOM status code.
     * @param ps                    the status of the sub-operation.
     * @param numberOfSuboperations the initial total number of sub-operations.
     */
    public static void notifyProgession(
            Status state,
            String iuid,
            String cuid,
            int status,
            ProgressStatus ps,
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

    /**
     * Calculates the total number of sub-operations from a DIMSE command dataset.
     *
     * @param cmd the DIMSE command dataset.
     * @return the total number of sub-operations.
     */
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

    /**
     * Concatenates an array of strings with a delimiter.
     *
     * @param ss    the array of strings.
     * @param delim the delimiter character.
     * @return the concatenated string.
     */
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

    /**
     * Appends a series of objects to a StringBuilder, followed by a line separator.
     *
     * @param sb the StringBuilder.
     * @param ss the objects to append.
     * @return the modified StringBuilder.
     */
    public static StringBuilder appendLine(StringBuilder sb, Object... ss) {
        for (Object s : ss)
            sb.append(s);
        return sb.append(LINE_SEPARATOR);
    }

    /**
     * Splits a string by a delimiter and trims leading/trailing whitespace from each part.
     *
     * @param s     the string to split.
     * @param delim the delimiter character.
     * @return a single trimmed String if no delimiter is found, otherwise a String array.
     */
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

    /**
     * Splits a string by a delimiter into an array.
     *
     * @param s     the string to split.
     * @param delim the delimiter character.
     * @return an array of strings.
     */
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

    /**
     * Custom substring method that also trims whitespace from both ends.
     *
     * @param s          the source string.
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex   the ending index, exclusive.
     * @return the trimmed substring.
     */
    private static String substring(String s, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && s.charAt(beginIndex) <= ' ')
            beginIndex++;
        while (beginIndex < endIndex && s.charAt(endIndex - 1) <= ' ')
            endIndex--;
        return beginIndex < endIndex ? s.substring(beginIndex, endIndex) : "";
    }

    /**
     * Trims trailing whitespace from a string.
     *
     * @param s the string to trim.
     * @return the trimmed string.
     */
    public static String trimTrailing(String s) {
        int endIndex = s.length();
        while (endIndex > 0 && s.charAt(endIndex - 1) <= ' ')
            endIndex--;
        return s.substring(0, endIndex);
    }

    /**
     * Parses a DICOM IS (Integer String) value.
     *
     * @param s the IS string.
     * @return the parsed long value.
     */
    public static long parseIS(String s) {
        return s != null && !s.isEmpty() ? Long.parseLong(s) : 0L;
    }

    /**
     * Parses a DICOM UV (Unsigned Very Long) value.
     *
     * @param s the UV string.
     * @return the parsed long value.
     */
    public static long parseUV(String s) {
        return s != null && !s.isEmpty() ? Long.parseUnsignedLong(s) : 0L;
    }

    /**
     * Parses a DICOM DS (Decimal String) value.
     *
     * @param s the DS string.
     * @return the parsed double value.
     */
    public static double parseDS(String s) {
        return s != null && !s.isEmpty() ? Double.parseDouble(s.replace(',', '.')) : 0;
    }

    /**
     * Formats a float as a DICOM DS (Decimal String).
     *
     * @param f the float value.
     * @return the formatted string.
     */
    public static String formatDS(float f) {
        String s = Float.toString(f);
        int l = s.length();
        if (s.startsWith(".0", l - 2))
            return s.substring(0, l - 2);
        int e = s.indexOf('E', l - 5);
        return e > 0 && s.startsWith(".0", e - 2) ? cut(s, e - 2, e) : s;
    }

    /**
     * Formats a double as a DICOM DS (Decimal String), truncating to 16 characters if necessary.
     *
     * @param d the double value.
     * @return the formatted string.
     */
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

    /**
     * Helper method to cut a section out of a string.
     */
    private static String cut(String s, int begin, int end) {
        int l = s.length();
        char[] ch = new char[l - (end - begin)];
        s.getChars(0, begin, ch, 0);
        s.getChars(end, l, ch, begin);
        return new String(ch);
    }

    /**
     * Matches a string against a key, which may contain wildcards (*, ?).
     *
     * @param s                the string to test.
     * @param key              the match key with optional wildcards.
     * @param matchNullOrEmpty if {@code true}, a null or empty string will match.
     * @param ignoreCase       if {@code true}, perform a case-insensitive match.
     * @return {@code true} if the string matches the key.
     */
    public static boolean matches(String s, String key, boolean matchNullOrEmpty, boolean ignoreCase) {
        if (key == null || key.isEmpty())
            return true;

        if (s == null || s.isEmpty())
            return matchNullOrEmpty;

        return containsWildCard(key) ? compilePattern(key, ignoreCase).matcher(s).matches()
                : ignoreCase ? key.equalsIgnoreCase(s) : key.equals(s);
    }

    /**
     * Compiles a wildcard string (*, ?) into a regular expression {@link Pattern}.
     *
     * @param key        the string with wildcards.
     * @param ignoreCase if {@code true}, compile with {@link Pattern#CASE_INSENSITIVE}.
     * @return the compiled {@link Pattern}.
     */
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

    /**
     * Checks if a string contains wildcard characters ('*' or '?').
     *
     * @param s the string to check.
     * @return {@code true} if wildcards are present.
     */
    public static boolean containsWildCard(String s) {
        return (s.indexOf('*') >= 0 || s.indexOf('?') >= 0);
    }

    /**
     * Returns an empty array if the input array is null.
     *
     * @param ss the input string array.
     * @return the input array or an empty array.
     */
    public static String[] maskNull(String[] ss) {
        return maskNull(ss, Normal.EMPTY_STRING_ARRAY);
    }

    /**
     * Returns a mask value if the input object is null.
     *
     * @param <T>  the type of the object.
     * @param o    the object to check.
     * @param mask the value to return if {@code o} is null.
     * @return {@code o} or {@code mask}.
     */
    public static <T> T maskNull(T o, T mask) {
        return o == null ? mask : o;
    }

    /**
     * Returns a mask value if the input string is null or empty.
     *
     * @param s    the string to check.
     * @param mask the value to return if {@code s} is null or empty.
     * @return {@code s} or {@code mask}.
     */
    public static String maskEmpty(String s, String mask) {
        return s == null || s.isEmpty() ? mask : s;
    }

    /**
     * Truncates a string to a maximum length.
     *
     * @param s      the string to truncate.
     * @param maxlen the maximum length.
     * @return the truncated string.
     */
    public static String truncate(String s, int maxlen) {
        return s.length() > maxlen ? s.substring(0, maxlen) : s;
    }

    /**
     * Replaces all non-ASCII and non-printable characters in a string with a replacement character.
     *
     * @param s           the string to process.
     * @param replacement the character to use for replacement.
     * @return the sanitized string.
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

    /**
     * A null-safe equals method.
     *
     * @param <T> the type of the objects.
     * @param o1  the first object.
     * @param o2  the second object.
     * @return {@code true} if the objects are equal.
     */
    public static <T> boolean equals(T o1, T o2) {
        return Objects.equals(o1, o2);
    }

    /**
     * Replaces placeholders in a string with system properties or environment variables. Placeholders are in the format
     * `${property.name}` or `${env.VAR_NAME}`.
     *
     * @param s the string with placeholders.
     * @return the string with placeholders replaced.
     */
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

    /**
     * Checks if an array contains a specific object.
     *
     * @param <T> the type of the objects.
     * @param a   the array.
     * @param o   the object to find.
     * @return {@code true} if the array contains the object.
     */
    public static <T> boolean contains(T[] a, T o) {
        for (T t : a)
            if (Objects.equals(t, o))
                return true;
        return false;
    }

    /**
     * Ensures an array is not empty.
     *
     * @param <T>     the type of the array elements.
     * @param a       the array.
     * @param message the exception message if the array is empty.
     * @return the original array.
     * @throws IllegalArgumentException if the array is empty.
     */
    public static <T> T[] requireNotEmpty(T[] a, String message) {
        if (a.length == 0)
            throw new IllegalArgumentException(message);
        return a;
    }

    /**
     * Ensures a string is not empty.
     *
     * @param s       the string.
     * @param message the exception message if the string is empty.
     * @return the original string.
     * @throws IllegalArgumentException if the string is empty.
     */
    public static String requireNotEmpty(String s, String message) {
        if (s.isEmpty())
            throw new IllegalArgumentException(message);
        return s;
    }

    /**
     * Ensures an array of strings contains no empty elements.
     *
     * @param ss      the string array.
     * @param message the exception message if an element is empty.
     * @return the original array.
     */
    public static String[] requireContainsNoEmpty(String[] ss, String message) {
        for (String s : ss)
            requireNotEmpty(s, message);
        return ss;
    }

    /**
     * Determines the appropriate {@link MediaType} for a given Transfer Syntax UID.
     *
     * @param ts the Transfer Syntax UID.
     * @return the corresponding {@link MediaType}.
     * @throws IllegalArgumentException if the Transfer Syntax is not supported.
     */
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

    /**
     * Determines the appropriate Transfer Syntax UID from a {@link MediaType}.
     *
     * @param bulkdataMediaType the media type.
     * @return the corresponding Transfer Syntax UID.
     */
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

    /**
     * Determines the appropriate SOP Class UID from a {@link MediaType}.
     *
     * @param bulkdataMediaType the media type.
     * @return the corresponding SOP Class UID, or {@code null}.
     */
    public static String sopClassOf(MediaType bulkdataMediaType) {
        String type = bulkdataMediaType.getType().toLowerCase();
        return type.equals("image") ? UID.SecondaryCaptureImageStorage.uid
                : type.equals("video") ? UID.VideoPhotographicImageStorage.uid
                        : MediaType.equalsIgnoreParameters(bulkdataMediaType, MediaType.APPLICATION_PDF_TYPE)
                                ? UID.EncapsulatedPDFStorage.uid
                                : MediaType.equalsIgnoreParameters(bulkdataMediaType, MediaType.APPLICATION_XML_TYPE)
                                        ? UID.EncapsulatedCDAStorage.uid
                                        : MediaType.isSTLType(bulkdataMediaType) ? UID.EncapsulatedSTLStorage.uid
                                                : MediaType.equalsIgnoreParameters(
                                                        bulkdataMediaType,
                                                        MediaType.MODEL_OBJ_TYPE)
                                                                ? UID.EncapsulatedOBJStorage.uid
                                                                : MediaType.equalsIgnoreParameters(
                                                                        bulkdataMediaType,
                                                                        MediaType.MODEL_MTL_TYPE)
                                                                                ? UID.EncapsulatedMTLStorage.uid
                                                                                : null;
    }

    /**
     * Extracts the Transfer Syntax UID from an "application/dicom" media type.
     *
     * @param type the media type.
     * @return the Transfer Syntax UID, or {@code null}.
     */
    public static String getTransferSyntax(MediaType type) {
        return type != null && MediaType.equalsIgnoreParameters(MediaType.APPLICATION_DICOM_TYPE, type)
                ? type.getParameters().get("transfer-syntax")
                : null;
    }

    /**
     * Creates an "application/dicom" media type with a specified Transfer Syntax.
     *
     * @param tsuid the Transfer Syntax UID.
     * @return the constructed {@link MediaType}.
     */
    public static MediaType applicationDicomWithTransferSyntax(String tsuid) {
        return new MediaType("application", "dicom", Collections.singletonMap("transfer-syntax", tsuid));
    }

    /**
     * Validates a DICOM file against a given Information Object Definition (IOD).
     *
     * @param file the DICOM file to validate.
     * @param iod  the IOD to validate against.
     */
    public void validate(File file, IOD iod) {
        if (iod == null)
            throw new IllegalStateException("IOD not initialized");
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

}
