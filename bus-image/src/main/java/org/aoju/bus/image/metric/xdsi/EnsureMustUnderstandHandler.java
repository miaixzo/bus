/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.metric.xdsi;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class EnsureMustUnderstandHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext ctx) {
        if (((Boolean) ctx.get("javax.xml.ws.handler.message.outbound")).booleanValue()) {
            try {
                Iterator<SOAPHeaderElement> iter = ctx.getMessage().getSOAPHeader().examineAllHeaderElements();
                while (iter.hasNext()) {
                    SOAPHeaderElement hdr = iter.next();
                    switch (hdr.getNodeName()) {
                        case "Action":
                        case "To":
                        case "ReplyTo":

                            hdr.setMustUnderstand(true);
                    }
                }
            } catch (SOAPException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {

        return true;
    }

    public void close(MessageContext context) {
    }

}
