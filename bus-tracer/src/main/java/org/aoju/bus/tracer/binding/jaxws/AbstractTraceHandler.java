/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.aoju.bus.tracer.binding.jaxws;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.consts.TraceConsts;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
abstract class AbstractTraceHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Set<QName> HANDLED_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Collections.singleton(TraceConsts.SOAP_HEADER_QNAME)));

    protected final Backend Backend;

    public AbstractTraceHandler(Backend Backend) {
        this.Backend = Backend;
    }

    @Override
    public final boolean handleMessage(final SOAPMessageContext context) {
        if (this.isOutgoing(context)) {
            this.handleOutgoing(context);
        } else {
            this.handleIncoming(context);
        }
        return true;
    }

    private boolean isOutgoing(MessageContext messageContext) {
        Object outboundBoolean = messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        return outboundBoolean != null && (Boolean) outboundBoolean;
    }

    @Override
    public void close(MessageContext context) {
    }

    protected abstract void handleIncoming(SOAPMessageContext context);

    protected abstract void handleOutgoing(SOAPMessageContext context);

    @Override
    public Set<QName> getHeaders() {
        return HANDLED_HEADERS;
    }

    SOAPHeader getOrCreateHeader(final SOAPMessage message) throws SOAPException {
        SOAPHeader header = message.getSOAPHeader();
        if (header == null) {
            header = message.getSOAPPart().getEnvelope().addHeader();
        }
        return header;
    }

}
