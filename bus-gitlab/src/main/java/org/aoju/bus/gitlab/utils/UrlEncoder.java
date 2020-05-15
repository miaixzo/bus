package org.aoju.bus.gitlab.utils;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.gitlab.GitLabApiException;

import java.net.URLEncoder;

public class UrlEncoder {

    /**
     * URL encodes a String in compliance with GitLabs special differences.
     *
     * @param s the String to URL encode
     * @return the URL encoded strings
     * @throws GitLabApiException if any exception occurs
     */
    public static String urlEncode(String s) throws GitLabApiException {

        try {
            String encoded = URLEncoder.encode(s, Charset.DEFAULT_UTF_8);
            // Since the encode method encodes plus signs as %2B,
            // we can simply replace the encoded spaces with the correct encoding here 
            encoded = encoded.replace(Symbol.PLUS, "%20");
            encoded = encoded.replace(Symbol.DOT, "%2E");
            encoded = encoded.replace(Symbol.HYPHEN, "%2D");
            encoded = encoded.replace(Symbol.UNDERLINE, "%5F");
            return (encoded);
        } catch (Exception e) {
            throw new GitLabApiException(e);
        }
    }
}
