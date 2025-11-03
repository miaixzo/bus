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
package org.miaixz.bus.auth.magic;

import org.miaixz.bus.core.basic.normal.ErrorRegistry;
import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Authorization error codes: 110xxx.
 *
 * @author Kimi Liu
 * @since Java 17+
 */

public class ErrorCode extends org.miaixz.bus.core.basic.normal.ErrorCode {

    /**
     * Indicates that the requested operation is not implemented.
     */
    public static final Errors _110001 = ErrorRegistry.builder().key("110001").value("Operation not implemented")
            .build();

    /**
     * Indicates that parameters are incomplete.
     */
    public static final Errors _110002 = ErrorRegistry.builder().key("110002").value("Parameters incomplete").build();

    /**
     * Indicates that the registry cannot be empty.
     */
    public static final Errors _110003 = ErrorRegistry.builder().key("110003").value("Registry cannot be empty")
            .build();

    /**
     * Indicates an unidentified authorization platform.
     */
    public static final Errors _110004 = ErrorRegistry.builder().key("110004").value("Unidentified platform").build();

    /**
     * Indicates an illegal redirect URI.
     */
    public static final Errors _110005 = ErrorRegistry.builder().key("110005").value("Illegal redirect URI").build();

    /**
     * Indicates an illegal authorization provider or request.
     */
    public static final Errors _110006 = ErrorRegistry.builder().key("110006").value("Illegal provider or request")
            .build();

    /**
     * Indicates an illegal authorization code.
     */
    public static final Errors _110007 = ErrorRegistry.builder().key("110007").value("Illegal code").build();

    /**
     * Indicates an illegal state parameter.
     */
    public static final Errors _110008 = ErrorRegistry.builder().key("110008").value("Illegal state").build();

    /**
     * Indicates that a refresh token is required and cannot be empty.
     */
    public static final Errors _110009 = ErrorRegistry.builder().key("110009")
            .value("Refresh token is required and cannot be empty").build();

    /**
     * Indicates an invalid Key ID (kid).
     */
    public static final Errors _110010 = ErrorRegistry.builder().key("110010").value("Invalid Key ID (kid)").build();

    /**
     * Indicates an invalid Team ID.
     */
    public static final Errors _110011 = ErrorRegistry.builder().key("110011").value("Invalid Team ID").build();

    /**
     * Indicates an invalid Client ID.
     */
    public static final Errors _110012 = ErrorRegistry.builder().key("110012").value("Invalid Client ID").build();

    /**
     * Indicates an invalid Client Secret.
     */
    public static final Errors _110013 = ErrorRegistry.builder().key("110013").value("Invalid Client Secret").build();

    /**
     * Indicates an illegal WeChat agent ID.
     */
    public static final Errors _110014 = ErrorRegistry.builder().key("110014").value("Illegal WeChat agent ID").build();

    /**
     * Error codes specific to Toutiao (ByteDance) authorization login.
     */

    public static class Toutiao {

        /**
         * Indicates that the API call was successful.
         */
        public static final Errors EC0 = ErrorRegistry.builder().key("0").value("API call successful").build();

        /**
         * Indicates an API configuration error: Client Key is missing.
         */
        public static final Errors EC1 = ErrorRegistry.builder().key("1")
                .value("API configuration error: Client Key missing").build();

        /**
         * Indicates an API configuration error: Client Key is incorrect. Please check if it matches the ClientKey in
         * the open platform.
         */
        public static final Errors EC2 = ErrorRegistry.builder().key("2").value(
                "API configuration error: Client Key incorrect. Please check if it matches the ClientKey in the open platform")
                .build();

        /**
         * Indicates missing authorization information.
         */
        public static final Errors EC3 = ErrorRegistry.builder().key("3").value("Missing authorization information")
                .build();

        /**
         * Indicates an incorrect response type.
         */
        public static final Errors EC4 = ErrorRegistry.builder().key("4").value("Incorrect response type").build();

        /**
         * Indicates an incorrect authorization type.
         */
        public static final Errors EC5 = ErrorRegistry.builder().key("5").value("Incorrect authorization type").build();

        /**
         * Indicates an incorrect client_secret.
         */
        public static final Errors EC6 = ErrorRegistry.builder().key("6").value("Incorrect client_secret").build();

        /**
         * Indicates that the authorize_code has expired.
         */
        public static final Errors EC7 = ErrorRegistry.builder().key("7").value("Authorize_code expired").build();

        /**
         * Indicates that the scheme of the specified URL is not HTTPS.
         */
        public static final Errors EC8 = ErrorRegistry.builder().key("8").value("Specified URL scheme is not HTTPS")
                .build();

        /**
         * Indicates an internal API error. Please contact Toutiao technical support.
         */
        public static final Errors EC9 = ErrorRegistry.builder().key("9")
                .value("Internal API error. Please contact Toutiao technical support").build();

        /**
         * Indicates that the access_token has expired.
         */
        public static final Errors EC10 = ErrorRegistry.builder().key("10").value("Access_token expired").build();

        /**
         * Indicates a missing access_token.
         */
        public static final Errors EC11 = ErrorRegistry.builder().key("11").value("Missing access_token").build();

        /**
         * Indicates missing parameters.
         */
        public static final Errors EC12 = ErrorRegistry.builder().key("12").value("Parameters missing").build();

        /**
         * Indicates an incorrect URL.
         */
        public static final Errors EC13 = ErrorRegistry.builder().key("13").value("Incorrect URL").build();

        /**
         * Indicates that the domain does not match the registered domain.
         */
        public static final Errors EC21 = ErrorRegistry.builder().key("21")
                .value("Domain does not match registered domain").build();

        /**
         * Indicates an unknown error. Please contact Toutiao technical support.
         */
        public static final Errors EC999 = ErrorRegistry.builder().key("999")
                .value("Unknown error. Please contact Toutiao technical support").build();

        /**
         * Retrieves the corresponding error object based on the error code.
         *
         * @param errorCode the error code string
         * @return the matching {@link Errors} object, or {@link #EC999} if no match is found
         */
        public static Errors getErrorCode(String errorCode) {
            Errors[] errorCodes = new Errors[] { EC0, EC1, EC2, EC3, EC4, EC5, EC6, EC7, EC8, EC9, EC10, EC11, EC12,
                    EC13, EC21, EC999 };
            for (Errors code : errorCodes) {
                if (errorCode.equals(code.getKey())) {
                    return code;
                }
            }
            return EC999;
        }
    }

}
