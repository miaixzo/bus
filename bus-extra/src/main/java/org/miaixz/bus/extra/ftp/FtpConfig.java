/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.ftp;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.miaixz.bus.extra.ssh.Connector;

/**
 * Configuration class for FTP (File Transfer Protocol) operations. This class encapsulates various parameters required
 * to establish and manage an FTP connection, including connection details, character encoding, timeouts, and
 * server-specific settings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FtpConfig implements Serializable {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852292979822L;

    /**
     * Connection information, including host, port, username, and password. This object is typically used to establish
     * the initial connection to the FTP server.
     */
    private Connector connector;
    /**
     * The character set used for encoding and decoding file names and other text-based data during FTP operations.
     * Defaults to UTF-8 if not specified.
     */
    private Charset charset;
    /**
     * The socket connection timeout duration in milliseconds. This specifies how long to wait for a connection to be
     * established before timing out.
     */
    private long soTimeout;
    /**
     * The server language code, which can be used for locale-specific FTP commands or responses.
     */
    private String serverLanguageCode;
    /**
     * A keyword identifying the server system type, which might influence certain FTP command behaviors.
     */
    private String systemKey;

    /**
     * Constructs a new, empty {@code FtpConfig} instance. All fields are initialized to their default values (null or
     * 0).
     */
    public FtpConfig() {
    }

    /**
     * Constructs a new {@code FtpConfig} instance with specified connection information and character set.
     *
     * @param connector The {@link Connector} object containing host, port, user, password, etc.
     * @param charset   The {@link Charset} to use for encoding and decoding.
     */
    public FtpConfig(final Connector connector, final Charset charset) {
        this(connector, charset, null, null);
    }

    /**
     * Constructs a new {@code FtpConfig} instance with specified connection information, character set, server language
     * code, and system key.
     *
     * @param connector          The {@link Connector} object containing host, port, user, password, etc.
     * @param charset            The {@link Charset} to use for encoding and decoding.
     * @param serverLanguageCode The server language code, e.g., "en", "zh".
     * @param systemKey          The server system keyword, e.g., "UNIX", "WINDOWS".
     */
    public FtpConfig(final Connector connector, final Charset charset, final String serverLanguageCode,
            final String systemKey) {
        this.connector = connector;
        this.charset = charset;
        this.serverLanguageCode = serverLanguageCode;
        this.systemKey = systemKey;
    }

    /**
     * Creates a default {@code FtpConfig} instance with no parameters set. This is a static factory method for
     * convenient object creation.
     *
     * @return A new {@code FtpConfig} instance.
     */
    public static FtpConfig of() {
        return new FtpConfig();
    }

    /**
     * Retrieves the connection information for the FTP server.
     *
     * @return The {@link Connector} object containing host, port, user, password, etc.
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * Sets the connection information for the FTP server.
     *
     * @param connector The {@link Connector} object to set.
     * @return This {@code FtpConfig} instance, allowing for method chaining.
     */
    public FtpConfig setConnector(final Connector connector) {
        this.connector = connector;
        return this;
    }

    /**
     * Sets the connection timeout duration for the FTP connection. If the {@link Connector} object is null, a new one
     * will be created.
     *
     * @param timeout The connection timeout duration in milliseconds.
     * @return This {@code FtpConfig} instance, allowing for method chaining.
     */
    public FtpConfig setConnectionTimeout(final long timeout) {
        if (null == connector) {
            connector = Connector.of();
        }
        connector.setTimeout(timeout);
        return this;
    }

    /**
     * Retrieves the character set used for FTP operations.
     *
     * @return The {@link Charset} used for encoding and decoding.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Sets the character set for FTP operations.
     *
     * @param charset The {@link Charset} to set.
     * @return This {@code FtpConfig} instance, allowing for method chaining.
     */
    public FtpConfig setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Retrieves the socket read timeout duration in milliseconds. This specifies how long to wait for data to be read
     * from the socket before timing out.
     *
     * @return The socket read timeout duration in milliseconds.
     */
    public long getSoTimeout() {
        return soTimeout;
    }

    /**
     * Sets the socket read timeout duration in milliseconds.
     *
     * @param soTimeout The socket read timeout duration to set.
     * @return This {@code FtpConfig} instance, allowing for method chaining.
     */
    public FtpConfig setSoTimeout(final long soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    /**
     * Retrieves the server language code.
     *
     * @return The server language code as a {@link String}.
     */
    public String getServerLanguageCode() {
        return serverLanguageCode;
    }

    /**
     * Sets the server language code.
     *
     * @param serverLanguageCode The server language code to set.
     * @return This {@code FtpConfig} instance, allowing for method chaining.
     */
    public FtpConfig setServerLanguageCode(final String serverLanguageCode) {
        this.serverLanguageCode = serverLanguageCode;
        return this;
    }

    /**
     * Retrieves the server system keyword.
     *
     * @return The server system keyword as a {@link String}.
     */
    public String getSystemKey() {
        return systemKey;
    }

    /**
     * Sets the server system keyword.
     *
     * @param systemKey The server system keyword to set.
     * @return This {@code FtpConfig} instance, allowing for method chaining.
     */
    public FtpConfig setSystemKey(final String systemKey) {
        this.systemKey = systemKey;
        return this;
    }

}
