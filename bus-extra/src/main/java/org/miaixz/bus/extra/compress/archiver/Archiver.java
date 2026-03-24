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
package org.miaixz.bus.extra.compress.archiver;

import java.io.Closeable;
import java.io.File;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Data archiving wrapper, which archives several files or directories into a compressed package.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Archiver extends Closeable {

    /**
     * Adds a file or directory to the archive. Directories are added recursively level by level.
     *
     * @param file The file or directory.
     * @return this
     */
    default Archiver add(final File file) {
        return add(file, null);
    }

    /**
     * Adds a file or directory to the archive. Directories are added recursively level by level.
     *
     * @param file   The file or directory.
     * @param filter A file filter that specifies which files or directories can be added. If
     *               {@link Predicate#test(Object)} is {@code true}, the file is added. If null, all are added.
     * @return this
     */
    default Archiver add(final File file, final Predicate<File> filter) {
        return add(file, null, filter);
    }

    /**
     * Adds a file or directory to the archive package. Directories are added recursively level by level.
     *
     * @param file   The file or directory.
     * @param path   The initial path of the file or directory. If null, it is placed at the root level.
     * @param filter A file filter that specifies which files or directories can be added. If
     *               {@link Predicate#test(Object)} is {@code true}, the file is kept. If null, all are added.
     * @return this
     */
    default Archiver add(final File file, final String path, final Predicate<File> filter) {
        return add(file, path, Function.identity(), filter);
    }

    /**
     * Adds a file or directory to the archive package. Directories are added recursively level by level.
     *
     * @param file           The file or directory.
     * @param path           The initial path of the file or directory. If null, it is placed at the root level.
     * @param fileNameEditor A function to edit the file name.
     * @param filter         A file filter that specifies which files or directories can be added. If
     *                       {@link Predicate#test(Object)} is {@code true}, the file is kept. If null, all are added.
     * @return this
     */
    Archiver add(File file, String path, Function<String, String> fileNameEditor, Predicate<File> filter);

    /**
     * Finishes archiving the added files. This method does not close the archive stream, allowing more files to be
     * added.
     *
     * @return this
     */
    Archiver finish();

    /**
     * Closes without throwing an exception.
     */
    @Override
    void close();

}
