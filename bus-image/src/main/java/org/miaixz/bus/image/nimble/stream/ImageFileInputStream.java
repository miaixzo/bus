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
package org.miaixz.bus.image.nimble.stream;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.ImageMetaData;

/**
 * Represents the ImageFileInputStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageFileInputStream extends ImageInputStream implements ImageReaderDescriptor {

    /**
     * The path value.
     */
    private final Path path;

    /**
     * The metadata value.
     */
    private ImageMetaData metadata;

    /**
     * Creates a new instance.
     *
     * @param path the path.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageFileInputStream(Path path) throws IOException {
        super(Files.newInputStream(path));
        this.path = path;
    }

    /**
     * Creates a new instance.
     *
     * @param path the path.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageFileInputStream(String path) throws IOException {
        this(FileSystems.getDefault().getPath(path));
    }

    /**
     * Gets the path.
     *
     * @return the path.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageMetaData getMetadata() throws IOException {
        if (metadata == null) {
            this.metadata = new ImageMetaData(this);
        }
        return metadata;
    }

    /**
     * Gets the image descriptor.
     *
     * @return the image descriptor.
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        try {
            getMetadata();
        } catch (IOException e) {
            return null;
        }
        return metadata.getImageDescriptor();
    }

}
