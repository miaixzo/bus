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

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class NoPresentationException extends IOException {

    @Serial
    private static final long serialVersionUID = 2852273099158L;

    public NoPresentationException(String cuid) {
        super(toMessage(cuid));
    }

    public NoPresentationException(String cuid, String tsuid) {
        super(toMessage(cuid, tsuid));
    }

    private static String toMessage(String cuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("No Presentation Context for Abstract Syntax: ");
        UID.promptTo(cuid, sb);
        sb.append(" negotiated");
        return sb.toString();
    }

    private static String toMessage(String cuid, String tsuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("No Presentation Context for Abstract Syntax: ");
        UID.promptTo(cuid, sb);
        sb.append(" with Transfer Syntax: ");
        UID.promptTo(tsuid, sb);
        sb.append(" negotiated");
        return sb.toString();
    }

}
