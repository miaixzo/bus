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
package org.miaixz.bus.auth.vendor;

import org.miaixz.bus.core.basic.normal.ErrorRegistry;
import org.miaixz.bus.core.basic.normal.Errors;

/**
 * VendorTokenSet error codes: 110xxx.
 *
 * @author Kimi Liu
 */
public final class VendorErrors {

    /**
     * This authorization feature is not supported.
     */
    public static final Errors _110000 = ErrorRegistry
            .register("110000", "This authorization feature is not supported");
    /**
     * Indicates that the registry cannot be empty.
     */
    public static final Errors _110001 = ErrorRegistry.register("110001", "BuiltinVendors cannot be empty");
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
     * Constructs a new VendorErrors instance.
     */
    private VendorErrors() {
        // No initialization required.
    }

}
