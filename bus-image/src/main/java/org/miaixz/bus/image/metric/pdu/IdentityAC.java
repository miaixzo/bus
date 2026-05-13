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
package org.miaixz.bus.image.metric.pdu;

import org.miaixz.bus.image.Builder;

/**
 * Represents the IdentityAC type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IdentityAC {

    /**
     * The server response value.
     */
    private final byte[] serverResponse;

    /**
     * Creates a new instance.
     *
     * @param serverResponse the server response.
     */
    public IdentityAC(byte[] serverResponse) {
        this.serverResponse = serverResponse.clone();
    }

    /**
     * Gets the server response.
     *
     * @return the server response.
     */
    public final byte[] getServerResponse() {
        return serverResponse.clone();
    }

    /**
     * Executes the length operation.
     *
     * @return the operation result.
     */
    public int length() {
        return 2 + serverResponse.length;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

    /**
     * Executes the prompt to operation.
     *
     * @param sb the sb.
     * @return the operation result.
     */
    StringBuilder promptTo(StringBuilder sb) {
        return sb.append("  UserIdentity[").append(Builder.LINE_SEPARATOR).append("    serverResponse: byte[")
                .append(serverResponse.length).append(']').append(Builder.LINE_SEPARATOR).append("  ]");
    }

}
