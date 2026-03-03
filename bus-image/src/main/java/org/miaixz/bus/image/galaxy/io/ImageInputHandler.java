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
package org.miaixz.bus.image.galaxy.io;

import java.io.IOException;

import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Fragments;
import org.miaixz.bus.image.galaxy.data.Sequence;

/**
 * Interface for handling the reading of image data from an {@link ImageInputStream}. Implementations of this interface
 * define how different parts of an image dataset are processed.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ImageInputHandler {

    /**
     * Reads a value from the {@link ImageInputStream} and populates the given {@link Attributes}.
     *
     * @param dis   The {@link ImageInputStream} to read from.
     * @param attrs The {@link Attributes} to populate with the read value.
     * @throws IOException If an I/O error occurs during reading.
     */
    void readValue(ImageInputStream dis, Attributes attrs) throws IOException;

    /**
     * Reads a value from the {@link ImageInputStream} and populates the given {@link Sequence}.
     *
     * @param dis The {@link ImageInputStream} to read from.
     * @param seq The {@link Sequence} to populate with the read value.
     * @throws IOException If an I/O error occurs during reading.
     */
    void readValue(ImageInputStream dis, Sequence seq) throws IOException;

    /**
     * Reads a value from the {@link ImageInputStream} and populates the given {@link Fragments}.
     *
     * @param dis   The {@link ImageInputStream} to read from.
     * @param frags The {@link Fragments} to populate with the read value.
     * @throws IOException If an I/O error occurs during reading.
     */
    void readValue(ImageInputStream dis, Fragments frags) throws IOException;

    /**
     * Called when the reading of a dataset starts.
     *
     * @param dis The {@link ImageInputStream} from which the dataset is being read.
     * @throws IOException If an I/O error occurs.
     */
    void startDataset(ImageInputStream dis) throws IOException;

    /**
     * Called when the reading of a dataset ends.
     *
     * @param dis The {@link ImageInputStream} from which the dataset was read.
     * @throws IOException If an I/O error occurs.
     */
    void endDataset(ImageInputStream dis) throws IOException;

}
