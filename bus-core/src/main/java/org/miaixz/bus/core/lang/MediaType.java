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
package org.miaixz.bus.core.lang;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.xyz.MapKit;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP 媒体类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class MediaType {

    /**
     * The media type {@code charset} parameter name.
     */
    public static final String CHARSET_PARAMETER = "charset";

    /**
     * The value of a type or subtype {@value #MEDIA_TYPE_WILDCARD}.
     */
    public static final String MEDIA_TYPE_WILDCARD = Symbol.STAR;

    /**
     * A {@code String} constant representing {@value #WILDCARD} media type .
     */
    public static final String WILDCARD = "*/*";

    /**
     * A {@link MediaType} constant representing {@value #WILDCARD} media type.
     */
    public static final MediaType WILDCARD_TYPE = new MediaType();

    /**
     * A {@code String} constant representing {@value #APPLICATION_XML} media type.
     */
    public static final String APPLICATION_XML = "application/xml";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_XML} media type.
     */
    public static final MediaType APPLICATION_XML_TYPE = new MediaType("application", "xml");

    /**
     * A {@code String} constant representing {@value #APPLICATION_ATOM_XML} media type.
     */
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_ATOM_XML} media type.
     */
    public static final MediaType APPLICATION_ATOM_XML_TYPE = new MediaType("application", "atom+xml");

    /**
     * A {@code String} constant representing {@value #APPLICATION_XHTML_XML} media type.
     */
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_XHTML_XML} media type.
     */
    public static final MediaType APPLICATION_XHTML_XML_TYPE = new MediaType("application", "xhtml+xml");

    /**
     * A {@code String} constant representing {@value #APPLICATION_SVG_XML} media type.
     */
    public static final String APPLICATION_SVG_XML = "application/svg+xml";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_SVG_XML} media type.
     */
    public static final MediaType APPLICATION_SVG_XML_TYPE = new MediaType("application", "svg+xml");

    /**
     * A {@code String} constant representing {@value #APPLICATION_JSON} media type.
     */
    public static final String APPLICATION_JSON = "application/json";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_JSON} media type.
     */
    public static final MediaType APPLICATION_JSON_TYPE = new MediaType("application", "json");

    /**
     * A {@code String} constant representing {@value #APPLICATION_FORM_URLENCODED} media type.
     */
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_FORM_URLENCODED} media type.
     */
    public static final MediaType APPLICATION_FORM_URLENCODED_TYPE = new MediaType("application", "x-www-form-urlencoded");

    /**
     * A {@code String} constant representing {@value #MULTIPART_FORM_DATA} media type.
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_FORM_DATA} media type.
     */
    public static final MediaType MULTIPART_FORM_DATA_TYPE = new MediaType("multipart", "form-data");

    /**
     * A {@code String} constant representing {@value #APPLICATION_OCTET_STREAM} media type.
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_OCTET_STREAM} media type.
     */
    public static final MediaType APPLICATION_OCTET_STREAM_TYPE = new MediaType("application", "octet-stream");

    /**
     * A {@code String} constant representing {@value #TEXT_PLAIN} media type.
     */
    public static final String TEXT_PLAIN = "text/plain";
    /**
     * A {@link MediaType} constant representing {@value #TEXT_PLAIN} media type.
     */
    public static final MediaType TEXT_PLAIN_TYPE = new MediaType("text", "plain");

    /**
     * A {@code String} constant representing {@value #TEXT_XML} media type.
     */
    public static final String TEXT_XML = "text/xml";
    /**
     * A {@link MediaType} constant representing {@value #TEXT_XML} media type.
     */
    public static final MediaType TEXT_XML_TYPE = new MediaType("text", "xml");

    /**
     * A {@code String} constant representing {@value #TEXT_HTML} media type.
     */
    public static final String TEXT_HTML = "text/html";
    /**
     * A {@link MediaType} constant representing {@value #TEXT_HTML} media type.
     */
    public static final MediaType TEXT_HTML_TYPE = new MediaType("text", "html");

    /**
     * A {@code String} constant representing {@value #SERVER_SENT_EVENTS} media type.
     */
    public static final String SERVER_SENT_EVENTS = "text/event-stream";
    /**
     * A {@link MediaType} constant representing {@value #SERVER_SENT_EVENTS} media type.
     */
    public static final MediaType SERVER_SENT_EVENTS_TYPE = new MediaType("text", "event-stream");

    /**
     * {@link String} representation of {@value #APPLICATION_JSON_PATCH_JSON} media type.
     */
    public static final String APPLICATION_JSON_PATCH_JSON = "application/json-patch+json";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_JSON_PATCH_JSON} media type.
     */
    public static final MediaType APPLICATION_JSON_PATCH_JSON_TYPE = new MediaType("application", "json-patch+json");

    /**
     * A {@code String} constant representing {@value #APPLICATION_SOAP_XML} media type.
     */
    public static final String APPLICATION_SOAP_XML = "application/soap+xml";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_SOAP_XML} media type.
     */
    public static final MediaType APPLICATION_SOAP_XML_TYPE = new MediaType("application", "soap+xml");

    /**
     * A {@code String} constant representing {@value #APPLICATION_DICOM} media type.
     */
    public final static String APPLICATION_DICOM = "application/dicom";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_DICOM} media type.
     */
    public final static MediaType APPLICATION_DICOM_TYPE = new MediaType("application", "dicom");

    /**
     * A {@code String} constant representing {@value #APPLICATION_DICOM_XML} media type.
     */
    public final static String APPLICATION_DICOM_XML = "application/dicom+xml";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_DICOM_XML} media type.
     */
    public final static MediaType APPLICATION_DICOM_XML_TYPE = new MediaType("application", "dicom+xml");

    /**
     * A {@code String} constant representing {@value #APPLICATION_DICOM_JSON} media type.
     */
    public final static String APPLICATION_DICOM_JSON = "application/dicom+json";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_DICOM_JSON} media type.
     */
    public final static MediaType APPLICATION_DICOM_JSON_TYPE = new MediaType("application", "dicom+json");

    /**
     * A {@code String} constant representing {@value #IMAGE_WILDCARD} media type.
     */
    public final static String IMAGE_WILDCARD = "image/*";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_WILDCARD} media type.
     */
    public final static MediaType IMAGE_WILDCARD_TYPE = new MediaType("image", "*");

    /**
     * A {@code String} constant representing {@value #IMAGE_GIF} media type.
     */
    public final static String IMAGE_GIF = "image/gif";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_GIF} media type.
     */
    public final static MediaType IMAGE_GIF_TYPE = new MediaType("image", "gif");

    /**
     * A {@code String} constant representing {@value #IMAGE_PNG} media type.
     */
    public final static String IMAGE_PNG = "image/png";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_PNG} media type.
     */
    public final static MediaType IMAGE_PNG_TYPE = new MediaType("image", "png");

    /**
     * A {@code String} constant representing {@value #IMAGE_JPEG} media type.
     */
    public final static String IMAGE_JPEG = "image/jpeg";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_JPEG} media type.
     */
    public final static MediaType IMAGE_JPEG_TYPE = new MediaType("image", "jpeg");

    /**
     * A {@code String} constant representing {@value #IMAGE_JLS} media type.
     */
    public final static String IMAGE_JLS = "image/jls";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_JLS} media type.
     */
    public final static MediaType IMAGE_JLS_TYPE = new MediaType("image", "jls");

    /**
     * A {@code String} constant representing {@value #IMAGE_JP2} media type.
     */
    public final static String IMAGE_JP2 = "image/jp2";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_JP2} media type.
     */
    public final static MediaType IMAGE_JP2_TYPE = new MediaType("image", "jp2");

    /**
     * A {@code String} constant representing {@value #IMAGE_J2C} media type.
     */
    public final static String IMAGE_J2C = "image/j2c";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_J2C} media type.
     */
    public final static MediaType IMAGE_J2C_TYPE = new MediaType("image", "j2c");

    /**
     * A {@code String} constant representing {@value #IMAGE_JPX} media type.
     */
    public final static String IMAGE_JPX = "image/jpx";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_JPX} media type.
     */
    public final static MediaType IMAGE_JPX_TYPE = new MediaType("image", "jpx");

    /**
     * A {@code String} constant representing {@value #IMAGE_JPH} media type.
     */
    public final static String IMAGE_JPH = "image/jph";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_JPH} media type.
     */
    public final static MediaType IMAGE_JPH_TYPE = new MediaType("image", "jph");

    /**
     * A {@code String} constant representing {@value #IMAGE_JPHC} media type.
     */
    public final static String IMAGE_JPHC = "image/jphc";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_JPHC} media type.
     */
    public final static MediaType IMAGE_JPHC_TYPE = new MediaType("image", "jphc");

    /**
     * A {@code String} constant representing {@value #IMAGE_DICOM_RLE} media type.
     */
    public final static String IMAGE_DICOM_RLE = "image/dicom-rle";
    /**
     * A {@link MediaType} constant representing {@value #IMAGE_DICOM_RLE} media type.
     */
    public final static MediaType IMAGE_DICOM_RLE_TYPE = new MediaType("image", "dicom-rle");

    /**
     * A {@code String} constant representing {@value #VIDEO_WILDCARD} media type.
     */
    public final static String VIDEO_WILDCARD = "video/*";
    /**
     * A {@link MediaType} constant representing {@value #VIDEO_WILDCARD} media type.
     */
    public final static MediaType VIDEO_WILDCARD_TYPE = new MediaType("video", "*");

    /**
     * A {@code String} constant representing {@value #VIDEO_MPEG} media type.
     */
    public final static String VIDEO_MPEG = "video/mpeg";
    /**
     * A {@link MediaType} constant representing {@value #VIDEO_MPEG} media type.
     */
    public final static MediaType VIDEO_MPEG_TYPE = new MediaType("video", "mpeg");

    /**
     * A {@code String} constant representing {@value #VIDEO_MP4} media type.
     */
    public final static String VIDEO_MP4 = "video/mp4";
    /**
     * A {@link MediaType} constant representing {@value #VIDEO_MP4} media type.
     */
    public final static MediaType VIDEO_MP4_TYPE = new MediaType("video", "mp4");

    /**
     * A {@code String} constant representing {@value #VIDEO_QUICKTIME} media type.
     */
    public final static String VIDEO_QUICKTIME = "video/quicktime";
    /**
     * A {@link MediaType} constant representing {@value #VIDEO_QUICKTIME} media type.
     */
    public final static MediaType VIDEO_QUICKTIME_TYPE = new MediaType("video", "quicktime");

    /**
     * A {@code String} constant representing {@value #APPLICATION_PDF} media type.
     */
    public final static String APPLICATION_PDF = "application/pdf";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_PDF} media type.
     */
    public final static MediaType APPLICATION_PDF_TYPE = new MediaType("application", "pdf");

    /**
     * A {@code String} constant representing {@value #TEXT_RTF} media type.
     */
    public final static String TEXT_RTF = "text/rtf";
    /**
     * A {@link MediaType} constant representing {@value #TEXT_RTF} media type.
     */
    public final static MediaType TEXT_RTF_TYPE = new MediaType("text", "rtf");

    /**
     * A {@code String} constant representing {@value #TEXT_CSV} media type.
     */
    public final static String TEXT_CSV = "text/csv";
    /**
     * A {@link MediaType} constant representing {@value #TEXT_CSV} media type.
     */
    public final static MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");

    /**
     * A {@code String} constant representing {@value #TEXT_CSV_UTF8} media type.
     */
    public final static String TEXT_CSV_UTF8 = "text/csv;charset=utf-8";
    /**
     * A {@link MediaType} constant representing {@value #TEXT_CSV_UTF8} media type.
     */
    public final static MediaType TEXT_CSV_UTF8_TYPE = new MediaType("text", "csv", "utf-8");

    /**
     * A {@code String} constant representing {@value #APPLICATION_ZIP} media type.
     */
    public final static String APPLICATION_ZIP = "application/zip";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_ZIP} media type.
     */
    public final static MediaType APPLICATION_ZIP_TYPE = new MediaType("application", "zip");

    /**
     * A {@code String} constant representing {@value #MULTIPART_RELATED} media type.
     */
    public final static String MULTIPART_RELATED = "multipart/related";
    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_RELATED} media type.
     */
    public final static MediaType MULTIPART_RELATED_TYPE = new MediaType("multipart", "related");

    /**
     * A {@code String} constant representing {@value #MULTIPART_RELATED_APPLICATION_DICOM} media type.
     */
    public final static String MULTIPART_RELATED_APPLICATION_DICOM = "multipart/related;type=\"application/dicom\"";
    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_RELATED_APPLICATION_DICOM} media type.
     */
    public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM_TYPE = new MediaType("multipart", "related", Collections.singletonMap("type", APPLICATION_DICOM));

    /**
     * A {@code String} constant representing {@value #MULTIPART_RELATED_APPLICATION_DICOM_XML} media type.
     */
    public final static String MULTIPART_RELATED_APPLICATION_DICOM_XML = "multipart/related;type=\"application/dicom+xml\"";
    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_RELATED_APPLICATION_DICOM_XML} media type.
     */
    public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM_XML_TYPE = new MediaType("multipart", "related", Collections.singletonMap("type", APPLICATION_DICOM_XML));

    /**
     * A {@code String} constant representing {@value #MODEL_STL} media type.
     */
    public final static String MODEL_STL = "model/stl";
    /**
     * A {@link MediaType} constant representing {@value #MODEL_STL} media type.
     */
    public final static MediaType MODEL_STL_TYPE = new MediaType("model", "stl");

    /**
     * A {@code String} constant representing {@value #MODEL_X_STL_BINARY} media type.
     */
    public final static String MODEL_X_STL_BINARY = "model/x.stl-binary";
    /**
     * A {@link MediaType} constant representing {@value #MODEL_X_STL_BINARY} media type.
     */
    public final static MediaType MODEL_X_STL_BINARY_TYPE = new MediaType("model", "x.stl-binary");

    /**
     * A {@code String} constant representing {@value #APPLICATION_SLA} media type.
     */
    public final static String APPLICATION_SLA = "application/sla";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_SLA} media type.
     */
    public final static MediaType APPLICATION_SLA_TYPE = new MediaType("application", "sla");

    /**
     * A {@code String} constant representing {@value #MODEL_OBJ} media type.
     */
    public final static String MODEL_OBJ = "model/obj";
    /**
     * A {@link MediaType} constant representing {@value #MODEL_OBJ} media type.
     */
    public final static MediaType MODEL_OBJ_TYPE = new MediaType("model", "obj");

    /**
     * A {@code String} constant representing {@value #MODEL_MTL} media type.
     */
    public final static String MODEL_MTL = "model/mtl";
    /**
     * A {@link MediaType} constant representing {@value #MODEL_MTL} media type.
     */
    public final static MediaType MODEL_MTL_TYPE = new MediaType("model", "mtl");

    /**
     * A {@code String} constant representing {@value #APPLICATION_VND_GENOZIP} media type.
     */
    public final static String APPLICATION_VND_GENOZIP = "application/vnd.genozip";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_VND_GENOZIP} media type.
     */
    public final static MediaType APPLICATION_VND_GENOZIP_TYPE = new MediaType("application", "vnd.genozip");

    /**
     * A {@code String} constant representing {@value #APPLICATION_X_BZIP2} media type.
     */
    public final static String APPLICATION_X_BZIP2 = "application/x-bzip2";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_X_BZIP2} media type.
     */
    public final static MediaType APPLICATION_X_BZIP2_TYPE = new MediaType("application", "x-bzip2");
    /**
     * A {@code String} constant representing {@value #APPLICATION_PRS_VCFBZIP} media type.
     */
    public final static String APPLICATION_PRS_VCFBZIP = "application/prs.vcfbzip";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_PRS_VCFBZIP} media type.
     */
    public final static MediaType APPLICATION_PRS_VCFBZIP_TYPE = new MediaType("application", "prs.vcfbzip");

    /**
     * A {@code String} constant representing {@value #APPLICATION_PRS_VCFBZIP2} media type.
     */
    public final static String APPLICATION_PRS_VCFBZIP2 = "application/prs.vcfbzip2";
    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_PRS_VCFBZIP2} media type.
     */
    public final static MediaType APPLICATION_PRS_VCFBZIP2_TYPE = new MediaType("application", "prs.vcfbzip2");

    public static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
    public static final String QUOTED = "\"([^\"]*)\"";
    public static final Pattern TYPE_SUBTYPE = Pattern.compile(TOKEN + Symbol.SLASH + TOKEN);
    public static final Pattern PARAMETER = Pattern.compile(";\\s*(?:" + TOKEN + "=(?:" + TOKEN + Symbol.OR + QUOTED + "))?");

    public final String type;
    public final String subtype;
    public final String charset;
    public final String mediaType;
    public Map<String, String> parameters;

    public MediaType() {
        this(null, MEDIA_TYPE_WILDCARD, MEDIA_TYPE_WILDCARD, null, null);
    }

    public MediaType(String mediaType) {
        this(mediaType, MEDIA_TYPE_WILDCARD, MEDIA_TYPE_WILDCARD, null, null);
    }

    public MediaType(String type, String subtype) {
        this(null, type, subtype, null, null);
    }

    public MediaType(String type, String subtype, String charset) {
        this(null, type, subtype, charset, null);
    }

    public MediaType(String mediaType, String type, String subtype, String charset) {
        this(mediaType, type, subtype, charset, null);
    }

    public MediaType(String type, String subtype, Map<String, String> params) {
        this(null, type, subtype, null, createParametersMap(params));
    }

    public MediaType(String type, String subtype, String charset, Map<String, String> params) {
        this(null, type, subtype, charset, createParametersMap(params));
    }

    public MediaType(String mediaType, String type, String subtype, String charset, Map<String, String> params) {
        this.type = null == type ? MEDIA_TYPE_WILDCARD : type;
        this.subtype = null == subtype ? MEDIA_TYPE_WILDCARD : subtype;
        this.charset = null == charset ? Charset.DEFAULT_UTF_8 : charset;
        this.mediaType = null == mediaType ? this.type + Symbol.C_SLASH + this.subtype + ";charset=" + this.charset : mediaType;
        if (MapKit.isNotEmpty(params)) {
            params = new TreeMap((Comparator<String>) (o1, o2) -> o1.compareToIgnoreCase(o2));
        }
        params = null == params ? new HashMap<>() : params;
        if (null != charset && !charset.isEmpty()) {
            params.put(CHARSET_PARAMETER, charset);
        }
        this.parameters = Collections.unmodifiableMap((Map) params);
    }

    /**
     * 返回媒体类型.
     *
     * @param text 字符串
     * @return the mediaType
     */
    public static MediaType valueOf(String text) {
        Matcher typeSubtype = TYPE_SUBTYPE.matcher(text);
        if (!typeSubtype.lookingAt()) {
            throw new IllegalArgumentException("No subtype found for: \"" + text + Symbol.C_DOUBLE_QUOTES);
        }
        String type = typeSubtype.group(1).toLowerCase(Locale.US);
        String subtype = typeSubtype.group(2).toLowerCase(Locale.US);

        String charset = null;
        Matcher parameter = PARAMETER.matcher(text);
        for (int s = typeSubtype.end(); s < text.length(); s = parameter.end()) {
            parameter.region(s, text.length());
            if (!parameter.lookingAt()) {
                throw new IllegalArgumentException("Parameter is not formatted correctly: " + text.substring(s) + " for:" + text);
            }

            String name = parameter.group(1);
            if (null == name || !name.equalsIgnoreCase("charset")) {
                continue;
            }
            String charsetParameter;
            String token = parameter.group(2);
            if (null != token) {
                charsetParameter = (token.startsWith(Symbol.SINGLE_QUOTE) && token.endsWith(Symbol.SINGLE_QUOTE) && token.length() > 2)
                        ? token.substring(1, token.length() - 1)
                        : token;
            } else {
                charsetParameter = parameter.group(3);
            }
            if (null != charset && !charsetParameter.equalsIgnoreCase(charset)) {
                throw new IllegalArgumentException("Multiple charsets defined: " + charset + " and: " + charsetParameter + " for: " + text);
            }
            charset = charsetParameter;
        }

        return new MediaType(text, type, subtype, charset);
    }

    private static TreeMap<String, String> createParametersMap(Map<String, String> initialValues) {
        TreeMap<String, String> map = new TreeMap((Comparator<String>) (o1, o2) -> o1.compareToIgnoreCase(o2));
        if (null != initialValues) {
            Iterator i$ = initialValues.entrySet().iterator();

            while (i$.hasNext()) {
                Entry<String, String> e = (Entry) i$.next();
                map.put(e.getKey().toLowerCase(), e.getValue());
            }
        }
        return map;
    }

    public boolean equals(Object object) {
        if (!(object instanceof MediaType)) {
            return false;
        } else {
            MediaType other = (MediaType) object;
            return this.type.equalsIgnoreCase(other.type)
                    && this.subtype.equalsIgnoreCase(other.subtype)
                    && this.parameters.equals(other.parameters);
        }
    }

    public int hashCode() {
        return (this.type.toLowerCase() + this.subtype.toLowerCase()).hashCode() + this.parameters.hashCode();
    }

    /**
     * 返回已编码的媒体类型,如“text/plain;charset=utf-8",适用于内容类型头部.
     *
     * @return the string
     */
    public String toString() {
        return mediaType;
    }

    /**
     * 返回高级媒体类型,如: "text", "image", "audio", "video", or "application".
     *
     * @return the string
     */
    public String type() {
        return type;
    }

    /**
     * 返回特定的媒体子类型,如： "plain" or "png", "mpeg", "mp4" or "xml".
     *
     * @return the string
     */
    public String subtype() {
        return subtype;
    }

    /**
     * 返回此媒体类型的字符集,如果该媒体类型没有指定字符集,则返回null.
     *
     * @return the string
     */
    public java.nio.charset.Charset charset() {
        return charset(null);
    }

    /**
     * 返回此媒体类型的字符集,或者{@code defaultValue},
     * 如果此媒体类型没有指定字符集,则当前运行时不支持该字符集
     *
     * @param defaultValue 字符集
     * @return the charset
     */
    public java.nio.charset.Charset charset(java.nio.charset.Charset defaultValue) {
        try {
            return null != charset ? java.nio.charset.Charset.forName(charset) : defaultValue;
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * 检查此媒体类型是否与其他媒体类型兼容
     * 例如:image/*与image/jpeg、image/png等兼容
     * 忽略媒体类型参数 这个函数是可交换的
     *
     * @param mediaType 要比较的媒体类型.
     * @return 如果类型兼容, 则为true, 否则为false.
     */
    public boolean isCompatible(MediaType mediaType) {
        return null != mediaType
                && (type.equals(MEDIA_TYPE_WILDCARD)
                || mediaType.type.equals(MEDIA_TYPE_WILDCARD)
                || (type.equalsIgnoreCase(mediaType.type)
                && (subtype.equals(MEDIA_TYPE_WILDCARD)
                || mediaType.subtype.equals(MEDIA_TYPE_WILDCARD)))
                || (type.equalsIgnoreCase(mediaType.type)
                && this.subtype.equalsIgnoreCase(mediaType.subtype)));
    }

    public static boolean isSTLType(MediaType mediaType) {
        return equalsIgnoreParameters(mediaType, MODEL_STL_TYPE)
                || equalsIgnoreParameters(mediaType, MODEL_X_STL_BINARY_TYPE)
                || equalsIgnoreParameters(mediaType, APPLICATION_SLA_TYPE);
    }

    public static boolean isSTLType(String type) {
        return MODEL_STL.equalsIgnoreCase(type)
                || MODEL_X_STL_BINARY.equalsIgnoreCase(type)
                || APPLICATION_SLA.equalsIgnoreCase(type);
    }

    public static boolean equalsIgnoreParameters(MediaType type1, MediaType type2) {
        return type1.getType().equalsIgnoreCase(type2.getType())
                && type1.getSubtype().equalsIgnoreCase(type2.getSubtype());
    }

    public static MediaType getMultiPartRelatedType(MediaType mediaType) {
        if (!MULTIPART_RELATED_TYPE.isCompatible(mediaType))
            return null;

        String type = mediaType.getParameters().get("type");
        if (type == null)
            return MediaType.WILDCARD_TYPE;

        MediaType partType = MediaType.valueOf(type);
        if (mediaType.getParameters().size() > 1) {
            Map<String, String> params = new HashMap<>(mediaType.getParameters());
            params.remove("type");
            partType = new MediaType(partType.getType(), partType.getSubtype(), params);
        }
        return partType;
    }

}
