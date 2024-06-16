/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * 资源接口定义
 * <p>资源是数据表示的统称，我们可以将任意的数据封装为一个资源，然后读取其内容。</p>
 * <p>资源可以是文件、URL、ClassPath中的文件亦或者jar(zip)包中的文件。</p>
 * <p>
 * 提供资源接口的意义在于，我们可以使用一个方法接收任意类型的数据，从而处理数据，
 * 无需专门针对File、InputStream等写多个重载方法，同时也为更好的扩展提供了可能。
 * </p>
 * <p>使用非常简单，假设我们需要从classpath中读取一个xml，我们不用关心这个文件在目录中还是在jar中：</p>
 * <pre>
 *     Resource resource = new ClassPathResource("test.xml");
 *     String xmlStr = resource.readString();
 * </pre>
 * <p>同样，我们可以自己实现Resource接口，按照业务需要从任意位置读取数据，比如从数据库中。</p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Resource {

    /**
     * 获取资源名，例如文件资源的资源名为文件名
     *
     * @return 资源名
     */
    String getName();

    /**
     * 获得解析后的{@link URL}，无对应URL的返回{@code null}
     *
     * @return 解析后的{@link URL}
     */
    URL getUrl();

    /**
     * 获取资源大小
     *
     * @return 资源大小
     */
    long size();

    /**
     * 获得 {@link InputStream}
     *
     * @return {@link InputStream}
     */
    InputStream getStream();

    /**
     * 检查资源是否变更
     * 一般用于文件类资源，检查文件是否被修改过。
     *
     * @return 是否变更
     */
    default boolean isModified() {
        return false;
    }

    /**
     * 将资源内容写出到流，不关闭输出流，但是关闭资源流
     *
     * @param out 输出流
     * @throws InternalException IO异常
     */
    default void writeTo(final OutputStream out) throws InternalException {
        try (final InputStream in = getStream()) {
            IoKit.copy(in, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得Reader
     *
     * @param charset 编码
     * @return {@link BufferedReader}
     */
    default BufferedReader getReader(final java.nio.charset.Charset charset) {
        return IoKit.toReader(getStream(), charset);
    }

    /**
     * 读取资源内容，读取完毕后会关闭流
     * 关闭流并不影响下一次读取
     *
     * @param charset 编码
     * @return 读取资源内容
     * @throws InternalException 包装{@link IOException}
     */
    default String readString(final java.nio.charset.Charset charset) throws InternalException {
        return IoKit.read(getReader(charset));
    }

    /**
     * 读取资源内容，读取完毕后会关闭流
     * 关闭流并不影响下一次读取
     *
     * @return 读取资源内容
     * @throws InternalException 包装IOException
     */
    default String readString() throws InternalException {
        return readString(Charset.UTF_8);
    }

    /**
     * 读取资源内容，读取完毕后会关闭流
     * 关闭流并不影响下一次读取
     *
     * @return 读取资源内容
     * @throws InternalException 包装IOException
     */
    default byte[] readBytes() throws InternalException {
        return IoKit.readBytes(getStream());
    }

}
