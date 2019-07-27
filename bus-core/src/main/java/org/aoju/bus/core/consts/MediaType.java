package org.aoju.bus.core.consts;

import lombok.Data;
import org.aoju.bus.core.utils.MapUtils;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class MediaType {

    /**
     * The media type {@code charset} parameter name.
     */
    public static final String CHARSET_PARAMETER = "charset";

    /**
     * The value of a type or subtype wildcard {@value #MEDIA_TYPE_WILDCARD}.
     */
    public static final String MEDIA_TYPE_WILDCARD = "*";

    /**
     * A {@code String} constant representing wildcard {@value #WILDCARD} media type .
     */
    public final static String WILDCARD = "*/*";

    /**
     * A {@link MediaType} constant representing wildcard {@value #WILDCARD} media type.
     */
    public final static MediaType WILDCARD_TYPE = new MediaType();

    /**
     * "application/xml"
     */
    public static final String APPLICATION_XML = "application/xml";
    public static final MediaType APPLICATION_XML_TYPE = new MediaType("application", "xml");

    /**
     * "application/atom+xml"
     */
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";
    public static final MediaType APPLICATION_ATOM_XML_TYPE = new MediaType("application", "atom+xml");

    /**
     * "application/xhtml+xml"
     */
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
    public static final MediaType APPLICATION_XHTML_XML_TYPE = new MediaType("application", "xhtml+xml");

    /**
     * "application/svg+xml"
     */
    public static final String APPLICATION_SVG_XML = "application/svg+xml";
    public static final MediaType APPLICATION_SVG_XML_TYPE = new MediaType("application", "svg+xml");

    /**
     * "application/json"
     */
    public static final String APPLICATION_JSON = "application/json";
    public static final MediaType APPLICATION_JSON_TYPE = new MediaType("application", "json");

    /**
     * "application/x-www-form-urlencoded"
     */
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final MediaType APPLICATION_FORM_URLENCODED_TYPE = new MediaType("application", "x-www-form-urlencoded");
    /**
     * "application/octet-stream"
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final MediaType APPLICATION_OCTET_STREAM_TYPE = new MediaType("application", "octet-stream");

    /**
     * "text/plain"
     */
    public static final String TEXT_PLAIN = "text/plain";
    public static final MediaType TEXT_PLAIN_TYPE = new MediaType("text", "plain");

    /**
     * "text/xml"
     */
    public static final String TEXT_XML = "text/xml";
    public static final MediaType TEXT_XML_TYPE = new MediaType("text", "xml");

    /**
     * "text/html"
     */
    public static final String TEXT_HTML = "text/html";
    public static final MediaType TEXT_HTML_TYPE = new MediaType("text", "html");

    /**
     * "application/dicom"
     */
    public final static String APPLICATION_DICOM = "application/dicom";
    public final static MediaType APPLICATION_DICOM_TYPE = new MediaType("application", "dicom");

    /**
     * "application/dicom+xml"
     */
    public final static String APPLICATION_DICOM_XML = "application/dicom+xml";
    public final static MediaType APPLICATION_DICOM_XML_TYPE = new MediaType("application", "dicom+xml");

    /**
     * "application/dicom+json"
     */
    public final static String APPLICATION_DICOM_JSON = "application/dicom+json";
    public final static MediaType APPLICATION_DICOM_JSON_TYPE = new MediaType("application", "dicom+json");

    /**
     * "image/*"
     */
    public final static String IMAGE_WILDCARD = "image/*";
    public final static MediaType IMAGE_WILDCARD_TYPE = new MediaType("image", "*");

    /**
     * "image/gif"
     */
    public final static String IMAGE_GIF = "image/gif";
    public final static MediaType IMAGE_GIF_TYPE = new MediaType("image", "gif");

    /**
     * "image/png"
     */
    public final static String IMAGE_PNG = "image/png";
    public final static MediaType IMAGE_PNG_TYPE = new MediaType("image", "png");

    /**
     * "image/jpeg"
     */
    public final static String IMAGE_JPEG = "image/jpeg";
    public final static MediaType IMAGE_JPEG_TYPE = new MediaType("image", "jpeg");

    /**
     * "image/x-jls"
     */
    public final static String IMAGE_X_JLS = "image/x-jls";
    public final static MediaType IMAGE_X_JLS_TYPE = new MediaType("image", "x-jls");

    /**
     * "image/jp2"
     */
    public final static String IMAGE_JP2 = "image/jp2";
    public final static MediaType IMAGE_JP2_TYPE = new MediaType("image", "jp2");

    /**
     * "image/jpx"
     */
    public final static String IMAGE_JPX = "image/jpx";
    public final static MediaType IMAGE_JPX_TYPE = new MediaType("image", "jpx");

    /**
     * "image/dicom+rle"
     */
    public final static String IMAGE_X_DICOM_RLE = "image/x-dicom+rle";
    public final static MediaType IMAGE_X_DICOM_RLE_TYPE = new MediaType("image", "x-dicom+rle");

    /**
     * "video/*"
     */
    public final static String VIDEO_WILDCARD = "video/*";
    public final static MediaType VIDEO_WILDCARD_TYPE = new MediaType("video", "*");

    /**
     * "video/mpeg"
     */
    public final static String VIDEO_MPEG = "video/mpeg";
    public final static MediaType VIDEO_MPEG_TYPE = new MediaType("video", "mpeg");

    /**
     * "video/mp4"
     */
    public final static String VIDEO_MP4 = "video/mp4";
    public final static MediaType VIDEO_MP4_TYPE = new MediaType("video", "mp4");

    /**
     * "application/pdf"
     */
    public final static String APPLICATION_PDF = "application/pdf";
    public final static MediaType APPLICATION_PDF_TYPE = new MediaType("application", "pdf");

    /**
     * "text/rtf"
     */
    public final static String TEXT_RTF = "text/rtf";
    public final static MediaType TEXT_RTF_TYPE = new MediaType("text", "rtf");

    /**
     * "text/csv"
     */
    public final static String TEXT_CSV = "text/csv";
    public final static MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");

    /**
     * "text/csv;charset=utf-8"
     */
    public final static String TEXT_CSV_UTF8 = "text/csv;charset=utf-8";
    public final static MediaType TEXT_CSV_UTF8_TYPE = new MediaType("text", "csv", "utf-8");

    /**
     * "application/zip"
     */
    public final static String APPLICATION_ZIP = "application/zip";
    public final static MediaType APPLICATION_ZIP_TYPE = new MediaType("application", "zip");

    /**
     * "multipart/form-data"
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final MediaType MULTIPART_FORM_DATA_TYPE = new MediaType("multipart", "form-data");

    /**
     * "multipart/mixed"
     */
    public final static String MULTIPART_MIXED = "multipart/mixed";
    public final static MediaType MULTIPART_MIXED_TYPE = new MediaType("multipart", "mixed");

    /**
     * "multipart/alternative"
     */
    public final static String MULTIPART_ALTERNATIVE = "multipart/alternative";
    public final static MediaType MULTIPART_ALTERNATIVE_TYPE = new MediaType("multipart", "alternative");

    /**
     * "multipart/digest"
     */
    public final static String MULTIPART_DIGEST = "multipart/digest";
    public final static MediaType MULTIPART_DIGEST_TYPE = new MediaType("multipart", "digest");

    /**
     * "multipart/related"
     */
    public final static String MULTIPART_parallel = "multipart/parallel";
    public final static MediaType MULTIPART_PARALLEL_TYPE = new MediaType("multipart", "parallel");

    /**
     * "multipart/related"
     */
    public final static String MULTIPART_RELATED = "multipart/related";
    public final static MediaType MULTIPART_RELATED_TYPE = new MediaType("multipart", "related");

    /**
     * "multipart/related;type=application/dicom"
     */
    public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM_TYPE = new MediaType("multipart", "related", Collections.singletonMap("type", APPLICATION_DICOM));

    /**
     * "multipart/related;type=application/dicom+xml"
     */
    public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM_XML_TYPE = new MediaType("multipart", "related", Collections.singletonMap("type", APPLICATION_DICOM_XML));

    public static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
    public static final String QUOTED = "\"([^\"]*)\"";
    public static final Pattern TYPE_SUBTYPE = Pattern.compile(TOKEN + "/" + TOKEN);
    public static final Pattern PARAMETER = Pattern.compile(";\\s*(?:" + TOKEN + "=(?:" + TOKEN + "|" + QUOTED + "))?");

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

    private MediaType(String mediaType, String type, String subtype, String charset, Map<String, String> params) {
        this.mediaType = mediaType == null ? APPLICATION_FORM_URLENCODED : mediaType;
        this.type = type == null ? MEDIA_TYPE_WILDCARD : type;
        this.subtype = subtype == null ? MEDIA_TYPE_WILDCARD : subtype;
        this.charset = charset == null ? org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8 : charset;
        if (MapUtils.isNotEmpty(params)) {
            params = new TreeMap(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            });
        }
        params = params == null ? new HashMap<>() : params;
        if (charset != null && !charset.isEmpty()) {
            params.put(CHARSET_PARAMETER, charset);
        }
        this.parameters = Collections.unmodifiableMap((Map) params);
    }

    /**
     * Returns a media type for {@code string}.
     */
    public static MediaType get(String string) {
        Matcher typeSubtype = TYPE_SUBTYPE.matcher(string);
        if (!typeSubtype.lookingAt()) {
            throw new IllegalArgumentException("No subtype found for: \"" + string + '"');
        }
        String type = typeSubtype.group(1).toLowerCase(Locale.US);
        String subtype = typeSubtype.group(2).toLowerCase(Locale.US);

        String charset = null;
        Matcher parameter = PARAMETER.matcher(string);
        for (int s = typeSubtype.end(); s < string.length(); s = parameter.end()) {
            parameter.region(s, string.length());
            if (!parameter.lookingAt()) {
                throw new IllegalArgumentException("Parameter is not formatted correctly: " + string.substring(s) + " for:" + string);
            }

            String name = parameter.group(1);
            if (name == null || !name.equalsIgnoreCase("charset")) continue;
            String charsetParameter;
            String token = parameter.group(2);
            if (token != null) {
                charsetParameter = (token.startsWith("'") && token.endsWith("'") && token.length() > 2)
                        ? token.substring(1, token.length() - 1)
                        : token;
            } else {
                charsetParameter = parameter.group(3);
            }
            if (charset != null && !charsetParameter.equalsIgnoreCase(charset)) {
                throw new IllegalArgumentException("Multiple charsets defined: " + charset + " and: " + charsetParameter + " for: " + string);
            }
            charset = charsetParameter;
        }

        return new MediaType(string, type, subtype, charset);
    }

    private static TreeMap<String, String> createParametersMap(Map<String, String> initialValues) {
        TreeMap<String, String> map = new TreeMap(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        if (initialValues != null) {
            Iterator i$ = initialValues.entrySet().iterator();

            while (i$.hasNext()) {
                Entry<String, String> e = (Entry) i$.next();
                map.put(e.getKey().toLowerCase(), e.getValue());
            }
        }
        return map;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MediaType)) {
            return false;
        } else {
            MediaType other = (MediaType) obj;
            return this.type.equalsIgnoreCase(other.type)
                    && this.subtype.equalsIgnoreCase(other.subtype)
                    && this.parameters.equals(other.parameters);
        }
    }

    public int hashCode() {
        return (this.type.toLowerCase() + this.subtype.toLowerCase()).hashCode() + this.parameters.hashCode();
    }

    /**
     * Returns the encoded media type, like "text/plain; charset=utf-8", appropriate for use in a
     * Content-Type header.
     */
    public String toString() {
        return mediaType;
    }

    /**
     * Returns the high-level media type, such as "text", "image", "audio", "video", or
     * "application".
     */
    public String type() {
        return type;
    }

    /**
     * Returns a specific media subtype, such as "plain" or "png", "mpeg", "mp4" or "xml".
     */
    public String subtype() {
        return subtype;
    }

    /**
     * Returns the charset of this media type, or null if this media type doesn't specify a charset.
     */
    public Charset charset() {
        return charset(null);
    }

    /**
     * Returns the charset of this media type, or {@code defaultValue} if either this media type
     * doesn't specify a charset, of it its charset is unsupported by the current runtime.
     */
    public Charset charset(Charset defaultValue) {
        try {
            return charset != null ? Charset.forName(charset) : defaultValue;
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

}
