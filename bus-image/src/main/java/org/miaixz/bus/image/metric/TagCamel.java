/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.metric;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.Logger;

import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 应用程序使用的通用DICOM标记
 * 这些标记的作用是提供公共标记(DICOM和non DICOM)的高级可访问性
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TagCamel implements Serializable {

    public static final String NO_VALUE = "UNKNOWN";
    public static final TagCamel UnknownTag = new TagCamel(0, "UnknownTag", "Unknown Tag", TagType.STRING);
    public static final TagCamel Group = new TagCamel("Group", "Group", TagType.STRING);
    public static final TagCamel PatientPseudoUID = new TagCamel("PatientPseudoUID",
            "Patient UID", TagType.STRING);
    public static final TagCamel SeriesLoading =
            new TagCamel("SeriesLoading", "Loading", TagType.INTEGER);
    public static final TagCamel Thumbnail =
            new TagCamel("Thumbnail", "Thumbnail", TagType.THUMBNAIL);
    public static final TagCamel ThumbnailPath = new TagCamel("ThumbnailPath", TagType.STRING);
    public static final TagCamel ExplorerModel =
            new TagCamel("ExplorerModel", "Explorer Model", TagType.OBJECT);
    public static final TagCamel PresentationModel = new TagCamel("PesentationModel", TagType.OBJECT);
    public static final TagCamel PresentationModelBirary = new TagCamel("PesentationModelBinary", TagType.BYTE);
    public static final TagCamel SplitSeriesNumber =
            new TagCamel("SplitSeriesNumber", "Split Number", TagType.INTEGER);
    public static final TagCamel SeriesSelected =
            new TagCamel("SeriesSelected", "Selected", TagType.BOOLEAN);
    public static final TagCamel SeriesOpen =
            new TagCamel("SeriesOpen", "Open", TagType.BOOLEAN);
    public static final TagCamel SeriesFocused = new TagCamel("SeriesFocused", TagType.BOOLEAN);
    public static final TagCamel ImageWidth =
            new TagCamel("ImageWidth", "=Image Width", TagType.INTEGER);
    public static final TagCamel ImageHeight =
            new TagCamel("ImageHeight", "Image Height", TagType.INTEGER);
    public static final TagCamel ImageDepth =
            new TagCamel("ImageDepth", "Image Depth", TagType.INTEGER);
    public static final TagCamel ImageOrientationPlane =
            new TagCamel("ImageOrientationPlane", "Image Orientation", TagType.STRING);
    public static final TagCamel ImageBitsPerPixel =
            new TagCamel("ImageBitsPerPixel", "Image Bits Per Pixel", TagType.INTEGER);
    public static final TagCamel ImageCache = new TagCamel("ImageCache", TagType.BOOLEAN);
    public static final TagCamel ShutterFinalShape = new TagCamel("ShutterFinalShape", TagType.OBJECT);
    public static final TagCamel ShutterRGBColor = new TagCamel("ShutterRGBColor", TagType.COLOR);
    public static final TagCamel ShutterPSValue = new TagCamel("ShutterPSValue", TagType.INTEGER);
    public static final TagCamel OverlayBitMask = new TagCamel("OverlayBitMask", TagType.INTEGER);
    public static final TagCamel OverlayBurninDataPath = new TagCamel("OverlayBurninDataPath", TagType.STRING);
    public static final TagCamel HasOverlay = new TagCamel("HasOverlay", TagType.BOOLEAN);
    public static final TagCamel ObjectToSave = new TagCamel("ObjectToSave", TagType.BOOLEAN);
    public static final TagCamel WadoCompressionRate = new TagCamel("WadoCompressionRate", TagType.INTEGER);
    public static final TagCamel WadoTransferSyntaxUID = new TagCamel("WadoTransferSyntaxUID", TagType.STRING);
    public static final TagCamel DirectDownloadFile = new TagCamel("DirectDownloadFile", TagType.STRING);
    public static final TagCamel DirectDownloadThumbnail = new TagCamel("DirectDownloadThumbnail", TagType.STRING);
    public static final TagCamel ReadFromDicomdir = new TagCamel("ReadFromDicomdir", TagType.BOOLEAN);
    public static final TagCamel WadoParameters = new TagCamel("WadoParameters", TagType.OBJECT);
    public static final TagCamel WadoInstanceReferenceList = new TagCamel("WadoInstanceReferenceList", TagType.LIST);
    public static final TagCamel DicomSpecialElementList = new TagCamel("DicomSpecialElementList", TagType.LIST);
    public static final TagCamel SlicePosition = new TagCamel("SlicePosition", TagType.DOUBLE, 3, 3);
    public static final TagCamel SuvFactor = new TagCamel("SUVFactor", TagType.DOUBLE);
    public static final TagCamel RootElement = new TagCamel("RootElement", TagType.STRING);
    public static final TagCamel FilePath = new TagCamel("FilePath", TagType.STRING);
    public static final TagCamel FileName = new TagCamel("FileName", TagType.STRING);
    public static final TagCamel CurrentFolder =
            new TagCamel("CurrentFolder", "Current Folder", TagType.STRING);
    public static final TagCamel Checked = new TagCamel("Checked", TagType.BOOLEAN);
    public static final TagCamel SubseriesInstanceUID = new TagCamel("SubseriesInstanceUID", TagType.STRING);
    // One or more Items shall be included in this sequence
    public static final TagCamel VOILUTsExplanation = new TagCamel("VOILUTsExplanation", TagType.STRING, 1, Integer.MAX_VALUE);
    public static final TagCamel VOILUTsData = new TagCamel("VOILUTsData", TagType.OBJECT);
    // Only a single Item shall be included in this sequence
    public static final TagCamel ModalityLUTExplanation = new TagCamel("ModalityLUTExplanation", TagType.STRING);
    public static final TagCamel ModalityLUTType = new TagCamel("ModalityLUTType", TagType.STRING);
    public static final TagCamel ModalityLUTData = new TagCamel("ModalityLUTData", TagType.OBJECT);
    // Only a single Item shall be included in this sequence
    public static final TagCamel PRLUTsExplanation = new TagCamel("PRLUTsExplanation", TagType.STRING);
    public static final TagCamel PRLUTsData = new TagCamel("PRLUTsData", TagType.OBJECT);
    public static final TagCamel MonoChrome = new TagCamel("MonoChrome", TagType.BOOLEAN);
    protected static final Map<String, TagCamel> tags = Collections.synchronizedMap(new HashMap<>());
    private static final long serialVersionUID = 1L;
    private static final AtomicInteger idCounter = new AtomicInteger(Integer.MAX_VALUE);

    static {
        addTag(ImageBitsPerPixel);
        addTag(ImageCache);
        addTag(ImageDepth);
        addTag(ImageHeight);
        addTag(ImageOrientationPlane);
        addTag(ImageWidth);
        addTag(SeriesFocused);
        addTag(SeriesLoading);
        addTag(SeriesOpen);
        addTag(SeriesSelected);
        addTag(SlicePosition);
        addTag(SuvFactor);
        addTag(DirectDownloadFile);
        addTag(DirectDownloadThumbnail);
        addTag(RootElement);
        addTag(FileName);
        addTag(FilePath);
        addTag(CurrentFolder);

        // DICOM
        addTag(SubseriesInstanceUID);
        addTag(VOILUTsExplanation);
        addTag(VOILUTsData);
        addTag(ModalityLUTExplanation);
        addTag(ModalityLUTType);
        addTag(ModalityLUTData);
        addTag(PRLUTsExplanation);
        addTag(PRLUTsData);
        addTag(MonoChrome);
    }

    protected final int id;
    protected final String keyword;
    protected final String displayedName;
    protected final TagType type;
    protected final int vmMin;
    protected final int vmMax;
    protected final transient Object defaultValue;
    protected int anonymizationType;

    public TagCamel(int id, String keyword, String displayedName, TagType type, int vmMin, int vmMax, Object defaultValue) {
        this.id = id;
        this.keyword = keyword;
        this.displayedName = displayedName;
        this.type = null == type ? TagType.STRING : type;
        this.anonymizationType = 0;
        this.defaultValue = defaultValue;
        this.vmMax = vmMax < 1 ? 1 : vmMax;
        this.vmMin = vmMin < 1 ? 1 : vmMin;

        if (!isTypeCompliant(defaultValue)) {
            throw new IllegalArgumentException("defaultValue is not compliant to the tag type");
        }
    }

    public TagCamel(int id, String keyword, TagType type, int vmMin, int vmMax) {
        this(id, keyword, null, type, vmMin, vmMax, null);
    }

    public TagCamel(int id, String keyword, String displayedName, TagType type) {
        this(id, keyword, displayedName, type, 1, 1, null);
    }

    public TagCamel(int id, String keyword, TagType type) {
        this(id, keyword, null, type, 1, 1, null);
    }

    public TagCamel(String name, TagType type, int vmMin, int vmMax) {
        this(idCounter.getAndDecrement(), name, null, type, vmMin, vmMax, null);
    }

    public TagCamel(String keyword, String displayedName, TagType type) {
        this(idCounter.getAndDecrement(), keyword, displayedName, type, 1, 1, null);
    }

    public TagCamel(String keyword, TagType type) {
        this(idCounter.getAndDecrement(), keyword, null, type);
    }

    public static int getValueMultiplicity(Object value) {
        if (null == value) {
            return 0;
        }

        if (value.getClass().isArray()) {
            return Array.getLength(value);
        }
        return 1;
    }

    public static Object getValueFromIndex(Object value, int index) {
        if (null == value || !value.getClass().isArray()) {
            return value;
        }

        if (index >= 0 && index < Array.getLength(value)) {
            return Array.get(value, index);
        }
        return null;
    }

    protected static String getFormattedText(Object value, String format) {
        if (null == value) {
            return Normal.EMPTY;
        }

        String text;

        if (value instanceof String) {
            text = (String) value;
        } else if (value instanceof String[]) {
            text = Arrays.asList((String[]) value).stream().collect(Collectors.joining(Symbol.BACKSLASH));
        } else if (value instanceof TemporalAccessor) {
            text = TagValue.formatDateTime((TemporalAccessor) value);
        } else if (value instanceof TemporalAccessor[]) {
            text = Stream.of((TemporalAccessor[]) value).map(TagValue::formatDateTime).collect(Collectors.joining(", "));
        } else if (value instanceof float[]) {
            float[] array = (float[]) value;
            text = IntStream.range(0, array.length).mapToObj(i -> String.valueOf(array[i]))
                    .collect(Collectors.joining(", "));
        } else if (value instanceof double[]) {
            text = DoubleStream.of((double[]) value).mapToObj(String::valueOf).collect(Collectors.joining(", "));
        } else if (value instanceof int[]) {
            text = IntStream.of((int[]) value).mapToObj(String::valueOf).collect(Collectors.joining(", "));
        } else {
            text = value.toString();
        }

        if (StringKit.hasText(format) && !"$V".equals(format.trim())) {
            return formatValue(text, value instanceof Float || value instanceof Double, format);
        }

        return null == text ? Normal.EMPTY : text;
    }

    protected static String formatValue(String value, boolean decimal, String format) {
        String text = value;
        int index = format.indexOf("$V");
        int fmLength = 2;
        if (index != -1) {
            boolean suffix = format.length() > index + fmLength;
            // If the value ($V) is followed by ':' that means a number formatter is used
            if (suffix && format.charAt(index + fmLength) == Symbol.C_COLON) {
                fmLength++;
                if (format.charAt(index + fmLength) == 'f' && decimal) {
                    fmLength++;
                    String pattern = getPattern(index + fmLength, format);
                    if (null != pattern) {
                        fmLength += pattern.length() + 2;
                        try {
                            text = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.getDefault())).format(Double.parseDouble(text));
                        } catch (NumberFormatException e) {
                            Logger.warn("Cannot apply pattern to decimal value", e);
                        }
                    }
                } else if (format.charAt(index + fmLength) == 'l') {
                    fmLength++;
                    String pattern = getPattern(index + fmLength, format);
                    if (null != pattern) {
                        fmLength += pattern.length() + 2;
                        try {
                            int limit = Integer.parseInt(pattern);
                            int size = text.length();
                            if (size > limit) {
                                text = text.substring(0, limit) + "...";
                            }
                        } catch (NumberFormatException e) {
                            Logger.warn("Cannot apply pattern to decimal value", e);
                        }
                    }
                }
            }
            text = format.substring(0, index) + text;
            if (format.length() > index + fmLength) {
                text += format.substring(index + fmLength);
            }
        }
        return text;
    }

    private static String getPattern(int startIndex, String format) {
        int beginIndex = format.indexOf(Symbol.C_DOLLAR, startIndex);
        int endIndex = format.indexOf(Symbol.C_DOLLAR, startIndex + 2);
        if (beginIndex == -1 || endIndex == -1) {
            return null;
        }
        return format.substring(beginIndex + 1, endIndex);
    }

    public static void addTag(TagCamel tag) {
        if (null != tag) {
            tags.put(tag.getKeyword(), tag);
        }
    }

    public static TagCamel get(String keyword) {
        return tags.get(keyword);
    }

    public static <T> T getTagValue(Readable tagable, TagCamel tag, Class<T> type) {
        if (null != tagable && null != tag) {
            try {
                return type.cast(tagable.getTagValue(tag));
            } catch (ClassCastException e) {
                Logger.error("Cannot cast the value of \"{}\" into {}", tag.getKeyword(), type, e);
            }
        }
        return null;
    }

    public static String splitCamelCaseString(String s) {
        StringBuilder builder = new StringBuilder();
        for (String w : s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            builder.append(w);
            builder.append(Symbol.C_SPACE);
        }
        return builder.toString().trim();
    }

    public int getId() {
        return id;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDisplayedName() {
        if (null == displayedName) {
            return splitCamelCaseString(getKeyword());
        }
        return displayedName;
    }

    public TagType getType() {
        return type;
    }

    public int getValueMultiplicity() {
        return vmMax;
    }

    public boolean isTypeCompliant(Object value) {
        if (null == value) {
            return true;
        }
        Object clazz;
        if (value.getClass().isArray()) {
            if (vmMax == 1) {
                return false;
            }
            clazz = value.getClass().getComponentType();

            // Check the number of values
            int vmValue = Array.getLength(value);
            if (vmMax != Integer.MAX_VALUE && vmMax != vmValue) {
                return false;
            } else {
                // Fix in case of array type
                return type.getClazz().isAssignableFrom((Class<?>) clazz);
            }
        } else {
            clazz = value;
        }

        return type.isInstanceOf(clazz);
    }

    @Override
    public String toString() {
        return getDisplayedName();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (null == object) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        TagCamel other = (TagCamel) object;
        if (id != other.id) {
            return false;
        }
        if (null == keyword) {
            return null == other.keyword;
        } else return keyword.equals(other.keyword);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((null == keyword) ? 0 : keyword.hashCode());
        return result;
    }

    public void readValue(Object data, Tagable tagable) {
        tagable.setTagNoNull(this, getValue(data));
    }

    public Object getValue(Object data) {
        Object value = null;
        if (data instanceof XMLStreamReader) {
            XMLStreamReader xmler = (XMLStreamReader) data;

            if (isStringFamilyType()) {
                value = vmMax > 1 ? TagValue.getStringArrayTagAttribute(xmler, keyword, (String[]) defaultValue)
                        : TagValue.getTagAttribute(xmler, keyword, (String) defaultValue);
            } else if (TagType.DATE.equals(type) || TagType.TIME.equals(type) || TagType.DATETIME.equals(type)) {
                value = vmMax > 1 ? TagValue.getDatesFromElement(xmler, keyword, type, (TemporalAccessor[]) defaultValue)
                        : TagValue.getDateFromElement(xmler, keyword, type, (TemporalAccessor) defaultValue);
            } else if (TagType.INTEGER.equals(type)) {
                value = vmMax > 1 ? TagValue.getIntArrayTagAttribute(xmler, keyword, (int[]) defaultValue)
                        : TagValue.getIntegerTagAttribute(xmler, keyword, (Integer) defaultValue);
            } else if (TagType.FLOAT.equals(type)) {
                value = vmMax > 1 ? TagValue.getFloatArrayTagAttribute(xmler, keyword, (float[]) defaultValue)
                        : TagValue.getFloatTagAttribute(xmler, keyword, (Float) defaultValue);
            } else if (TagType.DOUBLE.equals(type)) {
                value = vmMax > 1 ? TagValue.getDoubleArrayTagAttribute(xmler, keyword, (double[]) defaultValue)
                        : TagValue.getDoubleTagAttribute(xmler, keyword, (Double) defaultValue);
            } else {
                value = vmMax > 1 ? TagValue.getStringArrayTagAttribute(xmler, keyword, (String[]) defaultValue)
                        : TagValue.getTagAttribute(xmler, keyword, (String) defaultValue);
            }
        }
        return value;
    }

    public boolean isStringFamilyType() {
        return TagType.STRING.equals(type) || TagType.TEXT.equals(type) || TagType.URI.equals(type);
    }

    public synchronized int getAnonymizationType() {
        return anonymizationType;
    }

    public synchronized void setAnonymizationType(int anonymizationType) {
        this.anonymizationType = anonymizationType;
    }

    public String getFormattedTagValue(Object value, String format) {
        return getFormattedText(value, format);
    }

    public enum TagType {
        STRING(String.class), TEXT(String.class), URI(String.class), DATE(LocalDate.class),
        DATETIME(LocalDateTime.class), TIME(LocalTime.class), BOOLEAN(Boolean.class), BYTE(Byte.class),
        INTEGER(Integer.class), FLOAT(Float.class), DOUBLE(Double.class), COLOR(Color.class),
        THUMBNAIL(org.miaixz.bus.image.metric.Thumbnail.class), LIST(List.class), OBJECT(Object.class), DICOM_DATE(LocalDate.class),
        DICOM_DATETIME(LocalDateTime.class), DICOM_TIME(LocalTime.class), DICOM_PERIOD(String.class),
        DICOM_PERSON_NAME(String.class), DICOM_SEQUENCE(Object.class), DICOM_SEX(String.class);

        private final Class<?> clazz;

        TagType(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public boolean isInstanceOf(Object value) {
            if (null == value) {
                return true;
            }
            return clazz.isAssignableFrom(value.getClass());
        }
    }

}
