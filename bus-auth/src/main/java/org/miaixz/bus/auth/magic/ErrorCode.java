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
package org.miaixz.bus.auth.magic;

import org.miaixz.bus.core.basic.normal.ErrorRegistry;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Authorization error codes: 110xxx.
 *
 * @author Kimi Liu
 */
public class ErrorCode extends org.miaixz.bus.core.basic.normal.ErrorCode {

    /**
     * Constructs a new ErrorCode instance.
     */
    public ErrorCode() {
        // No initialization required.
    }

    /**
     * This authorization feature is not supported.
     */
    public static final Errors _110000 = ErrorRegistry
            .register("110000", "This authorization feature is not supported");

    /**
     * Indicates that the registry cannot be empty.
     */
    public static final Errors _110001 = ErrorRegistry.register("110001", "Registry cannot be empty");

    /**
     * Indicates an unidentified authorization platform.
     */
    public static final Errors _110002 = ErrorRegistry.register("110002", "Unidentified platform");

    /**
     * Indicates an illegal redirect URI.
     */
    public static final Errors _110003 = ErrorRegistry.register("110003", "Illegal redirect URI");

    /**
     * Indicates an illegal authorization provider or request.
     */
    public static final Errors _110004 = ErrorRegistry.register("110004", "Illegal provider or request");

    /**
     * Indicates an illegal authorization code.
     */
    public static final Errors _110005 = ErrorRegistry.register("110005", "Illegal code");

    /**
     * Indicates an illegal state parameter.
     */
    public static final Errors _110006 = ErrorRegistry.register("110006", "Illegal state");

    /**
     * Indicates that a refresh token is required and cannot be empty.
     */
    public static final Errors _110007 = ErrorRegistry
            .register("110007", "Refresh token is required and cannot be empty");

    /**
     * Indicates an invalid Key ID (kid).
     */
    public static final Errors _110008 = ErrorRegistry.register("110008", "Invalid Key ID (kid)");

    /**
     * Indicates an invalid Team ID.
     */
    public static final Errors _110009 = ErrorRegistry.register("110009", "Invalid Team ID");

    /**
     * Indicates an invalid Client ID.
     */
    public static final Errors _110010 = ErrorRegistry.register("110010", "Invalid Client ID");

    /**
     * Indicates an invalid Client Secret.
     */
    public static final Errors _110011 = ErrorRegistry.register("110011", "Invalid Client Secret");

    /**
     * Indicates an illegal WeChat agent ID.
     */
    public static final Errors _110012 = ErrorRegistry.register("110012", "Illegal WeChat agent ID");

    /**
     * Error codes specific to Toutiao (ByteDance) authorization login.
     *
     * @author Kimi Liu
     */
    public static class Toutiao {

        /**
         * Constructs a new Toutiao instance.
         */
        public Toutiao() {
            // No initialization required.
        }

        /**
         * Indicates that the API call was successful.
         */
        public static final Errors EC0 = ErrorRegistry.register(Symbol.ZERO, "API call successful");

        /**
         * Indicates an API configuration error: Client Key is missing.
         */
        public static final Errors EC1 = ErrorRegistry
                .register(Symbol.ONE, "API configuration error: Client Key missing");

        /**
         * Indicates an API configuration error: Client Key is incorrect. Please check if it matches the ClientKey in
         * the open platform.
         */
        public static final Errors EC2 = ErrorRegistry.register(
                Symbol.TWO,
                "API configuration error: Client Key incorrect. Please check if it matches the ClientKey in the open platform");

        /**
         * Indicates missing authorization information.
         */
        public static final Errors EC3 = ErrorRegistry.register(Symbol.THREE, "Missing authorization information");

        /**
         * Indicates an incorrect response type.
         */
        public static final Errors EC4 = ErrorRegistry.register(Symbol.FOUR, "Incorrect response type");

        /**
         * Indicates an incorrect authorization type.
         */
        public static final Errors EC5 = ErrorRegistry.register(Symbol.FIVE, "Incorrect authorization type");

        /**
         * Indicates an incorrect client_secret.
         */
        public static final Errors EC6 = ErrorRegistry.register(Symbol.SIX, "Incorrect client_secret");

        /**
         * Indicates that the authorize_code has expired.
         */
        public static final Errors EC7 = ErrorRegistry.register(Symbol.SEVEN, "Authorize_code expired");

        /**
         * Indicates that the scheme of the specified URL is not HTTPS.
         */
        public static final Errors EC8 = ErrorRegistry.register(Symbol.EIGHT, "Specified URL scheme is not HTTPS");

        /**
         * Indicates an internal API error. Please contact Toutiao technical support.
         */
        public static final Errors EC9 = ErrorRegistry
                .register(Symbol.NINE, "Internal API error. Please contact Toutiao technical support");

        /**
         * Indicates that the access_token has expired.
         */
        public static final Errors EC10 = ErrorRegistry.register("10", "Access_token expired");

        /**
         * Indicates a missing access_token.
         */
        public static final Errors EC11 = ErrorRegistry.register("11", "Missing access_token");

        /**
         * Indicates missing parameters.
         */
        public static final Errors EC12 = ErrorRegistry.register("12", "Parameters missing");

        /**
         * Indicates an incorrect URL.
         */
        public static final Errors EC13 = ErrorRegistry.register("13", "Incorrect URL");

        /**
         * Indicates that the domain does not match the registered domain.
         */
        public static final Errors EC21 = ErrorRegistry.register("21", "Domain does not match registered domain");

        /**
         * Indicates an unknown error. Please contact Toutiao technical support.
         */
        public static final Errors EC999 = ErrorRegistry
                .register("999", "Unknown error. Please contact Toutiao technical support");

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
