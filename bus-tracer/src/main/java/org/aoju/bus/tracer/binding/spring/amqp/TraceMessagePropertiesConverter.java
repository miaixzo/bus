package org.aoju.bus.tracer.binding.spring.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;

import java.util.HashMap;
import java.util.Map;


public class TraceMessagePropertiesConverter extends DefaultMessagePropertiesConverter {

    private final Backend backend;
    private final String profile;

    public TraceMessagePropertiesConverter() {
        this(Builder.getBackend(), TraceConsts.DEFAULT);
    }

    public TraceMessagePropertiesConverter(String profile) {
        this(Builder.getBackend(), profile);
    }

    TraceMessagePropertiesConverter(Backend backend, String profile) {
        this.backend = backend;
        this.profile = profile;
    }

    @Override
    public MessageProperties toMessageProperties(AMQP.BasicProperties source, Envelope envelope, String charset) {
        final MessageProperties messageProperties = super.toMessageProperties(source, envelope, charset);

        final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);
        if (filterConfiguration.shouldProcessContext(TraceFilterConfiguration.Channel.AsyncProcess)) {
            final Map<String, String> TraceContextMap = transformToTraceContextMap(
                    (Map<String, ?>) messageProperties.getHeaders().get(TraceConsts.TPIC_HEADER));
            if (TraceContextMap != null && !TraceContextMap.isEmpty()) {
                backend.putAll(filterConfiguration.filterDeniedParams(TraceContextMap, TraceFilterConfiguration.Channel.AsyncProcess));
            }
        }
        Builder.generateInvocationIdIfNecessary(backend);
        return messageProperties;
    }

    private Map<String, String> transformToTraceContextMap(final Map<String, ?> tpicMessageHeader) {
        final Map<String, String> TraceContext = new HashMap<>();
        if (tpicMessageHeader != null) {
            for (Map.Entry<String, ?> stringObjectEntry : tpicMessageHeader.entrySet()) {
                TraceContext.put(stringObjectEntry.getKey(), String.valueOf(stringObjectEntry.getValue()));
            }
        }
        return TraceContext;
    }

    @Override
    public AMQP.BasicProperties fromMessageProperties(MessageProperties source, String charset) {

        final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);
        if (!backend.isEmpty() && filterConfiguration.shouldProcessContext(TraceFilterConfiguration.Channel.AsyncDispatch)) {
            final Map<String, String> filteredParams = filterConfiguration.filterDeniedParams(backend.copyToMap(), TraceFilterConfiguration.Channel.AsyncDispatch);
            source.getHeaders().put(TraceConsts.TPIC_HEADER, filteredParams);
        }
        return super.fromMessageProperties(source, charset);
    }

}
