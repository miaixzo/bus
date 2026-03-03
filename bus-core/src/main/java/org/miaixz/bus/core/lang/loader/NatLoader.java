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
package org.miaixz.bus.core.lang.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A loader for native libraries (e.g., .dll, .so files) embedded within a JAR package. This loader extracts the native
 * library from the JAR to a temporary location and then loads it.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NatLoader extends StdLoader implements Loader {

    /**
     * Constructs a new {@code NatLoader}.
     */
    public NatLoader() {

    }

    /**
     * Loads a native library from the specified path within the JAR. The library file is extracted to a temporary
     * directory and then loaded using {@code System.load()}. The temporary file is deleted upon exit.
     *
     * @param path  The absolute path to the native library within the JAR (must start with '/').
     * @param clazz The context class to use for getting the resource stream. If {@code null}, {@code Loaders.class} is
     *              used.
     * @return An empty enumeration, as native libraries are loaded directly and not enumerated as resources.
     * @throws IOException              If an I/O error occurs during extraction or if the file is not found.
     * @throws IllegalArgumentException If the path is not absolute or the filename is too short.
     */
    @Override
    public Enumeration<Resource> load(String path, Class<?> clazz) throws IOException {
        if (null == path || !path.startsWith(Symbol.SLASH)) {
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");
        }

        // Get filename from path
        String[] parts = path.split(Symbol.SLASH);
        String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

        // Check if the filename is valid
        if (null == filename || filename.length() < 3) {
            throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
        }

        File dir = new File(System.getProperty(Keys.JAVA_IO_TMPDIR), StringKit.toString(System.nanoTime()));

        if (!dir.mkdir())
            throw new IOException("Failed to create temp directory " + dir.getName());

        dir.deleteOnExit();

        File file = new File(dir, filename);
        Class<?> aClass = null == clazz ? Loaders.class : clazz;
        try (InputStream is = aClass.getResourceAsStream(path)) {
            Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            file.delete();
            throw e;
        } catch (NullPointerException e) {
            file.delete();
            throw new FileNotFoundException("File " + path + " was not found inside JAR.");
        }

        try {
            System.load(file.getAbsolutePath());
        } finally {
            if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                file.delete();
            } else {
                file.deleteOnExit();
            }
        }
        return null;
    }

}
