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
package org.miaixz.bus.image.galaxy.io;

import java.io.IOException;

import org.miaixz.bus.image.galaxy.data.BulkData;

/**
 * Functional interface for creating {@link BulkData} objects from an {@link ImageInputStream}. This interface is used
 * to abstract the creation process of bulk data, allowing for different strategies to handle large data elements.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface BulkDataCreator {

    /**
     * Creates a {@link BulkData} object by reading from the provided {@link ImageInputStream}.
     * 
     * @param dis The {@link ImageInputStream} to read bulk data from.
     * @return A new {@link BulkData} instance containing the read data.
     * @throws IOException if an I/O error occurs during reading.
     */
    BulkData createBulkData(ImageInputStream dis) throws IOException;

}
