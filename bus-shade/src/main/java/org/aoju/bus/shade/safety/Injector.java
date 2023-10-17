/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.shade.safety;

import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.loader.Loaders;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * 框架注入器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Injector {

    /**
     * 往JAR包中注入框架的classes
     *
     * @param zos jar包输出流
     * @throws IOException I/O 异常
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
