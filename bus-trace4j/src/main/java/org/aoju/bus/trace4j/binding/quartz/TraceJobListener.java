package org.aoju.bus.trace4j.binding.quartz;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.Utilities;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import java.util.Map;


public class TraceJobListener extends JobListenerSupport {

    private final TraceBackend backend;

    private final String profile;

    public TraceJobListener() {
        this(Trace.getBackend(), TraceConsts.DEFAULT);
    }

    public TraceJobListener(final String profile) {
        this(Trace.getBackend(), profile);
    }

    TraceJobListener(final TraceBackend backend, final String profile) {
        this.backend = backend;
        this.profile = profile;
    }

    @Override
    public String getName() {
        return "Trace job listener";
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

        Utilities.generateInvocationIdIfNecessary(backend);
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
