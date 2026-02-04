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
package org.miaixz.bus.image;

import java.io.File;

import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Defines an extension point for custom post-processing of DICOM images. Implementations of this interface can be used
 * to perform additional business logic after an image has been processed or received.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Efforts {

    /**
     * Executes the post-processing logic on a DICOM image.
     *
     * @param attributes The complete DICOM attributes of the image.
     * @param file       The original DICOM file on the filesystem.
     * @param clazz      The class that invoked this post-processing step, providing context.
     * @return An object representing the result of the post-processing. The default implementation simply returns the
     *         original file.
     */
    default Object supports(Attributes attributes, File file, Class<?> clazz) {
        return file;
    }

}
