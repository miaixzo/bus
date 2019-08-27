package org.aoju.bus.tracer.binding.quartz;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import java.util.Map;


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
