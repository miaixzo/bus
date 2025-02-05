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
