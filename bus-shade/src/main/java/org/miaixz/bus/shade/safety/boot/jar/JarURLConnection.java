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
package org.miaixz.bus.shade.safety.boot.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
 * A custom {@link java.net.JarURLConnection} that intercepts input and output streams to provide on-the-fly decryption
 * and encryption of JAR entries. This is used in conjunction with {@link JarURLHandler} to handle encrypted resources
 * within standard JARs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarURLConnection extends java.net.JarURLConnection {

    private final java.net.JarURLConnection jarURLConnection;
    private final DecryptorProvider decryptorProvider;
    private final EncryptorProvider encryptorProvider;
    private final Key key;

    /**
     * Constructs a new {@code JarURLConnection}.
     *
     * @param jarURLConnection  The underlying {@link java.net.JarURLConnection} to delegate to.
     * @param decryptorProvider The provider responsible for decrypting input streams.
     * @param encryptorProvider The provider responsible for encrypting output streams.
     * @param key               The cryptographic key used for encryption and decryption.
     * @throws MalformedURLException If the URL of the underlying connection is malformed.
     */
    public JarURLConnection(java.net.JarURLConnection jarURLConnection, DecryptorProvider decryptorProvider,
            EncryptorProvider encryptorProvider, Key key) throws MalformedURLException {
        super(jarURLConnection.getURL());
        this.jarURLConnection = jarURLConnection;
        this.decryptorProvider = decryptorProvider;
        this.encryptorProvider = encryptorProvider;
        this.key = key;
    }

    /**
     * Establishes a connection to the resource referred to by this URL.
     *
     * @throws IOException If an I/O error occurs while establishing the connection.
     */
    @Override
    public void connect() throws IOException {
        jarURLConnection.connect();
    }

    /**
     * Returns the value of this {@code URLConnection}'s {@code connectTimeout} setting.
     *
     * @return The connect timeout in milliseconds.
     */
    @Override
    public int getConnectTimeout() {
        return jarURLConnection.getConnectTimeout();
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used when opening a communications link to the resource.
     *
     * @param timeout The connect timeout in milliseconds.
     */
    @Override
    public void setConnectTimeout(int timeout) {
        jarURLConnection.setConnectTimeout(timeout);
    }

    /**
     * Returns the value of this {@code URLConnection}'s {@code readTimeout} setting.
     *
     * @return The read timeout in milliseconds.
     */
    @Override
    public int getReadTimeout() {
        return jarURLConnection.getReadTimeout();
    }

    /**
     * Sets the read timeout to a specified timeout, in milliseconds.
     *
     * @param timeout The read timeout in milliseconds.
     */
    @Override
    public void setReadTimeout(int timeout) {
        jarURLConnection.setReadTimeout(timeout);
    }

    /**
     * Returns the URL of this {@code URLConnection}.
     *
     * @return The URL of this {@code URLConnection}.
     */
    @Override
    public URL getURL() {
        return jarURLConnection.getURL();
    }

    /**
     * Returns the value of the {@code content-length} header field.
     *
     * @return The content length, or -1 if not known.
     */
    @Override
    public int getContentLength() {
        return jarURLConnection.getContentLength();
    }

    /**
     * Returns the value of the {@code content-length} header field as a long.
     *
     * @return The content length, or -1 if not known.
     */
    @Override
    public long getContentLengthLong() {
        return jarURLConnection.getContentLengthLong();
    }

    /**
     * Returns the value of the {@code content-type} header field.
     *
     * @return The content type, or {@code null} if not known.
     */
    @Override
    public String getContentType() {
        return jarURLConnection.getContentType();
    }

    /**
     * Returns the value of the {@code content-encoding} header field.
     *
     * @return The content encoding, or {@code null} if not known.
     */
    @Override
    public String getContentEncoding() {
        return jarURLConnection.getContentEncoding();
    }

    /**
     * Returns the value of the {@code expires} header field.
     *
     * @return The expiration date, or 0 if not known.
     */
    @Override
    public long getExpiration() {
        return jarURLConnection.getExpiration();
    }

    /**
     * Returns the value of the {@code date} header field.
     *
     * @return The date, or 0 if not known.
     */
    @Override
    public long getDate() {
        return jarURLConnection.getDate();
    }

    /**
     * Returns the value of the {@code last-modified} header field.
     *
     * @return The last modified date, or 0 if not known.
     */
    @Override
    public long getLastModified() {
        return jarURLConnection.getLastModified();
    }

    /**
     * Returns the value of the named header field.
     *
     * @param name The name of a header field.
     * @return The value of the named header field, or {@code null} if not known.
     */
    @Override
    public String getHeaderField(String name) {
        return jarURLConnection.getHeaderField(name);
    }

    /**
     * Returns an unmodifiable Map of header fields.
     *
     * @return A {@code Map} of header fields.
     */
    @Override
    public Map<String, List<String>> getHeaderFields() {
        return jarURLConnection.getHeaderFields();
    }

    /**
     * Returns the value of the named header field as an integer.
     *
     * @param name    The name of a header field.
     * @param Default The default value.
     * @return The value of the named header field, or {@code Default} if not known.
     */
    @Override
    public int getHeaderFieldInt(String name, int Default) {
        return jarURLConnection.getHeaderFieldInt(name, Default);
    }

    /**
     * Returns the value of the named header field as a long.
     *
     * @param name    The name of a header field.
     * @param Default The default value.
     * @return The value of the named header field, or {@code Default} if not known.
     */
    @Override
    public long getHeaderFieldLong(String name, long Default) {
        return jarURLConnection.getHeaderFieldLong(name, Default);
    }

    /**
     * Returns the value of the named header field as a date.
     *
     * @param name    The name of a header field.
     * @param Default The default value.
     * @return The value of the named header field, or {@code Default} if not known.
     */
    @Override
    public long getHeaderFieldDate(String name, long Default) {
        return jarURLConnection.getHeaderFieldDate(name, Default);
    }

    /**
     * Returns the key for the {@code n}<sup>th</sup> header field.
     *
     * @param n The index of the header field.
     * @return The key for the {@code n}<sup>th</sup> header field, or {@code null} if fewer than {@code n} fields
     *         exist.
     */
    @Override
    public String getHeaderFieldKey(int n) {
        return jarURLConnection.getHeaderFieldKey(n);
    }

    /**
     * Returns the value for the {@code n}<sup>th</sup> header field.
     *
     * @param n The index of the header field.
     * @return The value for the {@code n}<sup>th</sup> header field, or {@code null} if fewer than {@code n} fields
     *         exist.
     */
    @Override
    public String getHeaderField(int n) {
        return jarURLConnection.getHeaderField(n);
    }

    /**
     * Retrieves the content of the URL connection.
     *
     * @return The content of the URL connection.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Object getContent() throws IOException {
        return jarURLConnection.getContent();
    }

    /**
     * Retrieves the content of the URL connection, trying to match one of the specified classes.
     *
     * @param classes An array of {@code Class} objects indicating the preferred types.
     * @return The content of the URL connection.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Object getContent(Class[] classes) throws IOException {
        return jarURLConnection.getContent(classes);
    }

    /**
     * Returns the permission object required by this {@code URLConnection} to make a connection to the resource.
     *
     * @return The permission object.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Permission getPermission() throws IOException {
        return jarURLConnection.getPermission();
    }

    /**
     * Returns an input stream that reads from this open connection. The stream returned will be a decrypted stream if
     * the resource is encrypted.
     *
     * @return An input stream that reads from this open connection.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        InputStream in = jarURLConnection.getInputStream();
        return decryptorProvider.decrypt(key, in);
    }

    /**
     * Returns an output stream that writes to this connection. The stream returned will be an encrypted stream if the
     * resource is to be encrypted.
     *
     * @return An output stream that writes to this connection.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream out = jarURLConnection.getOutputStream();
        return encryptorProvider.encrypt(key, out);
    }

    /**
     * Returns a string representation of this URL connection.
     *
     * @return A string representation of this URL connection.
     */
    @Override
    public String toString() {
        return jarURLConnection.toString();
    }

    /**
     * Returns the value of the {@code doInput} field for this {@code URLConnection}.
     *
     * @return The value of the {@code doInput} field.
     */
    @Override
    public boolean getDoInput() {
        return jarURLConnection.getDoInput();
    }

    /**
     * Sets the value of the {@code doInput} field for this {@code URLConnection}.
     *
     * @param doInput The new value.
     */
    @Override
    public void setDoInput(boolean doInput) {
        jarURLConnection.setDoInput(doInput);
    }

    /**
     * Returns the value of the {@code doOutput} field for this {@code URLConnection}.
     *
     * @return The value of the {@code doOutput} field.
     */
    @Override
    public boolean getDoOutput() {
        return jarURLConnection.getDoOutput();
    }

    /**
     * Sets the value of the {@code doOutput} field for this {@code URLConnection}.
     *
     * @param doOutput The new value.
     */
    @Override
    public void setDoOutput(boolean doOutput) {
        jarURLConnection.setDoOutput(doOutput);
    }

    /**
     * Returns the value of the {@code allowUserInteraction} field for this {@code URLConnection}.
     *
     * @return The value of the {@code allowUserInteraction} field.
     */
    @Override
    public boolean getAllowUserInteraction() {
        return jarURLConnection.getAllowUserInteraction();
    }

    /**
     * Sets the value of the {@code allowUserInteraction} field for this {@code URLConnection}.
     *
     * @param allowUserInteraction The new value.
     */
    @Override
    public void setAllowUserInteraction(boolean allowUserInteraction) {
        jarURLConnection.setAllowUserInteraction(allowUserInteraction);
    }

    /**
     * Returns the value of the {@code useCaches} field for this {@code URLConnection}.
     *
     * @return The value of the {@code useCaches} field.
     */
    @Override
    public boolean getUseCaches() {
        return jarURLConnection.getUseCaches();
    }

    /**
     * Sets the value of the {@code useCaches} field for this {@code URLConnection}.
     *
     * @param useCaches The new value.
     */
    @Override
    public void setUseCaches(boolean useCaches) {
        jarURLConnection.setUseCaches(useCaches);
    }

    /**
     * Returns the value of the {@code ifModifiedSince} field for this {@code URLConnection}.
     *
     * @return The value of the {@code ifModifiedSince} field.
     */
    @Override
    public long getIfModifiedSince() {
        return jarURLConnection.getIfModifiedSince();
    }

    /**
     * Sets the value of the {@code ifModifiedSince} field for this {@code URLConnection}.
     *
     * @param ifModifiedSince The new value.
     */
    @Override
    public void setIfModifiedSince(long ifModifiedSince) {
        jarURLConnection.setIfModifiedSince(ifModifiedSince);
    }

    /**
     * Returns the default value of the {@code useCaches} field.
     *
     * @return The default value of the {@code useCaches} field.
     */
    @Override
    public boolean getDefaultUseCaches() {
        return jarURLConnection.getDefaultUseCaches();
    }

    /**
     * Sets the default value of the {@code useCaches} field.
     *
     * @param defaultUseCaches The new default value.
     */
    @Override
    public void setDefaultUseCaches(boolean defaultUseCaches) {
        jarURLConnection.setDefaultUseCaches(defaultUseCaches);
    }

    /**
     * Sets the general request property value for the specified key.
     *
     * @param key   The header field name.
     * @param value The header field value.
     */
    @Override
    public void setRequestProperty(String key, String value) {
        jarURLConnection.setRequestProperty(key, value);
    }

    /**
     * Adds a general request property value for the specified key.
     *
     * @param key   The header field name.
     * @param value The header field value.
     */
    @Override
    public void addRequestProperty(String key, String value) {
        jarURLConnection.addRequestProperty(key, value);
    }

    /**
     * Returns the value of the named general request property for this connection.
     *
     * @param key The name of the request header field.
     * @return The value of the named general request property, or {@code null} if not set.
     */
    @Override
    public String getRequestProperty(String key) {
        return jarURLConnection.getRequestProperty(key);
    }

    /**
     * Returns an unmodifiable Map of general request properties for this connection.
     *
     * @return A {@code Map} of general request properties.
     */
    @Override
    public Map<String, List<String>> getRequestProperties() {
        return jarURLConnection.getRequestProperties();
    }

    /**
     * Returns the URL for the JAR file.
     *
     * @return The URL for the JAR file.
     */
    @Override
    public URL getJarFileURL() {
        return jarURLConnection.getJarFileURL();
    }

    /**
     * Returns the entry name of this connection.
     *
     * @return The entry name.
     */
    @Override
    public String getEntryName() {
        return jarURLConnection.getEntryName();
    }

    /**
     * Returns the JAR file for this connection.
     *
     * @return The JAR file.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public JarFile getJarFile() throws IOException {
        return jarURLConnection.getJarFile();
    }

    /**
     * Returns the manifest for this connection.
     *
     * @return The manifest.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Manifest getManifest() throws IOException {
        return jarURLConnection.getManifest();
    }

    /**
     * Returns the JAR entry for this connection.
     *
     * @return The JAR entry.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public JarEntry getJarEntry() throws IOException {
        return jarURLConnection.getJarEntry();
    }

    /**
     * Returns the attributes for this connection.
     *
     * @return The attributes.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Attributes getAttributes() throws IOException {
        return jarURLConnection.getAttributes();
    }

    /**
     * Returns the main attributes for this connection.
     *
     * @return The main attributes.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Attributes getMainAttributes() throws IOException {
        return jarURLConnection.getMainAttributes();
    }

    /**
     * Returns the certificates for this connection.
     *
     * @return The certificates.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Certificate[] getCertificates() throws IOException {
        return jarURLConnection.getCertificates();
    }

}
