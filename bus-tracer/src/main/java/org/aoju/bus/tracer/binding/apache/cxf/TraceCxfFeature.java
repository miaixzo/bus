package org.aoju.bus.tracer.binding.apache.cxf;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.binding.apache.cxf.interceptor.TraceRequestInInterceptor;
import org.aoju.bus.tracer.binding.apache.cxf.interceptor.TraceRequestOutInterceptor;
import org.aoju.bus.tracer.binding.apache.cxf.interceptor.TraceResponseInInterceptor;
import org.aoju.bus.tracer.binding.apache.cxf.interceptor.TraceResponseOutInterceptor;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

public class TraceCxfFeature extends AbstractFeature {

    private String profile;
    private Backend backend;

    public TraceCxfFeature() {
        this(Builder.getBackend(), TraceConsts.DEFAULT);
    }

    TraceCxfFeature(Backend backend, String profile) {
        this.backend = backend;
        this.profile = profile;
    }

    public TraceCxfFeature(String profile) {
        this(Builder.getBackend(), profile);
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
