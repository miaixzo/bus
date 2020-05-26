/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.office.bridge;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.ClassKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.office.builtin.MadeInOffice;
import org.aoju.bus.office.metric.AbstractOfficeEntryManager;
import org.aoju.bus.office.metric.RequestBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 负责执行通过不依赖于office安装的{@link OnlineOfficePoolManager}提交的任务。
 * 它将向LibreOffice在线服务器发送转换请求，并等待任务完成或达到配置的任务执行超时.
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class OnlineOfficeEntryManager extends AbstractOfficeEntryManager {

    private final String connectionUrl;

    /**
     * 使用指定的配置创建新的池条目.
     *
     * @param connectionUrl 指向LibreOffice在线服务器的URL.
     * @param config        输入配置.
     */
    public OnlineOfficeEntryManager(
            final String connectionUrl,
            final OnlineOfficeEntryBuilder config) {
        super(config);

        this.connectionUrl = connectionUrl;
    }

    private static File getFile(final URL url) {
        try {
            return new File(
                    new URI(StringKit.replace(url.toString(), Symbol.SPACE, "%20")).getSchemeSpecificPart());
        } catch (URISyntaxException ex) {
            return new File(url.getFile());
        }
    }

    private static File getFile(final String resourceLocation) throws FileNotFoundException {
        Assert.notNull(resourceLocation, "Resource location must not be null");
        if (resourceLocation.startsWith("classpath:")) {
            final String path = resourceLocation.substring("classpath:".length());
            final String description = "class path resource [" + path + "]";
            final ClassLoader cl = ClassKit.getDefaultClassLoader();
            final URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                throw new FileNotFoundException(
                        description + " cannot be resolved to absolute file path because it does not exist");
            }
            return getFile(url.toString());
        }
        try {
            return getFile(new URL(resourceLocation));
        } catch (MalformedURLException ex) {
            return new File(resourceLocation);
        }
    }

    private String buildUrl(final String connectionUrl) throws MalformedURLException {
        final URL url = new URL(connectionUrl);
        final String path = url.toExternalForm().toLowerCase();
        if (StringKit.endsWithAny(path, "lool/convert-to", "lool/convert-to/")) {
            return StringKit.appendIfMissing(connectionUrl, Symbol.SLASH);
        } else if (StringKit.endsWithAny(path, "lool", "lool/")) {
            return StringKit.appendIfMissing(connectionUrl, Symbol.SLASH) + "convert-to/";
        }
        return StringKit.appendIfMissing(connectionUrl, Symbol.SLASH) + "lool/convert-to/";
    }

    @Override
    protected void doExecute(final MadeInOffice task) throws InstrumentException {
        try {
            final RequestBuilder requestBuilder =
                    new RequestBuilder(
                            buildUrl(connectionUrl),
                            Math.toIntExact(config.getTaskExecutionTimeout()),
                            Math.toIntExact(config.getTaskExecutionTimeout()));
            task.execute(new OnlineOfficeBridgeFactory(requestBuilder));

        } catch (IOException ex) {
            throw new InstrumentException("Unable to create the HTTP client", ex);
        }
    }

    @Override
    protected void doStart() throws InstrumentException {
        taskExecutor.setAvailable(true);
    }

    @Override
    protected void doStop() throws InstrumentException {
        // Nothing to stop here.
    }

}
