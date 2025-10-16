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
package org.miaixz.bus.extra.ssh;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.ssh.provider.jsch.ChannelType;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Jsch (Java Secure Channel) utility class. JSch is a pure Java implementation of the SSH2 protocol, enabling
 * connections to SSH servers for operations like port forwarding, X11 forwarding, file transfers, and remote command
 * execution.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JschKit {

    /**
     * Opens an SSH session with the specified connection details. This method configures the session with the host,
     * port, user, password, and timeout from the provided {@link Connector}. It also sets default configurations for
     * host key checking and authentication methods.
     *
     * @param connector The {@link Connector} object containing connection information (host, port, user, password,
     *                  timeout).
     * @return An initialized JSch {@link Session} object, not yet connected.
     * @throws InternalException if a {@link JSchException} occurs during session creation.
     */
    public static Session openSession(final Connector connector) {
        final JSch jsch = new JSch();
        final com.jcraft.jsch.Session session;
        try {
            session = jsch.getSession(connector.getUser(), connector.getHost(), connector.getPort());
            session.setTimeout((int) connector.getTimeout());
        } catch (final JSchException e) {
            throw new InternalException(e);
        }

        session.setPassword(connector.getPassword());
        // Set prompt for first login, possible values: (ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");

        // Set preferred authentication methods, skipping Kerberos authentication for broader compatibility
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");

        return session;
    }

    /**
     * Opens and connects an SSH channel of a specified type. This method first creates the channel and then connects it
     * with the given timeout.
     *
     * @param session     The active SSH {@link Session}.
     * @param channelType The type of channel to open (e.g., shell, sftp), as defined in {@link ChannelType}.
     * @param timeout     The connection timeout duration in milliseconds.
     * @return A connected JSch {@link Channel} object.
     * @throws InternalException if a {@link JSchException} occurs during channel connection.
     */
    public static Channel openChannel(final Session session, final ChannelType channelType, final long timeout) {
        final Channel channel = createChannel(session, channelType, timeout);
        try {
            channel.connect((int) Math.max(timeout, 0));
        } catch (final JSchException e) {
            throw new InternalException(e);
        }
        return channel;
    }

    /**
     * Creates an SSH channel but does not connect it. If the session is not already connected, this method will connect
     * it first.
     *
     * @param session     The SSH {@link Session}.
     * @param channelType The type of channel to create (e.g., shell, sftp), as defined in {@link ChannelType}.
     * @param timeout     The session connection timeout duration in milliseconds, used if the session is not yet
     *                    connected.
     * @return An unconnected JSch {@link Channel} object.
     * @throws InternalException if a {@link JSchException} occurs during session connection or channel creation.
     */
    public static Channel createChannel(final Session session, final ChannelType channelType, final long timeout) {
        final Channel channel;
        try {
            if (!session.isConnected()) {
                session.connect((int) timeout);
            }
            channel = session.openChannel(channelType.getValue());
        } catch (final JSchException e) {
            throw new InternalException(e);
        }
        return channel;
    }

    /**
     * Closes the specified SSH session if it is not null and is currently connected.
     *
     * @param session The SSH {@link Session} to close.
     */
    public static void close(final Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * Closes the specified SSH channel if it is not null and is currently connected.
     *
     * @param channel The SSH {@link Channel} to close.
     */
    public static void close(final Channel channel) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
    }

}
