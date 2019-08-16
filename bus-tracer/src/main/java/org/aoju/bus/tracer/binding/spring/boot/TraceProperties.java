package org.aoju.bus.tracer.binding.spring.boot;

import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "tracer")
public class TraceProperties {

    private int sessionIdLength = 32;

    private int invocationIdLength = 32;

    private Map<TraceFilterConfiguration.Channel, Pattern> filter = new HashMap<>();

    private Map<String, Profile> profile = new HashMap<>();
    private TraceFilterConfiguration delegate = new TraceFilterConfiguration() {
        @Override
        public boolean shouldProcessParam(String paramName, Channel channel) {
            return true;
        }

        @Override
        public Map<String, String> filterDeniedParams(Map<String, String> unfiltered, Channel channel) {
            return unfiltered;
        }

        @Override
        public boolean shouldProcessContext(Channel channel) {
            return true;
        }

        @Override
        public boolean shouldGenerateInvocationId() {
            return invocationIdLength > 0;
        }

        @Override
        public int generatedInvocationIdLength() {
            return invocationIdLength;
        }

        @Override
        public boolean shouldGenerateSessionId() {
            return sessionIdLength > 0;
        }

        @Override
        public int generatedSessionIdLength() {
            return sessionIdLength;
        }
    };

    public int getSessionIdLength() {
        return sessionIdLength;
    }

    public void setSessionIdLength(int sessionIdLength) {
        this.sessionIdLength = sessionIdLength;
    }

    public int getInvocationIdLength() {
        return invocationIdLength;
    }

    public void setInvocationIdLength(int invocationIdLength) {
        this.invocationIdLength = invocationIdLength;
    }

    public Map<TraceFilterConfiguration.Channel, Pattern> getFilter() {
        return filter;
    }

    public void setFilter(Map<TraceFilterConfiguration.Channel, Pattern> filter) {
        this.filter = filter;
    }

    public Map<String, Profile> getProfile() {
        return profile;
    }

    public void setProfile(Map<String, Profile> profile) {
        this.profile = profile;
    }

    public TraceFilterConfiguration getAsFilterConfiguration() {
        return delegate;
    }


    public static class Profile {

        private Map<TraceFilterConfiguration.Channel, Pattern> filter;

        public Map<TraceFilterConfiguration.Channel, Pattern> getFilter() {
            return filter;
        }

        public void setFilter(Map<TraceFilterConfiguration.Channel, Pattern> filter) {
            this.filter = filter;
        }
    }

}
