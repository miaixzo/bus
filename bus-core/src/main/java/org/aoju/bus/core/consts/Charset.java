package org.aoju.bus.core.consts;

/**
 * 编码常量
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Charset {

    /**
     * Default Charset Info
     */
    public static final java.nio.charset.Charset DEFAULT = java.nio.charset.Charset.defaultCharset();
    public static final String DEFAULT_CHARSET = DEFAULT.displayName();
    /**
     * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
     */
    public static final String DEFAULT_ISO_8859_1 = "ISO-8859-1";
    public static final java.nio.charset.Charset ISO_8859_1 = java.nio.charset.Charset.forName(DEFAULT_ISO_8859_1);
    /**
     * Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the
     * Unicode character set
     */
    public static final String DEFAULT_US_ASCII = "US-ASCII";
    public static final java.nio.charset.Charset US_ASCII = java.nio.charset.Charset.forName(DEFAULT_US_ASCII);
    /**
     * GBK UCS Transformation Format
     */
    public static final String DEFAULT_GBK = "GBK";
    public static final java.nio.charset.Charset CHARSET_GBK = java.nio.charset.Charset.forName(DEFAULT_GBK);
    /**
     * Eight-bit UCS Transformation Format
     */
    public static final String DEFAULT_UTF_8 = "UTF-8";
    public static final java.nio.charset.Charset UTF_8 = java.nio.charset.Charset.forName(DEFAULT_UTF_8);
    /**
     * Sixteen-bit UCS Transformation Format, byte order identified by an
     * optional byte-order mark
     */
    public static final String DEFAULT_UTF_16 = "UTF-16";
    public static final java.nio.charset.Charset UTF_16 = java.nio.charset.Charset.forName(DEFAULT_UTF_16);
    /**
     * Sixteen-bit UCS Transformation Format, big-endian byte order
     */
    public static final String DEFAULT_UTF_16_BE = "UTF-16BE";
    public static final java.nio.charset.Charset UTF_16_BE = java.nio.charset.Charset.forName(DEFAULT_UTF_16_BE);
    /**
     * Sixteen-bit UCS Transformation Format, little-endian byte order
     */
    public static final String DEFAULT_UTF_16_LE = "UTF-16LE";
    public static final java.nio.charset.Charset UTF_16_LE = java.nio.charset.Charset.forName(DEFAULT_UTF_16_LE);
    /**
     * thirty-two-bit UCS Transformation Format, little-endian byte order
     */
    public static final String DEFAULT_UTF_32_BE = "UTF-32BE";
    public static final java.nio.charset.Charset UTF_32_BE = java.nio.charset.Charset.forName(DEFAULT_UTF_32_BE);
    /**
     * thirty-two-bit UCS Transformation Format, little-endian byte order
     */
    public static final String DEFAULT_UTF_32_LE = "UTF-32LE";
    public static final java.nio.charset.Charset UTF_32_LE = java.nio.charset.Charset.forName(DEFAULT_UTF_32_LE);

}
