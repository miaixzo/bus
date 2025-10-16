/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.safety.boot;

import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.miaixz.bus.shade.safety.Builder;

/**
 * Extends {@link JarArchiveEntry} to provide a consistent resource path for entries within Spring Boot's fat JAR
 * structure (specifically, entries under {@code BOOT-INF/classes/}). This class removes the {@code BOOT-INF/classes/}
 * prefix from the entry name to make it compatible with standard JAR resource loading mechanisms.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BootJarArchiveEntry extends JarArchiveEntry {

    /**
     * Constructs a new {@code BootJarArchiveEntry} from an existing {@link ZipArchiveEntry}.
     *
     * @param entry The {@link ZipArchiveEntry} to wrap.
     * @throws ZipException If a ZIP format error occurs.
     */
    public BootJarArchiveEntry(ZipArchiveEntry entry) throws ZipException {
        super(entry);
    }

    /**
     * Returns the name of the entry, with the {@code BOOT-INF/classes/} prefix removed. This ensures that resources
     * within the Spring Boot fat JAR can be accessed as if they were in a standard JAR structure.
     *
     * @return The normalized name of the entry.
     */
    @Override
    public String getName() {
        return super.getName().substring(Builder.BOOT_INF_CLASSES.length());
    }

}
