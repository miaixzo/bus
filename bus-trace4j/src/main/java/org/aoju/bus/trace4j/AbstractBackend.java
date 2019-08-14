package org.aoju.bus.trace4j;


import org.aoju.bus.trace4j.config.PropertiesBasedTraceFilterConfiguration;
import org.aoju.bus.trace4j.config.PropertyChain;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.consts.TraceConsts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class AbstractBackend implements TraceBackend {

    private PropertyChain _lazyPropertyChain = null;

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
