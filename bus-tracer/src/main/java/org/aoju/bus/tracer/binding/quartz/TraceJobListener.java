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
package org.aoju.bus.tracer.binding.quartz;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
public class TraceJobListener extends JobListenerSupport {

    private final Backend backend;

    private final String profile;

    public TraceJobListener() {
        this(Builder.getBackend(), TraceConsts.DEFAULT);
    }

    public TraceJobListener(final String profile) {
        this(Builder.getBackend(), profile);
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
        final TraceFilterConfiguration configuration = backend.getConfiguration(profile);

        if (configuration.shouldProcessContext(TraceFilterConfiguration.Channel.AsyncProcess)) {
            final Map<String, String> TraceContext = (Map<String, String>) context.getMergedJobDataMap().get(TraceConsts.TPIC_HEADER);

            if (TraceContext != null && !TraceContext.isEmpty()) {
                final Map<String, String> filteredContext = configuration.filterDeniedParams(TraceContext, TraceFilterConfiguration.Channel.AsyncProcess);
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
