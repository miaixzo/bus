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
