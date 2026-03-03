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
package org.miaixz.bus.shade.safety;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.loader.Loaders;

/**
 * Injects framework-specific classes and resources into a JAR archive. This class is responsible for adding necessary
 * components for the framework's operation into the target JAR, ensuring proper functionality after shading or
 * packaging.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Injector {

    /**
     * Injects framework classes and resources into the provided JAR output stream. It scans for resources within the
     * {@link Builder#XJAR_SRC_DIR} and adds them as entries to the JAR archive, creating necessary directory entries
     * along the way.
     *
     * @param zos The {@link JarArchiveOutputStream} to which the framework's classes and resources will be injected.
     * @throws IOException If an I/O error occurs during the injection process.
     */
    public static void inject(JarArchiveOutputStream zos) throws IOException {
        Set<String> directories = new HashSet<>();
        Enumeration<Resource> resources = Loaders.ant().load(Builder.XJAR_SRC_DIR + "**");
        while (resources.hasMoreElements()) {
            Resource resource = resources.nextElement();
            String name = resource.getName();
            String directory = name.substring(0, name.lastIndexOf(Symbol.SLASH) + 1);
            if (directories.add(directory)) {
                JarArchiveEntry xDirEntry = new JarArchiveEntry(directory);
                xDirEntry.setTime(System.currentTimeMillis());
                zos.putArchiveEntry(xDirEntry);
                zos.closeArchiveEntry();
            }
            JarArchiveEntry xJarEntry = new JarArchiveEntry(name);
            xJarEntry.setTime(System.currentTimeMillis());
            zos.putArchiveEntry(xJarEntry);
            try (InputStream ris = resource.getStream()) {
                Builder.transfer(ris, zos);
            }
            zos.closeArchiveEntry();
        }
    }

}
