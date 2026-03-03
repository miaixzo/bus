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
package org.miaixz.bus.core.io.resource;

import java.io.Serial;
import java.net.URL;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.core.xyz.*;

/**
 * ClassPath single resource access class. The input path must be a relative path. If an absolute path is provided, a
 * Linux path will have the leading "/" removed, while a Windows path will directly cause an error. The resource pointed
 * to by the input path must exist, otherwise an error will be reported.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassPathResource extends UrlResource {

    @Serial
    private static final long serialVersionUID = 2852230779720L;

    /**
     * The path of the resource relative to the ClassPath.
     */
    private final String path;
    /**
     * The ClassLoader used to load the resource.
     */
    private final ClassLoader classLoader;
    /**
     * The Class used to locate the resource.
     */
    private final Class<?> clazz;

    /**
     * Constructs a {@code ClassPathResource} with the given path relative to the ClassPath.
     *
     * @param path The path relative to the ClassPath.
     */
    public ClassPathResource(final String path) {
        this(path, null, null);
    }

    /**
     * Constructs a {@code ClassPathResource} with the given path and {@link ClassLoader}.
     *
     * @param path        The path relative to the ClassPath.
     * @param classLoader The {@link ClassLoader} to use for loading the resource.
     */
    public ClassPathResource(final String path, final ClassLoader classLoader) {
        this(path, classLoader, null);
    }

    /**
     * Constructs a {@code ClassPathResource} with the given path relative to the specified {@link Class}.
     *
     * @param path  The path relative to the given {@link Class}.
     * @param clazz The {@link Class} used to locate the path.
     */
    public ClassPathResource(final String path, final Class<?> clazz) {
        this(path, null, clazz);
    }

    /**
     * Constructs a {@code ClassPathResource} with the given path, {@link ClassLoader}, and {@link Class}.
     *
     * @param pathBaseClassLoader The path relative to the ClassLoader or Class.
     * @param classLoader         The {@link ClassLoader} to use. If {@code null}, the default ClassLoader is used.
     * @param clazz               The {@link Class} to use for locating the path. If {@code null}, the ClassLoader is
     *                            used.
     */
    public ClassPathResource(final String pathBaseClassLoader, final ClassLoader classLoader, final Class<?> clazz) {
        super((URL) null);
        Assert.notNull(pathBaseClassLoader, "Path must not be null");

        final String path = normalizePath(pathBaseClassLoader);
        this.path = path;
        this.name = StringKit.isBlank(path) ? null : FileName.getName(path);

        this.classLoader = ObjectKit.defaultIfNull(classLoader, ClassKit::getClassLoader);
        this.clazz = clazz;
        initUrl();
    }

    /**
     * Retrieves the path of the resource.
     *
     * @return The path of the resource.
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * Retrieves the absolute path of the resource. For non-existent resources, the concatenated absolute path is
     * returned.
     *
     * @return The absolute path of the resource.
     */
    public final String getAbsolutePath() {
        if (FileKit.isAbsolutePath(this.path)) {
            return this.path;
        }
        // The URL is asserted to be non-null during initialization.
        return FileKit.normalize(UrlKit.getDecodedPath(this.url));
    }

    /**
     * Retrieves the {@link ClassLoader} used by this resource.
     *
     * @return The {@link ClassLoader}.
     */
    public final ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * Initializes the URL of the resource based on the given resource path. Throws {@link NotFoundException} if the
     * resource does not exist.
     */
    private void initUrl() throws NotFoundException {
        if (null != this.clazz) {
            super.url = this.clazz.getResource(this.path);
        } else if (null != this.classLoader) {
            super.url = this.classLoader.getResource(this.path);
        } else {
            super.url = ClassLoader.getSystemResource(this.path);
        }
        if (null == super.url) {
            throw new NotFoundException("Resource of path [{}] not exist!", this.path);
        }
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return (null == this.path) ? super.toString() : Normal.CLASSPATH + this.path;
    }

    /**
     * Normalizes the given path format.
     *
     * @param path The path to normalize.
     * @return The normalized path.
     * @throws IllegalArgumentException if the path is an absolute path.
     */
    private String normalizePath(String path) {
        // Normalize the path
        path = FileKit.normalize(path);
        path = StringKit.removePrefix(path, Symbol.SLASH);

        Assert.isFalse(FileKit.isAbsolutePath(path), "Path [{}] must be a relative path !", path);
        return path;
    }

}
