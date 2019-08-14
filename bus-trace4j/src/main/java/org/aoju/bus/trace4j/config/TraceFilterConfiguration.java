package org.aoju.bus.trace4j.config;

import java.util.Map;

public interface TraceFilterConfiguration {

    boolean shouldProcessParam(String paramName, Channel channel);

    Map<String, String> filterDeniedParams(Map<String, String> unfiltered, Channel channel);

    boolean shouldProcessContext(Channel channel);

    boolean shouldGenerateInvocationId();

    int generatedInvocationIdLength();

    boolean shouldGenerateSessionId();

    int generatedSessionIdLength();

    enum Channel {
        IncomingRequest,
        OutgoingResponse,
        OutgoingRequest,
        IncomingResponse,
        AsyncDispatch,
        AsyncProcess
    }

}
