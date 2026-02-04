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
package org.miaixz.bus.limiter;

/**
 * Global context holder for the limiter module. This class provides static methods to set and retrieve the
 * {@link Context} object, making it accessible throughout the application.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Holder {

    /**
     * The global {@link Context} instance for the limiter module. This instance holds configuration and shared
     * resources.
     */
    private static Context context;

    /**
     * Sets the global {@link Context} instance. This method should typically be called once during application
     * initialization.
     *
     * @param context The {@link Context} object to be set as the global instance.
     */
    public static void set(Context context) {
        Holder.context = context;
    }

    /**
     * Retrieves the global {@link Context} instance.
     *
     * @return The currently set global {@link Context} object.
     */
    public static Context load() {
        return context;
    }

}
