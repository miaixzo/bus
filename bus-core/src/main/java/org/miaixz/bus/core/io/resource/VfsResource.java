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
package org.miaixz.bus.core.io.resource;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;

/**
 * VFS resource encapsulation. Supports VFS 3.x on JBoss AS 6+, JBoss AS 7 and WildFly 8+. This class provides an
 * abstraction for accessing resources within a Virtual File System (VFS), typically found in application servers like
 * JBoss/WildFly.
 * <p>
 * Inspired by {@code org.springframework.core.io.VfsUtils}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VfsResource implements Resource {

    /**
     * The package prefix for JBoss VFS 3.x classes.
     */
    private static final String VFS3_PKG = "org.jboss.vfs.";

    /**
     * Method reference for {@code VirtualFile.exists()}.
     */
    private static final Method VIRTUAL_FILE_METHOD_EXISTS;
    /**
     * Method reference for {@code VirtualFile.openStream()}.
     */
    private static final Method VIRTUAL_FILE_METHOD_GET_INPUT_STREAM;
    /**
     * Method reference for {@code VirtualFile.getSize()}.
     */
    private static final Method VIRTUAL_FILE_METHOD_GET_SIZE;
    /**
     * Method reference for {@code VirtualFile.getLastModified()}.
     */
    private static final Method VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED;
    /**
     * Method reference for {@code VirtualFile.toURL()}.
     */
    private static final Method VIRTUAL_FILE_METHOD_TO_URL;
    /**
     * Method reference for {@code VirtualFile.getName()}.
     */
    private static final Method VIRTUAL_FILE_METHOD_GET_NAME;
    /**
     * Method reference for {@code VirtualFile.getPhysicalFile()}.
     */
    private static final Method VIRTUAL_FILE_METHOD_GET_PHYSICAL_FILE;

    static {
        final Class<?> virtualFile = ClassKit.loadClass(VFS3_PKG + "VirtualFile");
        try {
            VIRTUAL_FILE_METHOD_EXISTS = virtualFile.getMethod("exists");
            VIRTUAL_FILE_METHOD_GET_INPUT_STREAM = virtualFile.getMethod("openStream");
            VIRTUAL_FILE_METHOD_GET_SIZE = virtualFile.getMethod("getSize");
            VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED = virtualFile.getMethod("getLastModified");
            VIRTUAL_FILE_METHOD_TO_URL = virtualFile.getMethod("toURL");
            VIRTUAL_FILE_METHOD_GET_NAME = virtualFile.getMethod("getName");
            VIRTUAL_FILE_METHOD_GET_PHYSICAL_FILE = virtualFile.getMethod("getPhysicalFile");
        } catch (final NoSuchMethodException ex) {
            throw new IllegalStateException("Could not detect JBoss VFS infrastructure", ex);
        }
    }

    /**
     * The underlying {@code org.jboss.vfs.VirtualFile} instance.
     */
    private final Object virtualFile;
    /**
     * The last modified timestamp of the resource at the time of creation.
     */
    private final long lastModified;

    /**
     * Constructs a {@code VfsResource} with the given JBoss VFS {@code VirtualFile} object.
     *
     * @param resource The {@code org.jboss.vfs.VirtualFile} instance.
     * @throws IllegalArgumentException if the {@code resource} is {@code null}.
     */
    public VfsResource(final Object resource) {
        Assert.notNull(resource, "VirtualFile must not be null");
        this.virtualFile = resource;
        this.lastModified = getLastModified();
    }

    /**
     * Checks if the VFS file represented by this resource exists.
     *
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    public boolean exists() {
        return MethodKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_EXISTS);
    }

    /**
     * Returns the name of this VFS resource.
     *
     * @return The name of the VFS file.
     */
    @Override
    public String getName() {
        return MethodKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_GET_NAME);
    }

    /**
     * Returns the URL of this VFS resource.
     *
     * @return The URL representing the VFS file.
     */
    @Override
    public URL getUrl() {
        return MethodKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_TO_URL);
    }

    /**
     * Returns an input stream for this VFS resource.
     *
     * @return An input stream for reading the VFS file.
     */
    @Override
    public InputStream getStream() {
        return MethodKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_GET_INPUT_STREAM);
    }

    /**
     * Checks if this VFS resource has been modified since it was last accessed.
     *
     * @return {@code true} if the VFS file has been modified, {@code false} otherwise.
     */
    @Override
    public boolean isModified() {
        return this.lastModified != getLastModified();
    }

    /**
     * Retrieves the last modified timestamp of the VFS file.
     *
     * @return The last modified timestamp in milliseconds since the epoch.
     */
    public long getLastModified() {
        return MethodKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED);
    }

    /**
     * Retrieves the size of the VFS file.
     *
     * @return The size of the VFS file in bytes.
     */
    @Override
    public long size() {
        return MethodKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_GET_SIZE);
    }

    /**
     * Retrieves the physical {@link File} object corresponding to the VFS resource. This may return {@code null} if the
     * VFS resource does not have a direct physical file representation.
     *
     * @return The physical {@link File} object, or {@code null}.
     */
    public File getFile() {
        return MethodKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_GET_PHYSICAL_FILE);
    }

}
