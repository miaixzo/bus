package org.aoju.bus.tracer.backend;


import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.config.PropertiesBasedTraceFilterConfiguration;
import org.aoju.bus.tracer.config.PropertyChain;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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
