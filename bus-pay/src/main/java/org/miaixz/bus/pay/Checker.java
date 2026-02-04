/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Validator for configuration classes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Checker {

    /**
     * Checks if payment is supported.
     *
     * @param context The payment context.
     * @param complex The payment complex object.
     * @return true if payment is supported, false otherwise.
     */
    public static boolean isSupportedPay(Context context, Complex complex) {
        boolean isSupported = StringKit.isNotEmpty(context.getAppKey()) && StringKit.isNotEmpty(context.getAppSecret());

        return isSupported;
    }

    /**
     * Checks the legitimacy of the configuration.
     *
     * @param context The payment context.
     * @param complex The payment complex object.
     */
    public static void checkConfig(Context context, Complex complex) {

    }

}
