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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.image.Format;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * A {@link SimpleFileVisitor} that reads DICOM files, merges them with additional attributes, and prints the resulting
 * dataset as a formatted string to the logger.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2String extends SimpleFileVisitor<Path> {

    /**
     * The format string for the output.
     */
    private final Format format;
    /**
     * Additional attributes to be merged into the dataset before printing.
     */
    private final Attributes cliAttrs;

    /**
     * Constructs a new {@code Dcm2String} visitor.
     *
     * @param format   The format string to use for printing the DICOM dataset.
     * @param cliAttrs Additional attributes to merge with the dataset from the file.
     */
    public Dcm2String(Format format, Attributes cliAttrs) {
        this.format = format;
        this.cliAttrs = cliAttrs;
    }

    /**
     * Called for each file visited. Reads the DICOM file, merges attributes, formats the content, and logs the output.
     *
     * @param path  The path to the file being visited.
     * @param attrs The basic attributes of the file.
     * @return {@link FileVisitResult#CONTINUE} to continue the file tree walk.
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
        try (ImageInputStream dis = new ImageInputStream(path.toFile())) {
            Attributes dataset = dis.readDataset();
            dataset.addAll(cliAttrs);
            Logger.info(format.format(dataset));
        } catch (IOException e) {
            Logger.error("Failed to parse DICOM file " + path);
            e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }

}
