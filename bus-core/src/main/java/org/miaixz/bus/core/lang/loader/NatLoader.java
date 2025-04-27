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
 * Jar包资源加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NatLoader extends StdLoader implements Loader {

    public NatLoader() {

    }

    public Enumeration<Resource> load(String path, Class<?> clazz) throws IOException {
        if (null == path || !path.startsWith(Symbol.SLASH)) {
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");
        }

        // 从路径获取文件名
        String[] parts = path.split(Symbol.SLASH);
        String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

        // 检查文件名是否正确
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
