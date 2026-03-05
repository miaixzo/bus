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

import org.miaixz.bus.core.lang.Normal;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AAssociateRQ extends AAssociateRQAC {

    @Override
    public void setIdentityAC(IdentityAC identityAC) {
        throw new UnsupportedOperationException();
    }

    public boolean containsPresentationContextFor(String as) {
        for (PresentationContext pc : pcs)
            if (as.equals(pc.getAbstractSyntax()))
                return true;
        return false;
    }

    public boolean containsPresentationContextFor(String as, String ts) {
        for (PresentationContext pc : pcs)
            if (as.equals(pc.getAbstractSyntax()) && pc.containsTransferSyntax(ts))
                return true;
        return false;
    }

    public boolean addPresentationContextFor(String as, String ts) {
        if (containsPresentationContextFor(as, ts))
            return false;

        int pcid = getNumberOfPresentationContexts() * 2 + 1;
        addPresentationContext(new PresentationContext(pcid, as, ts));
        return true;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512)).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        return promptTo("A-ASSOCIATE-RQ[", sb);
    }

}
