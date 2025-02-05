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
package org.miaixz.bus.shade.safety.boot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;

/**
 * 加密的URL连接
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BootURLConnection extends JarURLConnection {

    private final JarURLConnection jarURLConnection;
    private final DecryptorProvider decryptorProvider;
    private final EncryptorProvider encryptorProvider;
    private final Key key;

    public BootURLConnection(JarURLConnection jarURLConnection, DecryptorProvider decryptorProvider,
            EncryptorProvider encryptorProvider, Key key) throws MalformedURLException {
        super(jarURLConnection.getURL());
        this.jarURLConnection = jarURLConnection;
        this.decryptorProvider = decryptorProvider;
        this.encryptorProvider = encryptorProvider;
        this.key = key;
    }

    @Override
    public void connect() throws IOException {
        jarURLConnection.connect();
    }

    @Override
    public int getConnectTimeout() {
        return jarURLConnection.getConnectTimeout();
    }

    @Override
    public void setConnectTimeout(int timeout) {
        jarURLConnection.setConnectTimeout(timeout);
    }

    @Override
    public int getReadTimeout() {
        return jarURLConnection.getReadTimeout();
    }

    @Override
    public void setReadTimeout(int timeout) {
        jarURLConnection.setReadTimeout(timeout);
    }

    @Override
    public URL getURL() {
        return jarURLConnection.getURL();
    }

    @Override
    public int getContentLength() {
        return jarURLConnection.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return jarURLConnection.getContentLengthLong();
    }

    @Override
    public String getContentType() {
        return jarURLConnection.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return jarURLConnection.getContentEncoding();
    }

    @Override
    public long getExpiration() {
        return jarURLConnection.getExpiration();
    }

    @Override
    public long getDate() {
        return jarURLConnection.getDate();
    }

    @Override
    public long getLastModified() {
        return jarURLConnection.getLastModified();
    }

    @Override
    public String getHeaderField(String name) {
        return jarURLConnection.getHeaderField(name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return jarURLConnection.getHeaderFields();
    }

    @Override
    public int getHeaderFieldInt(String name, int Default) {
        return jarURLConnection.getHeaderFieldInt(name, Default);
    }

    @Override
    public long getHeaderFieldLong(String name, long Default) {
        return jarURLConnection.getHeaderFieldLong(name, Default);
    }

    @Override
    public long getHeaderFieldDate(String name, long Default) {
        return jarURLConnection.getHeaderFieldDate(name, Default);
    }

    @Override
    public String getHeaderFieldKey(int n) {
        return jarURLConnection.getHeaderFieldKey(n);
    }

    @Override
    public String getHeaderField(int n) {
        return jarURLConnection.getHeaderField(n);
    }

    @Override
    public Object getContent() throws IOException {
        return jarURLConnection.getContent();
    }

    @Override
    public Object getContent(Class[] classes) throws IOException {
        return jarURLConnection.getContent(classes);
    }

    @Override
    public Permission getPermission() throws IOException {
        return jarURLConnection.getPermission();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream in = jarURLConnection.getInputStream();
        return decryptorProvider.decrypt(key, in);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream out = jarURLConnection.getOutputStream();
        return encryptorProvider.encrypt(key, out);
    }

    @Override
    public String toString() {
        return jarURLConnection.toString();
    }

    @Override
    public boolean getDoInput() {
        return jarURLConnection.getDoInput();
    }

    @Override
    public void setDoInput(boolean doInput) {
        jarURLConnection.setDoInput(doInput);
    }

    @Override
    public boolean getDoOutput() {
        return jarURLConnection.getDoOutput();
    }

    @Override
    public void setDoOutput(boolean doOutput) {
        jarURLConnection.setDoOutput(doOutput);
    }

    @Override
    public boolean getAllowUserInteraction() {
        return jarURLConnection.getAllowUserInteraction();
    }

    @Override
    public void setAllowUserInteraction(boolean allowUserInteraction) {
        jarURLConnection.setAllowUserInteraction(allowUserInteraction);
    }

    @Override
    public boolean getUseCaches() {
        return jarURLConnection.getUseCaches();
    }

    @Override
    public void setUseCaches(boolean useCaches) {
        jarURLConnection.setUseCaches(useCaches);
    }

    @Override
    public long getIfModifiedSince() {
        return jarURLConnection.getIfModifiedSince();
    }

    @Override
    public void setIfModifiedSince(long ifModifiedSince) {
        jarURLConnection.setIfModifiedSince(ifModifiedSince);
    }

    @Override
    public boolean getDefaultUseCaches() {
        return jarURLConnection.getDefaultUseCaches();
    }

    @Override
    public void setDefaultUseCaches(boolean defaultUseCaches) {
        jarURLConnection.setDefaultUseCaches(defaultUseCaches);
    }

    @Override
    public void setRequestProperty(String key, String value) {
        jarURLConnection.setRequestProperty(key, value);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        jarURLConnection.addRequestProperty(key, value);
    }

    @Override
    public String getRequestProperty(String key) {
        return jarURLConnection.getRequestProperty(key);
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return jarURLConnection.getRequestProperties();
    }

    @Override
    public URL getJarFileURL() {
        return jarURLConnection.getJarFileURL();
    }

    @Override
    public String getEntryName() {
        return jarURLConnection.getEntryName();
    }

    @Override
    public JarFile getJarFile() throws IOException {
        return jarURLConnection.getJarFile();
    }

    @Override
    public Manifest getManifest() throws IOException {
        return jarURLConnection.getManifest();
    }

    @Override
    public JarEntry getJarEntry() throws IOException {
        return jarURLConnection.getJarEntry();
    }

    @Override
    public Attributes getAttributes() throws IOException {
        return jarURLConnection.getAttributes();
    }

    @Override
    public Attributes getMainAttributes() throws IOException {
        return jarURLConnection.getMainAttributes();
    }

    @Override
    public Certificate[] getCertificates() throws IOException {
        return jarURLConnection.getCertificates();
    }
}
