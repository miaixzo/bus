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
package org.miaixz.bus.image.galaxy.data;

import org.miaixz.bus.core.lang.Normal;

/**
 * Provides DICOM processing details.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Implementation {

    /**
     * Constructs a new Implementation instance.
     */
    public Implementation() {
        // No initialization required.
    }

    /**
     * Provides DICOM processing details.
     */
    private static final String IMPL_CLASS_UID = "1.3.51.0.42.1.1";

    /**
     * Provides DICOM processing details.
     */
    private static final String IMPL_VERS_NAME = versionName();

    /**
     * Provides DICOM processing details.
     *
     * @return the result.
     */
    private static String versionName() {
        StringBuilder sb = new StringBuilder(Normal._16);
        sb.append("miaixz-");
        sb.append(Implementation.class.getPackage().getImplementationVersion());
        return sb.substring(0, Math.min(16, sb.length()));
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public static String getClassUID() {
        return IMPL_CLASS_UID;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public static String getVersionName() {
        return IMPL_VERS_NAME;
    }

}
