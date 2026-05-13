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
package org.miaixz.bus.image.metric.hl7.net;

import java.net.Socket;

import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.HL7Exception;

/**
 * Defines the HL7ConnectionMonitor contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface HL7ConnectionMonitor {

    /**
     * Executes the on message received operation.
     *
     * @param conn the conn.
     * @param s    the s.
     * @param msg  the msg.
     */
    void onMessageReceived(Connection conn, Socket s, UnparsedHL7Message msg);

    /**
     * Executes the on message processed operation.
     *
     * @param conn the conn.
     * @param s    the s.
     * @param msg  the msg.
     * @param rsp  the rsp.
     * @param ex   the ex.
     */
    void onMessageProcessed(Connection conn, Socket s, UnparsedHL7Message msg, UnparsedHL7Message rsp, HL7Exception ex);

    /**
     * Executes the on message sent operation.
     *
     * @param hl7App the hl7 app.
     * @param s      the s.
     * @param msg    the msg.
     * @param ex     the ex.
     */
    void onMessageSent(HL7Application hl7App, Socket s, UnparsedHL7Message msg, Exception ex);

    /**
     * Executes the on message response operation.
     *
     * @param hl7App the hl7 app.
     * @param s      the s.
     * @param msg    the msg.
     * @param rsp    the rsp.
     * @param ex     the ex.
     */
    void onMessageResponse(
            HL7Application hl7App,
            Socket s,
            UnparsedHL7Message msg,
            UnparsedHL7Message rsp,
            Exception ex);

}
