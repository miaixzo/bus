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

import java.io.IOException;
import java.io.Serial;

import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.metric.TransferCapability;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class NoRolesException extends IOException {

    @Serial
    private static final long serialVersionUID = 2852273231891L;

    public NoRolesException(String cuid, TransferCapability.Role role) {
        super(toMessage(cuid, role));
    }

    private static String toMessage(String cuid, TransferCapability.Role role) {
        StringBuilder sb = new StringBuilder();
        sb.append("No Role Selection for SOP Class ");
        UID.promptTo(cuid, sb);
        sb.append(" as ").append(role).append(" negotiated");
        return sb.toString();
    }

}
