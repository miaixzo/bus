/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.tracer.binding.apache.cxf;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.Tracer;
import org.aoju.bus.tracer.binding.apache.cxf.interceptor.TraceRequestInInterceptor;
import org.aoju.bus.tracer.binding.apache.cxf.interceptor.TraceRequestOutInterceptor;
import org.aoju.bus.tracer.binding.apache.cxf.interceptor.TraceResponseInInterceptor;
import org.aoju.bus.tracer.binding.apache.cxf.interceptor.TraceResponseOutInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class TraceCxfFeature extends AbstractFeature {

    private String profile;
    private Backend backend;

    public TraceCxfFeature() {
        this(Tracer.getBackend(), Builder.DEFAULT);
    }

    TraceCxfFeature(Backend backend, String profile) {
        this.backend = backend;
        this.profile = profile;
    }

    public TraceCxfFeature(String profile) {
        this(Tracer.getBackend(), profile);
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
