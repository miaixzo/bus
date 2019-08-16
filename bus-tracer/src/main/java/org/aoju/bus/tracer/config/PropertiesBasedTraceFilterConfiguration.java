package org.aoju.bus.tracer.config;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A TraceFilterConfiguration that is based on a {@link PropertyChain}.
 * The default property chain may be obtained by the {@link #loadPropertyChain()} method.
 */
public final class PropertiesBasedTraceFilterConfiguration implements TraceFilterConfiguration {

    static final String Trace_CONFIG_PREFIX = "Builder.";
    static final String PROFILED_PREFIX = Trace_CONFIG_PREFIX + "profile.";
    static final String Trace_DEFAULT_PROFILE_PREFIX = Trace_CONFIG_PREFIX + TraceConsts.DEFAULT + ".";
    static final String GENERATE_INVOCATION_ID = "invocationIdLength";
    static final String GENERATE_SESSION_ID = "sessionIdLength";

    private static final Logger logger = LoggerFactory.getLogger(PropertiesBasedTraceFilterConfiguration.class);
    private final PropertyChain propertyChain;
    private final String profileName;
    private final Map<String, List<Pattern>> patternCache = new ConcurrentHashMap<>();

    public PropertiesBasedTraceFilterConfiguration(PropertyChain propertyChain) {
        this(propertyChain, null);
    }

    public PropertiesBasedTraceFilterConfiguration(PropertyChain propertyChain,
                                                   String profileName) {
        this.propertyChain = propertyChain;
        this.profileName = profileName;
    }

    /**
     * Loads a layered property chain based on:
     * <ol>
     * <li>System properties</li>
     * <li>merged entries from all {@code /META-INF/Builder.properties} files on the classpath (loaded in undefined order)</li>
     * <li>merged entries from all {@code /META-INF/Builder.default.properties} files on the classpath (loaded in undefined order)</li>
     * </ol>
     *
     * @return the propertyChain
     */
    public static PropertyChain loadPropertyChain() {
        try {
            final Properties TraceDefaultFileProperties = new TracePropertiesFileLoader().loadTraceProperties(TracePropertiesFileLoader.Trace_DEFAULT_PROPERTIES_FILE);
            final Properties TraceFileProperties = new TracePropertiesFileLoader().loadTraceProperties(TracePropertiesFileLoader.Trace_PROPERTIES_FILE);
            return PropertyChain.build(System.getProperties(), TraceFileProperties, TraceDefaultFileProperties);
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not load TraceProperties: " + ioe.getMessage(), ioe);
        }
    }

    private String getProfiledOrDefaultProperty(final String propertyName) {
        if (profileName != null && !TraceConsts.DEFAULT.equals(profileName)) {
            final String profiledProperty = propertyChain.getProperty(PROFILED_PREFIX + profileName + '.' + propertyName);
            if (profiledProperty != null)
                return profiledProperty;
        }
        return propertyChain.getProperty(Trace_DEFAULT_PROFILE_PREFIX + propertyName);
    }

    @Override
    public boolean shouldProcessParam(String paramName, Channel channel) {
        final String messageTypePropertyValue = getProfiledOrDefaultProperty(channel.name());
        final List<Pattern> patterns = retrievePatternsForPropertyValue(messageTypePropertyValue);
        return anyPatternMatchesParamName(patterns, paramName);
    }

    @Override
    public boolean shouldProcessContext(final Channel channel) {
        final String messageTypePropertyValue = getProfiledOrDefaultProperty(channel.name());
        return !StringUtils.isEmpty(messageTypePropertyValue);
    }

    @Override
    public boolean shouldGenerateInvocationId() {
        return generatedInvocationIdLength() > 0;
    }

    @Override
    public int generatedInvocationIdLength() {
        return parseIntOrZero(getProfiledOrDefaultProperty(GENERATE_INVOCATION_ID));
    }

    @Override
    public boolean shouldGenerateSessionId() {
        return generatedSessionIdLength() > 0;
    }

    @Override
    public int generatedSessionIdLength() {
        return parseIntOrZero(getProfiledOrDefaultProperty(GENERATE_SESSION_ID));
    }

    @Override
    public Map<String, String> filterDeniedParams(final Map<String, String> unfiltered, final Channel channel) {
        final Map<String, String> filtered = new HashMap<>(unfiltered.size());
        for (Map.Entry<String, String> entry : unfiltered.entrySet()) {
            if (shouldProcessParam(entry.getKey(), channel)) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private int parseIntOrZero(String intString) {
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private boolean anyPatternMatchesParamName(Iterable<Pattern> patterns, String paramName) {
        for (Pattern pattern : patterns) {
            if (patternMatchesParamName(pattern, paramName))
                return true;
        }
        return false;
    }

    private boolean patternMatchesParamName(Pattern pattern, String paramName) {
        return ".*".equals(pattern.pattern()) || pattern.matcher(paramName).matches();
    }

    private List<Pattern> retrievePatternsForPropertyValue(final String propertyValue) {
        if (propertyValue == null) {
            return Collections.emptyList();
        }
        final List<Pattern> patterns = patternCache.get(propertyValue);
        if (patterns != null) {
            return patterns;
        }

        final List<Pattern> unmodPatterns = Collections.unmodifiableList(extractPatterns(propertyValue));
        patternCache.put(propertyValue, unmodPatterns);
        return unmodPatterns;
    }

    List<Pattern> extractPatterns(final String propertyValue) {
        if (propertyValue == null)
            return Collections.emptyList();

        final List<Pattern> trimmedPatterns = new ArrayList<>();
        final StringTokenizer tokenizer = new StringTokenizer(propertyValue, ",");
        while (tokenizer.hasMoreTokens()) {
            final String trimmedString = tokenizer.nextToken().trim();
            if (!trimmedString.isEmpty()) {
                try {
                    trimmedPatterns.add(Pattern.compile(trimmedString));
                } catch (PatternSyntaxException e) {
                    logger.error("Can not compile pattern '" + trimmedString + "'. Message: " + e.getMessage() + " -- Ignore pattern");
                    logger.debug("Detailed Exception cause: " + e.getMessage(), e);
                }
            }
        }
        return trimmedPatterns;
    }
}
