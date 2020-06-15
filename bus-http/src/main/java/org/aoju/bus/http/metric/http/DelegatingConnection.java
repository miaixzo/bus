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
package org.aoju.bus.http.metric.http;

import org.aoju.bus.http.metric.Handshake;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

/**
 * 实现HTTPS连接的方法是，除了特定于HTTP的内容外，所有内容都委托给HTTP连接
 *
 * @author Kimi Liu
 * @version 5.9.9
 * @since JDK 1.8+
 */
public abstract class DelegatingConnection extends HttpsURLConnection {
    private final HttpURLConnection delegate;

    public DelegatingConnection(HttpURLConnection delegate) {
        super(delegate.getURL());
        this.delegate = delegate;
    }

    protected abstract Handshake handshake();

    @Override
    public abstract HostnameVerifier getHostnameVerifier();

    @Override
    public abstract void setHostnameVerifier(HostnameVerifier hostnameVerifier);

    @Override
    public abstract SSLSocketFactory getSSLSocketFactory();

    @Override
    public abstract void setSSLSocketFactory(SSLSocketFactory sslSocketFactory);

    @Override
    public String getCipherSuite() {
        Handshake handshake = handshake();
        return handshake != null ? handshake.cipherSuite().javaName() : null;
    }

    @Override
    public Certificate[] getLocalCertificates() {
        Handshake handshake = handshake();
        if (handshake == null) return null;
        List<Certificate> result = handshake.localCertificates();
        return !result.isEmpty() ? result.toArray(new Certificate[result.size()]) : null;
    }

    @Override
    public Certificate[] getServerCertificates() {
        Handshake handshake = handshake();
        if (handshake == null) return null;
        List<Certificate> result = handshake.peerCertificates();
        return !result.isEmpty() ? result.toArray(new Certificate[result.size()]) : null;
    }

    @Override
    public Principal getPeerPrincipal() {
        Handshake handshake = handshake();
        return handshake != null ? handshake.peerPrincipal() : null;
    }

    @Override
    public Principal getLocalPrincipal() {
        Handshake handshake = handshake();
        return handshake != null ? handshake.localPrincipal() : null;
    }

    @Override
    public void connect() throws IOException {
        connected = true;
        delegate.connect();
    }

    @Override
    public void disconnect() {
        delegate.disconnect();
    }

    @Override
    public InputStream getErrorStream() {
        return delegate.getErrorStream();
    }

    @Override
    public String getRequestMethod() {
        return delegate.getRequestMethod();
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        delegate.setRequestMethod(method);
    }

    @Override
    public int getResponseCode() throws IOException {
        return delegate.getResponseCode();
    }

    @Override
    public String getResponseMessage() throws IOException {
        return delegate.getResponseMessage();
    }

    @Override
    public boolean usingProxy() {
        return delegate.usingProxy();
    }

    @Override
    public boolean getInstanceFollowRedirects() {
        return delegate.getInstanceFollowRedirects();
    }

    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        delegate.setInstanceFollowRedirects(followRedirects);
    }

    @Override
    public boolean getAllowUserInteraction() {
        return delegate.getAllowUserInteraction();
    }

    @Override
    public void setAllowUserInteraction(boolean newValue) {
        delegate.setAllowUserInteraction(newValue);
    }

    @Override
    public Object getContent() throws IOException {
        return delegate.getContent();
    }

    @Override
    public Object getContent(Class[] types) throws IOException {
        return delegate.getContent(types);
    }

    @Override
    public String getContentEncoding() {
        return delegate.getContentEncoding();
    }

    @Override
    public int getContentLength() {
        return delegate.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return delegate.getContentLengthLong();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public long getDate() {
        return delegate.getDate();
    }

    @Override
    public boolean getDefaultUseCaches() {
        return delegate.getDefaultUseCaches();
    }

    @Override
    public void setDefaultUseCaches(boolean newValue) {
        delegate.setDefaultUseCaches(newValue);
    }

    @Override
    public boolean getDoInput() {
        return delegate.getDoInput();
    }

    @Override
    public void setDoInput(boolean newValue) {
        delegate.setDoInput(newValue);
    }

    @Override
    public boolean getDoOutput() {
        return delegate.getDoOutput();
    }

    @Override
    public void setDoOutput(boolean newValue) {
        delegate.setDoOutput(newValue);
    }

    @Override
    public long getExpiration() {
        return delegate.getExpiration();
    }

    @Override
    public String getHeaderField(int pos) {
        return delegate.getHeaderField(pos);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return delegate.getHeaderFields();
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return delegate.getRequestProperties();
    }

    @Override
    public void addRequestProperty(String field, String newValue) {
        delegate.addRequestProperty(field, newValue);
    }

    @Override
    public String getHeaderField(String key) {
        return delegate.getHeaderField(key);
    }

    @Override
    public long getHeaderFieldLong(String field, long defaultValue) {
        return delegate.getHeaderFieldLong(field, defaultValue);
    }

    @Override
    public long getHeaderFieldDate(String field, long defaultValue) {
        return delegate.getHeaderFieldDate(field, defaultValue);
    }

    @Override
    public int getHeaderFieldInt(String field, int defaultValue) {
        return delegate.getHeaderFieldInt(field, defaultValue);
    }

    @Override
    public String getHeaderFieldKey(int position) {
        return delegate.getHeaderFieldKey(position);
    }

    @Override
    public long getIfModifiedSince() {
        return delegate.getIfModifiedSince();
    }

    @Override
    public void setIfModifiedSince(long newValue) {
        delegate.setIfModifiedSince(newValue);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @Override
    public long getLastModified() {
        return delegate.getLastModified();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }

    @Override
    public Permission getPermission() throws IOException {
        return delegate.getPermission();
    }

    @Override
    public String getRequestProperty(String field) {
        return delegate.getRequestProperty(field);
    }

    @Override
    public URL getURL() {
        return delegate.getURL();
    }

    @Override
    public boolean getUseCaches() {
        return delegate.getUseCaches();
    }

    @Override
    public void setUseCaches(boolean newValue) {
        delegate.setUseCaches(newValue);
    }

    @Override
    public void setFixedLengthStreamingMode(long contentLength) {
        delegate.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public void setRequestProperty(String field, String newValue) {
        delegate.setRequestProperty(field, newValue);
    }

    @Override
    public int getConnectTimeout() {
        return delegate.getConnectTimeout();
    }

    @Override
    public void setConnectTimeout(int timeoutMillis) {
        delegate.setConnectTimeout(timeoutMillis);
    }

    @Override
    public int getReadTimeout() {
        return delegate.getReadTimeout();
    }

    @Override
    public void setReadTimeout(int timeoutMillis) {
        delegate.setReadTimeout(timeoutMillis);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void setFixedLengthStreamingMode(int contentLength) {
        delegate.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public void setChunkedStreamingMode(int chunkLength) {
        delegate.setChunkedStreamingMode(chunkLength);
    }

}