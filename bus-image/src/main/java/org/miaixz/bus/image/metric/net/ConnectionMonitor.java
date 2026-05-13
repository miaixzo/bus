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
package org.miaixz.bus.image.metric.net;

import java.net.Socket;

import org.miaixz.bus.image.metric.Connection;

/**
 * Defines the ConnectionMonitor contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ConnectionMonitor {

    /**
     * Executes the on connection established operation.
     *
     * @param conn       the conn.
     * @param remoteConn the remote conn.
     * @param s          the s.
     */
    void onConnectionEstablished(Connection conn, Connection remoteConn, Socket s);

    /**
     * Executes the on connection failed operation.
     *
     * @param conn       the conn.
     * @param remoteConn the remote conn.
     * @param s          the s.
     * @param e          the e.
     */
    void onConnectionFailed(Connection conn, Connection remoteConn, Socket s, Throwable e);

    /**
     * Executes the on connection rejected blacklisted operation.
     *
     * @param conn the conn.
     * @param s    the s.
     */
    void onConnectionRejectedBlacklisted(Connection conn, Socket s);

    /**
     * Executes the on connection rejected operation.
     *
     * @param conn the conn.
     * @param s    the s.
     * @param e    the e.
     */
    void onConnectionRejected(Connection conn, Socket s, Throwable e);

    /**
     * Executes the on connection accepted operation.
     *
     * @param conn the conn.
     * @param s    the s.
     */
    void onConnectionAccepted(Connection conn, Socket s);

}
