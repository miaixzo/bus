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
package org.miaixz.bus.extra.ftp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.NetKit;

/**
 * A simple encapsulation of an FTP server based on Apache FtpServer (http://apache.apache.org/ftpserver-project/). This
 * class provides convenient methods for configuring and starting an embedded FTP server.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleFtpServer {

    /**
     * The factory for creating and configuring the FTP server instance.
     */
    private final FtpServerFactory serverFactory;
    /**
     * The factory for creating and configuring FTP server listeners.
     */
    private final ListenerFactory listenerFactory;

    /**
     * Constructs a new {@code SimpleFtpServer} instance. Initializes {@link FtpServerFactory} and
     * {@link ListenerFactory}.
     */
    public SimpleFtpServer() {
        serverFactory = new FtpServerFactory();
        listenerFactory = new ListenerFactory();
    }

    /**
     * Creates a new {@code SimpleFtpServer} instance. To start the server, call {@link SimpleFtpServer#start()}.
     *
     * @return A new {@code SimpleFtpServer} instance.
     */
    public static SimpleFtpServer of() {
        return new SimpleFtpServer();
    }

    /**
     * Retrieves the underlying {@link FtpServerFactory} to allow for advanced configuration of the FTP server
     * properties.
     *
     * @return The {@link FtpServerFactory} instance.
     */
    public FtpServerFactory getServerFactory() {
        return this.serverFactory;
    }

    /**
     * Sets the connection configuration for the FTP server. Use {@link org.apache.ftpserver.ConnectionConfigFactory} to
     * create a {@link ConnectionConfig} object.
     *
     * @param connectionConfig The {@link ConnectionConfig} object to set.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     */
    public SimpleFtpServer setConnectionConfig(final ConnectionConfig connectionConfig) {
        this.serverFactory.setConnectionConfig(connectionConfig);
        return this;
    }

    /**
     * Retrieves the underlying {@link ListenerFactory} to allow for advanced configuration of FTP server listeners,
     * such as port, SSL, etc.
     *
     * @return The {@link ListenerFactory} instance.
     */
    public ListenerFactory getListenerFactory() {
        return this.listenerFactory;
    }

    /**
     * Sets the default port for the FTP server. If not set, the default port 21 will be used.
     *
     * @param port The port number to set. Must be a valid port number.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     * @throws IllegalArgumentException if the provided port is not a valid port number.
     */
    public SimpleFtpServer setPort(final int port) {
        Assert.isTrue(NetKit.isValidPort(port), "Invalid port!");
        this.listenerFactory.setPort(port);
        return this;
    }

    /**
     * Retrieves the {@link UserManager} for managing FTP user accounts. This manager can be used to add, find, and
     * delete user information.
     *
     * @return The {@link UserManager} instance.
     */
    public UserManager getUserManager() {
        return this.serverFactory.getUserManager();
    }

    /**
     * Sets a custom {@link UserManager} for the FTP server. This is typically used when user information is configured
     * via a properties file or a custom database.
     *
     * @param userManager The custom {@link UserManager} to set.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     */
    public SimpleFtpServer setUserManager(final UserManager userManager) {
        this.serverFactory.setUserManager(userManager);
        return this;
    }

    /**
     * Adds an FTP user to the server's user manager.
     *
     * @param user The {@link User} object containing FTP user information.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     * @throws InternalException if an {@link org.apache.ftpserver.ftplet.FtpException} occurs during user saving.
     */
    public SimpleFtpServer addUser(final User user) {
        try {
            getUserManager().save(user);
        } catch (final org.apache.ftpserver.ftplet.FtpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Adds an anonymous user to the FTP server. The anonymous user will have read/write permissions to the specified
     * home directory.
     *
     * @param homePath The home directory path for the anonymous user.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     */
    public SimpleFtpServer addAnonymous(final String homePath) {
        final BaseUser user = new BaseUser();
        user.setName("anonymous");
        user.setHomeDirectory(homePath);
        final List<Authority> authorities = new ArrayList<>();
        // Add write permission for the anonymous user
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        return addUser(user);
    }

    /**
     * Deletes an FTP user from the server's user manager.
     *
     * @param userName The username of the FTP user to delete.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     * @throws InternalException if an {@link org.apache.ftpserver.ftplet.FtpException} occurs during user deletion.
     */
    public SimpleFtpServer delUser(final String userName) {
        try {
            getUserManager().delete(userName);
        } catch (final org.apache.ftpserver.ftplet.FtpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Configures the FTP server to use SSL for secure connections. Use {@link SslConfigurationFactory} to create an
     * {@link SslConfiguration} object.
     *
     * @param ssl The {@link SslConfiguration} object containing SSL/TLS settings.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     */
    public SimpleFtpServer setSsl(final SslConfiguration ssl) {
        this.listenerFactory.setSslConfiguration(ssl);
        listenerFactory.setImplicitSsl(true); // Enable implicit SSL
        return this;
    }

    /**
     * Configures the FTP server to use SSL for secure connections with a given keystore file and password.
     *
     * @param keystoreFile The {@link File} object pointing to the keystore file (e.g., JKS, PKCS12).
     * @param password     The password for the keystore file.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     */
    public SimpleFtpServer setSsl(final File keystoreFile, final String password) {
        final SslConfigurationFactory sslFactory = new SslConfigurationFactory();
        sslFactory.setKeystoreFile(keystoreFile);
        sslFactory.setKeystorePassword(password);
        return setSsl(sslFactory.createSslConfiguration());
    }

    /**
     * Sets a custom user information configuration file. This method will reset the current user manager and configure
     * it to load users from the specified properties file.
     *
     * @param propertiesFile The {@link File} object pointing to the user properties file.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     */
    public SimpleFtpServer setUsersConfig(final File propertiesFile) {
        final PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(propertiesFile);
        return this.setUserManager(userManagerFactory.createUserManager());
    }

    /**
     * Adds an FTP action behavior listener (Ftplet) to the server. By implementing {@link Ftplet}, custom logic can be
     * executed in response to user actions.
     *
     * @param name   The name of the Ftplet.
     * @param ftplet The {@link Ftplet} instance, which defines custom listener rules.
     * @return This {@code SimpleFtpServer} instance, allowing for method chaining.
     */
    public SimpleFtpServer addFtplet(final String name, final Ftplet ftplet) {
        this.serverFactory.getFtplets().put(name, ftplet);
        return this;
    }

    /**
     * Starts the FTP server. This method will block the current thread until the server is shut down. A default
     * listener is created if none is explicitly configured.
     *
     * @throws InternalException if an {@link org.apache.ftpserver.ftplet.FtpException} occurs during server startup.
     */
    public void start() {
        // A Listener corresponds to a listening port.
        // Multiple listeners can be created, but here only one is listened to by default.
        serverFactory.addListener("default", listenerFactory.createListener());
        try {
            serverFactory.createServer().start();
        } catch (final org.apache.ftpserver.ftplet.FtpException e) {
            throw new InternalException(e);
        }
    }

}
