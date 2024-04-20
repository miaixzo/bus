/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.io.resource;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.ReflectKit;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * VFS资源封装
 * 参考：org.springframework.core.io.VfsUtils
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VfsResource implements Resource {

    private static final String VFS3_PKG = "org.jboss.vfs.";

    private static final Method VIRTUAL_FILE_METHOD_EXISTS;
    private static final Method VIRTUAL_FILE_METHOD_GET_INPUT_STREAM;
    private static final Method VIRTUAL_FILE_METHOD_GET_SIZE;
    private static final Method VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED;
    private static final Method VIRTUAL_FILE_METHOD_TO_URL;
    private static final Method VIRTUAL_FILE_METHOD_GET_NAME;

    static {
        Class<?> virtualFile = ClassKit.loadClass(VFS3_PKG + "VirtualFile");
        try {
            VIRTUAL_FILE_METHOD_EXISTS = virtualFile.getMethod("exists");
            VIRTUAL_FILE_METHOD_GET_INPUT_STREAM = virtualFile.getMethod("openStream");
            VIRTUAL_FILE_METHOD_GET_SIZE = virtualFile.getMethod("getSize");
            VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED = virtualFile.getMethod("getLastModified");
            VIRTUAL_FILE_METHOD_TO_URL = virtualFile.getMethod("toURL");
            VIRTUAL_FILE_METHOD_GET_NAME = virtualFile.getMethod("getName");
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Could not detect JBoss VFS infrastructure", ex);
        }
    }

    /**
     * org.jboss.vfs.VirtualFile实例对象
     */
    private final Object virtualFile;
    private final long lastModified;

    /**
     * 构造
     *
     * @param resource org.jboss.vfs.VirtualFile实例对象
     */
    public VfsResource(Object resource) {
        Assert.notNull(resource, "VirtualFile must not be null");
        this.virtualFile = resource;
        this.lastModified = getLastModified();
    }

    /**
     * VFS文件是否存在
     *
     * @return 文件是否存在
     */
    public boolean exists() {
        return ReflectKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_EXISTS);
    }

    @Override
    public String getName() {
        return ReflectKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_GET_NAME);
    }

    @Override
    public URL getUrl() {
        return ReflectKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_TO_URL);
    }

    @Override
    public InputStream getStream() {
        return ReflectKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_GET_INPUT_STREAM);
    }

    @Override
    public boolean isModified() {
        return this.lastModified != getLastModified();
    }

    /**
     * 获得VFS文件最后修改时间
     *
     * @return 最后修改时间
     */
    public long getLastModified() {
        return ReflectKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED);
    }

    /**
     * 获取VFS文件大小
     *
     * @return VFS文件大小
     */
    public long size() {
        return ReflectKit.invoke(virtualFile, VIRTUAL_FILE_METHOD_GET_SIZE);
    }

}