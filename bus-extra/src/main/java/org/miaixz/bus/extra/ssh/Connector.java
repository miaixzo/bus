/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.ssh;

/**
 * Represents a connector object that provides basic connection information for services like SSH, FTP, etc. This class
 * encapsulates the following details:
 * <ul>
 * <li>host: The hostname or IP address of the server.</li>
 * <li>port: The port number for the connection.</li>
 * <li>user: The username for authentication (defaults to "root").</li>
 * <li>password: The password for authentication.</li>
 * <li>timeout: The connection timeout in milliseconds.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Connector {

    /**
     * The hostname or IP address of the server.
     */
    private String host;
    /**
     * The port number for the connection.
     */
    private int port;
    /**
     * The username for authentication, defaulting to "root".
     */
    private String user = "root";
    /**
     * The password for authentication.
     */
    private String password;
    /**
     * The connection timeout duration in milliseconds.
     */
    private long timeout;

    /**
     * Constructs a new, empty {@code Connector} instance.
     */
    public Connector() {
    }

    /**
     * Constructs a new {@code Connector} instance with specified host, port, user, password, and timeout.
     *
     * @param host     The hostname or IP address of the server.
     * @param port     The port number for the connection.
     * @param user     The username for authentication.
     * @param password The password for authentication.
     * @param timeout  The connection timeout duration in milliseconds; a value of 0 may indicate the default timeout.
     */
    public Connector(final String host, final int port, final String user, final String password, final long timeout) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.timeout = timeout;
    }

    /**
     * Creates a new {@code Connector} instance with default parameters. This is useful for a builder-style pattern of
     * object creation.
     *
     * @return A new, empty {@code Connector} instance.
     */
    public static Connector of() {
        return new Connector();
    }

    /**
     * Creates a new {@code Connector} instance with specified host, port, user, and password. The timeout is set to its
     * default value (0).
     *
     * @param host     The hostname or IP address of the server.
     * @param port     The port number for the connection.
     * @param user     The username for authentication.
     * @param password The password for authentication.
     * @return A new {@code Connector} instance initialized with the provided details.
     */
    public static Connector of(final String host, final int port, final String user, final String password) {
        return of(host, port, user, password, 0);
    }

    /**
     * Creates a new {@code Connector} instance with specified host, port, user, password, and timeout.
     *
     * @param host     The hostname or IP address of the server.
     * @param port     The port number for the connection.
     * @param user     The username for authentication.
     * @param password The password for authentication.
     * @param timeout  The connection timeout duration in milliseconds; a value of 0 may indicate the default timeout.
     * @return A new {@code Connector} instance initialized with the provided details.
     */
    public static Connector of(
            final String host,
            final int port,
            final String user,
            final String password,
            final long timeout) {
        return new Connector(host, port, user, password, timeout);
    }

    /**
     * Retrieves the hostname or IP address of the server.
     *
     * @return The hostname as a {@link String}.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the hostname or IP address of the server.
     *
     * @param host The hostname to set.
     * @return This {@code Connector} instance, allowing for method chaining.
     */
    public Connector setHost(final String host) {
        this.host = host;
        return this;
    }

    /**
     * Retrieves the port number for the connection.
     *
     * @return The port number.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number for the connection.
     *
     * @param port The port number to set.
     * @return This {@code Connector} instance, allowing for method chaining.
     */
    public Connector setPort(final int port) {
        this.port = port;
        return this;
    }

    /**
     * Retrieves the username for authentication.
     *
     * @return The username as a {@link String}.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the username for authentication.
     *
     * @param name The username to set.
     * @return This {@code Connector} instance, allowing for method chaining.
     */
    public Connector setUser(final String name) {
        this.user = name;
        return this;
    }

    /**
     * Retrieves the password for authentication.
     *
     * @return The password as a {@link String}.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for authentication.
     *
     * @param password The password to set.
     * @return This {@code Connector} instance, allowing for method chaining.
     */
    public Connector setPassword(final String password) {
        this.password = password;
        return this;
    }

    /**
     * Retrieves the connection timeout duration in milliseconds.
     *
     * @return The connection timeout duration.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the connection timeout duration in milliseconds.
     *
     * @param timeout The connection timeout duration to set.
     * @return This {@code Connector} instance, allowing for method chaining.
     */
    public Connector setTimeout(final long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Returns a string representation of the connector object. This method is intended for logging and debugging
     * purposes only.
     *
     * @return A string representation of the connector's properties.
     */
    @Override
    public String toString() {
        return "Connector{" + "host='" + host + '\'' + ", port=" + port + ", user='" + user + '\'' + ", password='"
                + password + '\'' + ", timeout=" + timeout + '}';
    }

}
