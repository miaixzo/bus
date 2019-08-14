package org.aoju.bus.trace4j.binding.jaxws;


import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.Utilities;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.transport.SoapHeaderTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Map;


public class TraceServerHandler extends AbstractTraceHandler {

    private static final Logger logger = LoggerFactory.getLogger(TraceServerHandler.class);

    private final SoapHeaderTransport transportSerialization;

    public TraceServerHandler() {
        this(Trace.getBackend(), new SoapHeaderTransport());
    }

    public TraceServerHandler(TraceBackend TraceBackend, SoapHeaderTransport soapHeaderTransport) {
        super(TraceBackend);
        this.transportSerialization = soapHeaderTransport;
    }

    protected final void handleIncoming(SOAPMessageContext context) {
        final SOAPMessage soapMessage = context.getMessage();
        try {
            final SOAPHeader header = soapMessage.getSOAPHeader();

            if (header != null && TraceBackend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.IncomingRequest)) {
                final Map<String, String> parsedContext = transportSerialization.parseSoapHeader(header);
                final Map<String, String> filteredContext = TraceBackend.getConfiguration().filterDeniedParams(parsedContext, TraceFilterConfiguration.Channel.IncomingRequest);
                TraceBackend.putAll(filteredContext);
            }
        } catch (final SOAPException e) {
            logger.warn("Error during precessing of inbound soap header: {}", e.getMessage());
            logger.debug("Detailed: Error during precessing of inbound soap header: {}", e.getMessage(), e);
        }

        Utilities.generateInvocationIdIfNecessary(TraceBackend);
    }

    protected final void handleOutgoing(SOAPMessageContext context) {
        final SOAPMessage msg = context.getMessage();
        try {
            if (msg != null && !TraceBackend.isEmpty() && TraceBackend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingResponse)) {

                final SOAPHeader header = getOrCreateHeader(msg);

                final Map<String, String> filteredContext = TraceBackend.getConfiguration().filterDeniedParams(TraceBackend.copyToMap(), TraceFilterConfiguration.Channel.OutgoingResponse);
                transportSerialization.renderSoapHeader(filteredContext, header);

                msg.saveChanges();
                context.setMessage(msg);
            }

        } catch (final SOAPException e) {
            logger.error("TraceServerHandler : Exception occurred during processing of outbound message.");
            logger.debug("Detailed: TraceServerHandler : Exception occurred during processing of outbound message: {}", e.getMessage(), e);
        } finally {
            TraceBackend.clear();
        }
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        this.handleOutgoing(context);
        return true;
    }

}
