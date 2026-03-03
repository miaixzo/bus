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

/**
 * This class provides a mechanism to access the number of frames associated with a DICOM instance. By default, it
 * assumes a single frame, but can be extended or configured to retrieve the actual number of frames from a data source
 * based on the instance UID.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumberOfFramesAccessor {

    /**
     * Retrieves the number of frames for a given DICOM instance UID. By default, this implementation returns 1,
     * assuming a single-frame image. Subclasses or configurations may override this behavior to provide actual frame
     * counts based on external information or by parsing the DICOM data itself.
     *
     * @param iuid The instance UID of the DICOM image.
     * @return The number of frames for the specified instance UID. Defaults to 1.
     */
    public int getNumberOfFrames(String iuid) {
        return 1;
    }

}
