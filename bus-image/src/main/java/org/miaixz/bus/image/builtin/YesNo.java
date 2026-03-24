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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * This class defines standard DICOM codes for Yes, No, and Undetermined responses as {@link Code} objects. These codes
 * are commonly used in various DICOM contexts where a binary or ternary choice is required.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class YesNo {

    /**
     * Code representing a 'Yes' response.
     */
    public static final Code Yes = new Code("373066001", "SCT", null, "Yes");
    /**
     * Code representing a 'No' response.
     */
    public static final Code No = new Code("373067005", "SCT", null, "No");
    /**
     * Code representing an 'Undetermined' response.
     */
    public static final Code Undetermined = new Code("373068000", "SCT", null, "Undetermined");

}
