/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.tracer.binding.spring.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import org.miaixz.bus.tracer.Backend;
import org.miaixz.bus.tracer.Builder;
import org.miaixz.bus.tracer.Tracer;
import org.miaixz.bus.tracer.config.TraceFilterConfig;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class TraceMessagePropertiesConverter extends DefaultMessagePropertiesConverter {

    private final Backend backend;
    private final String profile;

    public TraceMessagePropertiesConverter() {
        this(Tracer.getBackend(), Builder.DEFAULT);
    }

    public TraceMessagePropertiesConverter(String profile) {
        this(Tracer.getBackend(), profile);
    }

    TraceMessagePropertiesConverter(Backend backend, String profile) {
        this.backend = backend;
        this.profile = profile;
    }

    @Override
    public MessageProperties toMessageProperties(AMQP.BasicProperties source, Envelope envelope, String charset) {
        final MessageProperties messageProperties = super.toMessageProperties(source, envelope, charset);

        final TraceFilterConfig filterConfiguration = backend.getConfiguration(profile);
        if (filterConfiguration.shouldProcessContext(TraceFilterConfig.Channel.AsyncProcess)) {
            final Map<String, String> traceContextMap = transformToTraceContextMap(
                    (Map<String, ?>) messageProperties.getHeaders().get(Builder.TPIC_HEADER));
            if (null != traceContextMap && !traceContextMap.isEmpty()) {
                backend.putAll(filterConfiguration.filterDeniedParams(traceContextMap, TraceFilterConfig.Channel.AsyncProcess));
            }
        }
        Builder.generateInvocationIdIfNecessary(backend);
        return messageProperties;
    }

    private Map<String, String> transformToTraceContextMap(final Map<String, ?> tpicMessageHeader) {
        final Map<String, String> TraceContext = new HashMap<>();
        if (null != tpicMessageHeader) {
            for (Map.Entry<String, ?> stringObjectEntry : tpicMessageHeader.entrySet()) {
                TraceContext.put(stringObjectEntry.getKey(), String.valueOf(stringObjectEntry.getValue()));
            }
        }
        return TraceContext;
    }

    @Override
    public AMQP.BasicProperties fromMessageProperties(MessageProperties source, String charset) {
        final TraceFilterConfig filterConfiguration = backend.getConfiguration(profile);
        if (!backend.isEmpty() && filterConfiguration.shouldProcessContext(TraceFilterConfig.Channel.AsyncDispatch)) {
            final Map<String, String> filteredParams = filterConfiguration.filterDeniedParams(backend.copyToMap(), TraceFilterConfig.Channel.AsyncDispatch);
            source.getHeaders().put(Builder.TPIC_HEADER, filteredParams);
        }
        return super.fromMessageProperties(source, charset);
    }

}
