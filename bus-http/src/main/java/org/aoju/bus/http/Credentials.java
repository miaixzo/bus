package org.aoju.bus.http;

import org.aoju.bus.core.io.ByteString;

import java.nio.charset.Charset;

/**
 * Factory for HTTP authorization credentials.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Credentials {
    private Credentials() {
    }

    /**
     * Returns an auth credential for the Basic scheme.
     */
    public static String basic(String username, String password) {
        return basic(username, password, org.aoju.bus.core.consts.Charset.ISO_8859_1);
    }

    public static String basic(String username, String password, Charset charset) {
        String usernameAndPassword = username + ":" + password;
        String encoded = ByteString.encodeString(usernameAndPassword, charset).base64();
        return "Basic " + encoded;
    }

}
