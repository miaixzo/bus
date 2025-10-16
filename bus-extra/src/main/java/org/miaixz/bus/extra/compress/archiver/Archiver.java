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
package org.miaixz.bus.extra.compress.archiver;

import java.io.Closeable;
import java.io.File;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Data archiving wrapper, which archives several files or directories into a compressed package.
 *
 * @author Kimi Liu
 * @since Java 17+
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
