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
package org.miaixz.bus.core.net;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents an HTTP Media Type (also known as MIME type).
 *
 * <p>
 * This class provides a structured representation of media types as defined in RFC 2046. It supports parsing of media
 * type strings, comparison of media types, and handling of parameters such as charset. The class includes predefined
 * constants for common media types.
 *
 * <p>
 * Media types consist of a type, a subtype, and optional parameters. For example, "text/html;charset=utf-8" has type
 * "text", subtype "html", and a charset parameter with value "utf-8".
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 *
 * // Create a media type from a string
 * MediaType mediaType = MediaType.valueOf("application/json;charset=utf-8");
 *
 * // Check compatibility
 * boolean compatible = MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
 *
 * // Get charset
 * java.nio.charset.Charset charset = mediaType.charset();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * A {@code String} constant representing {@value #APPLICATION_X_PROTOBUF} media type.
     */
    public static final String APPLICATION_X_PROTOBUF = "application/x-protobuf";

    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_X_PROTOBUF} media type.
     */
    public static final MediaType APPLICATION_X_PROTOBUF_TYPE = new MediaType("application", "x-protobuf");

    /**
     * A {@code String} constant representing {@value #MULTIPART_MIXED} media type.
     */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_MIXED} media type.
     */
    public static final MediaType MULTIPART_MIXED_TYPE = new MediaType("multipart", "mixed");

    /**
     * A {@code String} constant representing {@value #MULTIPART_ALTERNATIVE} media type.
     */
    public static final String MULTIPART_ALTERNATIVE = "multipart/alternative";

    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_ALTERNATIVE} media type.
     */
    public static final MediaType MULTIPART_ALTERNATIVE_TYPE = new MediaType("multipart", "alternative");

    /**
     * A {@code String} constant representing {@value #MULTIPART_DIGEST} media type.
     */
    public static final String MULTIPART_DIGEST = "multipart/digest";

    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_DIGEST} media type.
     */
    public static final MediaType MULTIPART_DIGEST_TYPE = new MediaType("multipart", "digest");

    /**
     * A {@code String} constant representing {@value #MULTIPART_PARALLEL} media type.
     */
    public static final String MULTIPART_PARALLEL = "multipart/parallel";

    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_PARALLEL} media type.
     */
    public static final MediaType MULTIPART_PARALLEL_TYPE = new MediaType("multipart", "parallel");

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
     * A {@code String} constant representing {@value #APPLICATION_JSON_FASTJSON} media type.
     */
    public static final String APPLICATION_JSON_FASTJSON = "application/json+fastjson";

    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_JSON_FASTJSON} media type.
     */
    public static final MediaType APPLICATION_JSON_FASTJSON_TYPE = new MediaType("application", "json+fastjson");

    /**
     * A {@code String} constant representing {@value #APPLICATION_JSON_GSON} media type.
     */
    public static final String APPLICATION_JSON_GSON = "application/json+gson";

    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_JSON_GSON} media type.
     */
    public static final MediaType APPLICATION_JSON_GSON_TYPE = new MediaType("application", "json+gson");

    /**
     * A {@code String} constant representing {@value #APPLICATION_JSON_JACKSON} media type.
     */
    public static final String APPLICATION_JSON_JACKSON = "application/json+jackson";

    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_JSON_JACKSON} media type.
     */
    public static final MediaType APPLICATION_JSON_JACKSON_TYPE = new MediaType("application", "json+jackson");

    /**
     * A {@code String} constant representing {@value #APPLICATION_FORM_URLENCODED} media type.
     */
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_FORM_URLENCODED} media type.
     */
    public static final MediaType APPLICATION_FORM_URLENCODED_TYPE = new MediaType("application",
            "x-www-form-urlencoded");

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
    public final static MediaType IMAGE_WILDCARD_TYPE = new MediaType("image", Symbol.STAR);

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
    public final static MediaType VIDEO_WILDCARD_TYPE = new MediaType("video", Symbol.STAR);

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
    public final static String MULTIPART_RELATED_APPLICATION_DICOM = "multipart/related;type=" + Symbol.DOUBLE_QUOTES
            + "application/dicom" + Symbol.DOUBLE_QUOTES;

    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_RELATED_APPLICATION_DICOM} media type.
     */
    public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM_TYPE = new MediaType("multipart", "related",
            Collections.singletonMap("type", APPLICATION_DICOM));

    /**
     * A {@code String} constant representing {@value #MULTIPART_RELATED_APPLICATION_DICOM_XML} media type.
     */
    public final static String MULTIPART_RELATED_APPLICATION_DICOM_XML = "multipart/related;type="
            + Symbol.DOUBLE_QUOTES + "application/dicom+xml" + Symbol.DOUBLE_QUOTES;

    /**
     * A {@link MediaType} constant representing {@value #MULTIPART_RELATED_APPLICATION_DICOM_XML} media type.
     */
    public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM_XML_TYPE = new MediaType("multipart", "related",
            Collections.singletonMap("type", APPLICATION_DICOM_XML));

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

    /**
     * Regular expression pattern for matching media type tokens.
     */
    public static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";

    /**
     * Regular expression pattern for matching quoted strings.
     */
    public static final String QUOTED = Symbol.DOUBLE_QUOTES + "([^" + Symbol.DOUBLE_QUOTES + "]*)"
            + Symbol.DOUBLE_QUOTES;

    /**
     * Compiled pattern for matching type/subtype in media type strings.
     */
    public static final Pattern TYPE_SUBTYPE = Pattern.compile(TOKEN + Symbol.SLASH + TOKEN);

    /**
     * Compiled pattern for matching parameters in media type strings.
     */
    public static final Pattern PARAMETER = Pattern
            .compile(";\\s*(?:" + TOKEN + "=(?:" + TOKEN + Symbol.OR + QUOTED + "))?");

    /**
     * The primary type of the media type (e.g., "text", "image", "application").
     */
    public final String type;

    /**
     * The subtype of the media type (e.g., "plain", "png", "json").
     */
    public final String subtype;

    /**
     * The charset parameter of the media type, or null if not specified.
     */
    public final String charset;

    /**
     * The full media type string representation.
     */
    public final String mediaType;

    /**
     * Immutable map of parameters for this media type.
     */
    public Map<String, String> parameters;

    /**
     * Creates a wildcard media type "*\/*".
     */
    public MediaType() {
        this(null, MEDIA_TYPE_WILDCARD, MEDIA_TYPE_WILDCARD, null, null);
    }

    /**
     * Creates a media type from the specified string.
     *
     * @param mediaType the media type string
     */
    public MediaType(String mediaType) {
        this(parse(mediaType));
    }

    /**
     * Creates a copy from a parsed media type.
     *
     * @param source parsed source
     */
    private MediaType(final MediaType source) {
        this.type = source.type;
        this.subtype = source.subtype;
        this.charset = source.charset;
        this.mediaType = source.mediaType;
        this.parameters = source.parameters;
    }

    /**
     * Creates a media type with the specified type and subtype.
     *
     * @param type    the primary type
     * @param subtype the subtype
     */
    public MediaType(String type, String subtype) {
        this(null, type, subtype, null, null);
    }

    /**
     * Creates a media type with the specified type, subtype, and charset.
     *
     * @param type    the primary type
     * @param subtype the subtype
     * @param charset the charset
     */
    public MediaType(String type, String subtype, String charset) {
        this(null, type, subtype, charset, null);
    }

    /**
     * Creates a media type with the specified values.
     *
     * @param mediaType the media type string
     * @param type      the primary type
     * @param subtype   the subtype
     * @param charset   the charset
     */
    public MediaType(String mediaType, String type, String subtype, String charset) {
        this(mediaType, type, subtype, charset, null);
    }

    /**
     * Creates a media type with the specified type, subtype, and parameters.
     *
     * @param type    the primary type
     * @param subtype the subtype
     * @param params  the parameters map
     */
    public MediaType(String type, String subtype, Map<String, String> params) {
        this(null, type, subtype, null, createParametersMap(params));
    }

    /**
     * Creates a media type with the specified type, subtype, charset, and parameters.
     *
     * @param type    the primary type
     * @param subtype the subtype
     * @param charset the charset
     * @param params  the parameters map
     */
    public MediaType(String type, String subtype, String charset, Map<String, String> params) {
        this(null, type, subtype, charset, createParametersMap(params));
    }

    /**
     * Creates a media type with the specified values.
     *
     * @param mediaType the media type string
     * @param type      the primary type
     * @param subtype   the subtype
     * @param charset   the charset
     * @param params    the parameters map
     */
    public MediaType(String mediaType, String type, String subtype, String charset, Map<String, String> params) {
        final String currentType = null == type ? MEDIA_TYPE_WILDCARD : type;
        if (currentType.isBlank() || currentType.indexOf(Symbol.C_CR) >= 0 || currentType.indexOf(Symbol.C_LF) >= 0) {
            throw new ValidateException("Media type must be non-blank and single-line");
        }
        this.type = currentType.toLowerCase(Locale.ROOT);
        final String currentSubtype = null == subtype ? MEDIA_TYPE_WILDCARD : subtype;
        if (currentSubtype.isBlank() || currentSubtype.indexOf(Symbol.C_CR) >= 0
                || currentSubtype.indexOf(Symbol.C_LF) >= 0) {
            throw new ValidateException("Media subtype must be non-blank and single-line");
        }
        this.subtype = currentSubtype.toLowerCase(Locale.ROOT);
        final LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        if (params != null) {
            for (final Entry<String, String> entry : params.entrySet()) {
                final String name = entry.getKey();
                if (name == null || name.isBlank() || name.indexOf(Symbol.C_CR) >= 0
                        || name.indexOf(Symbol.C_LF) >= 0) {
                    throw new ValidateException("Media parameter name must be non-blank and single-line");
                }
                final String value = entry.getValue();
                if (value == null || value.isBlank() || value.indexOf(Symbol.C_CR) >= 0
                        || value.indexOf(Symbol.C_LF) >= 0) {
                    throw new ValidateException("Media parameter value must be non-blank and single-line");
                }
                copy.put(name.toLowerCase(Locale.ROOT), value);
            }
        }
        if (null != charset && !charset.isEmpty()) {
            if (charset.isBlank() || charset.indexOf(Symbol.C_CR) >= 0 || charset.indexOf(Symbol.C_LF) >= 0) {
                throw new ValidateException("Media parameter value must be non-blank and single-line");
            }
            copy.put(CHARSET_PARAMETER, charset);
        }
        this.charset = copy.get(CHARSET_PARAMETER);
        this.parameters = Collections.unmodifiableMap(copy);
        if (null == mediaType) {
            final StringBuilder builder = new StringBuilder(this.type).append(Symbol.C_SLASH).append(this.subtype);
            for (final Entry<String, String> entry : copy.entrySet()) {
                builder.append(Symbol.SEMICOLON).append(Symbol.SPACE).append(entry.getKey()).append(Symbol.C_EQUAL)
                        .append(entry.getValue());
            }
            this.mediaType = builder.toString();
        } else {
            this.mediaType = mediaType;
        }
    }

    /**
     * Parses a media type string into a MediaType object.
     *
     * @param text the media type string to parse
     * @return the parsed MediaType
     * @throws IllegalArgumentException if the string is not a valid media type
     */
    public static MediaType valueOf(String text) {
        return parse(text);
    }

    /**
     * Parses a media type string into a MediaType object.
     *
     * @param text the media type string to parse
     * @return the parsed MediaType
     */
    public static MediaType parse(final String text) {
        if (text == null || text.isBlank()) {
            throw new ValidateException("Media value must be non-blank");
        }
        final Matcher typeSubtype = TYPE_SUBTYPE.matcher(text);
        if (!typeSubtype.lookingAt()) {
            throw new ProtocolException("No subtype found for: " + Symbol.DOUBLE_QUOTES + text + Symbol.DOUBLE_QUOTES);
        }
        final String type = typeSubtype.group(1).toLowerCase(Locale.ROOT);
        final String subtype = typeSubtype.group(2).toLowerCase(Locale.ROOT);
        final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        String charset = null;
        final Matcher parameter = PARAMETER.matcher(text);
        for (int start = typeSubtype.end(); start < text.length(); start = parameter.end()) {
            parameter.region(start, text.length());
            if (!parameter.lookingAt() || parameter.group(1) == null) {
                throw new ProtocolException(
                        "Parameter is not formatted correctly: " + text.substring(start) + " for:" + text);
            }
            final String name = parameter.group(1).toLowerCase(Locale.ROOT);
            String validValue = null == parameter.group(2) ? parameter.group(3) : parameter.group(2);
            if (validValue != null && validValue.length() >= 2 && validValue.startsWith(Symbol.DOUBLE_QUOTES)
                    && validValue.endsWith(Symbol.DOUBLE_QUOTES)) {
                validValue = validValue.substring(1, validValue.length() - 1);
            }
            if (validValue == null || validValue.isBlank() || validValue.indexOf(Symbol.C_CR) >= 0
                    || validValue.indexOf(Symbol.C_LF) >= 0) {
                throw new ValidateException("Media parameter value must be non-blank and single-line");
            }
            if (CHARSET_PARAMETER.equals(name)) {
                try {
                    java.nio.charset.Charset.forName(validValue);
                } catch (final IllegalArgumentException e) {
                    throw new ProtocolException("Invalid media charset", e);
                }
                if (null != charset && !validValue.equalsIgnoreCase(charset)) {
                    throw new ProtocolException(
                            "Multiple charsets defined: " + charset + " and: " + validValue + " for: " + text);
                }
                charset = validValue;
            }
            parameters.put(name, validValue);
        }
        return new MediaType(null, type, subtype, charset, parameters);
    }

    /**
     * Creates a case-insensitive parameters map from the specified initial values.
     *
     * @param initialValues the initial values for the map
     * @return a new TreeMap with case-insensitive keys
     */
    private static TreeMap<String, String> createParametersMap(Map<String, String> initialValues) {
        TreeMap<String, String> map = new TreeMap<>((o1, o2) -> o1.compareToIgnoreCase(o2));
        if (null != initialValues) {
            for (final Entry<String, String> e : initialValues.entrySet()) {
                final String name = e.getKey();
                if (name == null || name.isBlank() || name.indexOf(Symbol.C_CR) >= 0
                        || name.indexOf(Symbol.C_LF) >= 0) {
                    throw new ValidateException("Media parameter name must be non-blank and single-line");
                }
                final String value = e.getValue();
                if (value == null || value.isBlank() || value.indexOf(Symbol.C_CR) >= 0
                        || value.indexOf(Symbol.C_LF) >= 0) {
                    throw new ValidateException("Media parameter value must be non-blank and single-line");
                }
                map.put(name.toLowerCase(Locale.ROOT), value);
            }
        }
        return map;
    }

    /**
     * Checks if the specified media type represents an STL file format.
     *
     * @param mediaType the media type to check
     * @return true if the media type is an STL type, false otherwise
     */
    public static boolean isSTLType(MediaType mediaType) {
        return equalsIgnoreParameters(mediaType, MODEL_STL_TYPE)
                || equalsIgnoreParameters(mediaType, MODEL_X_STL_BINARY_TYPE)
                || equalsIgnoreParameters(mediaType, APPLICATION_SLA_TYPE);
    }

    /**
     * Checks if the specified string represents an STL file format.
     *
     * @param type the media type string to check
     * @return true if the type is an STL type, false otherwise
     */
    public static boolean isSTLType(String type) {
        return MODEL_STL.equalsIgnoreCase(type) || MODEL_X_STL_BINARY.equalsIgnoreCase(type)
                || APPLICATION_SLA.equalsIgnoreCase(type);
    }

    /**
     * Compares two media types for equality, ignoring parameters.
     *
     * @param type1 the first media type
     * @param type2 the second media type
     * @return true if the types and subtypes are equal (case-insensitive), false otherwise
     */
    public static boolean equalsIgnoreParameters(MediaType type1, MediaType type2) {
        return type1.getType().equalsIgnoreCase(type2.getType())
                && type1.getSubtype().equalsIgnoreCase(type2.getSubtype());
    }

    /**
     * Extracts the part type from a multipart/related media type.
     *
     * @param mediaType the multipart/related media type
     * @return the part type, or WILDCARD_TYPE if not a multipart/related type
     */
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

    /**
     * Compares this media type to the specified object for equality.
     *
     * @param object the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    public boolean equals(Object object) {
        if (!(object instanceof MediaType)) {
            return false;
        } else {
            MediaType other = (MediaType) object;
            return this.type.equalsIgnoreCase(other.type) && this.subtype.equalsIgnoreCase(other.subtype)
                    && this.parameters.equals(other.parameters);
        }
    }

    /**
     * Returns a hash code value for this media type.
     *
     * @return a hash code value for this media type
     */
    public int hashCode() {
        return (this.type.toLowerCase() + this.subtype.toLowerCase()).hashCode() + this.parameters.hashCode();
    }

    /**
     * Returns the encoded media type string, such as "text/plain;charset=utf-8", suitable for use in a Content-Type
     * header.
     *
     * @return the string representation of this media type
     */
    public String toString() {
        return mediaType;
    }

    /**
     * Returns the encoded media type value.
     *
     * @return media type value
     */
    public String value() {
        return mediaType;
    }

    /**
     * Returns the high-level media type, such as "text", "image", "audio", "video", or "application".
     *
     * @return the primary type of this media type
     */
    public String type() {
        return type;
    }

    /**
     * Returns the specific media subtype, such as "plain", "png", "mpeg", "mp4", or "xml".
     *
     * @return the subtype of this media type
     */
    public String subtype() {
        return subtype;
    }

    /**
     * Returns a parameter value by name.
     *
     * @param name parameter name
     * @return parameter value or null
     */
    public String parameter(final String name) {
        if (name == null || name.isBlank() || name.indexOf(Symbol.C_CR) >= 0 || name.indexOf(Symbol.C_LF) >= 0) {
            throw new ValidateException("Media parameter name must be non-blank and single-line");
        }
        return parameters.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the charset of this media type, or null if this media type does not specify a charset.
     *
     * @return the charset of this media type, or null
     */
    public java.nio.charset.Charset charset() {
        return charset(null);
    }

    /**
     * Returns the charset of this media type, or {@code defaultValue} if this media type does not specify a charset or
     * the current runtime does not support the charset.
     *
     * @param defaultValue the default charset to return if none is specified
     * @return the charset of this media type, or the default value
     */
    public java.nio.charset.Charset charset(java.nio.charset.Charset defaultValue) {
        try {
            return null != charset ? java.nio.charset.Charset.forName(charset) : defaultValue;
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Returns a media type with the supplied charset parameter.
     *
     * @param charset charset
     * @return media type with charset
     */
    public MediaType withCharset(final java.nio.charset.Charset charset) {
        if (charset == null) {
            throw new ValidateException("Charset must not be null");
        }
        final LinkedHashMap<String, String> copy = new LinkedHashMap<>(parameters);
        copy.put(CHARSET_PARAMETER, charset.name());
        return new MediaType(null, type, subtype, charset.name(), copy);
    }

    /**
     * Checks if this media type is compatible with another media type. For example, image/* is compatible with
     * image/jpeg, image/png, etc. Media type parameters are ignored. This function is commutative.
     *
     * @param mediaType the media type to compare with
     * @return true if the types are compatible, false otherwise
     */
    public boolean isCompatible(MediaType mediaType) {
        return null != mediaType && (type.equals(MEDIA_TYPE_WILDCARD) || mediaType.type.equals(MEDIA_TYPE_WILDCARD)
                || (type.equalsIgnoreCase(mediaType.type)
                        && (subtype.equals(MEDIA_TYPE_WILDCARD) || mediaType.subtype.equals(MEDIA_TYPE_WILDCARD)))
                || (type.equalsIgnoreCase(mediaType.type) && this.subtype.equalsIgnoreCase(mediaType.subtype)));
    }

}
