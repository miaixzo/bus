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

import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.pdu.AAssociateRJ;
import org.miaixz.bus.image.metric.pdu.IdentityAC;
import org.miaixz.bus.image.metric.pdu.IdentityRQ;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public interface IdentityNegotiator {

    default IdentityAC negotiate(Association as, IdentityRQ userIdentity) throws AAssociateRJ {
        return userIdentity != null && userIdentity.isPositiveResponseRequested()
                ? new IdentityAC(userIdentity.getType() > 2 ? userIdentity.getPrimaryField() : new byte[] {})
                : null;
    }

}
