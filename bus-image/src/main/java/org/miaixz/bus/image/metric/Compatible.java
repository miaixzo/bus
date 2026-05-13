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
package org.miaixz.bus.image.metric;

/**
 * Represents the Compatible type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Compatible {

    /**
     * The local conn value.
     */
    private final Connection localConn;

    /**
     * The remote conn value.
     */
    private final Connection remoteConn;

    /**
     * Creates a new instance.
     *
     * @param localConn  the local conn.
     * @param remoteConn the remote conn.
     */
    public Compatible(Connection localConn, Connection remoteConn) {
        this.localConn = localConn;
        this.remoteConn = remoteConn;
    }

    /**
     * Gets the local connection.
     *
     * @return the local connection.
     */
    public final Connection getLocalConnection() {
        return localConn;
    }

    /**
     * Gets the remote connection.
     *
     * @return the remote connection.
     */
    public final Connection getRemoteConnection() {
        return remoteConn;
    }

}
