/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
