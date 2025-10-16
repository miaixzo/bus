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
package org.miaixz.bus.core.io.compress;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Zip file replacer, used to replace content in a source Zip file and generate a new file.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipReplacer implements Closeable {

    /**
     * The Zip reader for the source Zip file.
     */
    private final ZipReader zipReader;
    /**
     * Flag indicating whether to ignore case when comparing paths.
     */
    private final boolean ignoreCase;

    /**
     * A map of resources to replace, where the key is the entry path and the value is the replacement resource.
     */
    private final Map<String, Resource> replacedResources = new HashMap<>();

    /**
     * Constructs a new ZipReplacer instance.
     *
     * @param zipReader  The ZipReader for the source Zip file.
     * @param ignoreCase {@code true} to ignore case when comparing paths, {@code false} otherwise.
     */
    public ZipReplacer(final ZipReader zipReader, final boolean ignoreCase) {
        this.zipReader = zipReader;
        this.ignoreCase = ignoreCase;
    }

    /**
     * Checks if two paths are the same, with an option to ignore case.
     *
     * @param entryPath  The first path.
     * @param targetPath The second path.
     * @param ignoreCase {@code true} to ignore case during comparison, {@code false} otherwise.
     * @return {@code true} if the paths are the same, {@code false} otherwise.
     */
    private static boolean isSamePath(String entryPath, String targetPath, final boolean ignoreCase) {
        entryPath = StringKit.removePrefix(FileName.normalize(entryPath), Symbol.SLASH);
        targetPath = StringKit.removePrefix(FileName.normalize(targetPath), Symbol.SLASH);
        return StringKit.equals(entryPath, targetPath, ignoreCase);
    }

    /**
     * Adds a resource to be replaced. If the path does not match an existing entry, no replacement occurs, and the
     * resource is not added.
     *
     * @param entryPath The path of the entry to be replaced within the Zip file.
     * @param resource  The resource containing the content for replacement.
     * @return This ZipReplacer instance.
     */
    public ZipReplacer addReplace(final String entryPath, final Resource resource) {
        replacedResources.put(entryPath, resource);
        return this;
    }

    /**
     * Writes the modified Zip content to the specified {@link ZipWriter}. Entries that are not replaced will be copied
     * from the source Zip file.
     *
     * @param writer The {@link ZipWriter} to write the modified content to.
     */
    public void write(final ZipWriter writer) {
        zipReader.read((entry) -> {
            String entryName;
            for (final String key : replacedResources.keySet()) {
                entryName = entry.getName();
                if (isSamePath(entryName, key, ignoreCase)) {
                    writer.add(key, replacedResources.get(key).getStream());
                } else {
                    writer.add(entryName, zipReader.get(entryName));
                }
            }
        });
    }

    @Override
    public void close() throws IOException {
        this.zipReader.close();
    }

}
