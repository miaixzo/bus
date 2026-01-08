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
