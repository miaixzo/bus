package org.aoju.bus.trace4j.binding.apache.cxf;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.binding.apache.cxf.interceptor.TraceRequestInInterceptor;
import org.aoju.bus.trace4j.binding.apache.cxf.interceptor.TraceRequestOutInterceptor;
import org.aoju.bus.trace4j.binding.apache.cxf.interceptor.TraceResponseInInterceptor;
import org.aoju.bus.trace4j.binding.apache.cxf.interceptor.TraceResponseOutInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

public class TraceCxfFeature extends AbstractFeature {

    private String profile;
    private TraceBackend backend;

    public TraceCxfFeature() {
        this(Trace.getBackend(), TraceConsts.DEFAULT);
    }

    TraceCxfFeature(TraceBackend backend, String profile) {
        this.backend = backend;
        this.profile = profile;
    }

    public TraceCxfFeature(String profile) {
        this(Trace.getBackend(), profile);
    }

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        final TraceRequestInInterceptor requestInInterceptor = new TraceRequestInInterceptor(backend, profile);
        final TraceResponseInInterceptor responseInInterceptor = new TraceResponseInInterceptor(backend, profile);
        final TraceRequestOutInterceptor requestOutInterceptor = new TraceRequestOutInterceptor(backend, profile);
        final TraceResponseOutInterceptor responseOutInterceptor = new TraceResponseOutInterceptor(backend, profile);

        provider.getInInterceptors().add(requestInInterceptor);
        provider.getInInterceptors().add(responseInInterceptor);

        provider.getOutInterceptors().add(requestOutInterceptor);
        provider.getOutInterceptors().add(responseOutInterceptor);

        provider.getOutFaultInterceptors().add(responseOutInterceptor);
        provider.getInFaultInterceptors().add(requestInInterceptor);
    }

}
