/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.tracer.backend;


import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.config.PropertiesBasedTraceFilterConfiguration;
import org.aoju.bus.tracer.config.PropertyChain;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
public abstract class AbstractBackend implements Backend {

    private PropertyChain _lazyPropertyChain;
    private Map<String, TraceFilterConfiguration> configurationCache = new ConcurrentHashMap<>();

    @Override
    public final TraceFilterConfiguration getConfiguration() {
        return getConfiguration(null);
    }

    @Override
    public final TraceFilterConfiguration getConfiguration(String profileName) {
        final String lookupProfile = profileName == null ? TraceConsts.DEFAULT : profileName;
        TraceFilterConfiguration filterConfiguration = configurationCache.get(lookupProfile);
        if (filterConfiguration == null) {
            filterConfiguration = new PropertiesBasedTraceFilterConfiguration(getPropertyChain(), lookupProfile);
            configurationCache.put(lookupProfile, filterConfiguration);
        }
        return filterConfiguration;
    }

    @Override
    public String getInvocationId() {
        return get(TraceConsts.INVOCATION_ID_KEY);
    }

    @Override
    public String getSessionId() {
        return get(TraceConsts.SESSION_ID_KEY);
    }

    private PropertyChain getPropertyChain() {
        if (_lazyPropertyChain == null) {
            _lazyPropertyChain = PropertiesBasedTraceFilterConfiguration.loadPropertyChain();
        }
        return _lazyPropertyChain;
    }

}
