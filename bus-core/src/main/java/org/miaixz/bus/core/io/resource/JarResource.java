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
package org.miaixz.bus.core.io.resource;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ZipKit;

/**
 * Jar包资源对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarResource extends UrlResource {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param uri JAR的URI
     */
    public JarResource(final URI uri) {
        super(uri);
    }

    /**
     * 构造
     *
     * @param url JAR的URL
     */
    public JarResource(final URL url) {
        super(url);
    }

    /**
     * 构造
     *
     * @param url  JAR的URL
     * @param name 资源名称
     */
    public JarResource(final URL url, final String name) {
        super(url, name);
    }

    /**
     * 获取URL对应的{@link JarFile}对象
     *
     * @return {@link JarFile}
     * @throws InternalException IO异常
     */
    public JarFile getJarFile() throws InternalException {
        try {
            return doGetJarFile();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取{@link JarFile} 首席按通过openConnection方式获取，如果得到的不是{@link JarURLConnection}，
     * 则尝试去除WAR、JAR等协议分隔符，裁剪分隔符前段来直接获取{@link JarFile}。
     *
     * @return {@link JarFile}
     * @throws IOException IO异常
     */
    private JarFile doGetJarFile() throws IOException {
        final URLConnection con = getUrl().openConnection();
        if (con instanceof JarURLConnection) {
            final JarURLConnection jarCon = (JarURLConnection) con;
            return jarCon.getJarFile();
        } else {
            final String urlFile = getUrl().getFile();
            int separatorIndex = urlFile.indexOf(Normal.WAR_URL_SEPARATOR);
            if (separatorIndex == -1) {
                separatorIndex = urlFile.indexOf(Normal.JAR_URL_SEPARATOR);
            }
            if (separatorIndex != -1) {
                return ZipKit.ofJar(urlFile.substring(0, separatorIndex));
            } else {
                return new JarFile(urlFile);
            }
        }
    }

}
