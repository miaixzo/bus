/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.bridge;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.office.builtin.MadeInOffice;
import org.aoju.bus.office.metric.AbstractOfficeEntryManager;
import org.aoju.bus.office.metric.RequestBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 负责执行通过不依赖于office安装的{@link OnlineOfficePoolManager}提交的任务。
 * 它将向LibreOffice在线服务器发送转换请求，并等待任务完成或达到配置的任务执行超时.
 *
 * @author Kimi Liu
 * @version 5.5.2
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
                    new URI(StringUtils.replace(url.toString(), Symbol.SPACE, "%20")).getSchemeSpecificPart());
        } catch (URISyntaxException ex) {
            return new File(url.getFile());
        }
    }

    private static File getFile(final String resourceLocation) throws FileNotFoundException {
        Assert.notNull(resourceLocation, "Resource location must not be null");
        if (resourceLocation.startsWith("classpath:")) {
            final String path = resourceLocation.substring("classpath:".length());
            final String description = "class path resource [" + path + "]";
            final ClassLoader cl = ClassUtils.getDefaultClassLoader();
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

    /**
     * Https SSL证书
     *
     * @param X509TrustManager 证书信息
     * @return SSLSocketFactory 安全套接字
     */
    private static SSLSocketFactory createTrustAllSSLFactory(X509TrustManager X509TrustManager) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{X509TrustManager}, new SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 HostnameVerifier
     *
     * @return the object
     */
    private static HostnameVerifier createTrustAllHostnameVerifier() {
        return (hostname, session) -> true;
    }

    private String buildUrl(final String connectionUrl) throws MalformedURLException {
        final URL url = new URL(connectionUrl);
        final String path = url.toExternalForm().toLowerCase();
        if (StringUtils.endsWithAny(path, "lool/convert-to", "lool/convert-to/")) {
            return StringUtils.appendIfMissing(connectionUrl, Symbol.SLASH);
        } else if (StringUtils.endsWithAny(path, "lool", "lool/")) {
            return StringUtils.appendIfMissing(connectionUrl, Symbol.SLASH) + "convert-to/";
        }
        return StringUtils.appendIfMissing(connectionUrl, Symbol.SLASH) + "lool/convert-to/";
    }

    @Override
    protected void doExecute(final MadeInOffice task) throws InstrumentException {
        try {
            final RequestBuilder requestBuilder =
                    new RequestBuilder(
                            buildUrl(connectionUrl),
                            Math.toIntExact(config.getTaskExecutionTimeout()),
                            Math.toIntExact(config.getTaskExecutionTimeout()));
            task.execute(new OnlineOfficeBridgeFactory(new Httpx(), requestBuilder));

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

    private KeyStore loadStore(
            final String store,
            final String storePassword,
            final String storeType,
            final String storeProvider)
            throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException,
            NoSuchProviderException {

        if (store != null) {
            Assert.notNull(storePassword, "The password of store {0} must not be null", store);

            KeyStore keyStore;

            final String type = storeType == null ? KeyStore.getDefaultType() : storeType;
            if (storeProvider == null) {
                keyStore = KeyStore.getInstance(type);
            } else {
                keyStore = KeyStore.getInstance(type, storeProvider);
            }

            try (FileInputStream instream = new FileInputStream(getFile(store))) {
                keyStore.load(instream, storePassword.toCharArray());
            }

            return keyStore;
        }
        return null;
    }

    private static class X509TrustManager implements javax.net.ssl.X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

}
