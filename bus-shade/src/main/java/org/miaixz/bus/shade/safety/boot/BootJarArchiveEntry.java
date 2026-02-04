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
