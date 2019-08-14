package org.aoju.bus.trace4j.binding.jaxws;

import org.aoju.bus.trace4j.TraceBackend;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.List;

public class TraceClientHandlerResolver implements HandlerResolver {

    private final List<Handler> handlerList = new ArrayList<>();

    public TraceClientHandlerResolver() {
        handlerList.add(new TraceClientHandler());
    }

    TraceClientHandlerResolver(TraceBackend backend) {
        handlerList.add(new TraceClientHandler(backend));
    }

    @Override
    public final List<Handler> getHandlerChain(PortInfo portInfo) {
        return handlerList;
    }

}
