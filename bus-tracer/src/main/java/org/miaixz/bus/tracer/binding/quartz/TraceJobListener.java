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
package org.miaixz.bus.tracer.binding.quartz;

import org.miaixz.bus.tracer.Backend;
import org.miaixz.bus.tracer.Builder;
import org.miaixz.bus.tracer.Tracer;
import org.miaixz.bus.tracer.config.TraceFilterConfig;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class TraceJobListener extends JobListenerSupport {

    private final Backend backend;

    private final String profile;

    public TraceJobListener() {
        this(Tracer.getBackend(), Builder.DEFAULT);
    }

    public TraceJobListener(final String profile) {
        this(Tracer.getBackend(), profile);
    }

    TraceJobListener(final Backend backend, final String profile) {
        this.backend = backend;
        this.profile = profile;
    }

    @Override
    public String getName() {
        return "Builder job listener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        final TraceFilterConfig configuration = backend.getConfiguration(profile);

        if (configuration.shouldProcessContext(TraceFilterConfig.Channel.AsyncProcess)) {
            final Map<String, String> traceContext = (Map<String, String>) context.getMergedJobDataMap().get(Builder.TPIC_HEADER);
            if (null != traceContext && !traceContext.isEmpty()) {
                final Map<String, String> filteredContext = configuration.filterDeniedParams(traceContext, TraceFilterConfig.Channel.AsyncProcess);
                backend.putAll(filteredContext);
            }
        }

        Builder.generateInvocationIdIfNecessary(backend);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        backend.clear();
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        backend.clear();
    }

}
