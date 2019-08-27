package org.aoju.bus.tracer.binding.jaxws;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.transport.SoapHeaderTransport;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Map;


public class TraceClientHandler extends AbstractTraceHandler {


    private final SoapHeaderTransport transportSerialization = new SoapHeaderTransport();

    public TraceClientHandler() {
        this(Builder.getBackend());
    }

    public TraceClientHandler(Backend Backend) {
        super(Backend);
    }

    @Override
    public final boolean handleFault(final SOAPMessageContext context) {
        return true;
    }

    protected final void handleIncoming(final SOAPMessageContext context) {

        final SOAPMessage msg = context.getMessage();
        if (msg != null && Backend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingRequest)) {

            try {
                final SOAPHeader header = msg.getSOAPHeader();

                if (header != null) {
                    final Map<String, String> parsedContext = transportSerialization.parseSoapHeader(header);
                    Backend.putAll(Backend.getConfiguration().filterDeniedParams(parsedContext, TraceFilterConfiguration.Channel.OutgoingRequest));
                }
            } catch (final SOAPException e) {
                Logger.warn("Error during precessing of inbound soap header: " + e.getMessage());
                Logger.debug("Detailed: Error during precessing of inbound soap header: {}", e.getMessage(), e);
            }
        }
    }

    protected final void handleOutgoing(final SOAPMessageContext context) {

        final SOAPMessage msg = context.getMessage();
        if (msg != null && !Backend.isEmpty() && Backend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.IncomingResponse)) {

            try {
                final SOAPHeader header = getOrCreateHeader(msg);

                final Map<String, String> filteredContext = Backend.getConfiguration().filterDeniedParams(Backend.copyToMap(), TraceFilterConfiguration.Channel.IncomingResponse);
                transportSerialization.renderSoapHeader(filteredContext, header);

                msg.saveChanges();
            } catch (final SOAPException e) {
                Logger.warn("TraceClientHandler : Exception occurred during processing of outbound message.", e);
            }
        }
    }

}
