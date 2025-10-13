/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Server configuration class, used to store and manage server-related configuration information.
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class Args {

    /**
     * The parameter name for the request method, used to identify the request handling method.
     */
    public static final String METHOD = "method";
    /**
     * The parameter name for formatted data, used to specify the response data format.
     */
    public static final String FORMAT = "format";
    /**
     * The parameter name for version information, used to specify the API version.
     */
    public static final String VERSION = "v";
    /**
     * The parameter name for signature information, used to verify the request signature.
     */
    public static final String SIGN = "sign";
    /**
     * The authorization header, storing the access token.
     */
    public static final String X_ACCESS_TOKEN = "X-Access-Token";
    /**
     * The access source header, identifying the channel source of the request.
     */
    public static final String X_REMOTE_CHANNEL = "x_remote_channel";

    /**
     * Encryption configuration class, defining encryption-related parameters.
     */
    @Getter
    @Setter
    public static class Encrypt {

        /**
         * Indicates whether encryption is enabled.
         */
        private boolean enabled;

        /**
         * The encryption key.
         */
        private String key;

        /**
         * The type of encryption algorithm.
         */
        private String type;

        /**
         * The encryption offset (if applicable).
         */
        private String offset;
    }

    /**
     * Decryption configuration class, defining decryption-related parameters.
     */
    @Getter
    @Setter
    public static class Decrypt {

        /**
         * Indicates whether decryption is enabled.
         */
        private boolean enabled;

        /**
         * The decryption key.
         */
        private String key;

        /**
         * The type of decryption algorithm.
         */
        private String type;

        /**
         * The decryption offset (if applicable).
         */
        private String offset;
    }

    /**
     * Rate limiting configuration class, defining traffic limiting-related parameters.
     */
    @Getter
    @Setter
    public static class Limit {

        /**
         * Indicates whether rate limiting is enabled.
         */
        private boolean enabled;
    }

    /**
     * Security configuration class, defining security-related parameters.
     */
    @Getter
    @Setter
    public static class Security {

        /**
         * Indicates whether security mechanisms are enabled.
         */
        private boolean enabled;

        /**
         * Indicates whether mock mode is enabled (for testing or debugging).
         */
        private boolean mock;
    }

}
