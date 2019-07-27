package org.aoju.bus.http.internal.http.second;

import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.http.Headers;

/**
 * HTTP header: the name is an ASCII string, but the value can be UTF-8.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Header {

    public static final ByteString PSEUDO_PREFIX = ByteString.encodeUtf8(":");

    public static final String RESPONSE_STATUS_UTF8 = ":status";
    public static final String TARGET_METHOD_UTF8 = ":method";
    public static final String TARGET_PATH_UTF8 = ":path";
    public static final String TARGET_SCHEME_UTF8 = ":scheme";
    public static final String TARGET_AUTHORITY_UTF8 = ":authority";

    public static final ByteString RESPONSE_STATUS = ByteString.encodeUtf8(RESPONSE_STATUS_UTF8);
    public static final ByteString TARGET_METHOD = ByteString.encodeUtf8(TARGET_METHOD_UTF8);
    public static final ByteString TARGET_PATH = ByteString.encodeUtf8(TARGET_PATH_UTF8);
    public static final ByteString TARGET_SCHEME = ByteString.encodeUtf8(TARGET_SCHEME_UTF8);
    public static final ByteString TARGET_AUTHORITY = ByteString.encodeUtf8(TARGET_AUTHORITY_UTF8);


    /**
     * Name in case-insensitive ASCII encoding.
     */
    public final ByteString name;
    /**
     * Value in UTF-8 encoding.
     */
    public final ByteString value;
    final int hpackSize;

    // TODO: search for toLowerCase and consider moving logic here.
    public Header(String name, String value) {
        this(ByteString.encodeUtf8(name), ByteString.encodeUtf8(value));
    }

    public Header(ByteString name, String value) {
        this(name, ByteString.encodeUtf8(value));
    }

    public Header(ByteString name, ByteString value) {
        this.name = name;
        this.value = value;
        this.hpackSize = 32 + name.size() + value.size();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Header) {
            Header that = (Header) other;
            return this.name.equals(that.name)
                    && this.value.equals(that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return StringUtils.format("%s: %s", name.utf8(), value.utf8());
    }

    interface Listener {
        void onHeaders(Headers headers);
    }

}
